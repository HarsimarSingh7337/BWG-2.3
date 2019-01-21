package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class Searched_ISBN_Sale extends AppCompatActivity implements Search_Result_Sale.communicateWithParentActivity {

    private int code;
    private AlertDialog alertDialog;
    private String emp_Id;
    private String resp;
    private SharedPreferences sharedPreferences;
    private String isbn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched__isbn__sale);

        Bundle bundle = getIntent().getExtras();
         isbn= Objects.requireNonNull(bundle).getString("isbn");

        // send isbn to server to get response in return
        sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        emp_Id = sharedPreferences.getString("empId", null);
        new AsyncTasks().execute();
    }

    @Override
    public void fragmentDetached() {
        this.finish();
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("empId", emp_Id);
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.get(getApplicationContext(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/getIsbnListFromDatabase?isbn="+isbn, new AsyncHttpResponseHandler() {
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
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = LayoutInflater.from(Searched_ISBN_Sale.this).inflate(R.layout.progress_bar_plain,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(Searched_ISBN_Sale.this);
            ab.setCancelable(false);
            ab.setView(v);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try{
                switch (code){
                    case 200:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        parsingJsonFromServer();
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        showNoDataFoundMesage();
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

    private void parsingJsonFromServer() {
        try {
            Search_ISBN_Fragment.lisbn.clear();
            Search_ISBN_Fragment.lshelf.clear();
            Search_ISBN_Fragment.lqty.clear();

            JSONObject jobject = new JSONObject(resp);          // Received a String from server and putted into JSONObject
            JSONArray jarray = (JSONArray) jobject.get("data");  // putting into JSONArray

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject jo = (JSONObject) jarray.get(i);     // fetching json from JsonArray

                String shlf = jo.get("SHELF").toString();
                String isbn = jo.get("ISBN").toString();
                String qty = jo.get("QTY").toString();

                Search_ISBN_Fragment.lshelf.add(shlf);
                Search_ISBN_Fragment.lisbn.add(isbn);
                Search_ISBN_Fragment.lqty.add(qty);
            }
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            Search_Result_Sale searchResultSale = new Search_Result_Sale();
            searchResultSale.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            searchResultSale.show(fragmentManager, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), Barcode_Scanner_WM.class).putExtra("loc", "salesearch"));
        Searched_ISBN_Sale.this.finish();
    }

    public void showNoDataFoundMesage(){
        AlertDialog.Builder ab=new AlertDialog.Builder(Searched_ISBN_Sale.this);
        ab.setCancelable(false);
        ab.setTitle("Alert!!!");
        ab.setMessage("No Record Found for this ISBN");
        ab.setNeutralButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            finish();
            startActivity(new Intent(getApplicationContext(),Barcode_Scanner_WM.class).putExtra("loc", "salesearch"));
        });
        ab.create().show();
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

            android.app.AlertDialog.Builder abb = new android.app.AlertDialog.Builder(Searched_ISBN_Sale.this);
            abb.setTitle("Error");
            abb.setMessage(sb.toString());
            abb.setCancelable(false);
            abb.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            abb.create().show();
        }
        catch(Exception ignored){}
    }
}