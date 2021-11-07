package com.example.maptest;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maptest.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener{

    private static final int MULTIPLE_PERMISSION = 10235;
    private String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<StreetLight> lightlist = new ArrayList<StreetLight>(); //TODO: STREET LIGHT DATA LIST
    private ArrayList<Restaurant> restaurantlist = new ArrayList<Restaurant>(); //TODO: RESTAURANT DATA LIST

    @Override
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

        //TODO: Add a marker from the list
        StreetLightjsonParsing(GetJsonString("test/test.json")); //get json from the file & parse the data
        RestaurantjsonParsing(GetJsonString("test/restaurant_test.json"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.50864875768504, 126.93194528072515))); //move the camera to the first point
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12)); //zoom in
        for(int idx = 0; idx < restaurantlist.size(); idx++){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(restaurantlist.get(idx).getY(), restaurantlist.get(idx).getX()))
                    .title(restaurantlist.get(idx).getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            mMap.addMarker(markerOptions);
        }
        for (int idx = 0; idx < lightlist.size(); idx++){ //create markers for every data
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(lightlist.get(idx).getY(), lightlist.get(idx).getX())) //set position !!Y is the latitude, X is the longitude!!
                    .title(lightlist.get(idx).getId());
            mMap.addMarker(markerOptions);
        }

        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

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
        Intent intent = new Intent(MapsActivity.this, ListActivity.class);
        startActivity(intent);


        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }


    public class StreetLight{ //TODO: street light object class
        private String id;
        private double x;
        private double y;

        public String getId(){
            return id;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public class Restaurant{//TODO: restaurant object class
        private String id;
        private double x;
        private double y;
        private String level;
        private String name;

        public double getY() { return y; }

        public double getX() { return x; }

        public String getId() { return id; }

        public String getLevel() { return level; }

        public String getName() { return name; }

        public void setY(double y) { this.y = y; }

        public void setX(double x) { this.x = x; }

        public void setId(String id) { this.id = id; }

        public void setLevel(String level) { this.level = level; }

        public void setName(String name) { this.name = name; }
    }

    public void StreetLightjsonParsing(String json){ //TODO: parse the string to data objects
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
    public void RestaurantjsonParsing(String json){ //TODO: parse the string to data objects
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
}