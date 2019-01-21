package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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

public class BWG_Sieve_Removed_ID extends android.support.v4.app.DialogFragment {

    private TextView mesageTrackingID;
    private ImageView imageTrackingID;
    private String val;
    private int code;
    private AlertDialog alertDialog;
    private Dialog dialog;
    CommunicationWithParentt communicationWithParentt;
    private ProgressBar progressBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            communicationWithParentt = (CommunicationWithParentt) context;
        }
        catch(ClassCastException ignored){ }
    }

    @Override
    public void onDetach() {
        communicationWithParentt.acknowledgeScannerr();
        super.onDetach();
    }

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
        View rootView = inflater.inflate(R.layout.activity_bwg__sieve__removed__id,container,false);

        val = Objects.requireNonNull(getArguments()).getString("val");
        TextView isbn = rootView.findViewById(R.id.isbntextview);
        imageTrackingID=rootView.findViewById(R.id.errorimagetrackingid);
        mesageTrackingID=rootView.findViewById(R.id.errormessagetrackingid);
        progressBar = rootView.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        this.communicationWithParentt = (CommunicationWithParentt) getActivity();

        isbn.setText(val);

        try
        {
            new MyAsyncTasks().execute();
        }
        catch(Exception ignored){}


        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTasks extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void...v) {
            SharedPreferences sharedPreferences=Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
            SyncHttpClient httpClient=new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(2,1000);
            httpClient.addHeader("trackingId",val);
            httpClient.head("http://"+sharedPreferences.getString("ip",null)+":8080/com.bookswagon/service/bookswagon/remove/", new AsyncHttpResponseHandler() {

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
            AlertDialog.Builder ab=new AlertDialog.Builder(getContext());
            ab.setCancelable(false);
            ab.setView(v);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            progressBar.setVisibility(View.INVISIBLE);
            if(Objects.requireNonNull(getActivity()).isDestroyed()){
                return;
            }
            if(code==200){

                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                imageTrackingID.setImageResource(R.drawable.sucess);
                mesageTrackingID.setText("Tracking ID Removed successfully");
                mesageTrackingID.setTextColor(getResources().getColor(R.color.lightGreen));

                Handler handler=new Handler();
                handler.postDelayed(() -> {
                        if(dialog!=null){
                            dialog.dismiss();
                        }
                    // startActivity(new Intent(getApplicationContext(),Barcode_Scanner_Sieve.class).putExtra("loc","remove"));
                   // BWG_Sieve_Removed_ID.this.finish();
                },500);
            }
            else if(code==500){

                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                imageTrackingID.setImageResource(R.drawable.error);
                mesageTrackingID.setText("Tracking ID Not found");
                mesageTrackingID.setTextColor(getResources().getColor(R.color.colorPrimary));

                MediaPlayer player=MediaPlayer.create(getActivity(),R.raw.beep);
                player.start();

                Handler handler=new Handler();
                handler.postDelayed(() -> {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                    //startActivity(new Intent(getApplicationContext(),Barcode_Scanner_Sieve.class).putExtra("loc","remove"));
                    //BWG_Sieve_Removed_ID.this.finish();
                },500);

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

    interface CommunicationWithParentt{
        void acknowledgeScannerr();
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