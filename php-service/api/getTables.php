<?php

include "../lib/connect.php";
include "../lib/response.php";

return json_response(200, ["tables"=>["domanda","quiz","utente"]]);