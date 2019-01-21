package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

public class Search_ISBN_Fragment extends android.support.v4.app.DialogFragment implements Fragment_ShowReceivedDataFromServer.communication {

    private TextInputLayout wrapperIsbn;
    private TextInputEditText isbnNumber;
    private int code;
    private AlertDialog alertDialog;
    private SharedPreferences sharedPreferences;
    private String emp_Id;
    private String resp;
    public static ArrayList<String> lshelf = new ArrayList<>();
    public static ArrayList<String> lisbn = new ArrayList<>();
    public static ArrayList<String> lqty = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Search in Stock");
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_search__option_, container, false);
        wrapperIsbn = rootView.findViewById(R.id.wrapperisbnnumber);
        isbnNumber = rootView.findViewById(R.id.isbnnumber);
        Button button = rootView.findViewById(R.id.submit_radiobuttonselection);

        button.setOnClickListener(view -> {

            if (isbnNumber.getText().toString().trim().length() == 0) {
                wrapperIsbn.setError("Invalid field");
            } else {
                if(isbnNumber.getText().toString().length() > 15){
                    Toast.makeText(getActivity(),"Invalid Length",Toast.LENGTH_LONG).show();
                }
                else {
                    // send isbn to server to get response in return
                    sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
                    emp_Id = sharedPreferences.getString("empId", null);
                    new AsyncTasks().execute();
                }
            }
        });
        return rootView;
    }

    @Override
    public void isConnected() {

    }


    @SuppressLint("StaticFieldLeak")
    class AsyncTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("emp_id", emp_Id);
                httpClient.setMaxRetriesAndTimeout(1,1000);
                httpClient.get(getActivity(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/getIsbnListFromDatabase?isbn="+isbnNumber.getText().toString(), new AsyncHttpResponseHandler() {
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
                        parsingJsonFromServer();
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        Toast.makeText(getActivity(), "No Record Found", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        break;
                }
            }
            catch(Exception ignored){ }
        }
    }

        public void parsingJsonFromServer(){
            try {
                lisbn.clear();
                lshelf.clear();
                lqty.clear();

                JSONObject jobject = new JSONObject(resp);          // Received a String from server and putted into JSONObject
                JSONArray jarray = (JSONArray)jobject.get("data");  // putting into JSONArray

                for (int i = 0; i < jarray.length(); i++) {

                    JSONObject jo = (JSONObject) jarray.get(i);     // fetching json from JsonArray

                    String shlf = jo.get("SHELF").toString();
                    String isbn = jo.get("ISBN").toString();
                    String qty = jo.get("QTY").toString();

                    lshelf.add(shlf);
                    lisbn.add(isbn);
                    lqty.add(qty);
                }
                android.support.v4.app.FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                ShowManualSearched_List showManualSearched_list = new ShowManualSearched_List();
                showManualSearched_list.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                showManualSearched_list.show(fragmentManager, "");
            } catch (JSONException ignored) {

            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }
}
