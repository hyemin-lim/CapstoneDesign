package com.example.httpurlconnection_get_test.ViewControler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.httpurlconnection_get_test.Model.StreetLight;
import com.example.httpurlconnection_get_test.Model.StreetLightList;
import com.example.httpurlconnection_get_test.R;
import com.example.httpurlconnection_get_test.ViewControler.Adapter.Adapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    EditText editText;
    TextView textView;

    static RequestQueue requestQueue;

    RecyclerView recyclerView;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);

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

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

    }

    public void makeRequest() {
        String url = editText.getText().toString();     // Todo:- url이라는 이름의 변수에 텍스트필드에서 받은 주소 가져옴.

        StringRequest request = new StringRequest(
                Request.Method.GET,                    // Todo:- GET 메서드 사용
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        println("응답 -> " + response);

                        processResponse(response);
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
        Gson gson = new Gson();     // Todo:- 가로등 api 불러올 때 gson 이용

        StreetLightList streetLightList = gson.fromJson(response, StreetLightList.class);

        println("가로등 정보의 수 : " + streetLightList.street_light.size());

        for (int i = 0; i < streetLightList.street_light.size(); i++) {
            StreetLight streetLight = streetLightList.street_light.get(i);

            adapter.addItem(streetLight);
        }

        adapter.notifyDataSetChanged();
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
            StreetLightList streetLightList = gson.fromJson(json, StreetLightList.class);

            println("가로등 정보의 수 : " + streetLightList.street_light.size());

            for (int i = 0; i < streetLightList.street_light.size(); i++) {
                StreetLight streetLight = streetLightList.street_light.get(i);

                adapter.addItem(streetLight);
            }

            adapter.notifyDataSetChanged();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        /******* end of test by json file ***************************************************************************************/

    }

}
