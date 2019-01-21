package com.example.geek.barcode_scanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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

public class Barcode_Scanner_WM extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener,
        Inventory_Scan_Results.Communicate {

    private BarcodeReader barcodeReader;
    private String shelfNumber="";
    private String loc="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        boolean flashStatus = sharedPreferences.getBoolean("flash", false);

        //View view;
        if (flashStatus) {
            //flash is on
            setContentView(R.layout.barcode_scanner1);
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            // flash is off
            setContentView(R.layout.barcode_scanner);
            Objects.requireNonNull(getSupportActionBar()).hide();
        }

        LinearLayout scanCountContainer = findViewById(R.id.scan_count_container);
        scanCountContainer.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        shelfNumber = Objects.requireNonNull(bundle).getString("shelfNumber");
        loc = bundle.getString("loc");
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
    public void onScanned(Barcode barcode) {
        if(barcode.displayValue.length()==10 || barcode.displayValue.length()==13 ){
            String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(barcode.displayValue);
            if(matcher.matches()){
                barcodeReader.playBeep();
                switch (loc) {
                    case "inventory":
                        FragmentManager fragmentManager=getSupportFragmentManager();
                        Inventory_Scan_Results inventory_scan_results=new Inventory_Scan_Results();
                        Bundle bundle=new Bundle();
                        bundle.putString("isbn",barcode.displayValue);
                        bundle.putString("shelfnumber",shelfNumber);
                        inventory_scan_results.setArguments(bundle);
                        inventory_scan_results.show(fragmentManager,"");
                        barcodeReader.pauseScanning();
                        // startActivity(new Intent(getApplicationContext(), Inventory_Scan_Results.class).putExtra("isbn", barcode.displayValue).putExtra("shelfnumber", shelfNumber));
                        break;
                    case "salesearch":
                        startActivity(new Intent(getApplicationContext(), Searched_ISBN_Sale.class).putExtra("isbn", barcode.displayValue));
                        finish();
                        break;
                    case "search":
                        startActivity(new Intent(getApplicationContext(), Search_Result_By_Scanner.class).putExtra("isbn", barcode.displayValue));
                        finish();
                        break;

                    default:
                        break;
                }
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
    public void onScanError(String errorMessage) {
    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(getApplicationContext(),"Camera Permission Denied",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumeScan() {
        barcodeReader.resumeScanning();
    }
}
