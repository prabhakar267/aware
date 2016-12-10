package com.americanminion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Bulletin extends Fragment implements Constants{

    Activity activity;
    private Handler mHandler;

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    SharedPreferences s;
    long  time;
    SharedPreferences.Editor e;
    String uid;
    ProgressDialog progress;

    GPSTracker gpsTracker;

    public Bulletin() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_chat, container, false);
        progress = new ProgressDialog(activity);

        messagesContainer = (ListView) v.findViewById(R.id.messagesContainer);
        messageET = (EditText) v.findViewById(R.id.messageEdit);
        sendBtn = (Button) v.findViewById(R.id.chatSendButton);


        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);

        mHandler = new Handler(Looper.getMainLooper());

        initControls();

        return v;
    }


    private void initControls() {

        s = PreferenceManager.getDefaultSharedPreferences(activity);
        e = s.edit();
        uid = s.getString(USER_ID, null);

        adapter = new ChatAdapter(activity, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);


        getChat();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                SendMessage(messageText);
                messageET.setText("");

                // displayMessage(chatMessage);
            }
        });


        /*while(true) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 10000);
        }*/




        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    //do your code here
                   // getChat();
                }
                catch (Exception e) {
                }
                finally{
                    //also call the same runnable
                    handler.postDelayed(this, 10000);
                }
            }
        };
        handler.postDelayed(runnable, 10000);
    }



    /**
     * Get chat
     */
    public void getChat() {

        String lat,lon;
        gpsTracker = new GPSTracker(activity);
        if(gpsTracker.canGetLocation()){
            lat = Double.toString(gpsTracker.getLatitude());
            lon = Double.toString(gpsTracker.getLongitude());

        } else {
            Toast.makeText(activity, "Turn on location services", Toast.LENGTH_LONG).show();
            return;
        }


        progress.setTitle("Please wait");
        progress.setMessage("Logging In...");
        progress.show();


        // to fetch city names
        String uri = API_LINK +"get-message?"+ "user_id="+ uid + "&lat=" + lat + "&lon=" + lon + "&channel=random";
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

                        Log.e("result", res);
                        try {

                            JSONArray arr = new JSONArray(res);
                            JSONObject ob;
                            for(int i=0;i<arr.length();i++){

                                ob = arr.getJSONObject(i);

                                ChatMessage chatMessage = new ChatMessage();
                                chatMessage.setMessage(ob.getString("message"));
                                chatMessage.setDate(ob.getString("timestamp"));
                                chatMessage.setMe(false);
                                chatMessage.setId(ob.getInt("id"));
                                chatMessage.setAuthor(ob.getString("author"));

                                Log.e("dispalyin",ob.getString("message"));


                                displayMessage(chatMessage);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(activity, "Some error occurred", Toast.LENGTH_LONG).show();

                        }
                        progress.dismiss();

                    }
                });
            }
        });
    }



    /**
     * Upload data to server
     */
    public void SendMessage(final String msg) {

        String lat,lon;
        gpsTracker = new GPSTracker(activity);
        if(gpsTracker.canGetLocation()){
            lat = Double.toString(gpsTracker.getLatitude());
            lon = Double.toString(gpsTracker.getLongitude());

        } else {
            Toast.makeText(activity, "Turn on location services", Toast.LENGTH_LONG).show();
            return;
        }



        progress.setTitle("Please wait");
        progress.setMessage("Sending message...");
        progress.show();

        // to fetch city names
        String uri = API_LINK + "add-message";
        Log.e("executing", uri + " " + uid);

        //Set up client
        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", msg)
                .addFormDataPart("lat", lat)
                .addFormDataPart("lon", lon)
                .addFormDataPart("user_id", uid)
                .addFormDataPart("channel", "random")
                .build();

        //Execute request
        Request request = new Request.Builder()
                .url(uri)
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(requestBody)
                .build();

        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                progress.dismiss();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String res = response.body().string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Log.e("here",res +" ");


                        try {
                            JSONObject object = new JSONObject(res);
                            String status = object.getString("success");
                            Toast.makeText(activity, status, Toast.LENGTH_LONG).show();
                            if (status.equals("true")) {
                                ChatMessage chatMessage = new ChatMessage();
                                chatMessage.setId(122);//dummy
                                chatMessage.setMessage(msg);
                                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                                chatMessage.setMe(true);
                                displayMessage(chatMessage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progress.dismiss();

                    }
                });

            }
        });
    }


    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }


}
