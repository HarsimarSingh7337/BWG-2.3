package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;
import java.util.Objects;

import info.androidhive.barcode.BarcodeReader;

public class Barcode_Scanner_Sieve extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener,
        BWG_Sieve_Scan_New_Results.CommunicationWithParent,
        BWG_Sieve_Removed_ID.CommunicationWithParentt {

    private BarcodeReader barcodeReader;
    private String loc="";
    private TextView scannedCount;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        boolean flashStatus = sharedPreferences.getBoolean("flash", false);

        if (flashStatus){
            //flash is on
            setContentView(R.layout.barcode_scanner1);
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            // flash is off
            setContentView(R.layout.barcode_scanner);
            Objects.requireNonNull(getSupportActionBar()).hide();
        }

        Bundle bundle = getIntent().getExtras();
        loc = Objects.requireNonNull(bundle).getString("loc");
        scannedCount = findViewById(R.id.scanned_count);
        LinearLayout scanCountContainer = findViewById(R.id.scan_count_container);

        if(Objects.requireNonNull(loc).equals("new")){
            scannedCount.setVisibility(View.VISIBLE);
            scanCountContainer.setVisibility(View.VISIBLE);
            scannedCount.setText("Scanned Count: "+String.valueOf(BWG_Sieve_Scan_New_Results.list.size()));
        }
        else{
            scannedCount.setVisibility(View.GONE);
            scanCountContainer.setVisibility(View.GONE);
        }
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeReader.pauseScanning();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeReader.resumeScanning();
    }

    @Override
    public void onScanned(Barcode barcode) {
        barcodeReader.playBeep();

        switch (loc) {
            case "new":
                android.support.v4.app.FragmentManager fragmentManager=getSupportFragmentManager();
                BWG_Sieve_Scan_New_Results bwg_sieve_scan_new_results=new BWG_Sieve_Scan_New_Results();
                Bundle bundle=new Bundle();
                bundle.putString("val",barcode.displayValue);
                bwg_sieve_scan_new_results.setArguments(bundle);
                bwg_sieve_scan_new_results.show(fragmentManager,"");
                barcodeReader.pauseScanning();
                //startActivity(new Intent(getApplicationContext(), BWG_Sieve_Scan_New_Results.class).putExtra("val", barcode.displayValue));
                break;

            case "remove":
               // startActivity(new Intent(getApplicationContext(), BWG_Sieve_Removed_ID.class).putExtra("val", barcode.displayValue));
                android.support.v4.app.FragmentManager fragmentManager1 =getSupportFragmentManager();
                BWG_Sieve_Removed_ID bwg_sieve_removed_id=new BWG_Sieve_Removed_ID();
                Bundle bundle1=new Bundle();
                bundle1.putString("val",barcode.displayValue);
                bwg_sieve_removed_id.setArguments(bundle1);
                bwg_sieve_removed_id.show(fragmentManager1,"");
                barcodeReader.pauseScanning();
                break;

            default:
                break;
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
    public void acknowledgeScanner() {
        barcodeReader.resumeScanning();
        scannedCount.setText("Scanned Count: "+String.valueOf(BWG_Sieve_Scan_New_Results.list.size()));
    }

    @Override
    public void acknowledgeScannerr() {
        barcodeReader.resumeScanning();
    }
}