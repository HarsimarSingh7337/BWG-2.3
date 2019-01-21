package com.example.geek.barcode_scanner;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Add_More extends android.support.v4.app.DialogFragment {

    private TextView shelfRetrived;
    private Handler handler;
    Communicatee communicatee;

    private Runnable runnable= () -> {
        try{
            Dialog dialog = getDialog();
            if(dialog!=null){
                dialog.dismiss();
            }
        }
        catch(Exception ignored){
        }
      //  startActivity(new Intent(getActivity(), Barcode_Scanner.class).putExtra("loc", "addmore").putExtra("shelfNumber", shelfRetrived.getText().toString()).putExtra("launchBy", "addmore"));
      //  Add_More.this.finish();
      //  handler.removeCallbacks(runnable);
      //  handler=null;
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            communicatee = (Communicatee) context;
        }
        catch(ClassCastException ignored){ }
    }

    @Override
    public void onDetach() {
        communicatee.resumeScannerr();
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
        View rootView = inflater.inflate(R.layout.activity_add__more,container,true);

        communicatee = (Communicatee) getActivity();
        //SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
        Button btn_finish = rootView.findViewById(R.id.btn_finish);
        //TextView loggedEmpId = rootView.findViewById(R.id.logedempid);
        //loggedEmpId.setText(sharedPreferences.getString("empId", null));
        //loggedEmpId.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Medium.ttf"));

        TextView isbnRetrieved = rootView.findViewById(R.id.isbntextview);
        shelfRetrived = rootView.findViewById(R.id.shelftextview);

        isbnRetrieved.setText(Objects.requireNonNull(getArguments()).getString("isbn"));
        shelfRetrived.setText(getArguments().getString("shelfnumber"));

        String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(isbnRetrieved.getText().toString());

        handler=new Handler();
        if(matcher.matches()){
            try {
                Home_Screen.cursor = Home_Screen.database.rawQuery("select QTY from scandata where ISBN ='" + isbnRetrieved.getText().toString().trim() + "'", null);
                if (Home_Screen.cursor.moveToFirst()) {
                    // this will execute when same isbn is scanned multiple time, increment qty by getting previous from database
                    int qty = Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"));
                    qty += 1;
                    String query = "update scandata set QTY='" + qty + "' where ISBN='" + isbnRetrieved.getText().toString().trim() + "'";
                    Home_Screen.database.execSQL(query);
                }
                else{
                    // when new isbn is scanned, store it in database with qty=1
                    int qty = 1;
                    String query = "insert into scandata (ISBN,QTY) values ('" + isbnRetrieved.getText().toString().trim() + "','" + qty + "')";
                    Home_Screen.database.execSQL(query);
                }
            }
            catch (Exception ignored) {
            }
            finally {
                if(Home_Screen.cursor!=null){
                    Home_Screen.cursor.close();
                }
            }
            handler.postDelayed(runnable,1500);
        }
        else{
            // ISBN not matched with the given pattern
            AlertDialog.Builder ab=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            ab.setCancelable(false);
            ab.setTitle("Wrong  ISBN...");
            ab.setMessage("This is not a valid ISBN");
            ab.setNeutralButton("OK", (dialog, which) -> {
                dialog.dismiss();
                handler.postDelayed(runnable,1500);
            });
            ab.create().show();
        }

        btn_finish.setOnClickListener(v -> {

            handler.removeCallbacks(runnable);
            android.support.v4.app.FragmentManager fragmentManager =getActivity().getSupportFragmentManager();
            fragment_finish_show_options fragmentFinishShowOptions = new fragment_finish_show_options();
            fragmentFinishShowOptions.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            Bundle bundle = new Bundle();
            bundle.putString("shelf", shelfRetrived.getText().toString());
            fragmentFinishShowOptions.setArguments(bundle);
            fragmentFinishShowOptions.show(fragmentManager, "");
        });

        return rootView;
    }

  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__more);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        View view = getSupportActionBar().getCustomView();

        SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        TextView loggedEmpId = findViewById(R.id.logedempid);
        loggedEmpId.setText(sharedPreferences.getString("empId", null));
        loggedEmpId.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Medium.ttf"));
        TextView copyrightText = findViewById(R.id.copyright_text);
        TextView allRightReservedText = findViewById(R.id.allrightsreserved_text);
        TextView version = findViewById(R.id.version_text);

        copyrightText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        allRightReservedText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        version.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        TextView appname = view.findViewById(R.id.actionbar_appname);
        appname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        ImageView nav_back = view.findViewById(R.id.actionbar_backbutton);
        ImageView nav_dropdown = view.findViewById(R.id.actionbar_dropdown);
        nav_dropdown.setVisibility(View.INVISIBLE);
        nav_back.setOnClickListener(view1 -> {
            if(handler!=null){
                handler.removeCallbacks(runnable);
                handler=null;
            }
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(" Are You sure you want to remove scanned results(if any) ?");
            ab.setPositiveButton("Yes", (dialogInterface, i) -> {
                try {
                    startActivity(new Intent(getApplicationContext(), TabLayoutScreeen.class));
                    finishAffinity();
                } catch (Exception ignored) {
                }
            });
            ab.setNegativeButton("No", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                handler=new Handler();
                handler.postDelayed(runnable,1500);
            });
            ab.create();
            ab.show();
        });

        TextView isbnRetrieved = findViewById(R.id.isbntextview);
        shelfRetrived = findViewById(R.id.shelftextview);

        Bundle bundle = getIntent().getExtras();
        isbnRetrieved.setText(Objects.requireNonNull(bundle).getString("isbn"));
        shelfRetrived.setText(bundle.getString("shelfnumber"));

        String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(isbnRetrieved.getText().toString());

        if(matcher.matches()){
            try {
                Home_Screen.cursor = Home_Screen.database.rawQuery("select QTY from scandata where ISBN ='" + isbnRetrieved.getText().toString().trim() + "'", null);
                if (Home_Screen.cursor.moveToFirst()) {
                    // this will execute when same isbn is scanned multiple time, increment qty by getting previous from database
                    int qty = Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"));
                    qty += 1;
                    String query = "update scandata set QTY='" + qty + "' where ISBN='" + isbnRetrieved.getText().toString().trim() + "'";
                    Home_Screen.database.execSQL(query);
                }
                else{
                    // when new isbn is scanned, store it in database with qty=1
                    int qty = 1;
                    String query = "insert into scandata (ISBN,QTY) values ('" + isbnRetrieved.getText().toString().trim() + "','" + qty + "')";
                    Home_Screen.database.execSQL(query);
                }
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Exception"+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error",e.getMessage());
            }
            finally {
                if(Home_Screen.cursor!=null){
                    Home_Screen.cursor.close();
                }
            }

            handler=new Handler();
            handler.postDelayed(runnable,1500);
        }
        else{
            // ISBN not matched with the given pattern
            AlertDialog.Builder ab=new AlertDialog.Builder(Add_More.this);
            ab.setCancelable(false);
            ab.setTitle("Wrong  ISBN...");
            ab.setMessage("This is not a valid ISBN");
            ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    handler=new Handler();
                    handler.postDelayed(runnable,1500);
                }
            });
            ab.create().show();
        }
    }*/

   /* public void finish(View v) {

        handler.removeCallbacks(runnable);
        handler=null;
        FragmentManager fragmentManager = getFragmentManager();
        fragment_finish_show_options fragmentFinishShowOptions = new fragment_finish_show_options();
        fragmentFinishShowOptions.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
        Bundle bundle = new Bundle();
        bundle.putString("shelf", shelfRetrived.getText().toString());
        fragmentFinishShowOptions.setArguments(bundle);
        fragmentFinishShowOptions.show(fragmentManager, "");
    }*/

   /* @Override
    public void onBackPressed() {
        if(handler!=null){
            handler.removeCallbacks(runnable);
            handler=null;
        }
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage(" Are You sure you want to remove scanned results(if any) ?");
        ab.setCancelable(false);
        ab.setPositiveButton("Yes", (dialogInterface, i) -> {
            try {
                startActivity(new Intent(getApplicationContext(), TabLayoutScreeen.class));
                finishAffinity();
            } catch (Exception ignored) {
            }
        });
        ab.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            handler=new Handler();
            handler.postDelayed(runnable,1500);

        });
        ab.create();
        ab.show();
    }*/

    interface Communicatee{
        void resumeScannerr();
    }
}

class ISBN {
    String isbn;
    int qty;
}