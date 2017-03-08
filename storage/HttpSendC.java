package com.example.chokyuseong.catchmekr.storage;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by CHOKYUSEONG on 2017-02-22.
 */

public class HttpSendC {
    HttpURLConnection conn;
    Object obj;
    String URLString ="";
    String WWW_FORM = "application/x-www-form-urlencoded";
    JSONArray jsonArray;

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public HttpSendC(Object obj, String URLString) {

        this.obj = obj;
        this.URLString = URLString;

    }
    public void Initiation(){
        try {
            System.out.println(URLString);
            URL url = new URL(URLString);
            conn =(HttpURLConnection) url.openConnection();
            setHeader();
            setBody(obj);
            conn.getResponseCode();
            readStreamJSON(conn);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setBody(Object obj) {
        String tempstr = "";
        if(obj instanceof String){
            tempstr = "ValidID="+ obj;
            System.out.println("instanceof 비교  String");
        }else if(obj instanceof Map){
            tempstr = generateParameters((Map) obj);
            System.out.println("instanceof 비교");
        }
        if (tempstr != null && tempstr.length() > 0) {
            OutputStream outputStream = null;
            try {
                outputStream = conn.getOutputStream();
                outputStream.write(tempstr.getBytes("UTF-8"));
                outputStream.flush();
                System.out.println("flush됨");
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

    private void setHeader(){
        try{
            conn.setRequestProperty("Content-Type", WWW_FORM);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private JSONArray readStreamJSON(HttpURLConnection connection) {
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
        return jsonArray;
    }


    private String generateParameters(Map maps) {
        StringBuffer parameters = new StringBuffer();
        Iterator keys = maps.keySet().iterator();
        String key = "";
        while (keys.hasNext()) {
            key = keys.next().toString();
            parameters.append(String.format("%s=%s", key, maps.get(key)));
            parameters.append("&");
        }
        String params = parameters.toString();
        if (params.length() > 0) {
            System.out.println("변경전" +params);
            params = params.substring(0, params.length() - 1);
            System.out.println("변경후" + params);
        }
        return params;
    }




}
