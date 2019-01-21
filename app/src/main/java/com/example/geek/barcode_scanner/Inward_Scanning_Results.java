package com.example.geek.barcode_scanner;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inward_Scanning_Results extends AppCompatDialogFragment {

    private TextView invoiceText;
    private String vendor="";
    private Handler handler;
    Communicate communicate;
    public static ArrayList<String> notMatchedISBNList = new ArrayList<>();

    private Runnable runnable= () -> {
        try{
            Dialog dialog = getDialog();
            if(dialog!=null){
                dialog.dismiss();
            }
        }
        catch(Exception ignored){}
       // startActivity(new Intent(getApplicationContext(), Barcode_Scanner2.class).putExtra("loc", "inwardagain").putExtra("invoice", invoiceText.getText().toString()).putExtra("vendor",vendor));
       // Inward_Scanning_Results.this.finish();
       // handler.removeCallbacks(runnable);
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            communicate= (Communicate) context;
        }
        catch (ClassCastException ignored){ }
    }

    @Override
    public void onDetach() {
        communicate.resumeScanner();
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.activity_inward__scanning__results,container,false);

        communicate = (Communicate) getActivity();
        Button finishBtn = rootView.findViewById(R.id.finishbtn);
        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);

        invoiceText =rootView.findViewById(R.id.invoicetextview);
        TextView isbn = rootView.findViewById(R.id.isbntextview);

        invoiceText.setText(Objects.requireNonNull(getArguments()).getString("invoice"));
        isbn.setText(getArguments().getString("isbn"));
        vendor = getArguments().getString("vendor");

        String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(isbn.getText().toString());

        handler=new Handler();
        if(matcher.matches()){
            try {
                Home_Screen.cursor1=Home_Screen.database.rawQuery("select INVOICE,QTY from serverdata where ISBN='"+isbn.getText().toString()+"' ",null);
                if(Home_Screen.cursor1.moveToNext()){

                    Home_Screen.cursor = Home_Screen.database.rawQuery("select * from scandata where ISBN ='" + isbn.getText().toString().trim() + "'", null);
                    if (Home_Screen.cursor.moveToFirst()) {
                        int qty =Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"));  // got quantity of current isbn from database
                        qty+=1;
                        String queryy = "update scandata set QTY ='"+qty+"' where ISBN = '"+isbn.getText().toString()+"'";
                        Home_Screen.database.execSQL(queryy);

                    } else {
                        int qty = 1;
                        String query = "insert into scandata (ISBN,QTY) values ('" + isbn.getText().toString().trim() + "','" + qty + "')";
                        Home_Screen.database.execSQL(query);
                    }
                    handler.postDelayed(runnable,1500);
                }
                else{
                    notMatchedISBNList.add(isbn.getText().toString());
                   /* AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                    ab.setCancelable(false);
                    ab.setTitle("Warning!!!");
                    ab.setMessage("This ISBN is not found in given Invoice(s)");
                    ab.setNeutralButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        handler.postDelayed(runnable,1500);
                    });
                    ab.create().show();*/
                    handler.postDelayed(runnable,1800);
                    Toast.makeText(getActivity(),"ISBN not found in List",Toast.LENGTH_LONG).show();
                }
            } catch (Exception ignored) {
            } finally {
                if(Home_Screen.cursor!=null){
                    Home_Screen.cursor.close();
                }
            }
        }
        else{
            // ISBN not matched with the given pattern
            /*AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
            ab.setCancelable(false);
            ab.setTitle("Wrong  ISBN...");
            ab.setMessage("This is not a valid ISBN");
            ab.setNeutralButton("OK", (dialog, which) -> {
                dialog.dismiss();
                handler=new Handler();
                handler.postDelayed(runnable,1500);
            });
            ab.create().show();*/
            Toast.makeText(getActivity(),"Invalid ISBN",Toast.LENGTH_LONG).show();
        }

        finishBtn.setOnClickListener(c ->{
            handler.removeCallbacks(runnable);
            startActivity(new Intent(getActivity(),inward_results_showMyResult.class).putExtra("vendor",vendor).putExtra("invoice",invoiceText.getText().toString()));
        });
        return rootView;
    }

    interface Communicate{
        void resumeScanner();
    }

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inward__scanning__results);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        View view = getSupportActionBar().getCustomView();

        //SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
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
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(" Are You sure you want to remove scanned results ?");
            ab.setPositiveButton("Yes", (dialogInterface, i) -> {
                try {
                    this.finish();
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

        invoiceText = findViewById(R.id.invoicetextview);
        TextView isbn = findViewById(R.id.isbntextview);

        Bundle bundle = getIntent().getExtras();

        invoiceText.setText(Objects.requireNonNull(bundle).getString("invoice"));
        isbn.setText(bundle.getString("isbn"));
        vendor = bundle.getString("vendor");

        String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(isbn.getText().toString());

        if(matcher.matches()){
            try {
                Home_Screen.cursor1=Home_Screen.database.rawQuery("select INVOICE,QTY from serverdata where ISBN='"+isbn.getText().toString()+"' ",null);
                if(Home_Screen.cursor1.moveToNext()){

                    Home_Screen.cursor = Home_Screen.database.rawQuery("select * from scandata where ISBN ='" + isbn.getText().toString().trim() + "'", null);
                    if (Home_Screen.cursor.moveToFirst()) {
                        int qty =Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"));  // got quantity of current isbn from database
                        qty+=1;
                        String queryy = "update scandata set QTY ='"+qty+"' where ISBN = '"+isbn.getText().toString()+"'";
                        Home_Screen.database.execSQL(queryy);

                    } else {
                        int qty = 1;
                        String query = "insert into scandata (ISBN,QTY) values ('" + isbn.getText().toString().trim() + "','" + qty + "')";
                        Home_Screen.database.execSQL(query);
                    }
                    handler=new Handler();
                    handler.postDelayed(runnable,1500);
                }
                else{
                    AlertDialog.Builder ab=new AlertDialog.Builder(Inward_Scanning_Results.this);
                    ab.setCancelable(false);
                    ab.setTitle("Warning!!!");
                    ab.setMessage("This ISBN is not found in given Invoice(s)");
                    ab.setNeutralButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        handler=new Handler();
                        handler.postDelayed(runnable,1500);
                    });
                    ab.create().show();

                }

            } catch (Exception ignored) {
            } finally {
                if(Home_Screen.cursor!=null){
                    Home_Screen.cursor.close();
                }
            }
        }
        else{
            // ISBN not matched with the given pattern
            AlertDialog.Builder ab=new AlertDialog.Builder(Inward_Scanning_Results.this);
            ab.setCancelable(false);
            ab.setTitle("Wrong  ISBN...");
            ab.setMessage("This is not a valid ISBN");
            ab.setNeutralButton("OK", (dialog, which) -> {
                dialog.dismiss();
                handler=new Handler();
                handler.postDelayed(runnable,1500);
            });
            ab.create().show();
        }
    }*/

   /* @Override
    protected void onResume() {
        super.onResume();

        if(temp){
            handler=new Handler();
            handler.postDelayed(runnable,1500);
            temp=false;
        }
    }*/

   /* public void finish(View v){
        handler.removeCallbacks(runnable);
        temp=true;
        startActivity(new Intent(getApplicationContext(),inward_results_showMyResult.class).putExtra("vendor",vendor).putExtra("invoice",invoiceText.getText().toString()));
    }*/

   /* @Override
    protected void onPause() {
        super.onPause();
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }
    }*/
    // End of Class
}