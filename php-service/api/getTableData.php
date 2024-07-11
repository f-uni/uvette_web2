<?php

include "../lib/connect.php";
include "../lib/response.php";

include "../lib/sql_util.php";

include "../config.php";


//lettura parametro
$table_param=$_GET["table"];

//determino se esiste e genero query SQL con create statement
$statement=getCreateStatement($conn, $tables, $table_param);

if($statement){
    //$data=getTable($conn, $tables, $table_param);

    json_response(200, [
        "table" => $table_param,
        "statement" => convertMySQLToPostgres($statement)
    ]);
}else{
    json_response(400, ["error"=>"table doesn't exist"]);
}