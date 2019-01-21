package com.example.geek.barcode_scanner;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Mismatched_ISBN_Inward extends DialogFragment {

    private RecyclerView MyRecyclerView;
    private MyAsyncTask myAsyncTask;
    private AlertDialog alertDialog;
    private int code;
    private String vendor="";
    private JSONArray jsonArray = new JSONArray();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Mismatched ISBN List");
        getDialog().setCanceledOnTouchOutside(false);
        View rootView = inflater.inflate(R.layout.activity_mismatched__isbn__inward,container,false);

        vendor = getArguments().getString("vendor");
        MyRecyclerView = rootView.findViewById(R.id.recycler_view);
        Button btnSaveMismatchedIsbn = rootView.findViewById(R.id.btn_savemismatchedisbn);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        RecyclerView.Adapter mAdapter = new MyAdapter(Inward_Scanning_Results.notMatchedISBNList);
        MyRecyclerView.setAdapter(mAdapter);

        btnSaveMismatchedIsbn.setOnClickListener(v -> {


            try{
                for(int i=0;i<Inward_Scanning_Results.notMatchedISBNList.size();i++){

                    JSONObject jo=new JSONObject();
                    jo.put("data",Inward_Scanning_Results.notMatchedISBNList.get(i)+"-"+vendor);
                    jsonArray.put(jo);
                }

                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute();
            }
            catch(Exception ignored){
            }
        });

       return rootView;
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> list;

        MyAdapter(ArrayList<String> Data) {
            this.list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_mismatched_isbn_inward, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            MyRecyclerView = recyclerView;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.isbnvalue.setText(Inward_Scanning_Results.notMatchedISBNList.get(position));

            if (!MyRecyclerView.isComputingLayout()) {
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView isbnvalue;

        MyViewHolder(View v) {
            super(v);
            isbnvalue = v.findViewById(R.id.mismatched_isbn);
        }
    }

    class MyAsyncTask extends AsyncTask<Void,Void,Void>{

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
                httpClient.post(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/savemissingisbn/", entity, "application/json", new AsyncHttpResponseHandler() {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            switch(code){
                case 200:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Success");
                    alert.setMessage("Data Saved Successfully");
                    alert.setCancelable(false);
                    alert.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        getDialog().dismiss();
                        Inward_Scanning_Results.notMatchedISBNList.clear();
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
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        try{
            if(myAsyncTask!=null){
                myAsyncTask.cancel(true);
            }
        }
        catch(Exception ignored){
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
}
