package com.example.authtest.ViewController;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.authtest.R;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraCapture extends AppCompatActivity implements SensorEventListener {
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

    CameraSurfaceView cameraView;
    // TEST 할 때 코멘트 할 부분 ###################################################################################
    public String url = "http://52.42.115.23:3000/post";
    // TEST 할 때 코멘트 할 부분 ###################################################################################


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setContentView(R.layout.activity_camera_capture);

        // 카메라 미리보기 화면
        FrameLayout previewFrame = findViewById(R.id.previewFrame);
        cameraView = new CameraSurfaceView(this);
        previewFrame.addView(cameraView);

        //Todo:-  takePicture로 사진 찍은 후, 제보내용을 post 해주는 함수 넣기
        // post와 관련된 통신 내용은 나중으로 미뤘기 때문에..
        // 버튼에 따른 제보내용을 Log로 찍어서 보여주겠다.
        Button button1 = findViewById(R.id.button1);    // 서비스가 좋지 않음
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();

                //Todo:- post 하기
                Log.e("report", "좋음");
                // TEST 할 때 코멘트 할 부분 ###################################################################################
                clickPost("good");
                // AsyncTask를 통해 HttpURLConnection 수행.
                Intent getIntent = getIntent();
                if (getIntent != null) {
                    //별점과 별점수텍스트 받아오고 뷰에 세팅해줌
                    Double lat = getIntent.getExtras().getDouble("lat");
                    Double lng = getIntent.getExtras().getDouble("lng");
                    NetworkTask networkTask = new NetworkTask(url, null, "좋음", lat, lng);
                    networkTask.execute();
                }
                // TEST 할 때 코멘트 할 부분 ###################################################################################

            }
        });

        Button button2 = findViewById(R.id.button2);    // 위생이 좋지 않음
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();

                //Todo:- post 하기
                Log.e("report", "나쁨");
                // TEST 할 때 코멘트 할 부분 ###################################################################################
                clickPost("bad");
                // AsyncTask를 통해 HttpURLConnection 수행.
                Intent getIntent = getIntent();
                if (getIntent != null) {
                    //별점과 별점수텍스트 받아오고 뷰에 세팅해줌
                    Double lat = getIntent.getExtras().getDouble("lat");
                    Double lng = getIntent.getExtras().getDouble("lng");
                    NetworkTask networkTask = new NetworkTask(url, null, "나쁨", lat, lng);
                    networkTask.execute();
                }
                // TEST 할 때 코멘트 할 부분 ###################################################################################

            }
        });

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.CAMERA,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("허용된 권한 갯수 : " + permissions.size());
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("거부된 권한 갯수 : " + permissions.size());
                    }
                })
                .start();

    }

    public void clickPost(String report) {
        //Post 방식으로 보낼 서버 주소
        String serverUrl= "http://52.42.115.23:3000/post";

        //PostTest.php로 보낼 요청 객체 생성
        //결과를 String으로 받는 객체
        StringRequest stringRequest= new StringRequest(
                Request.Method.POST,                  // Todo:- POST 메서드 사용
                serverUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("response", "->onResponse>>"+response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("response", "->onErrorResponse>>"+error.getMessage());
                    }
                }
        ) {
            //POST 방식으로 보낼 데이터를
            //리턴해주는 콜백 메소드

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                HashMap<String, String> data= new HashMap<String, String>();
                data.put("report", report);
                data.put("report", report);   // 보낸 메시지 식별을 위해 사용자id도 같이 보냄. 지금은 사용자id 코드 내에서 수동으로 작성.
                Log.e("response", "->data>>"+data);
                return data;

            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        Log.e("response", "->stringRequest>>"+stringRequest);
        Log.e("response", "응답 보냄");
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
//                    finish();
                    Intent intent = new Intent(CameraCapture.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
        }
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    String outUriStr = MediaStore.Images.Media.insertImage(
                            getContentResolver(),
                            bitmap,
                            "Captured Image",
                            "Captured Image using Camera.");

                    if (outUriStr == null) {
                        Log.d("SampleCapture", "Image insert failed.");
                        return;
                    } else {
                        Uri outUri = Uri.parse(outUriStr);
                        sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
                    }

                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera camera = null;

        public CameraSurfaceView(Context context) {
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void getCameraInstance(){
            try {
                camera = Camera.open();
            } catch (Exception e){
                showToast("카메라가 다른 앱에서 사용중입니다.");
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            getCameraInstance();
            setCameraOrientation();

            try {
                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean capture(Camera.PictureCallback handler) {
            if (camera != null) {
                camera.takePicture(null, null, handler);
                return true;
            } else {
                return false;
            }
        }

        public void setCameraOrientation() {
            if (camera == null) {
                return;
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);

            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int rotation = manager.getDefaultDisplay().getRotation();

            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }

            camera.setDisplayOrientation(result);
        }

    }

}
