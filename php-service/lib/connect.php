<?php

//parametri di connessione
$servername= "localhost";
$username = "uvette";
$dbname= "my_uvette";
$password = null;

try {
    //connessione al db locale
    $conn = new PDO("mysql:host=".$servername.";dbname=".$dbname, $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

} catch(PDOException$e) {
    json_response(500, ["error" => $e->getMessage()]);
    die();
}
