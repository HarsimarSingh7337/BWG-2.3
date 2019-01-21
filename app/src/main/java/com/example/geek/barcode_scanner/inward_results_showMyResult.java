package com.example.geek.barcode_scanner;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class inward_results_showMyResult extends AppCompatActivity {

    private RecyclerView MyRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> list_isbn = new ArrayList<>();
    private ArrayList<Integer> list_scanned_qty = new ArrayList<>();
    private ArrayList<Integer> list_qty_server = new ArrayList<>();
    private ArrayList<String> invoice_server = new ArrayList<>();
    private int server_qty=0;
    private int server_scannedqty=0;
    private String invoice = "";
    private int code;
    private AlertDialog alertDialog;
    private SharedPreferences sharedPreferences;
    private ArrayList<String> checkboxList=new ArrayList<>();
    private ArrayList<String> clickedCheckBoxList=new ArrayList<>();
    private String finalInvoicesUserChecked="";
    private ArrayList<CheckBox> checkBoxReference=new ArrayList<>();
    private String resp="";
    private boolean noCheckBoxClicked=false;
    private final String clearScanTabelQuery = "delete from scandata";
    private ArrayList<String> closedCheckBox=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_inward_results_show_my_result);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar2);
        View view = getSupportActionBar().getCustomView();

        ImageView resetBtn = view.findViewById(R.id.reset_list);
        resetBtn.setOnClickListener(v -> {

            AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
            ab.setTitle("Confirmation");
            ab.setCancelable(false);
            ab.setMessage("Do you want to clear the scanned results ?");
            ab.setPositiveButton("Yes", (dialog, which) -> {
                Home_Screen.database.execSQL(clearScanTabelQuery);
                list_isbn.clear();
                list_qty_server.clear();
                list_scanned_qty.clear();
                invoice_server.clear();
                dialog.dismiss();

                // proceeding with nnext dialog and reloading list
                View vv = LayoutInflater.from(inward_results_showMyResult.this).inflate(R.layout.progress_bar_plain,null);
                TextView tv=vv.findViewById(R.id.progressbar_message);
                tv.setText("Reloading Result...");
                AlertDialog.Builder abb=new AlertDialog.Builder(inward_results_showMyResult.this);
                abb.setCancelable(false);
                abb.setView(vv);
                alertDialog = abb.create();
                alertDialog.show();

                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getList();
                        if(!MyRecyclerView.isComputingLayout()){
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(),"Data Reloaded",Toast.LENGTH_SHORT).show();
                            if(alertDialog!=null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                        }
                    }
                },1500);
                // end of data reloading here
            });
            ab.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            ab.show();
        });

        ImageView menuOption = view.findViewById(R.id.menu_option);
        menuOption.setOnClickListener(v -> {
            PopupMenu pp = new PopupMenu(getApplicationContext(), v);
            pp.getMenuInflater().inflate(R.menu.inward_show_result_menu, pp.getMenu());
            pp.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.openinvoice:
                        if(closedCheckBox.size()==0){

                            AlertDialog.Builder alert=new AlertDialog.Builder(inward_results_showMyResult.this);
                            alert.setTitle("Alert!!!");
                            alert.setMessage("No any closed invoice found...");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alert.show();
                        }
                        else{
                            FragmentManager fragmentManager=getSupportFragmentManager();
                            inward_open_invoice_list inwardopeninvoicelist=new inward_open_invoice_list();
                            inwardopeninvoicelist.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                            Bundle bundle=new Bundle();

                            StringBuilder sb=new StringBuilder();
                            for(String str: closedCheckBox){

                                sb.append(str);
                                sb.append(",");
                            }

                            String inv = sb.substring(0,sb.length()-1);

                            bundle.putString("invoice",inv);
                            inwardopeninvoicelist.setArguments(bundle);
                            inwardopeninvoicelist.show(fragmentManager,"");
                        }
                        break;
                }
                return false;
            });
            pp.show();

        });

        Bundle bundle=getIntent().getExtras();
        String vendor = Objects.requireNonNull(bundle).getString("vendor");
        invoice = bundle.getString("invoice");

        // container for check boxes
        LinearLayout checkboxContainer = findViewById(R.id.checkbox_container);

        sharedPreferences = Objects.requireNonNull(getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE));
        getList();

        MyRecyclerView = findViewById(R.id.card_recycle);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter(list_isbn);
        MyRecyclerView.setAdapter(mAdapter);
        TextView vendorText=findViewById(R.id.vendor);
        vendorText.setText(vendor);
        //status = findViewById(R.id.status);
       // status.setText("N/A");

        // checking whether invoice is single or multiple
        if(invoice.contains(",")){
            //comma separated invoices
            String[] inv = invoice.split(",");

            //getting multiple invoices
            checkboxList.addAll(Arrays.asList(inv));
        }
        else{
            // single invoice
            checkboxList.add(invoice);
        }

        // creating checkboxes dynamically according to invoices
        for(String checkboxname:checkboxList){
            CheckBox checkBox = new CheckBox(inward_results_showMyResult.this);
            checkBox.setText(checkboxname);
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.0f);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setOnClickListener(checkBoxListener(checkBox));

            checkBoxReference.add(checkBox);
            checkboxContainer.addView(checkBox);
        }

        new CheckStatus().execute();

        Button btn_updateStatus = findViewById(R.id.btn_updateStatus);
        btn_updateStatus.setOnClickListener(v ->{

            boolean flag=false;

            // checking if there is unchecked box before updating to server
            for(CheckBox ch:checkBoxReference){

                if(ch.isEnabled()){

                    if(!ch.isChecked()){
                        flag = true;
                    }
                    else{
                        flag=false;
                        break;
                    }
                }
            }

            if(noCheckBoxClicked){
                if(flag){
                    AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
                    ab.setTitle("Alert!!!");
                    ab.setMessage("Kindly select an Invoice Checkbox(s) to update its status");
                    ab.setCancelable(false);
                    ab.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
                else{
                    new AsyncTasks().execute();
                }
            }
            else{
                AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
                ab.setTitle("Alert!!!");
                ab.setMessage("Kindly select an Invoice Checkbox(s) to update its status");
                ab.setCancelable(false);
                ab.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                ab.show();
            }

            /*AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
            ab.setMessage(finalInvoicesUserChecked);
            ab.setCancelable(false);
            ab.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
            ab.show();*/
        });

    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> list;

        MyAdapter(ArrayList<String> Data) {
            this.list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_inward_showmydata, parent, false);
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

            try {
                if(list_qty_server.get(position).equals(list_scanned_qty.get(position))){
                    holder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightGreen  ));
                }
                else{
                    holder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.mehroon1));
                }
                holder.invoicevalue.setText(invoice_server.get(position));
                holder.orderqty.setText(String.valueOf(list_qty_server.get(position)));
                holder.shortQty.setText(String.valueOf(list_scanned_qty.get(position)));
                holder.isbnvalue.setText(list_isbn.get(position));
            } catch (Exception ignored) {

                AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
                ab.setTitle("Error");
                ab.setMessage(ignored.getMessage());
                ab.setCancelable(false);
                ab.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                ab.show();
            }
            if(!MyRecyclerView.isComputingLayout()){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return list_isbn.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView isbnvalue, invoicevalue, shortQty,orderqty;
        RelativeLayout relativeLayout;

        MyViewHolder(View v) {
            super(v);
            relativeLayout=v.findViewById(R.id.relativelayout);
            isbnvalue = v.findViewById(R.id.isbnvalue);
            shortQty = v.findViewById(R.id.shortqty);
            invoicevalue=v.findViewById(R.id.invoicevalue);
            orderqty=v.findViewById(R.id.orderqty);
        }
    }

    private void getList() {

        try {
            Home_Screen.cursor = Home_Screen.database.rawQuery("select distinct(ISBN) from serverdata", null);
            while(Home_Screen.cursor.moveToNext()) {

                String isbn = Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("ISBN"));
                StringBuilder stringBuilder = new StringBuilder();

                Home_Screen.cursor1 = Home_Screen.database.rawQuery("select INVOICE,QTY,SCANNEDQTY from serverdata where ISBN ='" + isbn + "' ", null);
                while(Home_Screen.cursor1.moveToNext()) {

                    server_qty+=Home_Screen.cursor1.getInt(Home_Screen.cursor1.getColumnIndex("QTY"));
                    server_scannedqty+=Home_Screen.cursor1.getInt(Home_Screen.cursor1.getColumnIndex("SCANNEDQTY"));
                    stringBuilder.append(Home_Screen.cursor1.getString(Home_Screen.cursor1.getColumnIndex("INVOICE")));
                    stringBuilder.append(",");
                }

                // updating scanned quantity below
                Cursor cur = Home_Screen.database.rawQuery("select QTY from scandata where ISBN='"+isbn+"' ", null);
                if(cur.moveToNext()) {

                    int actualScannnedQty = cur.getInt(cur.getColumnIndex("QTY"));
                    server_scannedqty+=actualScannnedQty;
                }
                cur.close();

                String temp = stringBuilder.toString().substring(0, stringBuilder.toString().length()-1);
                invoice_server.add(temp);
                list_qty_server.add(server_qty);
                list_scanned_qty.add(server_scannedqty);
                server_qty=0;
                server_scannedqty=0;

                // till this code is working fine
                list_isbn.add(isbn);
            }
            mAdapter.notifyDataSetChanged();

            }
        catch(Exception ignored){}
        finally {
            if( Home_Screen.cursor1!=null && Home_Screen.cursor!=null ){
                Home_Screen.cursor1.close();
                Home_Screen.cursor.close();
            }
        }
    }

    // below class will check and return status as 'open' or 'close' string
    // also disable check boxes on response
    @SuppressLint("StaticFieldLeak")
    class CheckStatus extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = LayoutInflater.from(inward_results_showMyResult.this).inflate(R.layout.progress_bar_plain,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
            ab.setCancelable(false);
            ab.setView(v);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("invoice", invoice);
                httpClient.setMaxRetriesAndTimeout(2,1000);
                httpClient.get(getApplicationContext(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/invoicestatus?invoice="+invoice, new AsyncHttpResponseHandler() {
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
            super.onPostExecute(aVoid);

            try{
                switch (code) {
                    case 200:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                      //  status.setText("Close");
                        disableCheckBox();
                        /*AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
                        ab.setCancelable(false);
                        ab.setMessage(resp);
                        ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ab.show();*/
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                       // status.setText("Open");
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

    // below class that will update status in the databse eg(0,1)
    @SuppressLint("StaticFieldLeak")
    class AsyncTasks extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = LayoutInflater.from(inward_results_showMyResult.this).inflate(R.layout.progress_bar_plain,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(inward_results_showMyResult.this);
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

                        AlertDialog.Builder alert=new AlertDialog.Builder(inward_results_showMyResult.this);
                        alert.setTitle("Status");
                        alert.setMessage("Scanning Closed for Invoice(s): "+finalInvoicesUserChecked);
                        alert.setCancelable(false);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                            }
                        });
                        alert.show();
                        break;
                    case 201:
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        timeOutExceptionMessage();

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

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                SyncHttpClient httpClient = new SyncHttpClient();
                httpClient.addHeader("invoice", finalInvoicesUserChecked);

                //StringEntity entity = new StringEntity(invoice);
                httpClient.setMaxRetriesAndTimeout(2,1000);
               // RequestParams params = new RequestParams();
               // params.add("invoice", invoice);
                httpClient.head(getApplicationContext(), "http://" + sharedPreferences.getString("ip", null) + ":8080/com.bookswagon/service/bookswagon/updatestatus/", new AsyncHttpResponseHandler() {
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

            android.app.AlertDialog.Builder abb = new android.app.AlertDialog.Builder(inward_results_showMyResult.this);
            abb.setTitle("Error");
            abb.setMessage(sb.toString());
            abb.setCancelable(false);
            abb.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
            });
            abb.create().show();
        }
        catch(Exception ignored){}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }

    View.OnClickListener checkBoxListener(CheckBox checkBox){

        return v -> {

            // detecting whether any of the check box is clicked or not
            noCheckBoxClicked=true;

                    if(clickedCheckBoxList.contains(checkBox.getText().toString())){
                            for(int i=0;i<clickedCheckBoxList.size();i++){

                                if(clickedCheckBoxList.get(i).equals(checkBox.getText().toString())){
                                    try{
                                        clickedCheckBoxList.remove(i);
                                    }
                                    catch(Exception ignored){}
                                }
                            }
                    }
                    else{
                        clickedCheckBoxList.add(checkBox.getText().toString());
                    }

               /* if(checkBox.isChecked()){
                    if(clickedCheckBoxList.contains(checkBox.getText().toString())){
                    }
                    else{
                        clickedCheckBoxList.add(checkBox.getText().toString());
                    }
                }
                else{
                    try{
                        for(int i=0;i<clickedCheckBoxList.size();i++){
                            if(clickedCheckBoxList.get(i).equals(checkBox.getText().toString())){
                                    clickedCheckBoxList.remove(i);
                            }
                        }
                    }
                    catch(Exception ignored){ }
                }*/

                if(clickedCheckBoxList.isEmpty()){
                    finalInvoicesUserChecked="";
                }
                else{
                    StringBuilder sb=new StringBuilder();
                    for(String st:clickedCheckBoxList){
                        sb.append(st);
                        sb.append(",");
                    }
                    finalInvoicesUserChecked= sb.substring(0,sb.length()-1);
                }
        };
    }

    public void disableCheckBox(){

        if(!resp.contains(",")){
            // response string does not have comma
            // its a single invoice

            for(CheckBox ch:checkBoxReference){
                if(ch.getText().toString().equals(resp)){
                    ch.setChecked(true);
                    ch.setEnabled(false);
                    ch.setClickable(false);
                    ch.setAlpha(0.5f);
                    ch.setTextColor(getResources().getColor(R.color.grey));
                    closedCheckBox.add(ch.getText().toString());
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ch.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                    }
                    else{
                        // api level below 21
                    }*/
                }
            }
        }
        else{
              // response string has comma separated fields
            // removed last comma in response string from server to make it more concise
            String str = resp.substring(0,resp.length()-1);

            // splitting string by comma
            String[] str1 = str.split(",");

            for(int i=0;i<str1.length;i++){

                String element = str1[i];

                for(CheckBox ch:checkBoxReference){

                    if(ch.getText().toString().equals(element)){
                        ch.setChecked(true);
                        ch.setEnabled(false);
                        ch.setClickable(false);
                        ch.setAlpha(0.5f);
                        ch.setTextColor(getResources().getColor(R.color.grey));
                        closedCheckBox.add(ch.getText().toString());
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ch.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                        }
                        else{
                            // api level below 21
                        }*/
                    }
                }
            }
        }
    }
    // end of class
}