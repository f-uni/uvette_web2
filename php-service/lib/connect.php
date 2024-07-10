<?php

$servername= "localhost";
$username = "uvette";
$dbname= "my_uvette";
$password = null;

try {
    $conn = new PDO("mysql:host=".$servername.";dbname=".$dbname, $username, $password);
    // set the PDO error mode to exception
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

} catch(PDOException$e) {
    echo "DB Error: " . $e->getMessage();
    die();
}
