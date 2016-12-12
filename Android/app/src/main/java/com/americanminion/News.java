package com.americanminion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class News extends Fragment implements Constants {

    Activity activity;
    private Handler mHandler;
    ListView listView;
    ProgressDialog progress;

    public News() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_news, container, false);
        listView = (ListView) v.findViewById(R.id.listView);
        progress = new ProgressDialog(activity);

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);

        mHandler = new Handler(Looper.getMainLooper());

        // start getting news
        getNewslist();

        return v;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }


    /**
     * Calls API to get bus list
     */
    public void getNewslist() {

        progress.setTitle("Please wait");
        progress.setMessage("Fetching latest news for you...");
        progress.show();

        String uri = NEWS_API;

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
                        Log.e("RESPONSE : ", "Done");
                        try {
                            JSONObject YTFeed = new JSONObject(String.valueOf(res));


                            JSONArray YTFeedItems = YTFeed.getJSONArray("articles");
                            Log.e("response", YTFeedItems + " ");

                            if (YTFeedItems.length() == 0) {
                                Utils.hideKeyboard(activity);
                                Snackbar.make(listView, "No results found", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                            }
                            listView.setAdapter(new NewsAdapter(activity, YTFeedItems));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        progress.dismiss();

                    }
                });
            }
        });
    }


    public class NewsAdapter extends BaseAdapter {

        Context context;
        JSONArray FeedItems;
        private LayoutInflater inflater = null;

        public NewsAdapter(Context context, JSONArray FeedItems) {
            this.context = context;
            this.FeedItems = FeedItems;

            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return FeedItems.length();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            try {
                return FeedItems.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.news_item, null);

            TextView Title = (TextView) vi.findViewById(R.id.VideoTitle);
            TextView Description = (TextView) vi.findViewById(R.id.VideoDescription);
            ImageView iv = (ImageView) vi.findViewById(R.id.VideoThumbnail);


            try {
                String x = FeedItems.getJSONObject(position).getString("title");
                x = Html.fromHtml(x).toString();
                Title.setText(x);


                String DescriptionText = FeedItems.getJSONObject(position).getString("description");

                DescriptionText = Html.fromHtml(DescriptionText).toString();
                Description.setText(DescriptionText);

                Picasso.with(context).load(FeedItems.getJSONObject(position).getString("urlToImage")).into(iv);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent browserIntent = null;
                    try {
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FeedItems.getJSONObject(position).getString("url")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    context.startActivity(browserIntent);
                }
            });


            return vi;
        }

    }
}
