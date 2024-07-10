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
        '/\byear\b/' => 'integer'
    ];

    //rimuove ENGINE, CHARSET e COLLATE dalla query
    $mysqlCreateStatement = preg_replace(
        ['/ENGINE=\w+/', '/DEFAULT CHARSET=\w+/', '/COLLATE=\w+/', '/AUTO_INCREMENT=\w+/'],
        '',
        $mysqlCreateStatement
    );

    //rimuove eventuali spazi in più
    $mysqlCreateStatement = preg_replace('/\s+/', ' ', $mysqlCreateStatement);

    //sostituzione dei tipi di dati
    foreach ($dataTypeMapping as $mysqlType => $postgresType) {
        $mysqlCreateStatement = preg_replace($mysqlType, $postgresType, $mysqlCreateStatement);
    }
    $mysqlCreateStatement = preg_replace('/\bAUTO_INCREMENT\b/', '', $mysqlCreateStatement);
    return trim($mysqlCreateStatement);
}

//lettura parametro
$table_param=$_GET["table"];

$statement=getCreateStatement($conn, $table_param);

if($statement){
    json_response(200, ["statement"=>convertMySQLToPostgres($statement)]);
}else{
    json_response(400, ["error"=>"table doesn't exist"]);
}