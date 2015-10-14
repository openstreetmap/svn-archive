package org.openstreetmap.josm.plugins.tofix.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.openstreetmap.josm.plugins.tofix.bean.ResponseBean;

/**
 *
 * @author ruben
 */
public class Request {

    public static ResponseBean sendPOST(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        //START - POST
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.flush();
        os.close();
        //POST - END
        int responseCode = con.getResponseCode();

        //Crear un ResponseBean para que regrese el String y el status de la peticion.
        ResponseBean responseBean = new ResponseBean();

        responseBean.setStatus(responseCode);//agregar el estatus

        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //Util.print(response.toString());
            responseBean.setValue(response.toString());//agrega el valor de la respuesta

        } else if (responseCode == HttpURLConnection.HTTP_GONE) {

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response_error = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response_error.append(inputLine);
            }
            in.close();

            responseBean.setValue(response_error.toString());// agregar respuesta de complete task

        } else {

            responseBean.setValue("error");

        }
        con.disconnect();
        return responseBean;
    }

    public static void sendPOST_Json(String url, String object) throws IOException {
        HttpURLConnection con = (HttpURLConnection) ((new URL(url).openConnection()));
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");
        con.connect();
        byte[] outputBytes = object.getBytes("UTF-8");
        OutputStream os = con.getOutputStream();
        os.write(outputBytes);
        os.close();
        int responseCode = con.getResponseCode();

    }

    public static String sendGET(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        // con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();

        } else {
            System.out.println("GET request not worked");
            return null;
        }

    }

}
