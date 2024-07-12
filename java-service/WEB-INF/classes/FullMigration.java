import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class FullMigration extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        //lettura parametri dal conrext della servlet
        ServletContext context = getServletContext(); 
        String phpServiceUrl = context.getInitParameter("PHP_SERVICE_URL");
        String pythonServiceUrl = context.getInitParameter("PYTHON_SERVICE_URL");

        
        List<Map<String,Object>> finalResult = new ArrayList<>();

        List<String> tables = MigrationUtil.getTables(phpServiceUrl);

        if(tables==null){
            MigrationUtil.jsonResponse(400, response, Map.of(
                "php-errors", "unable to fetch tables"
            ));
            return;
        }

        int errors = 0;
        for(String table : tables){
            try {
                //lettura dei dati dal servizio php
                String json = MigrationUtil.getTableData(phpServiceUrl, table);

                //invio dati al servizio django
                String result = MigrationUtil.sendTableData(pythonServiceUrl, json);

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                finalResult.add(Map.of(
                    "table", table,
                    "created", jsonObject.get("create_table").getAsBoolean(),
                    "rows", jsonObject.get("inserted_rows").getAsInt()
                ));

            } catch (PHPServiceException e) {
                //errore nel servizio php
                finalResult.add(Map.of(
                "table", table,
                "php-error", e.getMessage()
                ));
                errors++;
            } catch (DjangoServiceException e) {
                //errore nel servizio python
                finalResult.add(Map.of(
                "table", table,
                "python-error", e.getMessage()
                ));
                errors++;
            }
        }

        //invio risposta al client
        MigrationUtil.jsonResponse(errors==0?200:400, response, Map.of(
            "message", errors==0?"success":"error",
            "errors", errors,
            "tables", finalResult
        ));
    }
}