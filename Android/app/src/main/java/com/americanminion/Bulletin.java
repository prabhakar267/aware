package com.americanminion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private Button sendBtn, tagBtn;
    private ChatAdapter adapter;
    SharedPreferences s;
    String uid;
    String curTags="";
    ProgressDialog progress;

    SharedPreferences sharedPreferences;
    public String API_LINK;
    GPSTracker gpsTracker;

    String tagsShow = "0";
    public Bulletin() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_chat, container, false);
        progress = new ProgressDialog(activity);

        setHasOptionsMenu(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        API_LINK = sharedPreferences.getString(API_LINK_TEXT, "");

        messagesContainer = (ListView) v.findViewById(R.id.messagesContainer);
        messageET = (EditText) v.findViewById(R.id.messageEdit);
        sendBtn = (Button) v.findViewById(R.id.chatSendButton);
        tagBtn = (Button) v.findViewById(R.id.chatTagsButton);


        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);

        mHandler = new Handler(Looper.getMainLooper());

        initControls();

        return v;
    }


    private void initControls() {

        s = PreferenceManager.getDefaultSharedPreferences(activity);
        uid = s.getString(USER_ID, null);

        adapter = new ChatAdapter(activity, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        getChat();

        tagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(activity)
                        .title(R.string.title)
                        .items(R.array.items)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                curTags = "";

                                for (int i = 0; i < which.length; i++) {
                                    curTags = curTags + (which[i]+1);
                                }
                                return true;
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
            }
        });



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                if(curTags.equals("")) {
                    Toast.makeText(activity, "Select atleast one tag!", Toast.LENGTH_LONG).show();
                    return;
                }

                SendMessage(messageText);
                messageET.setText("");

                // displayMessage(chatMessage);
            }
        });

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


        // to fetch city names
        String uri = API_LINK +"get-message?"+ "user_id="+ uid + "&lat=" + lat + "&lon=" + lon + "&tags=" + tagsShow;
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


                        adapter = new ChatAdapter(activity, new ArrayList<ChatMessage>());
                        messagesContainer.setAdapter(adapter);

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
                                chatMessage.setVotes(ob.getString("score"));
                                chatMessage.setTags(ob.getString("tags"));

                                Log.e("dispalyin",ob.getString("message"));

                                displayMessage(chatMessage);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(activity, "Some error occurred", Toast.LENGTH_LONG).show();

                        }
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
        Log.e("executing", uri + " " + uid + " " + curTags);

        //Set up client
        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", msg)
                .addFormDataPart("lat", lat)
                .addFormDataPart("lon", lon)
                .addFormDataPart("user_id", uid)
                .addFormDataPart("tags", curTags)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {

            new MaterialDialog.Builder(activity)
                    .title(R.string.title)
                    .items(R.array.items)
                    .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                            tagsShow = "";

                            for (int i = 0; i < which.length; i++) {
                                tagsShow = tagsShow + (which[i]+1);

                            }

                            if(tagsShow.equals("")){
                                tagsShow = "0";
                            }

                            getChat();
                            return true;
                        }
                    })
                    .positiveText(R.string.choose)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bulettin_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
