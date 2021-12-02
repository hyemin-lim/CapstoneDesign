package com.example.authtest.ViewController;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.authtest.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RestaurantReview extends AppCompatActivity implements SensorEventListener {

    PieChart pieChart;
    int[] colorArray = new int[] {Color.LTGRAY, Color.BLUE, Color.RED};

    private int rGood = 0;
    private int rBad = 0;

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    static RequestQueue requestQueue;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    Double lat;
    Double lng;

    private ArrayList<com.example.authtest.Model.RestaurantReview> restaurantReviewList = new ArrayList<com.example.authtest.Model.RestaurantReview>(); //TODO: STREET LIGHT DATA LIST


    // Todo:- UI 개선하기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: 서버 통신
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setContentView(R.layout.activity_empty);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);

        setContentView(R.layout.activity_empty);
        GetAPI("http://52.42.115.23:3000/restaurant_review");


    }

    //TODO: 파이 차트에 데이터 넣는 함수
    private ArrayList<PieEntry> data1() {
        ArrayList<PieEntry> datavalue = new ArrayList<>();

        Log.e("ServiceExample", "rGood: " + rGood + ", rBad: " + rBad);
//        Log.e("piejson", "->"+rGood+" and "+rBad);

        datavalue.add(new PieEntry(rGood,"좋음"));
        datavalue.add(new PieEntry(rBad,"싫음"));

        return datavalue;
    }

    // Todo:- MapsActivity에 똑같은 메서드 있어서 재사용 기대해볼 수 있겠으나..
    //          그렇게 만들 시간없다.
    //TODO: 안씀
    public String GetJsonString(String fileName){ //TODO: get string from json file
        String json = "";
        try {
            InputStream is = getAssets().open(fileName); // json파일 이름
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            //json파일명을 가져와서 String 변수에 담음
            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.e("piejson", json);

        return json;
    }


    //TODO: 서버에서 받은 스트링으로 좋음/싫음 개수 계산하고 파이차트 만드는 기능.
    public void RestaurantReviewData(String json){ //TODO: parse the string to data objects
        Log.e("ServiceExample", "review Data");
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray reviews = jsonObject.getJSONArray("restaurant_review");
            Log.e("ServiceExample", "reviews: " + reviews);

            for(int i = 0; i < reviews.length(); i++){
                JSONObject review = reviews.getJSONObject(i);

                // Todo:- 디비에는 boolean으로 저장되어있지만, 숫자로 사용해야 함.
                int r = review.getInt("review");
                // TODO: 해당 위치의 평가만 받아옴
                //눌린 마커의 위경도 lat, lng
                Intent getIntent = getIntent();
                lat = getIntent.getExtras().getDouble("lat");
                lng = getIntent.getExtras().getDouble("lng");
                //리뷰의 위경도 rlat, rlng
                double rlat = review.getDouble("latitude");
                double rlng = review.getDouble("longitude");
                if(lat == rlat && lng == rlng){
                    if (r==1) {    // 좋음 누른 데이터 수
                        Log.e("ServiceExample", "true");
                        rGood++;
                    }
                    else{
                        rBad++;
                    }
                }

            }

            // Todo:- 파이차트 데이터
            pieChart = findViewById(R.id.pieChart);

            PieDataSet pieDataSet = new PieDataSet(data1(),"좋음싫음 설문조사");
            Log.e("ServiceExample", "onStart rGood: " + rGood + ", rBad: " + rBad);
            pieDataSet.setColors(colorArray);
            PieData pieData = new PieData(pieDataSet);
            pieChart.setDrawEntryLabels(true);
            pieChart.setUsePercentValues(true);
            pieData.setValueTextSize(30);
            pieChart.setCenterText("설문조사");
            pieChart.setCenterTextSize(25);
            pieChart.setHoleRadius(30);
            pieChart.setData(pieData);
            pieChart.invalidate();

        } catch (JSONException e){
//            Log.e("piejson", "exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(sensorManager!=null){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    // 이벤트발생!!
                    Intent intent = new Intent(RestaurantReview.this, CameraCapture.class);
                    Intent getIntent = getIntent();
                    if (getIntent != null) {
                        //별점과 별점수텍스트 받아오고 뷰에 세팅해줌
                        lat = getIntent.getExtras().getDouble("lat");
                        lng = getIntent.getExtras().getDouble("lng");
                        intent.putExtra("lat", lat);
                        intent.putExtra("lng", lng);
                    }

                    startActivity(intent);
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }

        }

    }
    //TODO: 서버 통신
    public void GetAPI(String url){ //TODO: get json string from the server
        String json = "";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ServiceExample","response -> " + response);
                        RestaurantReviewData(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ServiceExample", "error -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
        Log.e("ServiceExample", "get 요청");
    }

    public void processResponse(String response) {

        try {
            //TODO: 파일 생성
            //File file = new File(getApplicationContext().getFilesDir().getPath().toString() + "/test.json");
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/test.json");
            file.createNewFile();
            //TODO: 파일에 GET으로 받은 JSON STRING 쓰기
            FileWriter os = new FileWriter(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/test.json");
            int data = response.length();
            os.write(response);
            os.close();
            //TODO: 파일에서 JSON STRING 받아오기
            FileReader is = new FileReader(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/test.json");
            char[] buffer = new char[data];
            int isread;
            while ((isread = is.read(buffer)) != -1) {
                String d = new String(buffer, 0, isread);
                Log.d("ServiceExample", "file data: " + d);
            }
            is.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}