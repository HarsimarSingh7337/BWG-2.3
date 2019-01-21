package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Show_Scanned_List_Data extends AppCompatActivity {

    private RecyclerView MyRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> list_isbn = new ArrayList<>();
    private ArrayList<String> list_qty = new ArrayList<>();
    private TextView rack, rackqty;
    private SharedPreferences sharedPreferences;
    private int code = 0;
    private String resp = null;
    private String emp_Id, password;
    private JSONObject jsonObject;
    private AlertDialog alertDialog;
    String shelf;
    private boolean deviceHasFlash=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__scanned__list__data);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
        View view = getSupportActionBar().getCustomView();

        ImageView nav_dropdown = view.findViewById(R.id.actionbar_dropdown);
        ImageView option = view.findViewById(R.id.actionbar_option);
        nav_dropdown.setVisibility(View.INVISIBLE);

        option.setOnClickListener((View view1) -> {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view1);
            popupMenu.getMenuInflater().inflate(R.menu.menu1, popupMenu.getMenu());

            Menu m = popupMenu.getMenu();
            m.findItem(R.id.flashlight).setVisible(false);
            m.findItem(R.id.flashlight).setEnabled(false);

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.setip:
                        android.support.v4.app.FragmentManager fragmentManager =getSupportFragmentManager();
                        IP_Fragment ip_fragment = new IP_Fragment();
                        ip_fragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                        ip_fragment.show(fragmentManager, "");
                        break;
                    case R.id.flashlight:
                        // flashlight code here
                        checkFlashlightSupport();

                        if(!deviceHasFlash){
                            // flashlight not supportable in device
                            AlertDialog.Builder ab=new AlertDialog.Builder(Show_Scanned_List_Data.this);
                            ab.setTitle("Error");
                            ab.setMessage("Device has no Flashlight Support");
                            ab.setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
                            ab.create().show();
                        }
                        else{
                            android.support.v4.app.FragmentManager fragmentManager1=getSupportFragmentManager();
                            Flash flash=new Flash();
                            flash.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                            flash.show(fragmentManager1,"");
                        }
                        break;
                }
                return true;
            });
            popupMenu.show();
        });

        getList();
        sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);

        Bundle b = getIntent().getExtras();
        rack = findViewById(R.id.rack);
        rackqty = findViewById(R.id.rackqty);

        shelf= (Objects.requireNonNull(b).getString("rac"));
        rack.setText(shelf);

        MyRecyclerView = findViewById(R.id.card_recycle);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter(list_isbn);
        MyRecyclerView.setAdapter(mAdapter);

        new MasyncTasks().execute();
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> list;

        MyAdapter(ArrayList<String> Data) {
            this.list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_showscanned_data, parent, false);
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

            holder.displayisbnvalue.setText(list_isbn.get(position));
            holder.displayqtyvalue.setText(list_qty.get(position));

            if (!MyRecyclerView.isComputingLayout()) {
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return list_isbn.size();
        }
    }

     class MyViewHolder extends RecyclerView.ViewHolder {

        TextView displayisbnvalue, displayqtyvalue;
        ImageView removeBtn, increaseBtn, decreaseBtn;

        MyViewHolder(View v) {
            super(v);
            displayisbnvalue = v.findViewById(R.id.isbn_value);
            displayqtyvalue = v.findViewById(R.id.qty_value);
            removeBtn = v.findViewById(R.id.removebtn);
            decreaseBtn = v.findViewById(R.id.decreasebtn);
            increaseBtn = v.findViewById(R.id.increasebtn);

            removeBtn.setOnClickListener(view -> {
                try {
                    String query = "delete from scandata where ISBN ='" + displayisbnvalue.getText().toString().trim() + "'";
                    Home_Screen.database.execSQL(query);
                    Show_Scanned_List_Data.this.finish();
                } catch (Exception ignored) {
                } finally {
                    if(Home_Screen.cursor1!=null) {
                        Home_Screen.cursor.close();
                    }
                }
            });

            decreaseBtn.setOnClickListener(view -> {

                try {
                    String searchQuery = "select QTY from scandata where ISBN='" + displayisbnvalue.getText().toString().trim() + "'";
                    Home_Screen.cursor = Home_Screen.database.rawQuery(searchQuery, null);
                    if (Home_Screen.cursor.moveToNext()) {
                        int qty = Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"));
                        if (qty!=1) {
                            qty -= 1;
                            String updateQuery = "update scandata set QTY='" + qty + "' where ISBN ='" + displayisbnvalue.getText().toString().trim() + "' ";
                            Home_Screen.database.execSQL(updateQuery);

                            list_qty.remove(getAdapterPosition());
                            list_qty.add(getAdapterPosition(), String.valueOf(qty));

                            mAdapter.notifyItemChanged(getAdapterPosition());
                        }
                        else{
                                Toast.makeText(getApplicationContext(),"Qty cannot decrease from 1",Toast.LENGTH_LONG).show();
                            }
                    }
                } catch (Exception ignored) {

                } finally {
                    if(Home_Screen.cursor!=null) {
                        Home_Screen.cursor.close();
                    }
                }
            });

            increaseBtn.setOnClickListener(view -> {
                try {
                    String searchQuery = "select QTY from scandata where ISBN='" + displayisbnvalue.getText().toString().trim() + "'";
                    Home_Screen.cursor = Home_Screen.database.rawQuery(searchQuery, null);
                    if (Home_Screen.cursor.moveToNext()) {
                        int qty = Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"));
                        qty += 1;
                        String updateQuery = "update scandata set QTY='" + qty + "' where ISBN ='" + displayisbnvalue.getText().toString().trim() + "' ";
                        Home_Screen.database.execSQL(updateQuery);

                        list_qty.remove(getAdapterPosition());
                        list_qty.add(getAdapterPosition(), String.valueOf(qty));

                        mAdapter.notifyItemChanged(getAdapterPosition());
                    }
                } catch (Exception ignored) {
                } finally {
                    if(Home_Screen.cursor!=null) {
                        Home_Screen.cursor.close();
                    }
                }
            });
        }
    }

    private void getList() {
        try {
            String query = "select * from scandata";
            Home_Screen.cursor = Home_Screen.database.rawQuery(query, null);
            while (Home_Screen.cursor.moveToNext()) {

                list_isbn.add(Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("ISBN")));
                list_qty.add(String.valueOf(Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"))));
            }
        } catch (Exception ignored) {

        } finally {
            if(Home_Screen.cursor!=null){
                Home_Screen.cursor.close();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        list_isbn.clear();
        list_qty.clear();
    }

    @SuppressLint("StaticFieldLeak")
    class MasyncTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("empId", sharedPreferences.getString("empId", null));
                httpClient.addHeader("rack", rack.getText().toString());
                httpClient.get("http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/getTotalQtyInRack/", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        code = statusCode;
                        resp = new String(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] res, Throwable error) {
                        code = statusCode;
                        resp = new String(res);
                    }
                });
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            switch (code) {
                case 200:
                    rackqty.setText(resp);
                    break;
                case 201:
                    rackqty.setText("N/A");
                    break;
                default:
                    rackqty.setText("N/A");
                    // default value of code i.e 0
                    break;
            }
            super.onPostExecute(aVoid);
        }
    }

    public void submitData(View v){

        try{
            String query = "select * from scandata";
            Home_Screen.cursor1 = Home_Screen.database.rawQuery(query, null);
            if(!Home_Screen.cursor1.moveToNext()) {
                alertMessageOnEmptySubmission();
            }
            else{

                Date date = new Date();
                String type = "Inventory Scan";
                emp_Id = sharedPreferences.getString("empId", null);
                password = sharedPreferences.getString("password", null);

                JSONArray jsonArray = new JSONArray();
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
                httpClient.post(getApplicationContext(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/multipleinventory/", entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        code = statusCode;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] res, Throwable error) {
                        code = statusCode;
                    }
                });
            } catch (Exception ignored){ }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = LayoutInflater.from(Show_Scanned_List_Data.this).inflate(R.layout.progress_bar_plain,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(Show_Scanned_List_Data.this);
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
                        Toast.makeText(getApplicationContext(), "Data Submitted", Toast.LENGTH_SHORT).show();
                        goToHomeScreen();
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Data Submitted", Toast.LENGTH_SHORT).show();
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
            catch(Exception ignored){}
        }
    }

    public void goToHomeScreen(){
        startActivity(new Intent(getApplicationContext(), TabLayoutScreeen.class));
        finishAffinity();
    }

    public void alertMessageOnEmptySubmission(){

        AlertDialog.Builder ab=new AlertDialog.Builder(Show_Scanned_List_Data.this);
        ab.setTitle("Alert!!!");
        ab.setMessage("Empty List Found, Exit to Home Screen ?");
        ab.setCancelable(false);
        ab.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            startActivity(new Intent(getApplicationContext(),TabLayoutScreeen.class));
        });
        ab.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        ab.create().show();
    }

    public void checkFlashlightSupport(){
        deviceHasFlash = getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
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

                AlertDialog.Builder abb = new AlertDialog.Builder(Show_Scanned_List_Data.this);
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