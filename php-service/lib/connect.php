<?php

$servername= "localhost";
$username = "fcolpani";
$dbname= "my_fcolpani";
$password = null;

try {
    $conn = new PDO("mysql:host=".$servername.";dbname=".$dbname, $username, $password);
    // set the PDO error mode to exception
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

} catch(PDOException$e) {
    json_response(500, ["error" => $e->getMessage()]);
    die();
}
