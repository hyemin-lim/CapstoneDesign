package com.example.sms.ViewController;

import android.app.Activity;
import android.content.Context;
//import android.hardware.camera2.*;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.telephony.SmsManager;
import android.Manifest;
import android.content.pm.PackageManager;
import com.example.sms.Model.inform;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.Button;
import android.content.Context;
import android.view.View;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.sms.R;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    LinearLayout background;

    private inform model;

    // 카메라 플래시
    private CameraManager cameraManager;
    private String getCameraID;

    // 조도 센서
    
    SensorManager lightSensorManager;
    SensorEventListener listener;
    Sensor light;
    private static boolean mFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new inform();


        textView = findViewById(R.id.textView);
        background = (LinearLayout) findViewById(R.id.background);

        // 카메라 플래시
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

        // 조도센서
        lightSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        listener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)           // api 버전 체크하는 것
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView.setText(String.valueOf(event.values[0]));

                if (event.values[0] < 100 && !mFlashOn){
                    // 카메라 플래시 켜기
                    flashLightOn();
                    // Inform the user about the flashlight
                    // status using Toast message
                    Toast.makeText(MainActivity.this, "Flashlight is turned ON", Toast.LENGTH_SHORT).show();
                    background.setBackgroundColor(Color.YELLOW);

                    mFlashOn = true;

                }
                else if (event.values[0] >= 100 && mFlashOn ){
                    // 카메라 플래시 끄기
                    flashLightOff();
                    background.setBackgroundColor(Color.RED);

                    mFlashOn = false;
                }
            }
        };

        lightSensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_FASTEST);
    }
    //문자

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
        Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
    }

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
            if (isSMSPermissionAllowed()) {
            sms();

            }

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

}