package com.example.chokyuseong.catchmekr.storage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by CHOKYUSEONG on 2017-02-14.
 */

public class HttpClient {
    private static final String WWW_FORM = "application/x-www-form-urlencoded";
    private int httpStatusCode;
    private String body;
    private JSONArray jsonArray;


    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getBody() {
        return body;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    private Builder builder;

    private void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public void request(Boolean jflag) {
        HttpURLConnection conn = getConnection();
        setHeader(conn);
        setBody(conn);
        httpStatusCode = getStatusCode(conn);
        if (!jflag) {
            body = readStream(conn);
        }else{
            jsonArray = readStreamJSON(conn);
        }
        conn.disconnect();
    }
//익셉션처리하기.
    private HttpURLConnection getConnection() {
        try {
            URL url = new URL(builder.getUrl());
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setHeader(HttpURLConnection connection) {
        setContentType(connection);
        setRequestMethod(connection);
        connection.setConnectTimeout(5000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
    }

    private void setContentType(HttpURLConnection connection) {
        connection.setRequestProperty("Content-Type", WWW_FORM);
    }

    private void setRequestMethod(HttpURLConnection connection) {
        try {
            connection.setRequestMethod(builder.getMethod());
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }

    private void setBody(HttpURLConnection connection) {
        String parameter = builder.getParameters();
        if (parameter != null && parameter.length() > 0) {
            OutputStream outputStream = null;
            try {
                outputStream = connection.getOutputStream();
                outputStream.write(parameter.getBytes("UTF-8"));
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getStatusCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -10;
    }

    private String readStream(HttpURLConnection connection) {
        String result = "";
        BufferedReader reader = null;
        String[] getJsonData = new String[3];
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
//

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
            }
        }
        return result;
    }


    private JSONArray readStreamJSON(HttpURLConnection connection)
    {
        String result = "";
        BufferedReader reader = null;
        String[] getJsonData = new String[4];
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
             jsonArray = new JSONArray(result);

//            JSONObject json = new JSONObject();
//            for (int i = 0; i < jsonArray.length(); i++) {
//                json = jsonArray.getJSONObject(i);
//                getJsonData[0] = json.getString("CMK_PHNO");
//                getJsonData[1] = json.getString("CMK_LAT");
//                getJsonData[2] = json.getString("CMK_LON");
//                getJsonData[3] = json.getString("LOGTIME");
//            }
//            result = "[SpringData-1] ---- " +getJsonData[0]+"[SpringData-2] ---- " + getJsonData[1]+"[SpringData-3] ---- " +getJsonData[2] +"[SpringData-4] ---- " +getJsonData[3];
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
            }
        }
        //System.out.println("httpclient jsonArray:" + jsonArray);


        return jsonArray;
    }






    public static String convertInputStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }

    public static class Builder {
        private Map parameters;
        private String method;
        private String url;

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public Builder(String method, String url) {
            if (method == null) {
                method = "GET";
            }
            this.method = method;
            this.url = url;
            this.parameters = new HashMap();
        }

        public void addOrReplace(String key, String value) {
            this.parameters.put(key, value);
        }

        public String getParameters() {
            return generateParameters();
        }
//        public String getParameterMapValue(String str){
//            return parameters.get(str).toString();
//        }

        public void setTestMap(Map<String,String> map){
            this.parameters = map;

        }


        public String getParameter(String key) {
            return this.parameters.get(key).toString();
        }



        private String generateParameters() {
            StringBuffer parameters = new StringBuffer();
            Iterator keys = getKeys();
            String key = "";
            while (keys.hasNext()) {
                key = keys.next().toString();
                parameters.append(String.format("%s=%s", key, this.parameters.get(key)));
                parameters.append("&");
            }
            String params = parameters.toString();
            if (params.length() > 0) {
                params = params.substring(0, params.length() - 1);
            }
            return params;
        }

        private Iterator getKeys() {
            return this.parameters.keySet().iterator();
        }

        public HttpClient create() {
            HttpClient client = new HttpClient();
            client.setBuilder(this);
            return client;
        }
    }
}
