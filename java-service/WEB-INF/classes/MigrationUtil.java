import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class MigrationUtil {
    
    public static void jsonResponse(int code, HttpServletResponse response, Map<String, Object> data) throws IOException{
        response.setStatus(code);
		response.setContentType("application/json");
        Gson gson = new Gson();
        response.getWriter().println(gson.toJson(data));
    }

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

                Gson gson = new Gson();
                @SuppressWarnings("rawtypes")
                Map map = gson.fromJson(response.toString(), Map.class);
                return (List<String>) map.get("tables");

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
                @SuppressWarnings("rawtypes")
                Map map = gson.fromJson(errorResponse.toString(), Map.class);
                throw new PHPServiceException((String) map.get("error"));
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

    public static String sendTableData(String url, String data) throws DjangoServiceException{

        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            //apro connessione
            URL urlObj = new URL(url+"/sendTableData");
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

            //gestisto eventuali ettori lato server
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                Gson gson = new Gson();
                @SuppressWarnings("rawtypes")
                Map map = gson.fromJson(errorResponse.toString(), Map.class);
                throw new DjangoServiceException((String) map.get("error"));
            }
        } catch (IOException e) {
            throw new DjangoServiceException("Server Error");
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

class PHPServiceException extends Exception{
    public PHPServiceException(String message){
        super(message);
    }
}

class DjangoServiceException extends Exception{
    public DjangoServiceException(String message){
        super(message);
    }
}