<?php

include "../lib/connect.php";
include "../lib/response.php";

//lista di tabelle esportabili
return json_response(200, [
    "tables"=>[
        "domanda",
        "partecipazione",
        "quiz",
        "risposta",
        "risposta_utente_quiz",
        "utente"
    ]
]);