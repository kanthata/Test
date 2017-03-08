package com.example.chokyuseong.catchmekr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chokyuseong.catchmekr.storage.HttpClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CMKmain extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    static LatLng Userlatlng;
    public MapFragment mapFragment;
    public JSONObject json;
    public boolean mustStop = false;
    public TextView tvPhno;
    public TextView tvLat;
    public TextView tvLon;

    public TextView tvaccx;
    public TextView tvaccy;
    public TextView tvaccz;
    //가속센서 객체


    private SensorManager mSensorManager = null;
    public SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;

    //가속값 메인 연산용객체
    double Maccx=0;
    double Maccy=0;
    double Maccz=0;
    double MangleXZ;
    double MangleYZ;
    double gasoknumber =0.00005;

    //기본값 세팅
    Double lat = 37.564;
    Double lon = 126.97;

    public String phno;
    public double latitude;
    double longitude;
    String logtime;
    String pno;
    public TelephonyManager tmr;
    ArrayList<Marker> seoul = new ArrayList<>();
    // 유저 충돌체크위한 위치 객체리스트
    ArrayList<LatLng> userpositionList;
    int whoami;


    class SpotThread implements Runnable {
        String name = "";
        SpotTask spotTask;
        int i = 0;

        GoogleMap Map;

        public SpotThread(GoogleMap map) {
            this.Map = map;
            pno = tmr.getLine1Number();
            pno = pno.substring(pno.length() - 10, pno.length());
            pno = "0" + pno;
            System.out.println("SpotThread 생성자 값:" + pno);

            mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
        }

        @Override
        public void run() {
            while (true) {

                Maccx= Maccx*gasoknumber*-1;
                //Maccy= Maccy*gasoknumber;
                Maccz= Maccz*gasoknumber;

                if(Maccz >0){
                    Maccy = (Maccy-5)*-1;
                }else
                {
                    //Maccy = ((Maccy-7)*-1)+2;
                    Maccy= (9.8-Maccy+5)*-1;
                }
                Maccy= Maccy*gasoknumber;


                lat = lat + Maccy;
                lon = lon + Maccx;
                System.out.println("ACCX 값은???????????????????????????????????:[" + Maccx +"]");
                System.out.println("i 값은 ? :[" + i + "]");
//                TextView name = (TextView) findViewById(R.id.etName);
//                TextView country = (TextView) findViewById(R.id.etCountry);
//                TextView twitter = (TextView) findViewById(R.id.etTwitter);
                spotTask = new SpotTask();
                Map params = new HashMap();

                params.put("PHNO", pno);
                params.put("LATITUDE", lat);
                params.put("LONGITUDE", lon);
                spotTask.execute(params);
                try {
                    Thread.sleep(300);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mustStop) {
                    spotTask = null;
                    break;
                }
                i++;
//                lat += 0.0001;
//                lon += 0.0001;
            }
            Thread.interrupted();

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmkmain);

        tvPhno = (TextView) findViewById(R.id.tvPhno);
        tvLat = (TextView) findViewById(R.id.tvLat);
        tvLon = (TextView) findViewById(R.id.tvLon);
        tvaccx = (TextView) findViewById(R.id.textaccx);
        tvaccy = (TextView) findViewById(R.id.textaccy);
        tvaccz = (TextView) findViewById(R.id.textaccz);


        tmr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        //구글맵 객체 생성지정
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //가독도센서 객체 초기화 리스너등록
        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //Using the Accelometer
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new AccelometerListener();


        System.out.print("oncreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mustStop = true;
        System.out.println("onPause클릭 + mustStop :[" + mustStop + "]");
        mSensorManager.unregisterListener(mAccLis);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mAccLis);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        System.out.println("OnmapReady");
        SpotThread sth = new SpotThread(map);
        Thread th = new Thread(sth);
        th.setDaemon(true);
        th.start();
    }

    public class SpotTask extends AsyncTask<Map<String, String>, Integer, JSONArray> {

        @Override
        protected JSONArray doInBackground(Map<String, String>... params) {
            System.out.println("param[0] :" + params[0]);
            HttpClient.Builder http = new HttpClient.Builder("POST", "http://192.168.0.13:8083/catchserver/latlon");
            http.setTestMap(params[0]);
            HttpClient post = http.create();
            post.request(true);
            int statusCode = post.getHttpStatusCode();
            JSONArray body = post.getJsonArray();
            return body;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);

            for(int tempi=0; tempi<seoul.size(); tempi ++){
                seoul.get(tempi).remove();
            }

            try {
               // pno = tmr.getLine1Number();
                System.out.println("OnpostExecute 들어옴!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("jsonArray.length() :[" + jsonArray.length() + "]");
                userpositionList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    json = new JSONObject();
                    json = jsonArray.getJSONObject(i);

                    phno = json.getString("CMK_PHNO");
                    latitude = Double.valueOf(json.getString("CMK_LAT")).doubleValue();
                    longitude = Double.valueOf(json.getString("CMK_LON")).doubleValue();
                    logtime = json.getString("LOGTIME");

                    if (phno.equals(pno)) {
                        System.out.println("안들어오나?");
                        tvPhno.setText(phno);
                        tvLat.setText(Double.toString(latitude));
                        tvLon.setText(Double.toString(longitude));
                    } else {
//                        tvPhno.setText(phno);
//                        tvLat.setText(Double.toString(latitude));
//                        tvLon.setText(Double.toString(longitude));
                    }

                    //System.out.println(latitude + "---" + longitude);
                    Userlatlng = new LatLng(latitude, longitude);
                    userpositionList.add(Userlatlng);

                    ////////////////////////////////////////////////////////

                    seoul.add(googleMap.addMarker(new MarkerOptions().position(Userlatlng).title(phno)));
//                    if(!seoul.isEmpty()){
//                        for (int deli = 0; deli < (seoul.size()-2); deli++){
//                            seoul.get(deli).remove();
//                        }
//                    }

                    System.out.println("phno=" + phno + "pno=" + pno);
                    if (phno.equals(pno)) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Userlatlng));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        whoami = i;
                    }
                }
                if(crash()){
                    Toast.makeText(getApplicationContext(),"충돌함",Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {

            }
        }
        //충돌체크
        public boolean crash(){
            double distance = 0.0005;
            double mySpot_lat = userpositionList.get(whoami).latitude;
            double mySpot_lon = userpositionList.get(whoami).longitude;
            double someone_lat;
            double someone_lon;
            for(int i = 0; i <userpositionList.size(); i++){
            if(i==whoami){
                continue;
            }
            someone_lat = userpositionList.get(i).latitude;
            someone_lon = userpositionList.get(i).longitude;

            if( distance > Math.sqrt(Math.pow(mySpot_lat-someone_lat,2) + Math.pow(mySpot_lon-someone_lon,2))){
                return true;
            }
        }
        return false;
    }

    }
    public class AccelometerListener implements SensorEventListener{
        double accX;
        double accY;
        double accZ;
        double angleXZ;
        double angleYZ;

        @Override
        public void onSensorChanged(SensorEvent event) {
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];
            //System.out.println("onSensorChanged 값 : ["+Double.toString(accX)+"]");
            angleXZ = Math.atan2(accX,  accZ) * 180/Math.PI;
            angleYZ = Math.atan2(accY,  accZ) * 180/Math.PI;
            tvaccx.setText(Double.toString(accX));
            tvaccy.setText(Double.toString(accY));
            tvaccz.setText(Double.toString(accZ));

            Maccx = accX;
            Maccy = accY;
            Maccz = accZ;
            MangleXZ = angleXZ;
            MangleYZ = angleYZ;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}
