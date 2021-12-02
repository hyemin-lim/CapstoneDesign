package com.example.authtest.ViewController;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.Call;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.authtest.Model.Crosswalk;
import com.example.authtest.Model.Restaurant;
import com.example.authtest.Model.StreetLight;
import com.example.authtest.Model.SunsetList;
import com.example.authtest.Model.inform;
import com.example.authtest.R;
import com.example.authtest.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolygonClickListener {
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

    //TODO: 서버 통신
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

    private SensorManager sensorManager = null;
    private Sensor mGyroSensor = null;
    private SensorEventListener mGyroLis;

    MediaPlayer mAudio = null;

    //Using the Gyroscope
    //Roll and Pitch
    private double pitch;
    private double roll;
    private double yaw;
    //timestamp and dt
    private double timestamp;
    private double dt;
    // for radian -> dgree
    private double RAD2DGR = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_CODE_LOCATION = 2;

    private static final int MULTIPLE_PERMISSION = 10235;
    private static final String TAG = "ServiceExample";

    private String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // 폴리곤의 꼭짓점들
    private List<LatLng> vertices = new ArrayList<>();
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<StreetLight> lightlist = new ArrayList<StreetLight>(); // STREET LIGHT DATA LIST
    private ArrayList<Restaurant> restaurantlist = new ArrayList<Restaurant>(); // RESTAURANT DATA LIST
    private ArrayList<Crosswalk> crosslist = new ArrayList<Crosswalk>(); //: Cross walk DATA LIST


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGyroLis = new GyroListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        model = new inform();

        //TODO: 서버 통신
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
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

                Log.e("time", "조건으로 들어가는 시간들 " + now_HHMM + "   " + alarm_HHMM);

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
        GetStreetLightAPI("http://52.42.115.23:3000/street_light_api");
        GetRestaurantAPI("http://52.42.115.23:3000/restaurant_api");
        GetCrosswWalkAPI("http://52.42.115.23:3000/crosswalk_api");
        GetJaywalkingAPI("http://52.42.115.23:3000/jaywalking_api");

        try
        {
            sleep(5000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if(!hasPermissions(this, PERMISSIONS)){ //권한요청이 미리 안 되어있을 경우 -> hasPermissions에서 판단.
            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSION); // 권한 요청을 함 -> onrequestPermissionResult로 연결.
        }else{ // 권한 요청이 미리 되어있는 경우 지도 실행

            binding = ActivityMapsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapFragment);
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
            case MULTIPLE_PERMISSION: // 권한요청이 들어왔을 때
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // permission granted 되었을때 실행 코드.
                    binding = ActivityMapsBinding.inflate(getLayoutInflater());
                    setContentView(binding.getRoot());

                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else { // permission setting 하는 팝업창 띄우기.
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("app permission");
                    alertDialog.setMessage("Set permission");
                    // 권한설정 클릭 시 이벤트 발생.
                    alertDialog.setPositiveButton("setting permission",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package: " + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                    dialog.cancel();
                                }
                            });
                    // 취소
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

        // Add a marker from the list
        // 신호등, 음식점 json 파일 읽고 마킹
        //get json from the file & parse the data




        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.50864875768504, 126.93194528072515))); //move the camera to the first point
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12)); //zoom in
        for(int idx = 0; idx < restaurantlist.size(); idx++){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(restaurantlist.get(idx).getY(), restaurantlist.get(idx).getX()))
                    .title(restaurantlist.get(idx).getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            BitmapDrawable bitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.restaurant);
            Bitmap b = bitmapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mMap.addMarker(markerOptions);
        }
        for (int idx = 0; idx < lightlist.size(); idx++){ //create markers for every data
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(lightlist.get(idx).getY(), lightlist.get(idx).getX())) //set position !!Y is the latitude, X is the longitude!!
                    .title(lightlist.get(idx).getId());
            BitmapDrawable bitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.street_light);
            Bitmap b = bitmapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mMap.addMarker(markerOptions);
        }

        // 위험지역 json 파일 읽고 마킹
        // json 파일 파싱

        PolygonOptions testPolygonOptions = new PolygonOptions();
        // 폴리곤 꼭짓점 리스트에 있던 점들을 폴리곤 객체에 넣기.
        for(int i = 0; i < vertices.size(); i++){
            testPolygonOptions.clickable(true).add(vertices.get(i));
        }
        Polygon testPolygon = mMap.addPolygon(testPolygonOptions);
        // 태그 달기
        testPolygon.setTag("test");

        // gps 위치가 폴리곤 안에 있는지
        // 여기서부터는 GPS 위치 받아오는 코드
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

//        Log.d("Main", "isGPSEnabled="+ isGPSEnabled);
//        Log.d("Main", "isNetworkEnabled="+ isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // 폴리곤 안에 있는지
                if(PolyUtil.containsLocation(location.getLatitude(), location.getLongitude(), vertices, true)){
                    Log.i(TAG, ""+location.getLongitude()+"/" + location.getLatitude());

                    // 위험 알람 보내기
                    dangerZoneNot();

                }

                AddCrosswalkMarker();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {

            }

            public void onProviderDisabled(String provider) {

            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);







        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnPolygonClickListener(this);

    }

    public String GetJsonString(String fileName){ // get string from json file
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
        return json;
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
                        processResponseSunset();
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
        if (requestQueue != null){
            requestQueue.add(request);
            println("요청 보냄.");

        }
    }

    public void println(String data) {
        Log.d("time", data);
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
//            textView.setText(sunsetList.sunset.get(0).sunset_time); // "1735"

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

    public void AddCrosswalkMarker(){
//        mMap.clear(); // clear 해버리면 이전 마커들 모두 사라져서,, 그냥 주변에 있는거 띄우는건 이렇게 보여줘도 될듯..!
        Location userLocation = getMyLocation();
        MarkerOptions markerOptions = new MarkerOptions();
        for (int idx = 0; idx < crosslist.size(); idx++){//create markers for every data

            Location targetLocation = new Location("");
            targetLocation.setLatitude(crosslist.get(idx).getY());
            targetLocation.setLongitude(crosslist.get(idx).getX());
            if(userLocation != null) {
                float distance = userLocation.distanceTo(targetLocation);
                if(distance <= 100) {   // 가까워지면 횡단보도 마커 표시
                    markerOptions.position(new LatLng(crosslist.get(idx).getY(), crosslist.get(idx).getX())); //set position !!Y is the latitude, X is the longitude!!
                    BitmapDrawable bitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.traffic_light);
                    Bitmap b = bitmapDraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    mMap.addMarker(markerOptions);
                }
            }
        }

    }

    public void CrossWalkjsonParsing(String json){ //TODO: parse the string to data objects
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray crosswalks = jsonObject.getJSONArray("crosswalk");
            for(int i = 0; i < crosswalks.length(); i++){
                JSONObject slightObject = crosswalks.getJSONObject(i);
                Crosswalk cross = new Crosswalk();
                cross.setX(slightObject.getDouble("traffic_light_x"));
                cross.setY(slightObject.getDouble("traffic_light_y"));
                crosslist.add(cross);

            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void dangerZoneNot(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        Toast.makeText(getApplicationContext(), "In Danger Zone!", Toast.LENGTH_SHORT).show();

        // 1초 진동
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            ringtone.play();
        } else {
            vibrator.vibrate(1000);
        }

    }

    public void polygonParsing(String json){ // parse the string to data objects
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray coordinateArray = jsonObject.getJSONArray("coordinates");
            JSONArray coordinates = coordinateArray.getJSONArray(0);
            int length = coordinates.length();

            for(int i = 0; i < length; i++){
                vertices.add(new LatLng(coordinates.getJSONArray(i).getDouble(1), coordinates.getJSONArray(i).getDouble(0)));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        makeRequestSunset();
        sensorManager.registerListener(mGyroLis,mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(mGyroLis);
    }

    //gyro
    @Override
    public void onPause(){
        super.onPause();
//        Log.e("LOG", "onPause()");
        sensorManager.unregisterListener(mGyroLis);
        lightSensorManager.unregisterListener(listener, light);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        Log.e("LOG", "onDestroy()");
        sensorManager.unregisterListener(mGyroLis);

    }
    //gyro
    private class GyroListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {

            //gyro
            /* 각 축의 각속도 성분을 받는다. */
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

            /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
             * dt : 센서가 현재 상태를 감지하는 시간 간격
             * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            /* 맨 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
            if (dt - timestamp * NS2S != 0) {

                /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
                 * 여기까지의 pitch, roll의 단위는 '라디안'이다.
                 * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */
                pitch = pitch + gyroY * dt;
                roll = roll + gyroX * dt;
                yaw = yaw + gyroZ * dt;

                // 콘솔 로그 띄우기
//                Log.e("LOG", "GYROSCOPE           [P]:" + String.format("%.4f", gyroX)
//                        + "           [Q]:" + String.format("%.4f", gyroY)
//                        + "           [R]:" + String.format("%.4f", gyroZ)
//                        + "           [Pitch]: " + String.format("%.1f", pitch * RAD2DGR)
//                        + "           [Roll]: " + String.format("%.1f", roll * RAD2DGR)
//                        + "           [Yaw]: " + String.format("%.1f", yaw * RAD2DGR)
//                        + "           [dt]: " + String.format("%.4f", dt));

                // 디바이스가 Roll(x방향/뒤방향) 방향으로 움직이고, Roll(적분값)이 -50도 보다 작을 때 전화제스쳐로 함.
                if (pitch * RAD2DGR < -50) {
                    Intent intent = new Intent(MapsActivity.this, Call.class);
                    startActivity(intent);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

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
            Toast.makeText(MapsActivity.this, "Flashlight is turned ON", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MapsActivity.this, "Flashlight is turned OFF", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace();
        }
    }

    private void setAlarm() {
        //AlarmReceiver에 값 전달
        Intent receiverIntent = new Intent(MapsActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, receiverIntent, 0);

        String from = alarmDay + " " + alarmTime+":00";    // 오늘 hh시 mm분 ss초 알람 보내기
        Log.e("time", "알람 보낼 날짜와 시간 : " + from);

        alarm_HHMM = Integer.parseInt(alarmHour+alarmMinute);
        Log.e("time", "알람 보낼 날짜와 시간 alarm_HHMM : " + alarm_HHMM);

        //날짜 포맷을 바꿔주는 소스코드
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datetime = null;
        try {
            datetime = dateFormat.parse(from);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (datetime!=null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(datetime);

            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(),pendingIntent);
        }
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {
        Toast.makeText(this, "Jaywalking Frequent Zone: "+polygon.getTag(), Toast.LENGTH_LONG).show();
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
    //TODO: Called when the user clicks a marker
    @Override
    public boolean onMarkerClick(final Marker marker){
        // Retrieve the data from the marker.
        Toast.makeText(this, "This marker is:\n" + marker.getTitle(), Toast.LENGTH_LONG).show();
        marker.setSnippet("clicked: \n"+ marker.getTitle());

        //:- 마커 누르면, 그 좌표 intent로 넘겨주기.
        // 카메라 찍을때까지 그 값 넘겨받고 report 할 때 "좋음" or "나쁨"이랑 같이 보내주기.
        Double lat = marker.getPosition().latitude;
        Double lng = marker.getPosition().longitude;
        Intent intent = new Intent(MapsActivity.this, RestaurantReview.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        startActivity(intent);


        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
    //TODO: 서버 통신
    public void GetStreetLightAPI(String url){ //TODO: get json string from the server
        String json = "";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ServiceExample","response -> " + response);
                        StreetLightjsonParsing(response);
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
    public void GetRestaurantAPI(String url){ //TODO: get json string from the server
        String json = "";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ServiceExample","response -> " + response);
                        RestaurantjsonParsing(response);

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
    public void GetJaywalkingAPI(String url){ //TODO: get json string from the server
        String json = "";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ServiceExample","response -> " + response);
                        polygonParsing(response);

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
    public void GetCrosswWalkAPI(String url){ //TODO: get json string from the server
        String json = "";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ServiceExample","response -> " + response);
                        CrossWalkjsonParsing(response);

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
    public void StreetLightjsonParsing(String json){ //: parse the string to data objects
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray streetlights = jsonObject.getJSONArray("street_light");
            for(int i = 0; i < streetlights.length(); i++){
                JSONObject slightObject = streetlights.getJSONObject(i);
                StreetLight light = new StreetLight();
                light.setId(slightObject.getString("street_light_id"));
                light.setX(slightObject.getDouble("street_light_x"));
                light.setY(slightObject.getDouble("street_light_y"));
                lightlist.add(light);

            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
    public void RestaurantjsonParsing(String json){ //: parse the string to data objects
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray restaurants = jsonObject.getJSONArray("restaurant");
            for(int i = 0; i < restaurants.length(); i++){
                JSONObject restaurantsJSONObject = restaurants.getJSONObject(i);
                Restaurant restaurant = new Restaurant();
                restaurant.setId(restaurantsJSONObject.getString("restaurant_id"));
                restaurant.setX(restaurantsJSONObject.getDouble("restaurant_x"));
                restaurant.setY(restaurantsJSONObject.getDouble("restaurant_y"));
                restaurant.setLevel(restaurantsJSONObject.getString("level"));
                restaurant.setName(restaurantsJSONObject.getString("restaurant_name"));
                restaurantlist.add(restaurant);

            }
        } catch (JSONException e){
            e.printStackTrace();
        }
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

}