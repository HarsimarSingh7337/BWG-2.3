package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;


public class inward_results_showMyList extends AppCompatActivity {

    private RecyclerView MyRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> list_isbn = new ArrayList<>();
    private ArrayList<String> list_qty = new ArrayList<>();
    private ArrayList<String> list_orderqty = new ArrayList<>();
    private ArrayList<String> list_isbn_server = new ArrayList<>();
    private ArrayList<String> list_qty_server = new ArrayList<>();
    private String resp;
    private AlertDialog alertDialog;
    private int code;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_inward_results_show_my_result);

        new MyAsyncTask().execute();
        MyRecyclerView = findViewById(R.id.card_recycle);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter(list_isbn);
        MyRecyclerView.setAdapter(mAdapter);

        Bundle bundle=getIntent().getExtras();

        getList();
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> list;

        MyAdapter(ArrayList<String> Data) {
            this.list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_inward_showmylist, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            MyRecyclerView = recyclerView;
        }

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            try {
                holder.displayisbnvalue.setText(list_isbn.get(position));
                holder.actualqty.setText(list_qty.get(position));
                holder.orderqty.setText(list_orderqty.get(position));

                int orqty = Integer.parseInt(holder.orderqty.getText().toString());
                int acqty = Integer.parseInt(holder.actualqty.getText().toString());

                if (orqty == acqty) {
                    holder.status.setText("Complete");
                } else if (orqty > acqty) {
                    holder.status.setText("Incomplete");
                } else {
                    holder.status.setText("Extra Scanned Items Found ");
                }
                mAdapter.notifyDataSetChanged();
            } catch (Exception ignored) {
            }
        }

        @Override
        public int getItemCount() {
            return list_isbn.size();
        }
    }

     class MyViewHolder extends RecyclerView.ViewHolder {

        TextView displayisbnvalue, orderqty, actualqty, status;

        MyViewHolder(View v) {
            super(v);
            displayisbnvalue = v.findViewById(R.id.isbn_value);
            orderqty = v.findViewById(R.id.orderqty);
            actualqty = v.findViewById(R.id.actualqty);
            status = v.findViewById(R.id.status);
        }
    }

    private void getList(){
        try {
            Home_Screen.cursor = Home_Screen.database.rawQuery("select * from scandata", null);
            if (Home_Screen.cursor.moveToFirst()) {
                list_isbn.add(Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("ISBN")));
                list_qty.add(Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("QTY")));
                // this condition will never execute
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception Occured", Toast.LENGTH_LONG).show();
        } finally {
            Home_Screen.cursor.close();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                JSONObject jsonObject = new JSONObject();
                String invoice = "";
                jsonObject.put("invoice", invoice);
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
                String emp_Id = sharedPreferences.getString("empId", null);

                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("empId", emp_Id);
                httpClient.addHeader("content-type", "application/json");

                StringEntity entity = new StringEntity(jsonObject.toString());
                httpClient.get(getApplicationContext(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/inwardCheck/", entity, "application/json", new AsyncHttpResponseHandler() {
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
            } catch (Exception ignored){
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            View v = LayoutInflater.from(inward_results_showMyList.this).inflate(R.layout.progress_bar_plain,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyList.this);
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
                        Toast.makeText(getApplicationContext(), "No Record Found", Toast.LENGTH_SHORT).show();
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

    private void parsingJsonFromServer(){
        try {
            JSONObject jobject = new JSONObject(resp);          // Received a String from server and putted into JSONObject
            JSONArray jarray = (JSONArray) jobject.get("data");  // putting into JSONArray

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject jo = (JSONObject) jarray.get(i);

                String isbn = jo.get("isbn").toString();
                String qty = jo.get("qty").toString();

                list_isbn_server.add(isbn);
                list_qty_server.add(qty);
            }

            for (int i = 0; i < list_isbn.size(); i++) {

                String isbnn = list_isbn.get(i);

                for (int j = 0; j < list_isbn_server.size(); j++) {
                    if (isbnn.equals(list_isbn_server.get(j))) {
                        String qtty = list_qty_server.get(j);
                        list_orderqty.add(i, qtty);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        super.onDestroy();
    }
}