<?php

include "../lib/connect.php";
include "../lib/response.php";

function getCreateStatement($conn,$table_name){
    foreach($conn->query('show tables') as $table) {
		if($table[0]==$table_name){
            foreach ($conn->query("show create table {$table[0]}") as $row) {
                return str_replace("`", "", $row['Create Table']);
            }
        }
	}
    return null;
}

function convertMysqlToPostgres($mysqlCreateStatement) {
    // Mappa dei tipi di dati MySQL a PostgreSQL
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
        '/\byear\b/' => 'integer'
    ];

    // Rimuove ENGINE, CHARSET e COLLATE
    $mysqlCreateStatement = preg_replace(
        ['/ENGINE=\w+/', '/DEFAULT CHARSET=\w+/', '/COLLATE=\w+/'],
        '',
        $mysqlCreateStatement
    );

    // Rimuove eventuali spazi in più
    $mysqlCreateStatement = preg_replace('/\s+/', ' ', $mysqlCreateStatement);

    // Sostituzione dei tipi di dati
    foreach ($dataTypeMapping as $mysqlType => $postgresType) {
        $mysqlCreateStatement = preg_replace($mysqlType, $postgresType, $mysqlCreateStatement);
    }

    // Rimuove AUTO_INCREMENT (in PostgreSQL si usa SERIAL per colonne con auto-incremento)
    $mysqlCreateStatement = preg_replace('/\bAUTO_INCREMENT\b/', 'SERIAL', $mysqlCreateStatement);

    // Restituisce lo statement convertito
    return trim($mysqlCreateStatement);
}

$table_param=$_GET["table"];

$statement=getCreateStatement($conn, $table_param);

if($statement){
    json_response(200, ["statement"=>convertMysqlToPostgres($statement),"statementmy"=>$statement]);
}else{
    json_response(400, ["error"=>"table doesn't exist"]);
}
