package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class inward_scanner_options extends android.support.v4.app.DialogFragment {

    Communicate communicate;
    private Button btnExport;
    private String vendor="";
    private AlertDialog alertDialog;
    private int code;
    private MyAsyncTaskss myAsyncTaskss;
    private JSONArray jsonArray=new JSONArray();
    ArrayList<String> invoiceList = new ArrayList();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            communicate = (Communicate) context;
        }
        catch (ClassCastException ignored){ }
    }

    @Override
    public void onDetach() {
        communicate.resumeScan();
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Review");
        getDialog().setCanceledOnTouchOutside(false);
        View rootView = inflater.inflate(R.layout.fragment_inward_scanner_options,container,false);

        communicate = (Communicate) getActivity();
        Button btnShowResult = rootView.findViewById(R.id.btn_showresult);
        Button btnDiscard = rootView.findViewById(R.id.btn_exit);
        Button btnMismatchedIsbn = rootView.findViewById(R.id.btn_mismatched_isbn);
        btnExport = rootView.findViewById(R.id.btn_export);

        vendor = Objects.requireNonNull(getArguments()).getString("vendor");
        String invoice = getArguments().getString("invoice");

        btnShowResult.setOnClickListener(v->{
            startActivity(new Intent(getActivity(),inward_results_showMyResult.class).putExtra("vendor",vendor).putExtra("invoice",invoice));
        });

        btnDiscard.setOnClickListener(v->{
            AlertDialog.Builder ab=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            ab.setCancelable(false);
            ab.setMessage("Scanned Results will be cleared (if any).");
            ab.setTitle("Sure to Exit ?");
            ab.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            ab.setPositiveButton("Ok", (dialog, which) -> {
                startActivity(new Intent(getActivity(),TabLayoutScreeen.class));
                getActivity().finishAffinity();
            });
            ab.create().show();
        });

        btnMismatchedIsbn.setOnClickListener(v -> {

            if(Inward_Scanning_Results.notMatchedISBNList.size()==0){
                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setTitle("Message");
                ab.setMessage("No Mismatched ISBN scanned yet.");
                ab.setCancelable(false);
                ab.setNegativeButton("", (dialog, which) -> {
                });
                ab.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                });
                ab.show();
            }
            else{
                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Mismatched_ISBN_Inward mismatched_isbn_inward = new Mismatched_ISBN_Inward();
                mismatched_isbn_inward.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                Bundle bundle = new Bundle();
                bundle.putString("vendor",vendor);
                mismatched_isbn_inward.setArguments(bundle);
                mismatched_isbn_inward.show(fragmentManager,"");
            }
        });

        btnExport.setOnClickListener(v -> {

            Cursor cur=null,cur1=null;
            int scanQTY=0;

            try{
                /*
                // getting distinct server invoices from database
                Home_Screen.cursor = Home_Screen.database.rawQuery("select distinct(INVOICE) from serverdata", null);
                while(Home_Screen.cursor.moveToNext()) {
                    String servinv = Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("INVOICE"));

                    // getting server isbn and qty from database corresponding to invoice
                    cur = Home_Screen.database.rawQuery("select isbn,qty,scannedqty from serverdata where invoice = '"+servinv+"' ",null);
                    while(cur.moveToNext()){

                        String servisbn = cur.getString(cur.getColumnIndex("ISBN"));
                        String servqty =String.valueOf(cur.getInt(cur.getColumnIndex("QTY")));
                        int qt = cur.getInt(cur.getColumnIndex("SCANNEDQTY"));

                        // getting scanned qty from database
                        cur1 = Home_Screen.database.rawQuery("select qty from scandata where isbn = '"+servisbn+"' ",null);
                        if(cur1.moveToNext()){
                            scanQTY =cur1.getInt(cur1.getColumnIndex("QTY"));
                        }
                        else{
                            scanQTY = 0;
                        }

                        int temp = scanQTY+qt;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("invoice",servinv);
                        jsonObject.put("isbn",servisbn);
                        jsonObject.put("qty",servqty);
                        jsonObject.put("scannedqty",String.valueOf(temp));
                        jsonObject.put("vendor",vendor);

                        jsonArray.put(jsonObject);
                    }
                }
                 AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);
                ab.setTitle("JSON");
                ab.setMessage(jsonArray.toString());
                ab.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
                ab.show();

               // jo.put("data",jsonArray);

                //myAsyncTaskss = new MyAsyncTaskss();
                //myAsyncTaskss.execute();
                */

                Home_Screen.cursor = Home_Screen.database.rawQuery("select distinct(ISBN) from serverdata", null);
                    while(Home_Screen.cursor.moveToNext()) {
                        int servscanedqty=0;
                        int servqty=0;
                        String servisbn = Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("ISBN"));

                        // getting invoices,totalQTY,scannedQty from server table
                        cur = Home_Screen.database.rawQuery("select invoice,qty,scannedqty from serverdata where isbn = '"+servisbn+"' ",null);
                        StringBuilder sb=new StringBuilder();
                        while(cur.moveToNext()){

                            sb.append(cur.getString(cur.getColumnIndex("INVOICE")));
                            sb.append(",");

                            servqty += cur.getInt(cur.getColumnIndex("QTY"));
                            servscanedqty += cur.getInt(cur.getColumnIndex("SCANNEDQTY"));
                        }
                         // removed last comma in merged invoices
                        String inv = sb.toString().substring(0,sb.length()-1);

                        // getting scanned qty from curring scanning table
                        cur1 = Home_Screen.database.rawQuery("select qty from scandata where isbn = '"+servisbn+"' ",null);
                        if(cur1.moveToNext()){

                            scanQTY =cur1.getInt(cur1.getColumnIndex("QTY"));
                        }
                        else{
                            scanQTY = 0;
                        }
                        int temp = scanQTY+servscanedqty;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("invoice",inv);
                        jsonObject.put("isbn",servisbn);
                        jsonObject.put("qty",servqty);
                        jsonObject.put("scannedqty",String.valueOf(temp));
                        jsonObject.put("vendor",vendor);

                        jsonArray.put(jsonObject);
                    }

               /* AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);
                ab.setTitle("JSON");
                ab.setMessage(jsonArray.toString());
                ab.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
                ab.show();*/
                myAsyncTaskss = new MyAsyncTaskss();
                myAsyncTaskss.execute();

            }
            catch(Exception i){
                /*AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);
                ab.setTitle("Exception!!!");
                ab.setMessage(i.getMessage());
                ab.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
                ab.show();*/
            }
            finally{
                    Home_Screen.cursor.close();
                    assert cur != null;
                    cur.close();
                    assert cur1 != null;
                    cur1.close();
            }
        });

        return rootView;
    }

    interface Communicate{
        void resumeScan();
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTaskss extends AsyncTask<Void,Void,Void>{

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

            switch (code){
                case 200:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Success");
                    alert.setMessage("Data Exported Successfully");
                    alert.setCancelable(false);
                    alert.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        btnExport.setEnabled(false);
                        btnExport.setClickable(false);
                        btnExport.setBackgroundColor(getResources().getColor(R.color.grey));
                        btnExport.setTextColor(getResources().getColor(R.color.black));
                    });
                    alert.show();
                    break;

                case 300:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    AlertDialog.Builder alertt = new AlertDialog.Builder(getActivity());
                    alertt.setTitle("Alert!!!");
                    alertt.setMessage("This ISBN is already closed.");
                    alertt.setCancelable(false);
                    alertt.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    alertt.show();
                    break;

                default:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    timeOutExceptionMessage();
                    break;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity().getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0));
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.addHeader("content-type", "application/json");

                StringEntity entity = new StringEntity(jsonArray.toString());
                RequestParams params = new RequestParams();
                params.add("json", jsonArray.toString());
                params.setUseJsonStreamer(true);
                httpClient.post(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/export/", entity, "application/json", new AsyncHttpResponseHandler() {
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
            catch (Exception io) {
                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);
                ab.setTitle("Error");
                ab.setMessage(io.getMessage());
                ab.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                ab.show();
            }
            return null;
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
            abb.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            abb.create().show();
        }
        catch(Exception ignored){}
    }

    @Override
    public void onStop() {
        super.onStop();
        if(myAsyncTaskss!=null){
         myAsyncTaskss.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        super.onDestroy();
    }
}
