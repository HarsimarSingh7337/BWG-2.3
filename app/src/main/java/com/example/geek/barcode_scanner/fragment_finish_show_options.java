package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;

public class fragment_finish_show_options extends android.support.v4.app.DialogFragment {

    private SharedPreferences sharedPreferences;
    private String emp_Id, password;
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private int code;
    private AlertDialog alertDialog;
    CommunicatewithScanner communicatewithScanner;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            communicatewithScanner = (CommunicatewithScanner) context;
        }
        catch(ClassCastException ignored){
        }
    }

    @Override
    public void onDetach() {
        communicatewithScanner.acknowledgeScanner();
        super.onDetach();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCancelable(false);
        getDialog().setTitle("Review");
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_finish_show_options, container, true);

        communicatewithScanner = (CommunicatewithScanner) getActivity();
        String shelf = Objects.requireNonNull(getArguments()).getString("shelf");

        Button showListBtn = rootView.findViewById(R.id.showlistbtn);
        Button submitBtn = rootView.findViewById(R.id.submitbtn);
        Button discardBtn = rootView.findViewById(R.id.discardbtn);

        showListBtn.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), Show_Scanned_List_Data.class).putExtra("rac", shelf));
        });

        submitBtn.setOnClickListener(view -> {

            try{
                String query = "select * from scandata";
                Home_Screen.cursor1 = Home_Screen.database.rawQuery(query, null);
                if(!Home_Screen.cursor1.moveToFirst()) {
                    alertMessageOnEmptySubmission();
                }
                else{

                    sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
                    Date date = new Date();
                    String type = "Inventory Scan";
                    emp_Id = sharedPreferences.getString("empId", null);
                    password = sharedPreferences.getString("password", null);

                    jsonArray = new JSONArray();
                    jsonObject = new JSONObject();

                        jsonObject.put("shelf", shelf);
                        jsonObject.put("type", type);
                        jsonObject.put("date", String.valueOf(date));
                        jsonObject.put("emp_id", emp_Id);

                        Home_Screen.cursor = Home_Screen.database.rawQuery(query, null);
                        while (Home_Screen.cursor.moveToNext()) {

                            JSONObject ja = new JSONObject();
                            ja.put("ISBN", Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("ISBN")));
                            ja.put("QTY", String.valueOf(Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"))));
                            jsonArray.put(ja);
                        }
                        jsonObject.put("data", jsonArray);
                        new MyAsyncTasks().execute();
                }
            }
            catch(Exception ignored){
            }
            finally {
                if(Home_Screen.cursor!=null || Home_Screen.cursor1!=null){
                    Home_Screen.cursor.close();
                    Home_Screen.cursor1.close();
                }
            }
        });

        discardBtn.setOnClickListener(v ->{
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setMessage(" Are You sure you want to remove scanned results(if any) ?");
            ab.setPositiveButton("Yes", (dialogInterface, i) -> {
                try {
                    startActivity(new Intent(getActivity(), TabLayoutScreeen.class));
                    Objects.requireNonNull(getActivity()).finishAffinity();
                } catch (Exception ignored) {
                }
            });
            ab.setNegativeButton("No", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });
            ab.create();
            ab.show();
        });
        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(1,1000);
                httpClient.addHeader("empId", emp_Id);
                httpClient.addHeader("password", password);
                httpClient.addHeader("content-type", "application/json");

                StringEntity entity = new StringEntity(jsonObject.toString());
                RequestParams params = new RequestParams();
                params.add("json", jsonObject.toString());
                params.setUseJsonStreamer(true);
                httpClient.post(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/multipleinventory/", entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        code = statusCode;
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] res, Throwable error) {
                        code = statusCode;
                    }
                });
            } catch (Exception ignored) { }
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                switch (code) {
                    case 200:
                            if(alertDialog!=null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "Data Submitted", Toast.LENGTH_SHORT).show();
                            goToHomeScreen();
                            break;

                    case 201:
                            if(alertDialog!=null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "Data Submitted", Toast.LENGTH_SHORT).show();
                            goToHomeScreen();
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
    public void goToHomeScreen(){
        startActivity(new Intent(getActivity(), TabLayoutScreeen.class));
        Objects.requireNonNull(getActivity()).finishAffinity();
    }

    public void alertMessageOnEmptySubmission(){

        AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
        ab.setTitle("Alert!!!");
        ab.setMessage("Empty List Found, Exit to Home Screen ?");
        ab.setCancelable(false);
        ab.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            startActivity(new Intent(getActivity(),TabLayoutScreeen.class));
            Objects.requireNonNull(getActivity()).finishAffinity();
        });
        ab.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        ab.create().show();
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

    interface CommunicatewithScanner{
        void acknowledgeScanner();
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        super.onDestroy();
    }
}