package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Inward_Scanning extends android.support.v4.app.DialogFragment {

    private TextInputLayout wrapperinvoice;
    private TextInputEditText invoiceinput;
    private String vendor="";
    private int code;
    private String resp="";
    private AlertDialog alertDialog;
    private ArrayList<String> listOfInvoice = new ArrayList<>();
    private String invoice="";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Enter Invoices");
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_inward__scanning, container, false);
        vendor = Objects.requireNonNull(getArguments()).getString("vendor");

        invoiceinput = rootView.findViewById(R.id.invoiceinput);
        wrapperinvoice = rootView.findViewById(R.id.wrapperinvoice);
        Button invoicebtnsubmit = rootView.findViewById(R.id.invoicebtnsubmit);

        invoicebtnsubmit.setOnClickListener(view -> {

            listOfInvoice.clear();

            if (invoiceinput.getText().toString().trim().length() == 0) {
                wrapperinvoice.setError("Required Field");
            }
            else  {
                if(invoiceinput.getText().toString().contains(",")){
                    //invoice has comma separated values
                    String inv = invoiceinput.getText().toString();
                    String[] str = inv.split(",");

                    for(int i=0;i<str.length;i++){

                        if(listOfInvoice.contains(str[i])){
                            // already in list
                        }
                        else{
                            listOfInvoice.add(str[i]);
                        }
                    }
                    StringBuilder sb=new StringBuilder();
                    for(String st: listOfInvoice){
                        //traversing list of invoices
                        sb.append(st);
                        sb.append(",");
                    }

                    invoice = sb.substring(0,sb.length()-1);
                    new MTask().execute();
                }
                else {
                    // invoice does not have comma
                    invoice = invoiceinput.getText().toString();
                    new MTask().execute();
                }
            }
        });
        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    class MTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("invoice", invoice);
                jsonObject.put("vendor",vendor);

                SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
                String emp_Id = sharedPreferences.getString("empId", null);

                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.addHeader("empId", emp_Id);

                StringEntity entity = new StringEntity(jsonObject.toString());

                httpClient.post(getActivity(),"http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/inwardCheck/",entity,"application/json",new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        code = statusCode;
                        resp = new String(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] res, Throwable error) {
                        code = statusCode;
                    }
                });
            }
            catch(Exception ignored){}
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
            if (code==200){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                try {
                    JSONObject jobject = new JSONObject(resp);            // Received a String from server and putted into JSONObject
                    JSONArray jarray = (JSONArray) jobject.get("data");  // putting into JSONArray

                    // traversing json array
                    for (int i = 0; i < jarray.length(); i++) {

                        JSONObject jo = (JSONObject) jarray.get(i);

                        // getting isbn and qty and invoice from server as json response ans storing in local variables
                        String isbn = jo.get("isbn").toString();
                        int qty = Integer.parseInt(jo.get("qty").toString());
                        String inv = jo.get("invoice").toString();
                        int scqty = Integer.parseInt(jo.getString("scannedqty"));

                        // storing data in android sql database
                        String query = "insert into serverdata values('" + inv + "','" + isbn + "','" + qty + "','"+scqty+"')";
                        Home_Screen.database.execSQL(query);
                    }
                }
                catch(Exception ignored){ }
                // entered invoices found in database
                startActivity(new Intent(getActivity(), Barcode_Scanner2.class).putExtra("loc", "inward").putExtra("invoice", invoice).putExtra("vendor",vendor));
            }
            else if(code==201){
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                // entered invoices are not found in database
                Toast.makeText(getActivity(),"Invalid Invoice(s)",Toast.LENGTH_SHORT).show();
            }
            else{
                if(alertDialog!=null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                timeOutExceptionMessage();
            }
        }
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
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
            abb.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            abb.create().show();
        }
        catch(Exception ignored){}
    }
}