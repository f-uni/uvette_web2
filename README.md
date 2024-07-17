# Progetto WEB - Migrazione dati


## Analisi

Alla base del funzionamento del nostro sistema vi sono i seguenti principi:
- Il DB locale che riceve i dati non deve necessariamente avere le tabelle che si vogliono importare, è quindi necessario esportare anche il create statement della tabella.
- Generalizzare l’esportazione e l’importazione delle tabelle con un formato comune tra il servizio PHP e quello PYTHON, nel nostro caso un JSON strutturato ad hoc.
- Mantenere sicuro il server PHP dichiarando le tabelle esportabili così che non venga esposto l’intero DB.
Tutti e tre i servizi operano con protocollo HTTP e risposte in formato JSON




## Funzionamento

- #### PHP Web Service
    - ##### Struttura progetto:
        - ``api/``: cartella contenente gli endpoint esposti
        - ``lib/``: cartella contenente le librerie usate
        - ``config.php``: file di configurazione del servizio in cui vengono dichiarate le tabelle che il servizio può esportare.

    - ##### Funzionamento:
	    - ``GET`` - ``/api/getTables.php``: restituisce le tabelle esportabili in formato json 
	    - ``GET`` - ``/api/getTableData.php?table={table}``: riceve un parametro table, se esiste la tabella richiesta, genera il create statement associato, legge le righe e le colonne e restituisce i dati in formato JSON (vedi ``php-service/response.json``). Utilizziamo un’unica API per richiedere tutte le tabelle tramite un parametro che contiene il nome della tabella desiderata presente all’interno del DB (il nome della tabella viene ricavato dalla precedente richiesta effettuata da ``/api/getTables.php``). Mediante apposita funzione lo statement SQL per la creazione della tabella viene convertito in sintassi postgres.

	
- #### JAVA Web Service
    - ##### Struttura progetto:
        Progetto Tomcat standard

    - ##### Funzionamento:
	    - ``GET`` - ``/MigrateTable?table={table}``: riceve un parametro table, contatta il servizio php /api/getTableData.php specificando la tabella richiesta e salvo errori, invia il risultato al servizio python.
	    - ``GET`` - ``/FullMigration``: legge la lista di tabelle esportabili dal servizio php mediante l’api “/api/getTables.php” e per ogni tabella esegue lo stesso comportamento dell’endpoint ``/MigrateTable``


- #### PYTHON Web Service
    - ##### Struttura progetto:
        Progetto Django standard

    - ##### Funzionamento:
	    - ``POST`` - ``/importTable`` riceve un JSON nel formato ad hoc, controlla se la tabella da importare esiste già, se non esiste viene creata eseguendo il create statement inviato, successivamente importa i dati nella tabella
	

    

## Istruzioni

- ### php-service
    Contiene il web-service caricato sul server altervista all'url: ``https://uvette.altervista.org/php-service/api``

- ### java-service
    Contiene il web-service tomcat

    - Installazione: copiare l'intera cartella ``java-service`` nella cartella ``webapps`` del server Tomcat, se necessario ricompilare i file ``.java`` nella cartella ``WEB-INF/classes/`` con il comando

        ```bash
            javac  -cp "./WEB-INF/lib/*" ./WEB-INF/classes/*    
        ```
    - Configurazione: modificare se necessario i due url dei servizi nel file ``WEB-INF/web.xml`` ``(riga 16 - 20)``

- ### python-service
    Contiene il web-service django

    - Installazione: installare se necessario i moduli nel file ``requirements.txt``, avviare il server django con il comando: 
    ```bash
        python .\manage.py runserver
    ```
    
    - Configurazione: modificare i parametri di connessione al database postgres nel file ``python_service/settings.py`` ``(riga 74)``
