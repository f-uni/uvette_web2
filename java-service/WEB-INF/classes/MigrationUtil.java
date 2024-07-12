import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.List;
import java.util.Map;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

//classe util per la gestione della migrazione
public class MigrationUtil {
    
    //metodo per inviare risposta json al client
    public static void jsonResponse(int code, HttpServletResponse response, Map<String, Object> data) throws IOException{
        response.setStatus(code);
		response.setContentType("application/json");
        Gson gson = new Gson();
        response.getWriter().println(gson.toJson(data));
    }

    //metodo per leggere la tabelle disponibili nel servizio PHP
    public static List<String> getTables(String url){
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            //apro connessione
            URL urlObj = new URL(url+"/getTables.php");
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");

            //controllo stato della connessione
            int responseCode = conn.getResponseCode();

            //controllo risposta
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                //parse del json
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                JsonArray tablesArray = jsonObject.getAsJsonArray("tables");
                //converto in lista di stringhe
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> tableList = new Gson().fromJson(tablesArray, listType);
                return tableList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    //metodo per leggere i dati della tabella dal servizio php
    public static String getTableData(String url, String table) throws PHPServiceException{

        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            //apro connessione
            URL urlObj = new URL(url+"/getTableData.php?table="+ URLEncoder.encode(table, Charset.forName("UTF8")));
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");

            //controllo stato della connessione
            int responseCode = conn.getResponseCode();

            //controllo risposta
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();

            //gestisto eventuali ettori lato server
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(errorResponse.toString(), JsonObject.class);
                throw new PHPServiceException(jsonObject.get("error").getAsString());
            }
        } catch (IOException e) {
            throw new PHPServiceException("Server Error");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
        
    }

    //metodo per l'invio della tabella al servizio python
    public static String sendTableData(String url, String data) throws DjangoServiceException{

        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            //apro connessione
            URL urlObj = new URL(url+"/importTable");
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                byte[] input = data.getBytes("utf-8");
                wr.write(input, 0, input.length);
            }

            //controllo stato della connessione
            int responseCode = conn.getResponseCode();

            //controllo risposta
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();

            //gestisto eventuali errori lato server
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(errorResponse.toString(), JsonObject.class);
                throw new DjangoServiceException(jsonObject.get("error").getAsString());
            }
        } catch (IOException e) {
            throw new DjangoServiceException("Server Error, "+ e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
        
    }

}

//classe per gestire gli errori nel servizio PHP
class PHPServiceException extends Exception{
    public PHPServiceException(String message){
        super(message);
    }
}

//classe per gestire gli errori nel servizio python
class DjangoServiceException extends Exception{
    public DjangoServiceException(String message){
        super(message.replaceAll("\n", " ").replaceAll("\"", ""));
    }
}