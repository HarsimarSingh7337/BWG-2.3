package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class BWG_Sieve_Scan_New_Results extends android.support.v4.app.DialogFragment {

    private TextView isbn,mesageTrackingID,count,datetime;
    private ImageView imageTrackingID;
    private String val;
    private int code;
    public static ArrayList<String> list=new ArrayList<>();
    private AlertDialog alertDialog;
    private String response;
    private Dialog dialog;
    CommunicationWithParent communicationWithParent;
    private TextView oldAddress,newAddress,oldAddressTag,newAddressTag;
    private ProgressBar progressBar;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            communicationWithParent = (CommunicationWithParent) context;
        }
        catch(ClassCastException ignored){ }
    }

    @Override
    public void onDetach() {
        communicationWithParent.acknowledgeScanner();
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
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        View rootView =inflater.inflate(R.layout.activity_bwg__sieve__scan__new__results,container,false);

        val = Objects.requireNonNull(getArguments()).getString("val");

        isbn = rootView.findViewById(R.id.isbntextview);
        imageTrackingID=rootView.findViewById(R.id.errorimagetrackingid);
        mesageTrackingID=rootView.findViewById(R.id.errormessagetrackingid);
        datetime=rootView.findViewById(R.id.datetime);
        oldAddress = rootView.findViewById(R.id.oldaddress);
        newAddress = rootView.findViewById(R.id.newaddress);
        oldAddressTag = rootView.findViewById(R.id.oldaddresstag);
        newAddressTag = rootView.findViewById(R.id.newaddresstag);
        progressBar = rootView.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        newAddress.setVisibility(View.GONE);
        oldAddress.setVisibility(View.GONE);
        newAddressTag.setVisibility(View.GONE);
        oldAddressTag.setVisibility(View.GONE);

        count=rootView.findViewById(R.id.count);
        this.communicationWithParent = (CommunicationWithParent) getActivity();

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
        protected Void doInBackground(Void... v) {

            SharedPreferences sharedPreferences=Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
            SyncHttpClient httpClient=new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(2,1000);
            httpClient.addHeader("trackingId",val);
            httpClient.get("http://"+sharedPreferences.getString("ip",null)+":8080/com.bookswagon/service/bookswagon/tracking/", new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    code=statusCode;
                    response=new String(responseBody);

                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    code=statusCode;
                    if(responseBody!=null){
                        response=new String(responseBody);
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar_plain,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
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
                list.add(isbn.getText().toString());
                count.setText(String.valueOf(list.size()));
                imageTrackingID.setImageResource(R.drawable.sucess);
                mesageTrackingID.setText("Tracking ID saved successfully");
                mesageTrackingID.setTextColor(getResources().getColor(R.color.lightGreen));
                datetime.setVisibility(View.GONE);
                newAddress.setVisibility(View.GONE);
                oldAddress.setVisibility(View.GONE);
                newAddressTag.setVisibility(View.GONE);
                oldAddressTag.setVisibility(View.GONE);

                Handler handler=new Handler();
                handler.postDelayed(() -> {
                     if(dialog!=null){
                         dialog.dismiss();
                     }
                    //startActivity(new Intent(getApplicationContext(),Barcode_Scanner_Sieve.class).putExtra("loc","new"));
                    //BWG_Sieve_Scan_New_Results.this.finish();

                },1000);
            }
            else if(code==300){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                count.setText(String.valueOf(list.size()));
                imageTrackingID.setImageResource(R.drawable.error);
                mesageTrackingID.setText("Tracking ID found in Cancelled List");
                mesageTrackingID.setTextColor(getResources().getColor(R.color.red));
                datetime.setVisibility(View.GONE);

                try{
                    JSONObject jsonObject=new JSONObject(response);
                    String oldaddr = jsonObject.get("oldaddress").toString();
                    String newaddr = jsonObject.get("newaddress").toString();

                    if(oldaddr.isEmpty()){
                        oldAddress.setVisibility(View.GONE);
                        oldAddressTag.setVisibility(View.GONE);
                    }
                    else if(newaddr.isEmpty()){
                        newAddress.setVisibility(View.GONE);
                        newAddressTag.setVisibility(View.GONE);
                    }
                    else{
                        oldAddress.setVisibility(View.VISIBLE);
                        oldAddressTag.setVisibility(View.VISIBLE);
                        newAddress.setVisibility(View.VISIBLE);
                        newAddressTag.setVisibility(View.VISIBLE);
                        oldAddress.setText(oldaddr);
                        newAddress.setText(newaddr);
                    }
                }
                catch(Exception ignored){
                }

                MediaPlayer player=MediaPlayer.create(getActivity(),R.raw.beep);
                player.start();

                Handler handler=new Handler();
                handler.postDelayed(() -> {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                    // startActivity(new Intent(getApplicationContext(),Barcode_Scanner_Sieve.class).putExtra("loc","new"));
                   // BWG_Sieve_Scan_New_Results.this.finish();
                },2000);
            }
            else if(code==500){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                count.setText(String.valueOf(list.size()));
                imageTrackingID.setImageResource(R.drawable.error);
                mesageTrackingID.setText("Tracking ID already exists in New Scanned List");
                mesageTrackingID.setTextColor(getResources().getColor(R.color.colorPrimary));
                datetime.setVisibility(View.VISIBLE);
                datetime.setText(response);
                newAddress.setVisibility(View.GONE);
                oldAddress.setVisibility(View.GONE);
                newAddressTag.setVisibility(View.GONE);
                oldAddressTag.setVisibility(View.GONE);

                MediaPlayer player=MediaPlayer.create(getActivity(),R.raw.beep);
                player.start();

                Handler handler=new Handler();
                handler.postDelayed(() -> {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                    //startActivity(new Intent(getApplicationContext(),Barcode_Scanner_Sieve.class).putExtra("loc","new"));
                   // BWG_Sieve_Scan_New_Results.this.finish();
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

    interface CommunicationWithParent{
        void acknowledgeScanner();
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