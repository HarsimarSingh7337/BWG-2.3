package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Inward_Select_Vendor extends android.support.v4.app.DialogFragment {

    private String vendor="";
    private int code;
    private AlertDialog alertDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Choose a Vendor");
        getDialog().setCanceledOnTouchOutside(false);
        View rootView =  inflater.inflate(R.layout.fragment_inward__select__vendor, container, false);

        ArrayList<String> list=new ArrayList<>();
        list.add("Baker & Taylor");
        list.add("Bertram");
        list.add("Bookazine");
        list.add("Gardner");
        list.add("Ingram");
        Spinner spinner = rootView.findViewById(R.id.spinner_vendor_name);
        ArrayAdapter adapter=new ArrayAdapter<>(Objects.requireNonNull(getActivity()),R.layout.vendor_name_list,list);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vendor = (String)adapterView.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button submit=rootView.findViewById(R.id.submitVendor);
        submit.setOnClickListener(view -> {
            try{
                new MTasks().execute();
            }
            catch(Exception ignored){}
        });
        return rootView;
        // end of onCreateView
    }

    @SuppressLint("StaticFieldLeak")
    class MTasks extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("vendor", vendor);

                SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0);
                String emp_Id = sharedPreferences.getString("empId", null);

                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.addHeader("empId", emp_Id);
                httpClient.addHeader("content-type", "application/json");

                StringEntity entity = new StringEntity(jsonObject.toString());
                httpClient.get(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/vendorCheck/", entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        code = statusCode;
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

            try{
                if(code==200) {
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("vendor", vendor);
                    android.support.v4.app.FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                    Inward_Scanning inwardScanning = new Inward_Scanning();
                    inwardScanning.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                    inwardScanning.setArguments(bundle);
                    inwardScanning.show(fragmentManager, "");
                }
                else if(code==201){
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    Toast.makeText(getActivity(),"No such Vendor in Database", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    timeOutExceptionMessage();
                }
            }
            catch(Exception ignored){}
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