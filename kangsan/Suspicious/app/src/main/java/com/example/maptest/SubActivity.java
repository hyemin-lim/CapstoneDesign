package com.example.maptest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;

public class SubActivity extends Activity implements SensorEventListener{


    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float x,y,z;
    private static final int SHAKE_THRESHOLD = 1000; // 작을수록 느린 스피드에서도 감지를 한다.

    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    private TextView textview=null;
    private int count;

    @Override
    public void onCreate(Bundle savedInstanceStae) {
        super.onCreate(savedInstanceStae);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_sub);

    }

    @Override
    public void onStart(){
        super.onStart();

        if(accelerormeterSensor!=null){
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(sensorManager!=null){
            sensorManager.unregisterListener(this);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //정확도 설정
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        //Sensor 정보가 변하면 실행됨.
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            //최근 측정한 시간과 현재 시간을 비교하여 0.1초 이상되었을 때 흔듬을 감지한다.
            if(gabOfTime > 100){
                lastTime =currentTime;
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];

                speed = Math.abs(x+y+z - lastX - lastY - lastZ)/gabOfTime * 10000;

                if(speed>SHAKE_THRESHOLD) {
                    //이벤트 발생!!
                    finish();
                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
        }
    }

}