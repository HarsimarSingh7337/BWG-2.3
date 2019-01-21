package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class BWG_Sieve_Cancelled_ID extends android.support.v4.app.DialogFragment {

    private ImageView alertImage;
    private TextView alerttext;
    private String val;
    private int code;
    private AlertDialog alertDialog;
    private Dialog dialog;
    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);
        View rootView = inflater.inflate(R.layout.activity_bwg__sieve__cancelled__id,container,false);

        alertImage=rootView.findViewById(R.id.errorimagetrackingid);
        alerttext=rootView.findViewById(R.id.errormessagetrackingid);
        TextView isbn = rootView.findViewById(R.id.isbntextview);
        progressBar = rootView.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        isbn.setText(Objects.requireNonNull(getArguments()).getString("val"));
        val=getArguments().getString("val");
        try
        {
            new MyAsyncTasks().execute();
        }
        catch(Exception ignored) { }
        return rootView;
    }

  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwg__sieve__cancelled__id);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar1);

        View view = getSupportActionBar().getCustomView();
        ImageView sout= view.findViewById(R.id.actionbar_dropdown);
        sout.setVisibility(View.INVISIBLE);
        ImageView option = view.findViewById(R.id.actionbar_option);
        option.setVisibility(View.INVISIBLE);

        Bundle bundle=getIntent().getExtras();

        alertImage=findViewById(R.id.errorimagetrackingid);
        alerttext=findViewById(R.id.errormessagetrackingid);
        TextView isbn = findViewById(R.id.isbntextview);
        isbn.setText(Objects.requireNonNull(bundle).getString("val"));
        val=bundle.getString("val");
        pd=new ProgressDialog(BWG_Sieve_Cancelled_ID.this);

        try
        {
            new MyAsyncTasks().execute();
        }
        catch(Exception ignored) { }

    }*/

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTasks extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void...v) {
            SharedPreferences sharedPreferences=Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
            SyncHttpClient httpClient=new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(2,1000);
            httpClient.addHeader("trackingId",val);
            httpClient.head("http://"+sharedPreferences.getString("ip",null)+":8080/com.bookswagon/service/bookswagon/cancel/", new AsyncHttpResponseHandler() {

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

           View v = LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar_plain,null);
           AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
           ab.setCancelable(false);
           ab.setView(v);
           alertDialog = ab.create();
           alertDialog.show();
        }

        @Override
        protected void onPostExecute(Void v){
            super.onPostExecute(v);

            progressBar.setVisibility(View.INVISIBLE);

            if(code==200){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                alertImage.setImageResource(R.drawable.sucess);
                alerttext.setText("Tracking ID saved successfully");
                alerttext.setTextColor(getResources().getColor(R.color.lightGreen));

                Handler handler=new Handler();
                handler.postDelayed(() ->{
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                },700);
            }
            else if(code==300){

                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                alertImage.setImageResource(R.drawable.error);
                alerttext.setText("Tracking ID found in Scanned List");
                alerttext.setTextColor(getResources().getColor(R.color.red));

                MediaPlayer player=MediaPlayer.create(getActivity(),R.raw.beep);
                player.start();

                Handler handler=new Handler();
                handler.postDelayed(() ->{
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                } ,1000);
            }

            else if(code==500){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                alertImage.setImageResource(R.drawable.error);
                alerttext.setText("Tracking ID already in Cancelled List");
                alerttext.setTextColor(getResources().getColor(R.color.red));

                MediaPlayer player=MediaPlayer.create(getActivity(),R.raw.beep);
                player.start();

                Handler handler=new Handler();
                handler.postDelayed(() -> {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                },1000);
            }
            else{
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                    timeOutExceptionMessage();
            }
        }
    }
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        if(dialog!=null){
            dialog.dismiss();
        }
        super.onDestroy();
    }


    public void timeOutExceptionMessage(){
        try{
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to Connect to Server...");
            sb.append("\n");
            sb.append("\n");
            sb.append("Possible Causes:");
            sb.append("\n");
            sb.append("-> IP Address is Invalid.");
            sb.append("\n");
            sb.append("-> Device has WI-FI off.");
            sb.append("\n");
            sb.append("-> Application is not connected with service.");
            sb.append("\n");
            sb.append("-> Exception from Service");

            android.app.AlertDialog.Builder abb = new android.app.AlertDialog.Builder(getActivity());
            abb.setTitle("Error");
            abb.setMessage(sb.toString());
            abb.setCancelable(false);
            abb.setPositiveButton("OK", (dialogg, which) -> {
                dialogg.dismiss();
                if(dialog!=null){
                    dialog.dismiss();
                }
            });
            abb.create().show();
        }
        catch(Exception ignored){}
    }
}