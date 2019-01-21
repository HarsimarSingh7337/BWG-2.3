package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class Login_Screen extends AppCompatActivity {

    private Button loginBtn;
    private TextInputLayout wrapperEmpId, wrapperPassword;
    private TextInputEditText empId, password;
    public static final String MY_SHARED_PREFERENCES = "MyPrefs";
    private AlertDialog alertDialog;
    private int code;
    private ProgressBar progressBar;
    private ImageView checkscimage;
    private SharedPreferences sharedPreferences;
    private boolean isServiceConnected = false;
    private Thread thread = null;
    private Animation shakeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
        View view = getSupportActionBar().getCustomView();

        ImageView drop_down = view.findViewById(R.id.actionbar_dropdown);
        ImageView option = view.findViewById(R.id.actionbar_option);
        drop_down.setVisibility(View.INVISIBLE);

        option.setOnClickListener((View view1) -> {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view1);
            popupMenu.getMenuInflater().inflate(R.menu.menu_ip, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.setip:
                        android.support.v4.app.FragmentManager fragmentManager =getSupportFragmentManager();
                        IP_Fragment ip_fragment = new IP_Fragment();
                        ip_fragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                        ip_fragment.show(fragmentManager, "");
                        break;
                }
                return true;
            });
            popupMenu.show();
        });
        TextView appname = view.findViewById(R.id.custom_actionbar1_appname);
        TextView barcodescanner = findViewById(R.id.barcodescanner);
        TextView copyrightText = findViewById(R.id.copyright_text);
        TextView allRightReservedText = findViewById(R.id.allrightsreserved_text);
        TextView version = findViewById(R.id.version_text);
        loginBtn=findViewById(R.id.loginBtn);

        checkscimage = findViewById(R.id.checkscimage);
        progressBar = findViewById(R.id.pbar);

        copyrightText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        allRightReservedText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        appname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        version.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        barcodescanner.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/aclonica.ttf"));

        wrapperEmpId = findViewById(R.id.wrapperloginempid);
        wrapperPassword = findViewById(R.id.wrapperloginpassword);
        empId = findViewById(R.id.loginempid);
        password = findViewById(R.id.loginpassword);
        shakeAnimation = AnimationUtils.loadAnimation(Login_Screen.this,R.anim.shake);
        shakeAnimation.setDuration(50);

        try {
            sharedPreferences = getSharedPreferences(MY_SHARED_PREFERENCES, 0);
            empId.setText(sharedPreferences.getString("empId", null));
        } catch (Exception ignored) {
        }

        loginBtn.setOnClickListener(v -> {

            if (empId.getText().toString().trim().length() == 0) {
                wrapperEmpId.setError("Invalid Field");
            } else if (password.getText().toString().trim().length() == 0) {
                wrapperPassword.setError("Invalid Field");
            } else {
                sharedPreferences = getSharedPreferences(MY_SHARED_PREFERENCES, 0);
                try {
                    if (!isServiceConnected) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(Login_Screen.this);
                        ab.setTitle("Alert!!!");
                        ab.setMessage("Please check IP Address or Service Connection...");
                        ab.setCancelable(false);
                        ab.setNeutralButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
                        ab.create().show();
                    } else {
                        if (Objects.requireNonNull(sharedPreferences.getString("ip", null)).equals("")) {
                        } else {
                            new CheckLoginCredentials().execute();
                            // end of else
                        }
                    }
                }catch(NullPointerException npe) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(Login_Screen.this);
                    ab.setMessage("Please Provide IP Address first");
                    ab.setNegativeButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
                    AlertDialog dialog = ab.create();
                    Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.CENTER);
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
            finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (Objects.requireNonNull(sharedPreferences.getString("ip", null)).equals("")) {
            } else {
                View v = LayoutInflater.from(Login_Screen.this).inflate(R.layout.progress_bar_plain,null);
                TextView tv = v.findViewById(R.id.progressbar_message);
                tv.setText("Connecting to Server...");

                AlertDialog.Builder ab=new AlertDialog.Builder(Login_Screen.this);
                ab.setCancelable(false);
                ab.setView(v);
                alertDialog = ab.create();
                alertDialog.show();
            }
        } catch (Exception e) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            IP_Fragment ip_fragment = new IP_Fragment();
            ip_fragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            ip_fragment.show(fragmentManager, "");
        }
        thread = new Thread(() -> {
            while (thread != null) {
                try {
                    new MAsyncTask().execute();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onPause() {
        thread = null;
        super.onPause();
    }

    // Asynchronous task that checks whether app is conneccted
    // to web service and displays "green tick" or "red sign" in UI according to response.
    @SuppressLint("StaticFieldLeak")
    class MAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SyncHttpClient httpClient = new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(1,2000);
            httpClient.addHeader("temp", "connectMe");
            httpClient.head("http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/isAppConnected/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    code = statusCode;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    code = statusCode;
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (code == 200) {
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    checkscimage.setImageResource(R.drawable.sc);
                    isServiceConnected = true;
                } else {
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    checkscimage.setImageResource(R.drawable.snc);
                    isServiceConnected = false;
                }
            } catch (Exception ignored) {
            }
        }
    }

    // Asynctask class to check login credentials on button click
    @SuppressLint("StaticFieldLeak")
    class CheckLoginCredentials extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = LayoutInflater.from(Login_Screen.this).inflate(R.layout.progress_bar_plain,null);
            TextView tv=v.findViewById(R.id.progressbar_message);
            tv.setText("Checking credentials...");
            AlertDialog.Builder ab=new AlertDialog.Builder(Login_Screen.this);
            ab.setCancelable(false);
            ab.setView(v);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            SyncHttpClient httpClient = new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(2,2000);
            httpClient.addHeader("empId", Objects.requireNonNull(empId.getText()).toString());
            httpClient.addHeader("password", Objects.requireNonNull(password.getText()).toString());
            httpClient.head("http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/authentication/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    code = statusCode;
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    code = statusCode;
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (code == 200) {
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("empId", Objects.requireNonNull(empId.getText()).toString());
                    editor.putString("password", Objects.requireNonNull(password.getText()).toString());
                    editor.putBoolean("isLoggedIn",true);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), TabLayoutScreeen.class));
                } else {
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    loginBtn.startAnimation(shakeAnimation);
                    Toast.makeText(getApplicationContext(), "Invalid ID or Password", Toast.LENGTH_LONG).show();
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        super.onDestroy();
    }
    // end of class
}