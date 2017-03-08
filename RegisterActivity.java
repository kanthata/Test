package com.example.chokyuseong.catchmekr;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.chokyuseong.catchmekr.storage.HttpSendC;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private boolean validate = false;
    private AlertDialog dialog;
    private EditText obj_etId;
    private EditText obj_pwd;
    private EditText obj_age;
        private ImageView Imgview;
    private Button btnRegisterConfirm;
    private String userGender;
    private static final int RESULT_LOAD_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegisterConfirm = (Button) findViewById(R.id.btnRegisterConfirm);
        obj_etId = (EditText) findViewById(R.id.etId);
        obj_pwd = (EditText)findViewById(R.id.etPwd) ;
        obj_age = (EditText)findViewById(R.id.etAge) ;
        Button obj_btnValid = (Button) findViewById(R.id.btnValid);

        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        int genderGroupId = genderGroup.getCheckedRadioButtonId();
        userGender = ((RadioButton) findViewById(genderGroupId)).getText().toString();
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                RadioButton genderButton = (RadioButton)findViewById(i);
                userGender = genderButton.getText().toString();
            }
        });


        obj_btnValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = obj_etId.getText().toString();
                if(validate){
                    return;
                }
                if(userID.equals(""))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog= builder.setMessage("아이디는 빈칸 안됨").setPositiveButton("확인",null).create();
                    dialog.show();
                    return;
                }
                RegisterTask rt = new RegisterTask();
                rt.execute(userID);
            }
        });

        Imgview = (ImageView) findViewById(R.id.Imgview);
        Imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        btnRegisterConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = ((BitmapDrawable) Imgview.getDrawable()).getBitmap();
                ImageandData IAD = new ImageandData(image);

                String userID = obj_etId.getText().toString();
                String userPwd = obj_pwd.getText().toString();
                String useGender = userGender;
                String userAge = obj_age.getText().toString();
                Map<String,String> map = new HashMap<String, String>();
                map.put("userId",userID);
                map.put("userPwd",userPwd);
                map.put("useGender",useGender);
                map.put("userAge",userAge);

                IAD.execute(map);
            }
        });




    }//onCreate 종료


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            Imgview.setImageURI(selectedImage);
        }
    }
    private class RegisterTask extends AsyncTask<String,Void,JSONArray>{
        @Override
        protected JSONArray doInBackground(String... params) {
            HttpSendC HSC =new HttpSendC(params[0],"http://192.168.0.13:8083/catchserver/validChk");
            System.out.println("생성자생성");
            HSC.Initiation();
            System.out.println("initiation");
            JSONArray ja = HSC.getJsonArray();

            return ja;
        }
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            System.out.println("종료됨 registerTast");
            int temp =1;
            for(int i = 0 ; i <jsonArray.length();i++){
                try {
                     temp = Integer.parseInt(jsonArray.getJSONObject(i).get("val").toString()) ;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(temp==0){
                Toast.makeText(RegisterActivity.this, "중복회원없음", Toast.LENGTH_SHORT).show();
                obj_etId.setEnabled(false);
                btnRegisterConfirm.setEnabled(true);

            }else{
                Toast.makeText(RegisterActivity.this, "중복회원", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class  ImageandData extends AsyncTask<Map,Void,String>{
        Bitmap imgdatas;

        public ImageandData(Bitmap imgdata) {
            this.imgdatas = imgdata;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONArray jsonArray = null;
            try{
                jsonArray = new JSONArray(s);
                String reg_result = jsonArray.getJSONObject(0).get("val").toString();
                if(Integer.parseInt(reg_result)>0){
                    Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(LoginIntent);
                    finish();
                }
            }catch(Exception e){

            }

        }

        @Override
        protected String doInBackground(Map... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imgdatas.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            //ArrayList<Map> dataToSend = new ArrayList<>();
            HashMap<String,String> sendImage_string = new HashMap<>();

            sendImage_string.put("image",encodedImage);
            //sendImage_string.put("image","ggg");
            Map<String,String> sendRegisterData = new HashMap<>();


            //uploadinfo에 같이 데이터 태워보내기
            sendImage_string.put("userId",params[0].get("userId").toString());
            sendImage_string.put("userPwd",params[0].get("userPwd").toString());
            sendImage_string.put("useGender",params[0].get("useGender").toString());
            sendImage_string.put("userAge",params[0].get("userAge").toString());
            //uploadImageInfo(sendImage_string);
            return uploadImageInfo(sendImage_string);
        }
    }


    private String uploadImageInfo(HashMap<String,String> aParams) {
        final int TIME_OUT = 20;
        final String POST_METHOD = "POST";
        final String SERVER_URL = "http://192.168.0.13:8083/catchserver/android_register";
        int result = 0;
        String line = null;
        URL url = null;
        HttpURLConnection httpConn = null;
        StringBuffer response = new StringBuffer();
        try {
            url = new URL(SERVER_URL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(TIME_OUT * 1000);
            httpConn.setConnectTimeout(TIME_OUT * 1000);
            httpConn.setReadTimeout(TIME_OUT * 1000);
            httpConn.setRequestMethod(POST_METHOD);
            // HTTP 요청 시에 urlencoded 방식으로 인코딩 후 전송한다
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 이미지 정보를 write한다
            OutputStream os = httpConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(aParams));
            writer.flush();
            writer.close();
            os.close();
            int responseCode = httpConn.getResponseCode();
            System.out.println("responseCode" + responseCode);
            if (responseCode >= 200 && responseCode < 300) {
                InputStream in = httpConn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(in));

                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpConn.disconnect();
        }
        return String.valueOf(response);
    }
    private String getPostDataString(HashMap<String, String> aParams) {
        boolean isFirst = true;
        StringBuilder result = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : aParams.entrySet()) {
                if (true == isFirst) {
                    isFirst = false;
                } else {
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (Exception e) {

        }
        return result.toString();
    }

}
