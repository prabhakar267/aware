package com.americanminion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A login screen that offers login via phone/password.
 */
public class LoginActivity extends AppCompatActivity implements Constants{


    // UI references.
    private Handler mHandler;
    private EditText mPhoneView;
    private EditText mPasswordView;
    private SharedPreferences mSharedPreferences;
    ProgressDialog progress;
    SharedPreferences sharedPreferences;
    public String API_LINK;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mPhoneView = (EditText) findViewById(R.id.phone);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPasswordView = (EditText) findViewById(R.id.password);
        imageView = (ImageView) findViewById(R.id.imageView);

        progress = new ProgressDialog(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        API_LINK = sharedPreferences.getString(API_LINK_TEXT, "http://192.168.1.6:5000/");


        if(mSharedPreferences.getString(USER_ID, null) != null){

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(LoginActivity.this)
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

        Button mphoneSignInButton = (Button) findViewById(R.id.phone_sign_in_button);
        mphoneSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mHandler = new Handler(Looper.getMainLooper());

        getSupportActionBar().hide();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid phone, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid phone address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Login(phone, password);
        }
    }


    /**
     * Upload data to server
     */
    public void Login(String mphone, String mPassword) {

        progress.setTitle("Please wait");
        progress.setMessage("Logging In...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        // to fetch city names
        String uri = API_LINK +"add-user?"+ "name="+ mphone + "&password=" + mPassword;
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
                            JSONObject object = new JSONObject(res);
                            String status = object.getString("success");

                            if (status.equals("true")) {
                                String access_token = object.getString("user_id");
                                mSharedPreferences
                                        .edit()
                                        .putString(USER_ID, access_token)
                                        .apply();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                mPasswordView.setError(status);
                                mPasswordView.requestFocus();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();

                        }
                        progress.dismiss();

                    }
                });
            }
        });
    }
}
