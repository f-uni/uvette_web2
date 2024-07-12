<?php

//funzione che restituisce il create statement della tabella desiderata
function getCreateStatement($conn, $tables, $table_name){

	//controllo se la tabella desiderata è nella lista di quelle autorizzate
	if(!in_array($table_name, $tables))
		return null;

	foreach($conn->query('show tables') as $table) {
		//se esiste la tabella nel db
		if($table[0]==$table_name){
			foreach ($conn->query("show create table $table_name") as $row) {
				//ritorno create statement
				return str_replace("`", "", $row['Create Table']);
			}
			break;
		}
	}
	
	return null;
}

//funzione che restituisce righe e colonne della tabella desiderata
function getTable($conn, $tables, $table_name){

	//controllo se la tabella desiderata è nella lista di quelle autorizzate
	if(!in_array($table_name, $tables))
		return null;

	foreach($conn->query('show tables') as $table) {
		//se esiste la tabella nel db
		if($table[0]==$table_name){
			
			$result=[];

			//fetch righe
			$stmt = $conn->prepare("SELECT * FROM $table_name");
			$stmt->execute();
			$result["rows"] = $stmt->fetchAll(PDO::FETCH_NUM);
			
			if(!empty($result["rows"])){
				//fetch colonne
				$stmt->execute();
				$result["cols"] = array_keys($stmt->fetchAll(PDO::FETCH_ASSOC)[0]);
				return $result;

			}
			break;
		}
	}

	return null;
}

//funzione per convertire query in sintassi MySQL in sintassi PostgreSQL 
function convertMySQLToPostgres($mysqlCreateStatement) {
    //mappa dei tipi di dati MySQL a PostgreSQL
    $dataTypeMapping = [
        '/\bint\b/' => 'integer',
        '/\bsmallint\b/' => 'smallint',
        '/\btinyint\b/' => 'smallint',
        '/\bmediumint\b/' => 'integer',
        '/\bbigint\b/' => 'bigint',
        '/\bvarchar\((\d+)\)\b/' => 'varchar($1)',
        '/\bchar\((\d+)\)\b/' => 'char($1)',
        '/\btext\b/' => 'text',
        '/\bblob\b/' => 'bytea',
        '/\bfloat\b/' => 'real',
        '/\bdouble\b/' => 'double precision',
        '/\bdecimal\((\d+),(\d+)\)\b/' => 'numeric($1,$2)',
        '/\btimestamp\b/' => 'timestamp',
        '/\bdatetime\b/' => 'timestamp',
        '/\bdate\b/' => 'date',
        '/\btime\b/' => 'time',
        '/\byear\b/' => 'integer',
        '/\benum\((.*?)\)/' => 'text'
    ];

    //rimuovo ENGINE, CHARSET e COLLATE dalla query
    $mysqlCreateStatement = preg_replace(
        ['/ENGINE=\w+/', '/DEFAULT CHARSET=\w+/', '/COLLATE=\w+/', '/AUTO_INCREMENT=\w+/'],
        '',
        $mysqlCreateStatement
    );

    //rimuovo eventuali spazi in più
    $mysqlCreateStatement = preg_replace('/\s+/', ' ', $mysqlCreateStatement);

    //sostituisco i tipi di dati
    foreach ($dataTypeMapping as $mysqlType => $postgresType) {
        $mysqlCreateStatement = preg_replace($mysqlType, $postgresType, $mysqlCreateStatement);
    }

    //rimuovo AUTO_INCREMENT non supportato
    $mysqlCreateStatement = preg_replace('/\bAUTO_INCREMENT\b/', '', $mysqlCreateStatement);

    return trim($mysqlCreateStatement);
}