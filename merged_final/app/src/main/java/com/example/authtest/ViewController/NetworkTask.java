package com.example.authtest.ViewController;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkTask extends AsyncTask<Void, Void, String> {
    private String url;
    private ContentValues values;
    private String report;
    private Double lat;
    private Double lng;

    public NetworkTask(String url, ContentValues values, String report, Double lat, Double lng) {

        this.url = url;
        this.values = values;
        this.report = report;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected String doInBackground(Void... params) {

        String result; // 요청 결과를 저장할 변수.
        RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
        result = requestHttpURLConnection.request(url, values, report, lat, lng); // 해당 URL로 부터 결과물을 얻어온다.

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        Log.e("response", "->onPostExecute>>"+s);
    }
}
