import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MigrateTable extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{

        //leggo parametro della tabella richiesta
        String table = request.getParameter("table");

        //se il parametro non è assegnato ritorno errore
        if(table==null){
            MigrationUtil.jsonResponse(400, response, Map.of(
                "error", "missing parameter table"
            ));
            return;
        }
        
        //lettura parametri dal conrext della servlet
        ServletContext context = getServletContext(); 
        String phpServiceUrl = context.getInitParameter("PHP_SERVICE_URL");
        String pythonServiceUrl = context.getInitParameter("PYTHON_SERVICE_URL");

        try {
            //lettura dei dati dal servizio php
            String json = MigrationUtil.getTableData(phpServiceUrl, table);

            //invio dati al servizio django
            String result = MigrationUtil.sendTableData(pythonServiceUrl, json);

            //invio risposta al client
            MigrationUtil.jsonResponse(200, response, Map.of(
                "message", result
            ));

        } catch (PHPServiceException e) {
            //errore nel servizio php
            MigrationUtil.jsonResponse(400, response, Map.of(
                "php-error", e.getMessage()
            ));
        } catch (DjangoServiceException e) {
            //errore nel servizio python
            MigrationUtil.jsonResponse(400, response, Map.of(
                "python-error", e.getMessage()
            ));
        }
    } 

}