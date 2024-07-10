<?php

function json_response($code, $array){
    header('Content-Type: application/json; charset=utf-8');
    http_response_code($code);
    echo json_encode($array);
}