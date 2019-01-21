package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Inventory_Scan_Results extends DialogFragment {

    private TextView isbnRetrieved, shelfRetrived;
    private JSONObject jsonObject;
    private SharedPreferences sharedPreferences;
    private AlertDialog alertDialog;
    private String emp_Id, password;
    private int code;
    private MyAsyncTask myAsyncTask=null;
    Communicate communicate;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            communicate= (Communicate) context;
        }
        catch(ClassCastException ignored){ }
    }

    @Override
    public void onDetach() {
        communicate.resumeScan();
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        /*Dialog dialog=getDialog();
        if(dialog!=null){
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.activity_inventory_scan_results,container,false);

        communicate= (Communicate) getActivity();
        Button btn_addmore = rootView.findViewById(R.id.btn_addmore);
        Button btn_submit = rootView.findViewById(R.id.btn_submit);

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
        //TextView loggedEmpId = rootView.findViewById(R.id.logedempid);
       // loggedEmpId.setText(sharedPreferences.getString("empId", null));
       // loggedEmpId.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Medium.ttf"));

        isbnRetrieved = rootView.findViewById(R.id.isbntextview);
        shelfRetrived = rootView.findViewById(R.id.shelftextview);

        isbnRetrieved.setText(Objects.requireNonNull(getArguments()).getString("isbn"));
        shelfRetrived.setText(getArguments().getString("shelfnumber"));

        String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(isbnRetrieved.getText().toString());

        if(matcher.matches()){
            try{
                Home_Screen.cursor = Home_Screen.database.rawQuery("select * from scandata where ISBN ='" + isbnRetrieved.getText().toString().trim() + "'", null);
                if (Home_Screen.cursor.moveToFirst()) {
                    // this condition will never execute
                }
                else{
                    int qty = 1;
                    String query = "insert into scandata (ISBN,QTY) values ('" + isbnRetrieved.getText().toString().trim() + "','" + qty + "')";
                    Home_Screen.database.execSQL(query);
                }
            } catch(Exception ignored){
            } finally {
                if(Home_Screen.cursor!=null){
                    Home_Screen.cursor.close();
                }
            }
        }
        else{
            // ISBN not matched with the given pattern
            AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
            ab.setCancelable(false);
            ab.setTitle("Wrong  ISBN...");
            ab.setMessage("This is not a valid ISBN");
            ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ab.create().show();
        }

        btn_addmore.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), Barcode_Scanner.class).putExtra("shelfNumber", shelfRetrived.getText().toString()).putExtra("loc", "empty").putExtra("launchBy", "abc"));
            getActivity().finish();
        });

        btn_submit.setOnClickListener(v -> {
            try {
                // getting logged in user from session
                Date date = new Date();
                String type = "Inventory Scan";
                emp_Id = sharedPreferences.getString("empId", null);
                password = sharedPreferences.getString("password", null);

                try {
                    JSONArray jsonArray = new JSONArray();

                    jsonObject = new JSONObject();
                    jsonObject.put("type", type);
                    jsonObject.put("shelf", shelfRetrived.getText().toString());
                    jsonObject.put("date", String.valueOf(date));
                    jsonObject.put("emp_id", emp_Id);

                    JSONObject jo=new JSONObject();
                    jo.put("ISBN", isbnRetrieved.getText().toString());
                    jo.put("QTY", "1");

                    jsonArray.put(jo);

                    jsonObject.put("data",jsonArray);

                    myAsyncTask=new MyAsyncTask();
                    myAsyncTask.execute();

                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {}
        });
        return rootView;
    }

   /* @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Normal app init code...
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        View view = getSupportActionBar().getCustomView();

        /*sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
        TextView loggedEmpId = findViewById(R.id.logedempid);
        loggedEmpId.setText(sharedPreferences.getString("empId", null));
        loggedEmpId.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Medium.ttf"));

        TextView appname = view.findViewById(R.id.actionbar_appname);
        appname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        ImageView nav_back = view.findViewById(R.id.actionbar_backbutton);
        ImageView nav_dropdown = view.findViewById(R.id.actionbar_dropdown);
        nav_dropdown.setVisibility(View.INVISIBLE);
        nav_back.setOnClickListener(view1 -> {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(" Are You sure you want to remove scanned results ?");
            ab.setPositiveButton("Yes", (dialogInterface, i) -> {
                startActivity(new Intent(getApplicationContext(),TabLayoutScreeen.class));
                finishAffinity();
            });
            ab.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            ab.create();
            ab.show();
        });
        isbnRetrieved = findViewById(R.id.isbntextview);
        shelfRetrived = findViewById(R.id.shelftextview);

        Bundle bundle = getIntent().getExtras();

        isbnRetrieved.setText(Objects.requireNonNull(bundle).getString("isbn"));
        shelfRetrived.setText(bundle.getString("shelfnumber"));

        String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(isbnRetrieved.getText().toString());

        if(matcher.matches()){
            try{
                Home_Screen.cursor = Home_Screen.database.rawQuery("select * from scandata where ISBN ='" + isbnRetrieved.getText().toString().trim() + "'", null);
                if (Home_Screen.cursor.moveToFirst()) {
                    // this condition will never execute
                }
                else{
                    int qty = 1;
                    String query = "insert into scandata (ISBN,QTY) values ('" + isbnRetrieved.getText().toString().trim() + "','" + qty + "')";
                    Home_Screen.database.execSQL(query);
                }
            } catch(Exception e){
                Log.e("Error: ",e.getMessage());
            } finally {
                if(Home_Screen.cursor!=null){
                    Home_Screen.cursor.close();
                }
            }
        }
        else{
            // ISBN not matched with the given pattern
            AlertDialog.Builder ab=new AlertDialog.Builder(Inventory_Scan_Results.this);
            ab.setCancelable(false);
            ab.setTitle("Wrong  ISBN...");
            ab.setMessage("This is not a valid ISBN");
            ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ab.create().show();
        }
    }*/

   /* public void addMore(View v) {
        startActivity(new Intent(getApplicationContext(), Barcode_Scanner.class).putExtra("shelfNumber", shelfRetrived.getText().toString()).putExtra("loc", "empty").putExtra("launchBy", "abc"));
    }*/

    /*public void submitData(View v) {
        // this will submit a single ISBN Scanned into database
        try {
            // getting logged in user from session
            Date date = new Date();
            String type = "Inventory Scan";
            emp_Id = sharedPreferences.getString("empId", null);
            password = sharedPreferences.getString("password", null);

            try {
                jsonObject = new JSONObject();
                jsonObject.put("type", type);
                jsonObject.put("shelf", shelfRetrived.getText().toString());
                jsonObject.put("isbnn", isbnRetrieved.getText().toString());
                jsonObject.put("qty", "1");
                jsonObject.put("date", String.valueOf(date));
                jsonObject.put("emp_id", emp_Id);

                myAsyncTask=new MyAsyncTask();
                myAsyncTask.execute();

            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } catch (Exception ignored) {}
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if(myAsyncTask!=null){
            myAsyncTask.cancel(true);
        }
    }

     @SuppressLint("StaticFieldLeak")
     class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                     SyncHttpClient httpClient = new SyncHttpClient();
                     httpClient.setMaxRetriesAndTimeout(1,1000);
                     httpClient.addHeader("empId", emp_Id);
                     httpClient.addHeader("password", password);

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

            } catch (Exception ignored) {
            }
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
                            Objects.requireNonNull(getActivity()).finishAffinity();
                            startActivity(new Intent(getActivity(), TabLayoutScreeen.class));
                            Toast.makeText(getActivity(), "Record Submitted", Toast.LENGTH_SHORT).show();
                            break;

                    case 201:
                            if(alertDialog!=null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                            Objects.requireNonNull(getActivity()).finishAffinity();
                            startActivity(new Intent(getActivity(), TabLayoutScreeen.class));
                            Toast.makeText(getActivity(), "Record Submitted", Toast.LENGTH_SHORT).show();
                            break;

                    default:
                            if(alertDialog!=null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                            timeOutExceptionMessage();
                            break;
                }
            } catch (Exception ignored) {
            }
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

                AlertDialog.Builder abb = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
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

    interface Communicate{
        void resumeScan();
    }

    // End of Class
}