<?php

include "../lib/connect.php";
include "../lib/response.php";

include "../config.php";

//lista di tabelle esportabili
return json_response(200, ["tables"=>$tables]);