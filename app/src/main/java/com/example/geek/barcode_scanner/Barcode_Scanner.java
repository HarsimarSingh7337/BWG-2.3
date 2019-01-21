package com.example.geek.barcode_scanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.androidhive.barcode.BarcodeReader;

@SuppressWarnings("StatementWithEmptyBody")
public class Barcode_Scanner extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener,
        Add_More.Communicatee,fragment_finish_show_options.CommunicatewithScanner {

    private BarcodeReader barcodeReader;
    private String shelfNumber="";
    private String loc="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        boolean flashStatus = sharedPreferences.getBoolean("flash", false);

       // View view;
        if (flashStatus) {
            //flash is on
            setContentView(R.layout.barcode_scanner1);
            Objects.requireNonNull(getSupportActionBar()).hide();
            /*Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
            view = getSupportActionBar().getCustomView();*/
        } else {
            // flash is off
            setContentView(R.layout.barcode_scanner);
            Objects.requireNonNull(getSupportActionBar()).hide();
           /* Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
            view = getSupportActionBar().getCustomView();*/
        }

        LinearLayout scanCountContainer = findViewById(R.id.scan_count_container);
        scanCountContainer.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        shelfNumber = Objects.requireNonNull(bundle).getString("shelfNumber");
        loc = bundle.getString("loc");
        //String invoice = bundle.getString("invoice");
       // String vendor = bundle.getString("vendor");

       /* ImageView sout= view.findViewById(R.id.actionbar_dropdown);
        sout.setVisibility(View.INVISIBLE);

        ImageView option = view.findViewById(R.id.actionbar_option);
        option.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu2, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()){
                    case R.id.menu_finishScanning:
                        barcodeReader.pauseScanning();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragment_finish_show_options fragmentFinishShowOptions = new fragment_finish_show_options();
                        fragmentFinishShowOptions.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("shelf",shelfNumber);
                        fragmentFinishShowOptions.setArguments(bundle1);
                        fragmentFinishShowOptions.show(fragmentManager, "");
                        break;
                    case R.id.menu_exitToHome:
                        barcodeReader.pauseScanning();
                        AlertDialog.Builder ab=new AlertDialog.Builder(Barcode_Scanner.this);
                        ab.setCancelable(false);
                        ab.setMessage("Scanned Results will be Cleared (if any).");
                        ab.setTitle("Sure to Exit ?");
                        ab.setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            barcodeReader.resumeScanning();
                        });
                        ab.setPositiveButton("Ok", (dialog, which) -> {
                            startActivity(new Intent(getApplicationContext(),TabLayoutScreeen.class));
                            finishAffinity();
                        });
                        ab.create().show();
                        break;
                }
                return true;
            });
            popupMenu.show();
        });*/
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        barcodeReader.pauseScanning();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragment_finish_show_options fragmentFinishShowOptions = new fragment_finish_show_options();
        fragmentFinishShowOptions.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
        Bundle bundle1 = new Bundle();
        bundle1.putString("shelf",shelfNumber);
        fragmentFinishShowOptions.setArguments(bundle1);
        fragmentFinishShowOptions.show(fragmentManager, "");
    }

    @Override
    public void onScanned(Barcode barcode) {
        if(barcode.displayValue.length()==10 || barcode.displayValue.length()==13 ){
            String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(barcode.displayValue);
            if(matcher.matches()){
                barcodeReader.playBeep();
                switch (loc) {
                    case "empty":
                        android.support.v4.app.FragmentManager fragmentManager=getSupportFragmentManager();
                        Add_More add_more=new Add_More();
                        Bundle bundle=new Bundle();
                        bundle.putString("isbn",barcode.displayValue);
                        bundle.putString("shelfnumber",shelfNumber);
                        add_more.setArguments(bundle);
                        add_more.show(fragmentManager,"");
                        barcodeReader.pauseScanning();
                        //startActivity(new Intent(getApplicationContext(), Add_More.class).putExtra("isbn", barcode.displayValue).putExtra("shelfnumber", shelfNumber));
                        //finish();
                        break;
                    /*case "addmore":
                        startActivity(new Intent(getApplicationContext(), Add_More.class).putExtra("isbn",barcode.displayValue).putExtra("shelfnumber", shelfNumber));
                        finish();
                        break;*/
                    default:
                        Toast.makeText(getApplicationContext(), "Invalid Result", Toast.LENGTH_SHORT).show();
                        Log.e("Invalid Result: ", barcode.displayValue);
                        break;
                }
            }
            else{
                        // wrong output of barcode
            }
        }
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
    }

    @Override
    public void onScanError(String errorMessage){
        Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraPermissionDenied(){
        Toast.makeText(getApplicationContext(),"Camera Permission Denied",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumeScannerr() {
        barcodeReader.resumeScanning();
    }

    @Override
    public void acknowledgeScanner() {
        barcodeReader.resumeScanning();
    }

    // End of Class
}