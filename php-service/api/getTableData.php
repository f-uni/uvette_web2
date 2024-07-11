<?php

include "../lib/connect.php";
include "../lib/response.php";
include "../lib/sql_util.php";

include "../config.php";

//lettura parametro
$table_param=preg_replace('/[^a-zA-Z0-9_]+/', '', $_GET["table"]);

//determino se esiste e genero query SQL con create statement
$statement=getCreateStatement($conn, $tables, $table_param);

if($statement){
    $data=getTable($conn, $tables, $table_param);
    if($data){
        json_response(200, [
            "table" => $table_param,
            "statement" => convertMySQLToPostgres($statement),
            "columns" => $data["cols"],
            "rows" => $data["rows"]
        ]);
    }else{
        json_response(400, ["error"=>"empty table"]);
    }
}else{
    json_response(400, ["error"=>"table does not exist"]);
}