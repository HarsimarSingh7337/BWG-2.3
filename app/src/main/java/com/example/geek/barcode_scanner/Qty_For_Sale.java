package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;

public class Qty_For_Sale extends DialogFragment {

    private TextInputLayout wrapperqtyinput;
    private TextInputEditText qtyinput;
    private String shelf, isbn;
    private SharedPreferences sharedPreferences;
    private String emp_Id;
    private JSONObject jsonObject;
    private AlertDialog alertDialog;
    private int code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Quantity for Sale:");
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_qty__for__sale, container, false);
        shelf = getArguments().getString("shelf");
        isbn = getArguments().getString("isbn");

        qtyinput = rootView.findViewById(R.id.qtyinput);
        wrapperqtyinput = rootView.findViewById(R.id.wrapperqtyinput);
        Button btnsubmit = rootView.findViewById(R.id.btnsubmit);

        btnsubmit.setOnClickListener(view -> {

            if ( qtyinput.getText().toString().trim().length() == 0 || qtyinput.getText().toString().equals("0") || qtyinput.getText().toString().equals("00") || qtyinput.getText().toString().equals("000") || qtyinput.getText().toString().equals("0000") || qtyinput.getText().toString().equals("00000") || qtyinput.getText().toString().equals("000000") || qtyinput.getText().toString().equals("0000000") || qtyinput.getText().toString().equals("00000000")) {
                // empty qty box
                wrapperqtyinput.setError("Invalid Quantity");
            } else {

                try {
                    sharedPreferences = getActivity().getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
                    emp_Id = sharedPreferences.getString("empId", null);

                    jsonObject = new JSONObject();
                    jsonObject.put("isbn", isbn);
                    jsonObject.put("shelf", shelf);
                    jsonObject.put("qty", qtyinput.getText().toString());
                } catch (Exception ignored) {
                }
                new AsyncTasks().execute();
            }
        });
        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(1,500);
                httpClient.addHeader("empId", emp_Id);
                httpClient.addHeader("content-type", "application/json");

                StringEntity entity = new StringEntity(jsonObject.toString());
                httpClient.post(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/sale/", entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        code = statusCode;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] res, Throwable error) {
                        code = statusCode;
                    }
                });
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            View v = LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
            ab.setCancelable(false);
            ab.setView(v);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try{
                switch (code) {
                    case 200:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        messageQuantityUpdated();
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        messageNotSufficientQuantity();
                        break;
                    default:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        timeOutExceptionMessage();
                        break;
                }
            }
            catch(Exception ignored){}
        }
    }

    public void messageQuantityUpdated(){
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setCancelable(false);
        ab.setTitle("Success");
        ab.setMessage("Quantity updated for ISBN: "+isbn);
        ab.setNeutralButton("OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            startActivity(new Intent(getActivity(), TabLayoutScreeen.class));
        });
        ab.show();
    }

    public void messageNotSufficientQuantity(){
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setCancelable(false);
        ab.setTitle("Error");
        ab.setMessage("Not Sufficient quantity in this Rack");
        ab.setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
        ab.show();
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

            AlertDialog.Builder abb = new AlertDialog.Builder(getActivity());
            abb.setTitle("Error");
            abb.setMessage(sb.toString());
            abb.setCancelable(false);
            abb.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
            abb.create().show();
        }
        catch(Exception ignored){}
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        super.onDestroy();
    }

    //End of Class
}