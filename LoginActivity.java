package com.example.chokyuseong.catchmekr;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.chokyuseong.catchmekr.storage.HttpClient;
import  android.content.SharedPreferences;


import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    TextView tvTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //폰내부 데이터 로그인 흔적이 있으면 바로넘어가자~
//        if (getPreferences("CMKMID")!=""){
//            cmkmgo() ;
//
//        }
        //System.out.println("getPreferences(CMKMID) =[" + getPreferences("CMKMID") + "]");


        Button loginbtn =(Button) findViewById(R.id.loginBtn);
        final Map<String,String> map = new HashMap<>();

        //System.out.println("찍혀랏 [" + map + "]");
        loginbtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                EditText idText =(EditText) findViewById(R.id.idText);
                EditText pwdText =(EditText) findViewById(R.id.passwordText);
                map.put("id",idText.getText().toString());
                map.put("pwd",pwdText.getText().toString());

                System.out.println("찍혀랏 [" + map + "]");
                LoginTask lt = new LoginTask();
                tvTest =(TextView) findViewById(R.id.tvtest);


                lt.execute(map);
            }
        });

        Button registerBtn = (Button) findViewById(R.id.registerButton);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });




    }

    public void cmkmgo(){
        Intent cmkmain_go = new Intent(LoginActivity.this,CMKmain.class);
        LoginActivity.this.startActivity(cmkmain_go);
        finish();
    }
    // 값 불러오기
    private String getPreferences(String str){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString(str, "");
    }

    // 값 저장하기
    private void savePreferences(String str){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("CMKMID", str);
        System.out.println("로그인 하면서 저장됨");
        editor.commit();
    }

    // 값(Key Data) 삭제하기
    private void removePreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("CMKMID");
        editor.commit();
    }

    // 값(ALL Data) 삭제하기
    private void removeAllPreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }



    class LoginTask extends AsyncTask<Map,Integer,String>{

        @Override
        protected String doInBackground(Map... map) {
            HttpClient.Builder http = new HttpClient.Builder("POST", "http://192.168.0.13:8083/catchserver/Login_Check");
            http.setTestMap(map[0]);
            HttpClient post = http.create();
            post.request(false);
            int statusCode = post.getHttpStatusCode();
            String body = post.getBody();
            return body;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);

            if (s.equals("0")){
                System.out.println("아이디없음");

            }else{
                savePreferences("already");

                tvTest.setText("Login성공...");

                cmkmgo();
                System.out.println("로그인성공");
            }
        }
    }






}
