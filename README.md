# Progetto WEB - Migrazione dati

## Analisi


## Istruzioni

- ### php-service
    Contiene le api caricate sul server altervista all'url: ``https://uvette.altervista.org/php-service/api``

- ### java-service
    Contiene il web-service tomcat

    - Installazione: copiare l'intera cartella nella cartella ``webapps`` del server Tomcat
    - Configurazione: modificare se necessario i due url dei servizi nel file ``WEB-INF/web.xml`` ``(riga 16 - 20)``

- ### python-service
    Contiene il web-service django

    - Installazione: installare se necessario i moduli nel file ``requirements.txt``, avviare il server django con il comando: 
    ```bash
        python .\manage.py runserver
    ```
    
    - Configurazione: modificare i parametri di connessione al database postges nel file ``python_service/settings.py`` ``(riga 74)``