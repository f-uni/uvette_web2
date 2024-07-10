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

$table_param=$_GET["table"];

$statement=getCreateStatement($conn, $table_param);

if($statement){
    json_response(200, ["statement"=>$statement]);
}else{
    json_response(400, ["error"=>"table doesn't exist"]);
}
