package com.americanminion;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import me.itangqi.waveloadingview.WaveLoadingView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Home extends Fragment implements Constants {

    Activity activity;
    private Handler mHandler;
    SharedPreferences sharedPreferences;
    public String API_LINK;

    public Home() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_home, container, false);

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        API_LINK = sharedPreferences.getString(API_LINK_TEXT, "http://192.168.1.6:5000/");

        WaveLoadingView waveLoadingView = (WaveLoadingView) v.findViewById(R.id.sulphur);
        waveLoadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(activity)
                        .title("Enter IP")
                        .content("for eg http://192.168.1.120:5000/")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("for eg http://192.168.1.120:5000/", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                // Do something
                                sharedPreferences.
                                        edit().
                                        putString(API_LINK_TEXT, input.toString()).apply();
                            }
                        }).show();
            }
        });

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
        String uri = API_LINK + "current-index/";
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

                        try {
                            JSONArray array = new JSONArray(res);

                            WaveLoadingView mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.sulphur);
                            setWave(mWaveLoadingView,
                                    decideColor(array.getJSONObject(0).getString("level")),
                                    "Sulphur Dioxide",
                                    decideProgress(array.getJSONObject(0).getString("level")));
                            mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.nitrogen);
                            setWave(mWaveLoadingView,
                                    decideColor(array.getJSONObject(1).getString("level")),
                                    "Oxides of nitrogen",
                                    decideProgress(array.getJSONObject(1).getString("level")));
                            mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.carbon);
                            setWave(mWaveLoadingView,
                                    decideColor(array.getJSONObject(2).getString("level")),
                                    "Carbon monoxide",
                                    decideProgress(array.getJSONObject(2).getString("level")));
                            mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.ozone);
                            setWave(mWaveLoadingView,
                                    decideColor(array.getJSONObject(3).getString("level")),
                                    "Ozone",
                                    decideProgress(array.getJSONObject(3).getString("level")));
                            mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.benzene);
                            setWave(mWaveLoadingView,
                                    decideColor(array.getJSONObject(4).getString("level")),
                                    "Benzene",
                                    decideProgress(array.getJSONObject(4).getString("level")));
                            mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.ammonia);
                            setWave(mWaveLoadingView,
                                    decideColor(array.getJSONObject(5).getString("level")),
                                    "Ammonia",
                                    decideProgress(array.getJSONObject(5).getString("level")));

                        } catch (JSONException e) {
                            Log.e("ERROR", e.getMessage());
                        }

                    }
                });
            }
        });
    }


    int decideColor(String level) {

        if (level.equals("1"))
            return R.color.level1;
        if (level.equals("2"))
            return R.color.level2;
        if (level.equals("3"))
            return R.color.level3;

        return R.color.level1;
    }


    int decideProgress(String level) {

        Log.e(":gee",level);


        if (level.equals("1"))
            return 30;
        if (level.equals("2"))
            return 60;
        if (level.equals("3"))
            return 90;

        return 30;
    }


}
