package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class BWG_Sieve_Address extends DialogFragment {

    private Dialog dialog;
    private AlertDialog alertDialog;
    private int code;
    private JSONObject jsonObject;

    @Override
    public void onStart() {
        super.onStart();

        dialog = getDialog();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("Update Address");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_bwg__sieve__address, container, false);

        TextInputLayout wrapperTrackingId = rootView.findViewById(R.id.wrappertrackingid);
        TextInputLayout wrapperOldAddress = rootView.findViewById(R.id.wrapperoldaddress);
        TextInputLayout wrapperNewAddress = rootView.findViewById(R.id.wrappernewaddress);

        TextInputEditText trackingId = rootView.findViewById(R.id.trackingid);
        TextInputEditText oldAddress = rootView.findViewById(R.id.oldaddress);
        TextInputEditText newAddress = rootView.findViewById(R.id.newaddress);

        Button submit = rootView.findViewById(R.id.btnsubmit);

        submit.setOnClickListener(v -> {

            if(trackingId.getText().toString().trim().length()==0){
                wrapperTrackingId.setError("Invalid Field");
            }
            else if(oldAddress.getText().toString().trim().length()==0){
                wrapperOldAddress.setError("Invalid Field");
            }
            else if(newAddress.getText().toString().trim().length()==0){
                wrapperNewAddress.setError("Invalid Field");
            }
            else{
                // everything textbox has some data and now proceed on button

                try{
                    jsonObject = new JSONObject();
                    jsonObject.put("trackingid",trackingId.getText().toString());
                    jsonObject.put("oldaddress",oldAddress.getText().toString());
                    jsonObject.put("newaddress",newAddress.getText().toString());

                    new UpdateAddress().execute();
                }
                catch(JSONException ignored){

                }

            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }

    }


    @SuppressLint("StaticFieldLeak")
    private class UpdateAddress extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... v) {

            try{
                SharedPreferences sharedPreferences=Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
                SyncHttpClient httpClient=new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(2,1000);
                StringEntity entity = new StringEntity(jsonObject.toString());
                httpClient.post(getActivity(),"http://"+sharedPreferences.getString("ip",null)+":8080/com.bookswagon/service/bookswagon/updateaddress/",entity,"application/json", new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        code=statusCode;
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        code=statusCode;
                    }
                });
            }
            catch(Exception ignored){
            }

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
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            if(Objects.requireNonNull(getActivity()).isDestroyed()){
                return;
            }
            if(code==200){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }

                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setTitle("Response");
                ab.setMessage("Address updated successfully");
                ab.setCancelable(false);
                ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogg, int which) {
                        dialogg.dismiss();
                        if(dialog!=null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                });
                ab.show();
            }
            else if(code==300){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }

                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setTitle("Response");
                ab.setMessage("Tracking ID found in New Scanned ID List...\nKindly remove it before updating address.");
                ab.setCancelable(false);
                ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogg, int which) {
                        dialogg.dismiss();
                    }
                });
                ab.show();
            }
            else if(code==201){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                timeOutExceptionMessage();
            }
            else{
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                timeOutExceptionMessage();
            }
        }
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

            AlertDialog.Builder abb = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            abb.setTitle("Error");
            abb.setMessage(sb.toString());
            abb.setCancelable(false);
            abb.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
            abb.create().show();
        }
        catch(Exception ignored){}
    }

}
