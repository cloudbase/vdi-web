package com.cloudbase.vdi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DoRequest {
    public static String executeGet(String targetURL, String keystoneToken) {

        try {
            System.out.println("vdi url:" + targetURL);
            StringBuilder result = new StringBuilder();
            URL url = new URL(targetURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Auth-Token", keystoneToken);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        /*
         * HttpURLConnection connection = null;
         * 
         * try { // Create connection URL url = new URL(targetURL); connection =
         * (HttpURLConnection) url.openConnection();
         * connection.setUseCaches(false); connection.setRequestMethod("GET");
         * 
         * // connection.setRequestProperty("Content-Length", //
         * Integer.toString(body.getBytes().length));
         * connection.setRequestProperty("X-Auth-Token", keystoneToken);
         * 
         * connection.setUseCaches(false); connection.setDoOutput(true);
         * 
         * // Send request /* DataOutputStream wr = new
         * DataOutputStream(connection.getOutputStream()); wr.writeBytes(body);
         * wr.close();
         * 
         * 
         * // Get Response InputStream is = connection.getInputStream();
         * BufferedReader rd = new BufferedReader(new InputStreamReader(is));
         * StringBuilder response = new StringBuilder(); // or StringBuffer if
         * // Java version 5+ String line; while ((line = rd.readLine()) !=
         * null) { response.append(line); response.append('\r'); } rd.close();
         * return response.toString(); }catch(
         * 
         * Exception e)
         * 
         * { e.printStackTrace(); return null; } finally
         * 
         * { if (connection != null) { connection.disconnect(); } }
         */

    }

    public static String executePost(String targetURL,
                                     String body,
                                     String keystoneToken,
                                     Map<String, List<String>> headers) {
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
            connection.setRequestProperty("Content-Type", "application/json");
            if (keystoneToken != null)
                if (keystoneToken.length() > 0) {
                    System.out.println("Token is sent to conductor");
                    connection.setRequestProperty("X-Auth-Token", keystoneToken);
                }

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if
                                                          // Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            if (headers != null)
                headers.putAll(connection.getHeaderFields());
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
