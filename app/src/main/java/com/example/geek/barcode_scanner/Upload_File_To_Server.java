package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.app.Activity.RESULT_OK;

public class Upload_File_To_Server extends DialogFragment {

    private TextView filePathTextView;
    private String filePath="";
    private static final int FILE_CHOOSE_CODE=1001;
    private Button uploadBtn;
    private String extractedData = "";
    private AlertDialog alertDialog;
    private int code;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Choose a CSV File");
        getDialog().setCanceledOnTouchOutside(false);
        View rootView  = inflater.inflate(R.layout.activity_upload__file__to__server,container,false);

        filePathTextView = rootView.findViewById(R.id.filepath);
        uploadBtn = rootView.findViewById(R.id.uploadfilebtn);
        Button btn_SelectFile = rootView.findViewById(R.id.btn_selectfile);

        btn_SelectFile.setOnClickListener(v -> {
            Intent i2 = new Intent(getActivity(), FileChooser.class);
            i2.putExtra(Constants.SELECTION_MODE,Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
            startActivityForResult(i2,FILE_CHOOSE_CODE);
        });

        uploadBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        uploadBtn.setEnabled(false);
        uploadBtn.setClickable(false);

        uploadBtn.setOnClickListener(v -> {
            try{
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                //Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVReader csvReader = new CSVReader(new FileReader(filePath));

                List<String[]> records = csvReader.readAll();
                for(String[] data: records){
                    JSONObject jo=new JSONObject();
                    jo.put("isbn",data[0]);
                    jo.put("qty",data[1]);
                    jo.put("invoice",data[2]);
                    jo.put("vendor",data[3]);

                    jsonArray.put(jo);
                }
                jsonObject.put("data",jsonArray);
                extractedData = jsonObject.get("data").toString();

        /*AlertDialog.Builder ab=new AlertDialog.Builder(Upload_File_To_Server.this);
        ab.setCancelable(false);
        ab.setTitle("Data");
        ab.setMessage(extractedData);
        ab.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
        ab.show();*/

                new MyAsyncTasks().execute();
            }
            catch(Exception e){
                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);
                ab.setTitle("Error");
                ab.setMessage(e.getMessage());
                ab.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
                ab.show();
            }

        });

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case FILE_CHOOSE_CODE:
                if(resultCode ==RESULT_OK){
                    Uri uri = data.getData();
                    filePath  = Objects.requireNonNull(uri).getPath();
                    filePathTextView.setText(filePath);
                    uploadBtn.setEnabled(true);
                    uploadBtn.setClickable(true);
                    uploadBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    uploadBtn.setTextColor(getResources().getColor(android.R.color.white));
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTasks extends AsyncTask<Void,Void,Void>{

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
                    alert.setMessage("Data Imported Successfully");
                    alert.setCancelable(false);
                    alert.setNeutralButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        getDialog().dismiss();
                    });
                    alert.show();
                    break;

                default:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    timeOutExceptionMessage();
                    break;
            }
        }

        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Void... voids) {

            try{
                SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity().getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0));
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.addHeader("content-type", "application/json");

                StringEntity entity = new StringEntity(extractedData);
                RequestParams params = new RequestParams();
                params.add("json", extractedData);
                params.setUseJsonStreamer(true);
                httpClient.post(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/import/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                ab.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
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
}