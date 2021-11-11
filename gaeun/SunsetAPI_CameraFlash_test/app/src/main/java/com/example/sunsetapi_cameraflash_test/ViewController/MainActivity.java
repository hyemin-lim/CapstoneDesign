package com.example.sunsetapi_cameraflash_test.ViewController;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sunsetapi_cameraflash_test.Model.SunsetList;
import com.example.sunsetapi_cameraflash_test.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/* Todo:- 안드로이드에서의 View와 Control은 분리되어 있지 않고,
        Activity 안에 포함되어 있다.
        onCreate()내부 : View와 관련된 내용
        onClick()내부 : Control과 관련된 내용

        그래서 단점이 한 곳의 코드가 비대해진다는 점이다.
        하지만, 사용하기 쉽다는 장점이 있다.
* */
public class MainActivity extends AppCompatActivity {
    // Todo:- 카메라 플래시
    private CameraManager cameraManager;
    private String getCameraID;

    // Todo:- 조도 센서
    SensorManager lightSensorManager;
    SensorEventListener listener;
    Sensor light;

    // Todo:- 알람 보내기
    private AlarmManager alarmManager;
    private GregorianCalendar mCalender;
    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    EditText editText;
    TextView textView;
    TextView textView2;
    static RequestQueue requestQueue;

    // Todo:- 오늘 날짜 구하기
    long now = System.currentTimeMillis();
    Date mDate = new Date(now);
    SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat alarmDate = new SimpleDateFormat("yyyy-MM-dd");
    String Today = simpleDate.format(mDate);
    String alarmDay = alarmDate.format(mDate);
    String alarmTime;
    String alarmHour;
    String alarmMinute;
    int alarm_HHMM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        // Todo:- 카메라 플래시
        // cameraManager to interact with camera devices
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        // Exception is handled, because to check whether
        // the camera resource is being used by another
        // service or not.
        try {
            // O means back camera unit,
            // 1 means front camera unit
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Todo:- 조도센서
        lightSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        listener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)           // api 버전 체크하는 것
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView2.setText(String.valueOf(event.values[0]));

                long now = System.currentTimeMillis();
                Date mDate = new Date(now);
                SimpleDateFormat HHMMFormat = new SimpleDateFormat("HHmm");
                String todayHHMM = HHMMFormat.format(mDate);
                int now_HHMM = Integer.parseInt(todayHHMM); // 카메라 플래시 조건애 사용될 현재시간을 정수로

                alarm_HHMM = 638; // Todo:- 테스트 위해 가짜 시간 넣음.
//                Log.e("time", "조건으로 들어가는 시간들" + now_HHMM + "   " + alarm_HHMM);

                // Todo:- 현재시간과 일몰시간 비교
                if ((event.values[0] < 100) && ((now_HHMM - alarm_HHMM)>0) ){         // Todo:- 카메라 플래시 켜는 조건(조도센서가 어둡고, 일몰시간 이후)
                    // 카메라 플래시 켜기
                    flashLightOn();
                }else{
                    // 카메라 플래시 끄기
                    flashLightOff();
                }
            }
        };
        lightSensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_FASTEST);

        // Todo:- 알람 보내기
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mCalender = new GregorianCalendar();
//        Log.v("HelloAlarmActivity", mCalender.getTime().toString());

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

    }


    //----------------------------------------------------------------------------------------------


    @Override
    protected void onPause() {
        super.onPause();
        lightSensorManager.unregisterListener(listener, light);
    }

    // 카메라 플래시
    // RequiresApi is set because, the devices which are
    // below API level 10 don't have the flash unit with
    // camera.
    @RequiresApi(api = Build.VERSION_CODES.M)   // api 버전 체크하는 것
    public void flashLightOn() {                // 플래시 켜는것
        // Exception is handled, because to check
        // whether the camera resource is being used by
        // another service or not.
        try {
            // true sets the torch in ON mode
            cameraManager.setTorchMode(getCameraID, true);

            // Inform the user about the flashlight
            // status using Toast message
            Toast.makeText(MainActivity.this, "Flashlight is turned ON", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)   // api 버전 체크하는 것
    public void flashLightOff() {               // 플래시 끄는것
        // Exception is handled, because to check
        // whether the camera resource is being used by
        // another service or not.
        try {
            // true sets the torch in OFF mode
            cameraManager.setTorchMode(getCameraID, false);

            // Inform the user about the flashlight
            // status using Toast message
            Toast.makeText(MainActivity.this, "Flashlight is turned OFF", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace();
        }
    }

    private void setAlarm() {
        //AlarmReceiver에 값 전달
        Intent receiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, receiverIntent, 0);

        String from = alarmDay + " " + alarmTime;    // 오늘 hh시 mm분 ss초 알람 보내기
        println("알람 보낼 날짜와 시간 : " + from);
        from = "2021-10-31 06:38:00";       // 테스트를 위해 임의로 날짜와 시간 지정
        println("테스트용 알람 보낼 날짜와 시간 : " + from);
        alarm_HHMM = Integer.parseInt(alarmHour+alarmMinute);

        //날짜 포맷을 바꿔주는 소스코드
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datetime = null;
        try {
            datetime = dateFormat.parse(from);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datetime);

        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(),pendingIntent);

    }

    public void makeRequest() {
        // Todo:- date를 위에서 구한 오늘 날짜로 넣기. 그리고 xml에서 ""으로 바꿔야 함.아니면 인풋 받지 말거나.
        String date = Today;
//        String date = editText.getText().toString();    // 20211031

        String url = "";    // 키값 들어있어서 뺐음. 카톡방에 링크 보냄.
        url = url + "&locdate=" + date;
        // Todo:- url에 서비스키 조심하기.
        // Todo:- url로 받으면 xml이 오기 때문에 호출이 되는지만 확인하고, 실제 값 사용은 json 테스트 파일 사용함.

        StringRequest request = new StringRequest(
                Request.Method.GET,                    // Todo:- GET 메서드 사용
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        println("응답 -> " + response);

                        processResponse(response);

                        // Todo:- 일몰시간 응답 받은 후, 알람 보내기
                        setAlarm();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        println("에러 -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
        println("요청 보냄.");
    }

    public void println(String data) {
        Log.d("MainActivity", data);
    }

    public void processResponse(String response) {
/*
            Gson gson = new Gson();     // Todo:- gson 이용
            SunsetList sunsetList = gson.fromJson(json, SunsetList.class);
            println("sunset : " + sunsetList.sunset.get(0).sunset_time);

            // 텍스트뷰에 일몰시간 보이기
            textView.setText(sunsetList.sunset.get(0).sunset_time); // "1735"

            // 일몰시간을 변형해서 알람시간으로 만들기
            String[] arr = sunsetList.sunset.get(0).sunset_time.split("");
            alarmHour = arr[0] + arr[1];
            alarmMinute = arr[2] + arr[3];
            alarmTime = alarmHour + ":" + alarmMinute;      // "17:35"
            println("alarmTime ; " + alarmTime);
*/
        /******* test by json file **********************************************************************************************/
        // Todo:- json 테스트 파일 불러올 때 gson 이용
        String json = "";
        try {
            InputStream is = getAssets().open("jsons/test.json"); // json파일 이름
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            //json파일명을 가져와서 String 변수에 담음
            json = new String(buffer, "UTF-8");
            Log.d("--  json = ", json);

            Gson gson = new Gson();     // Todo:- gson 이용
            SunsetList sunsetList = gson.fromJson(json, SunsetList.class);
            println("sunset : " + sunsetList.sunset.get(0).sunset_time);

            // 텍스트뷰에 일몰시간 보이기
            textView.setText(sunsetList.sunset.get(0).sunset_time); // "1735"

            // 일몰시간을 변형해서 알람시간으로 만들기
            String[] arr = sunsetList.sunset.get(0).sunset_time.split("");
            alarmHour = arr[0] + arr[1];
            alarmMinute = arr[2] + arr[3];
            alarmTime = alarmHour + ":" + alarmMinute;      // "17:35"
            println("alarmTime ; " + alarmTime);


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        /******* end of test by json file ***************************************************************************************/

    }

}
