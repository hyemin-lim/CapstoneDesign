package com.example.jaywalking;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.example.jaywalking.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnPolygonClickListener {

    private static final int MULTIPLE_PERMISSION = 10235;
    private static final String TAG = "ServiceExample";
    private String[] PERMISSIONS;
    {
        PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }
    //TODO: 폴리곤의 꼭짓점들
    private List<LatLng> vertices = new ArrayList<>();

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private AlarmManager alarmManager;
    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!hasPermissions(this, PERMISSIONS)){ //TODO:권한요청이 미리 안 되어있을 경우 -> hasPermissions에서 판단.
            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSION); //TODO: 권한 요청을 함 -> onrequestPermissionResult로 연결.
        }else{ //TODO: 권한 요청이 미리 되어있는 경우 지도 실행
            binding = ActivityMapsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);


            mapFragment.getMapAsync(this);
        }



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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.50864875768504, 126.93194528072515))); //move the camera to the first point
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        //TODO: json 파일 파싱
        polygonParsing(GetJsonString("polygon.json"));
        PolygonOptions testPolygonOptions = new PolygonOptions();
        //TODO: 폴리곤 꼭짓점 리스트에 있던 점들을 폴리곤 객체에 넣기.
        for(int i = 0; i < vertices.size(); i++){
            testPolygonOptions.clickable(true).add(vertices.get(i));
        }
        Polygon testPolygon = mMap.addPolygon(testPolygonOptions);
        //TODO: 태그 달기
        testPolygon.setTag("test");

        //TODO: 여기서부터는 GPS 위치 받아오는 코드
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled="+ isGPSEnabled);
        Log.d("Main", "isNetworkEnabled="+ isNetworkEnabled);


        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //TODO: 폴리곤 안에 있는지~
                if(PolyUtil.containsLocation(location.getLatitude(), location.getLongitude(), vertices, true)){
                    Log.i(TAG, ""+location.getLongitude()+"/" + location.getLatitude());
                    Toast.makeText(getApplicationContext(), "In Danger Zone!", Toast.LENGTH_SHORT).show();
                }

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






        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnPolygonClickListener(this);
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
        return json;
    }

    public void polygonParsing(String json){ //TODO: parse the string to data objects
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
    public void onPolygonClick(@NonNull Polygon polygon) {
        Toast.makeText(this, "Jaywalking Frequent Zone: "+polygon.getTag(), Toast.LENGTH_LONG).show();
    }





}