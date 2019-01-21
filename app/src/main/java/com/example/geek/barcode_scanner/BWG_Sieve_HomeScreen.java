package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class BWG_Sieve_HomeScreen extends Fragment {

    private ToggleButton toggleButton;
    private ImageView checkscimage;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private int code=500;
    private MyAsyncTasks myAsyncTasks;
    private Handler handler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_bwg__sieve__home_screen, container, false);

        sharedPreferences= Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
        TextView copyrightText = rootView.findViewById(R.id.copyright_text);
        TextView allRightReservedText = rootView.findViewById(R.id.allrightsreserved_text);
        TextView version = rootView.findViewById(R.id.version_text);
        checkscimage=rootView.findViewById(R.id.checkscimage);
        progressBar=rootView.findViewById(R.id.pbar);
        toggleButton=rootView.findViewById(R.id.togglebtn);
        Button newTrackingId = rootView.findViewById(R.id.newTrackingId);
        Button cancelledTrackingId = rootView.findViewById(R.id.cancelledTrackingId);
        Button removeTrackingId = rootView.findViewById(R.id.removeTrackingId);
        Button address = rootView.findViewById(R.id.address);
        TextView loggedEmpId = rootView.findViewById(R.id.logedempid);

        // applied font styles to some text components
        loggedEmpId.setText(sharedPreferences.getString("empId",""));
        loggedEmpId.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Medium.ttf"));
        copyrightText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Quicksand-Bold.ttf"));
        allRightReservedText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Quicksand-Bold.ttf"));
        version.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Quicksand-Bold.ttf"));

        // Button Click events below
        toggleButton.setOnClickListener(v -> {

            if(!toggleButton.isChecked()){
                toggleButton.setSelected(true);
                toggleButton.setBackground(getResources().getDrawable(R.drawable.custom_toggle_off));
                toggleButton.setTextColor(getResources().getColor(R.color.black));
            }
            else{
                toggleButton.setSelected(false);
                toggleButton.setBackground(getResources().getDrawable(R.drawable.custom_toggle_on));
                toggleButton.setTextColor(getResources().getColor(R.color.white));
            }
        });

        newTrackingId.setOnClickListener(v -> {
            if(!toggleButton.isChecked()){
                Toast.makeText(getActivity(),"Enable Counter First", Toast.LENGTH_LONG).show();
            }
            else{
                startActivity(new Intent(getActivity(),Barcode_Scanner_Sieve.class).putExtra("loc","new"));
            }
        });

        cancelledTrackingId.setOnClickListener(v -> {
            FragmentManager fragmentManager=getActivity().getSupportFragmentManager();
            BWG_Sieve_Insert_Tracking_ID bwg_sieve_insert_tracking_id=new BWG_Sieve_Insert_Tracking_ID();
            bwg_sieve_insert_tracking_id.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            bwg_sieve_insert_tracking_id.show(fragmentManager,"");
        });

        removeTrackingId.setOnClickListener(v -> {
            if(!toggleButton.isChecked()){
                Toast.makeText(getActivity(),"Enable Counter First", Toast.LENGTH_LONG).show();
            }
            else{
                startActivity(new Intent(getActivity(),Barcode_Scanner_Sieve.class).putExtra("loc","remove"));
            }
        });

        address.setOnClickListener(v -> {
            FragmentManager fragmentManager=getActivity().getSupportFragmentManager();
            BWG_Sieve_Address bwg_sieve_address = new BWG_Sieve_Address();
            bwg_sieve_address.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            bwg_sieve_address.show(fragmentManager,"");
        });

        return rootView;
    }

    private Runnable runnable=new Runnable() {

        @Override
        public void run(){
            try{
                myAsyncTasks = new MyAsyncTasks();
                myAsyncTasks.execute();
                handler.postDelayed(runnable,4000);
            }
            catch(Exception ignored){}
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler=new Handler();
        handler.postDelayed(runnable,2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(myAsyncTasks!=null){
            myAsyncTasks.cancel(true);
        }
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTasks extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids){
            SyncHttpClient httpClient=new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(2,1000);
            httpClient.addHeader("temp","isConnected");
            httpClient.head("http://"+sharedPreferences.getString("ip",null)+":8080/com.bookswagon/service/bookswagon/check/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    code=statusCode;
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    code=statusCode;
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void v){
            super.onPostExecute(v);
            try{
                if(code==200){
                    progressBar.setVisibility(View.INVISIBLE);
                    checkscimage.setImageResource(R.drawable.sc);
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    checkscimage.setImageResource(R.drawable.snc);
                }
            }
            catch(Exception ignored){}
        }
    }
}