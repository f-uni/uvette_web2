# Progetto WEB - Migrazione dati

## Analisi

- ### PHP Web Service
-  
- 
- https://uvette.altervista.org/php-service/api/getTableData.php?table={table}

- ### JAVA Web Service
    

## Istruzioni

- ### php-service
    Contiene il web-service caricato sul server altervista all'url: ``https://uvette.altervista.org/php-service/api``

- ### java-service
    Contiene il web-service tomcat

    - Installazione: copiare l'intera ``java-service`` cartella nella cartella ``webapps`` del server Tomcat
        se necessario ricompilare i file ``.java`` nella cartella ``WEB-INF/classes/`` con il comando
        ```bash
            javac  -cp "./WEB-INF/lib/*" ./WEB-INF/classes/*    
        ```
    - Configurazione: modificare se necessario i due url dei servizi nel file ``WEB-INF/web.xml`` ``(riga 16 - 20)``

- ### python-service
    Contiene il web-service django

    - Installazione: installare se necessario i moduli nel file ``requirements.txt``, avviare il server django con il comando: 
    ```bash
        python .\manage.py runserver
    ```
    
    - Configurazione: modificare i parametri di connessione al database postgres nel file ``python_service/settings.py`` ``(riga 74)``


 ## Utilizzo

