package com.example.maptest.ViewController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.maptest.Model.StreetLight;
import com.example.maptest.Model.StreetLightList;
import com.example.maptest.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maptest.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.widget.EditText;
import android.widget.TextView;


import com.example.maptest.Model.SunsetList;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


import com.example.maptest.Model.inform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    // Todo:- 카메라 플래시
    private CameraManager cameraManager;
    private String getCameraID;

    // Todo:- 조도 센서
    SensorManager lightSensorManager;
    SensorEventListener listener;
    Sensor light;
    private static boolean mFlashOn = false;

    //문자 보내기 정보
    private inform model;

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

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_CODE_LOCATION = 2;

    private static final int MULTIPLE_PERMISSION = 10235;
    private String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<StreetLight> lightlist = new ArrayList<StreetLight>(); //TODO: DATA LIST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        model = new inform();

        try{
            processResponseStreetLight();
            processResponseSunset();
            // Todo:- 일몰시간 응답 받은 후, 알람 보내기
            setAlarm();
        }catch (Exception e){
            e.printStackTrace();
        }


        settingGPS();

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


                long now = System.currentTimeMillis();
                Date mDate = new Date(now);
                SimpleDateFormat HHMMFormat = new SimpleDateFormat("HHmm");
                String todayHHMM = HHMMFormat.format(mDate);
                int now_HHMM = Integer.parseInt(todayHHMM); // 카메라 플래시 조건애 사용될 현재시간을 정수로

                alarm_HHMM = 330; // Todo:- 테스트 위해 가짜 시간 넣음.
//                Log.e("time", "조건으로 들어가는 시간들" + now_HHMM + "   " + alarm_HHMM);

                // Todo:- 현재시간과 일몰시간 비교
                if ((event.values[0] < 100) && ((now_HHMM - alarm_HHMM) > 0)) {         // Todo:- 카메라 플래시 켜는 조건(조도센서가 어둡고, 일몰시간 이후)
                    // 카메라 플래시 켜기
                    if (!mFlashOn) {
                        flashLightOn();
                    }

                    Location userLocation = getMyLocation();

                    for (int idx = 0; idx < lightlist.size(); idx++) {

                        if( userLocation != null ) {
                            // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
                            double latitude = userLocation.getLatitude();
                            double longitude = userLocation.getLongitude();

                            Location targetLocation = new Location("");
                            targetLocation.setLatitude(lightlist.get(idx).getY());
                            targetLocation.setLongitude(lightlist.get(idx).getX());


                            if(latitude == lightlist.get(idx).getY() && longitude == lightlist.get(idx).getX()){

                                if (isSMSPermissionAllowed()) {
                                    sms();
                                }
                            }
                        }
                    }
                    mFlashOn = true;
                } else if ((event.values[0] >= 100) && mFlashOn) {
                    // 카메라 플래시 끄기
                    flashLightOff();

                    mFlashOn = false;
                }
            }
        };
        lightSensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_FASTEST);

        // Todo:- 알람 보내기
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mCalender = new GregorianCalendar();
//        Log.v("HelloAlarmActivity", mCalender.getTime().toString());

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        if (!hasPermissions(this, PERMISSIONS)) { //TODO:권한요청이 미리 안 되어있을 경우 -> hasPermissions에서 판단.
            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSION); //TODO: 권한 요청을 함 -> onrequestPermissionResult로 연결.
        } else { //TODO: 권한 요청이 미리 되어있는 경우 지도 실행
            binding = ActivityMapsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
                Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
            }
        }
        return currentLocation;
    }

    private void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }


    public static boolean hasPermissions(Context context, String...permissions){
        if(context != null && permissions != null){
            for (String permission : permissions){
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSION: //TODO: 권한요청이 들어왔을 때
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //TODO: permission granted 되었을때 실행 코드.
                    binding = ActivityMapsBinding.inflate(getLayoutInflater());
                    setContentView(binding.getRoot());

                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else { //TODO: permission setting 하는 팝업창 띄우기.
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("app permission");
                    alertDialog.setMessage("Set permission");
                    //TODO: 권한설정 클릭 시 이벤트 발생.
                    alertDialog.setPositiveButton("setting permission",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package: " + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                    dialog.cancel();
                                }
                            });
                    //TODO: 취소
                    alertDialog.setNegativeButton("cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    alertDialog.show();
                }
                return;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //TODO: Add a marker from the list
        processResponseStreetLight();   //TODO:get json from the file & parse the data
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.50864875768504, 126.93194528072515))); //TODO:move the camera to the first point
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12)); //zoom in
        for (int idx = 0; idx < lightlist.size(); idx++){ //TODO:create markers for every data
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(lightlist.get(idx).getY(), lightlist.get(idx).getX())) //TODO:set position !! Y is the latitude, X is the longitude !!
                    .title(lightlist.get(idx).getId());
            mMap.addMarker(markerOptions);
        }
        //TODO: realtime gps tracker
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    public void processResponseStreetLight() {
        // Todo:- json 테스트 파일 불러올 때 gson 이용
        String json = "";
        try {
            InputStream is = getAssets().open("test/test.json"); // json파일 이름
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            //json파일명을 가져와서 String 변수에 담음
            json = new String(buffer, "UTF-8");
            Log.d("--  json = ", json);

            Gson gson = new Gson();     // Todo:- gson 이용
            StreetLightList streetLightList = gson.fromJson(json, StreetLightList.class);
            println("street light : " + streetLightList.street_light.get(0).street_light_id);

            for (int i=0; i<streetLightList.street_light.size(); i++){
                StreetLight light = new StreetLight();
                light.setId(streetLightList.street_light.get(i).street_light_id);
                light.setX(streetLightList.street_light.get(i).street_light_x);
                light.setY(streetLightList.street_light.get(i).street_light_y);
                lightlist.add(light);
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location\n"+location, Toast.LENGTH_LONG).show();
    }

    // 문자 ----------------------------------------------------------------------------------------------
    private boolean isSMSPermissionAllowed() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "PERMISSION IS GRANTED");
                return true;
            } else {
                Log.v("TAG", "PERMISSION IS REVOKED");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 12);
                return false;
            }
        } else {
            Log.v("TAG", "PERMISSION IS GRANTED");
            return true;
        }
    }

    public void sms() {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(model.phoneNumber, null, model.smsText, null, null);

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

    public void makeRequestSunset() {
        // Todo:- date를 위에서 구한 오늘 날짜로 넣기. 그리고 xml에서 ""으로 바꿔야 함.아니면 인풋 받지 말거나.
        String date = Today;
//        String date = editText.getText().toString();    // 20211031

        String url = "http://apis.data.go.kr/B090041/openapi/service/RiseSetInfoService/getAreaRiseSetInfo?serviceKey=8SpjCKqw1AV5V5kaC5TU65N1VXOZ6cLYyaMMYGQgQNmG5vkTEkJ2VAz8R2tyZnCbLQFKTwdzv0taVaeMd6OyAw%3D%3D&location=%EC%84%9C%EC%9A%B8";    // 키값 들어있어서 뺐음. 카톡방에 링크 보냄.
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

                        // Todo:- response로 json 파일 만들기



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
        Log.d("api call", data);
    }

    public void processResponseSunset() {
        // Todo:- json 테스트 파일 불러올 때 gson 이용
        String json = "";
        try {
            InputStream is = getAssets().open("test/sunset_test.json"); // json파일 이름
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

    }

}