package com.example.maptest;

import android.app.Activity;
import android.app.BackgroundServiceStartNotAllowedException;
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
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

public class MainActivity extends Activity {

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 2000;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;
    private Intent mBackgroundServiceIntent;

    private SensorManager sensorManager = null;
    private Sensor accelerormeterSensor = null;
    private Sensor mGyroSensor = null;
    private SensorEventListener mGyroLis;
    private SensorEventListener accelLis;

    MediaPlayer mAudio = null;


    //gyro
    // xml 연결
    TextView p;
    TextView q;
    TextView r;
    TextView textView;

    // Using the Accelometer & Gyroscoper


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
    //gyro

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        accelLis = new AccelListener();
        mGyroLis = new GyroListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //gyro
        p = (TextView)findViewById(R.id.p);
        q = (TextView)findViewById(R.id.q);
        r = (TextView)findViewById(R.id.r);
        textView = (TextView)findViewById(R.id.textView);


    }

    @Override
    public void onStart() {
        super.onStart();
        sensorManager.registerListener(mGyroLis,mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(accelLis, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);


        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelLis);
        sensorManager.unregisterListener(mGyroLis);
    }

    //gyro
    @Override
    public void onPause(){
        super.onPause();
        Log.e("LOG", "onPause()");
        sensorManager.unregisterListener(accelLis);
        sensorManager.unregisterListener(mGyroLis);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        sensorManager.unregisterListener(accelLis);
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
                Log.e("LOG", "GYROSCOPE           [P]:" + String.format("%.4f", gyroX)
                        + "           [Q]:" + String.format("%.4f", gyroY)
                        + "           [R]:" + String.format("%.4f", gyroZ)
                        + "           [Pitch]: " + String.format("%.1f", pitch * RAD2DGR)
                        + "           [Roll]: " + String.format("%.1f", roll * RAD2DGR)
                        + "           [Yaw]: " + String.format("%.1f", yaw * RAD2DGR)
                        + "           [dt]: " + String.format("%.4f", dt));

                // 디바이스가 Roll(x방향/뒤방향) 방향으로 움직이고, Roll(적분값)이 -50도 보다 작을 때 전화제스쳐로 함.
                if (pitch * RAD2DGR < -50) {
                    Intent intent = new Intent(getApplicationContext(), ThirdActivity.class);
                    startActivity(intent);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }
    private class AccelListener implements SensorEventListener{
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
                        Intent intent = new Intent(getApplicationContext(),SubActivity.class);
                        startActivity(intent);
                    }

                    lastX = event.values[DATA_X];
                    lastY = event.values[DATA_Y];
                    lastZ = event.values[DATA_Z];
                }

            }

        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public void StopTest(){
        if(mAudio.isPlaying())
        {
            mAudio.stop();
        }
        else
        {

        }
    }
}

