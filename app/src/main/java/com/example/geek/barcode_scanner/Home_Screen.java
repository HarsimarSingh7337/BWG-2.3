package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

public class Home_Screen extends Fragment {

    private ImageView checkscimage;
    private int code = 500;
    private ProgressBar progressBar;
    private String password,empID,ip;
    public static SQLiteDatabase database;
    public static Cursor cursor,cursor1;
    private MAsyncTask mAsyncTask;
    private Handler handler;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_home_screen, container, false);

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        password = sharedPreferences.getString("password", null);
        empID= sharedPreferences.getString("empId",null);
        ip= sharedPreferences.getString("ip",null);

        TextView loggedEmpId = view.findViewById(R.id.logedempid);
        loggedEmpId.setText(empID);

        TextView welcometag = view.findViewById(R.id.welcome_tag);
        TextView copyrightText = view.findViewById(R.id.copyright_text);
        TextView allRightReservedText = view.findViewById(R.id.allrightsreserved_text);
        TextView version = view.findViewById(R.id.version_text);

        copyrightText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Bold.ttf"));
        allRightReservedText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Bold.ttf"));
        version.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Bold.ttf"));

        checkscimage = view.findViewById(R.id.checkscimage);
        progressBar = view.findViewById(R.id.pbar);

        loggedEmpId.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Medium.ttf"));
        welcometag.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Medium.ttf"));

        //creating database connection
        database = getActivity().openOrCreateDatabase("warehouse", MODE_PRIVATE, null);
        database.execSQL("Create table if not Exists scandata(ISBN varchar(20),QTY int);");
        database.execSQL("Create table if not Exists serverdata(INVOICE varchar(20), ISBN varchar(20), QTY int, SCANNEDQTY int);");

        Button inventoryScan=view.findViewById(R.id.btn_inventory_scan);
        Button search=view.findViewById(R.id.btn_search);
        Button inwardScan=view.findViewById(R.id.btn_inward_scan);
        Button sale=view.findViewById(R.id.btn_sale);

        inventoryScan.setOnClickListener(v -> startActivity(new Intent(getActivity(), Inventory_Scan.class)));

        search.setOnClickListener(v -> {
            android.support.v4.app.FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
            Search_ISBN_Options searchIsbnOptions = new Search_ISBN_Options();
            searchIsbnOptions.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            searchIsbnOptions.show(fragmentManager, "");
        });

        inwardScan.setOnClickListener(v -> {
            android.support.v4.app.FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
            Inward_Select_Vendor inwardSelectVendor=new Inward_Select_Vendor();
            inwardSelectVendor.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            inwardSelectVendor.show(fragmentManager,"");
        });

        sale.setOnClickListener(v -> startActivity(new Intent(getActivity(), Barcode_Scanner_WM.class).putExtra("loc", "salesearch")));
        return view;
    }

    private Runnable runnable=new Runnable() {
        @Override
        public void run(){
            try{
                mAsyncTask = new MAsyncTask();
                mAsyncTask.execute();
                handler.postDelayed(runnable,4000);
            }
            catch(Exception ignored){}
        }
    };

    public void onResume(){
        super.onResume();

        String query = "delete from scandata";
        String queryy = "delete from serverdata";
        database.execSQL(query);
        database.execSQL(queryy);

        Search_ISBN_Fragment.lisbn.clear();
        Search_ISBN_Fragment.lshelf.clear();
        Search_ISBN_Fragment.lqty.clear();

        handler = new Handler();
        handler.postDelayed(runnable,1000);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mAsyncTask!=null){
            mAsyncTask.cancel(true);
        }
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

      @SuppressLint("StaticFieldLeak")
      class MAsyncTask extends AsyncTask<Void, Void, Void>{

        /*private int cod=500;
        private WeakReference<String> empIdWeakReference;
        private WeakReference<String> passwordWeakReference;
        private WeakReference<String> ipWeakReference;
        private WeakReference<ProgressBar> progressBarWeakReference;
        private WeakReference<ImageView> imageViewWeakReference;

            MAsyncTask(String empId, String password, String ip, ProgressBar pbar,ImageView im){
                this.empIdWeakReference=new WeakReference<>(empId);
                this.passwordWeakReference=new WeakReference<>(password);
                this.ipWeakReference=new WeakReference<>(ip);
                this.progressBarWeakReference=new WeakReference<>(pbar);
                this.imageViewWeakReference=new WeakReference<>(im);
        }*/
        @Override
        protected Void doInBackground(Void... voids) {

            SyncHttpClient httpClient = new SyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(2,1000);
            httpClient.addHeader("temp","isConnected");
            httpClient.head("http://" + sharedPreferences.getString("ip",null) + ":8080/com.bookswagon/service/bookswagon/check/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    code = statusCode;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    code = statusCode;
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            try {
                if (code == 200) {
                    progressBar.setVisibility(View.INVISIBLE);
                    checkscimage.setImageResource(R.drawable.sc);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    checkscimage.setImageResource(R.drawable.snc);
                }
            } catch (Exception ignored){ }
        }
    }
//End of Class
}