package com.americanminion;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.io.IOException;

import me.itangqi.waveloadingview.WaveLoadingView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Home extends Fragment implements Constants{

    Activity activity;
    private Handler mHandler;

    public Home() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_home, container, false);

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);

        mHandler = new Handler(Looper.getMainLooper());

        vote(v);


        return v;
    }


    void setWave(WaveLoadingView mWaveLoadingView, int color, String title, int progress) {

        Log.e("setting", title + " " + progress + color);

        mWaveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        mWaveLoadingView.setCenterTitleColor(color);
        mWaveLoadingView.setBottomTitleSize(18);
        mWaveLoadingView.setProgressValue(progress);
        mWaveLoadingView.setBorderWidth(10);
        mWaveLoadingView.setAmplitudeRatio(60);
        mWaveLoadingView.setWaveColor(ContextCompat.getColor(activity, color));
        mWaveLoadingView.setBorderColor(color);
        mWaveLoadingView.setCenterTitle(title);
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }



    /**
     * Upload data to server
     */
    public void vote(final View v) {

        // to fetch city names
        String uri = API_LINK + "/current-index/";
        Log.e("CALLING : ", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        final Request request = new Request.Builder()
                .url(uri)
                .build();

        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String res = response.body().string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        WaveLoadingView mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.sulphur);
                        setWave(mWaveLoadingView, R.color.level1, "Sulphur Dioxide",10);
                        mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.nitrogen);
                        setWave(mWaveLoadingView, R.color.level2, "Oxides of nitrogen",20);
                        mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.carbon);
                        setWave(mWaveLoadingView, R.color.level3, "Carbon monoxide",30);
                        mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.ozone);
                        setWave(mWaveLoadingView, R.color.level4, "Ozone",40);
                        mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.benzene);
                        setWave(mWaveLoadingView, R.color.level5, "Benzene",50);
                        mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.ammonia);
                        setWave(mWaveLoadingView, R.color.level1, "Ammonia",60);

                    }
                });
            }
        });
    }

}
