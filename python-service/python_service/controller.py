import json

from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings

import psycopg2
from psycopg2 import sql

def index(request):
	return HttpResponse("<html><body><p>Python Service</p></body></html>")

@csrf_exempt #disabilito i controlli csrf per l'invio di dati in POST
def importTable(request):
	#controllo il metodo della richiesta
	if request.method == "POST":
		try:
			#leggo il json contente i dati
			body = json.loads(request.body)

			#controllo se il json è valido
			if "table" in body and "statement" in body and "columns" in body and "rows" in body:
				#stabilisisco connessione con il database
				conn = get_connection()
				
				if not conn:
					return JsonResponse({"error":"unable to connect to local database"}, status=500)

				#controllo se esiste la tabella
				query = sql.SQL("""
					SELECT EXISTS (
						SELECT 1 
						FROM information_schema.tables 
						WHERE table_name = %s
					)
				""")
				cursor = conn.cursor()
				cursor.execute(query, (body["table"],))  
				exists = cursor.fetchone()[0]

				#se non esiste creo la tabella
				if not exists:
					try:
						create_query=sql.SQL(body["statement"])
						cursor.execute(create_query)
					except Exception as e:
						return JsonResponse({"error":"unable to create table, {}".format(e)}, status=500)

				#importo i dati
				try:
					insert_query = sql.SQL("INSERT INTO {table} ({fields}) VALUES {values}").format(
						table=sql.Identifier(body["table"]),
						fields=sql.SQL(',').join(map(sql.Identifier, body["columns"])),
						values=sql.SQL(',').join(
							sql.SQL('({})').format(sql.SQL(',').join(map(sql.Literal, row))) 
							for row in body["rows"]
						)
					)
					
					cursor.execute(insert_query, (body["table"]))
					conn.commit()

				except Exception as e:
					return JsonResponse({"error":"unable to insert data, {}".format(e)}, status=500)

				return JsonResponse({
					"message": "success",
					"inserted_rows": cursor.rowcount,
					"create_table": not exists
				}, status=200)
			else:
				return JsonResponse({"error":"invalid json"}, status=400)
		
		except json.JSONDecodeError as e:
			return JsonResponse({"error": "invalid json, {}".format(e)}, status=400)

	else:
		return JsonResponse({
			"error" : "invalid method {}".format(request.method)
		}, status=400)


def get_connection():
    db_settings = settings.DATABASES['default']
    
    try:
        conn = psycopg2.connect(
            dbname=db_settings['NAME'],
            user=db_settings['USER'],
            password=db_settings['PASSWORD'],
            host=db_settings['HOST'],
            port=db_settings['PORT']
        )
        return conn
    
    except psycopg2.DatabaseError as e:
        print(f"Errore nel database: {e}")
        return None
