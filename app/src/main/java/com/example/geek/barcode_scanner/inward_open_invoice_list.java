package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class inward_open_invoice_list extends DialogFragment {

    private int code;
    private AlertDialog alertDialog;
    private SharedPreferences sharedPreferences;
    private String selectedItem="";
    private CheckStatus checkStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Select an Invoice");
        View rootVeiew =  inflater.inflate(R.layout.fragment_inward_open_invoice_list, container, false);

        sharedPreferences = Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES,0));
        String invoice = Objects.requireNonNull(getArguments()).getString("invoice");
        ArrayList<String> list=new ArrayList<>();
        if(Objects.requireNonNull(invoice).contains(",")){
            //comma separated invoices
            String[] inv = invoice.split(",");

            //getting multiple invoices
            list.addAll(Arrays.asList(inv));
        }
        else{
            // single invoice
            list.add(invoice);
        }

        ListView listview = rootVeiew.findViewById(R.id.invoicelist);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(Objects.requireNonNull(getActivity()),R.layout.open_invoice_text_layout,list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener((parent, view, position, id) -> {

            selectedItem = (String) parent.getItemAtPosition(position);
             checkStatus=new CheckStatus();
             checkStatus.execute();
            //Toast.makeText(getActivity(),selectedItem,Toast.LENGTH_SHORT).show();
        });

        return rootVeiew;
    }

    // below class to open closed invoice in database

    @SuppressLint("StaticFieldLeak")
    class CheckStatus extends AsyncTask<Void,Void,Void> {

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
        protected Void doInBackground(Void... voids) {

            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("invoice", selectedItem);
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.head(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/openinvoice/", new AsyncHttpResponseHandler() {
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try{
                switch (code) {
                    case 200:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        AlertDialog.Builder ab=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                        ab.setCancelable(false);
                        ab.setMessage("Invoice opened successfully");
                        ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getActivity().finish();
                            }
                        });
                        ab.show();
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        AlertDialog.Builder abb=new AlertDialog.Builder(getActivity());
                        abb.setCancelable(false);
                        abb.setMessage("");
                        abb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getActivity().finish();
                            }
                        });
                        abb.show();
                        break;
                    default:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        timeOutExceptionMessage();
                        break;
                }
            }
            catch(Exception ignored){ }
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

            android.app.AlertDialog.Builder abb = new android.app.AlertDialog.Builder(getActivity());
            abb.setTitle("Error");
            abb.setMessage(sb.toString());
            abb.setCancelable(false);
            abb.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
            });
            abb.create().show();
        }
        catch(Exception ignored){}
    }
}