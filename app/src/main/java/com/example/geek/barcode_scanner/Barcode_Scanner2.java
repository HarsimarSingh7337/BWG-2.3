package com.example.geek.barcode_scanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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

public class Barcode_Scanner2 extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener,
        Inward_Scanning_Results.Communicate,inward_scanner_options.Communicate {

    private BarcodeReader barcodeReaderr;
    private String loc="";
    private String invoice="";
    private String vendor="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        boolean flashStatus = sharedPreferences.getBoolean("flash", false);

        View view;
        if (flashStatus) {
            //flash is on
            setContentView(R.layout.barcode_scanner1);
            Objects.requireNonNull(getSupportActionBar()).hide();
            /*Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
            view = getSupportActionBar().getCustomView();*/
        } else{
            // flash is off
            setContentView(R.layout.barcode_scanner);
            Objects.requireNonNull(getSupportActionBar()).hide();
            /*Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
            view = getSupportActionBar().getCustomView();*/
        }
        Bundle bundle = getIntent().getExtras();
        String shelfNumber = Objects.requireNonNull(bundle).getString("shelfNumber");
        loc = bundle.getString("loc");
        invoice = bundle.getString("invoice");
        vendor=bundle.getString("vendor");

        LinearLayout scanCountContainer = findViewById(R.id.scan_count_container);
        scanCountContainer.setVisibility(View.GONE);

       /* ImageView sout= view.findViewById(R.id.actionbar_dropdown);
        sout.setVisibility(View.INVISIBLE);

        ImageView option = view.findViewById(R.id.actionbar_option);
        option.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu3, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()){
                    case R.id.menu_showResult:
                        barcodeReaderr.pauseScanning();
                        startActivity(new Intent(getApplicationContext(),inward_results_showMyResult.class).putExtra("vendor",vendor).putExtra("invoice",invoice));
                        break;

                    case R.id.menu_exit:
                        barcodeReaderr.pauseScanning();
                        AlertDialog.Builder ab=new AlertDialog.Builder(Barcode_Scanner2.this);
                        ab.setCancelable(false);
                        ab.setMessage("Scanned Results will be cleared (if any).");
                        ab.setTitle("Sure to Exit ?");
                        ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    barcodeReaderr.resumeScanning();
                            }
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
        barcodeReaderr = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        FragmentManager fragmentManager=getSupportFragmentManager();
        inward_scanner_options inward_scanner_options = new inward_scanner_options();
        Bundle bundle=new Bundle();
        bundle.putString("vendor",vendor);
        bundle.putString("invoice",invoice);
        inward_scanner_options.setArguments(bundle);
        inward_scanner_options.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
        inward_scanner_options.show(fragmentManager,"");
        barcodeReaderr.pauseScanning();
    }

    @Override
    public void onScanned(Barcode barcode) {
        if(barcode.displayValue.length()==10 || barcode.displayValue.length()==13 ){
            String regex = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(barcode.displayValue);
            if(matcher.matches()){

                barcodeReaderr.playBeep();
                switch (loc) {
                    case "inward":
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Inward_Scanning_Results inward_scanning_results=new Inward_Scanning_Results();
                        Bundle bundle=new Bundle();
                        bundle.putString("isbn",barcode.displayValue);
                        bundle.putString("invoice",invoice);
                        bundle.putString("vendor",vendor);
                        inward_scanning_results.setArguments(bundle);
                        inward_scanning_results.show(fragmentManager,"");
                        barcodeReaderr.pauseScanning();
                        //startActivity(new Intent(getApplicationContext(), Inward_Scanning_Results.class).putExtra("isbn", barcode.displayValue).putExtra("invoice", invoice).putExtra("vendor", vendor));
                       // finish();
                        break;
                    /*case "inwardagain":
                        startActivity(new Intent(getApplicationContext(), Inward_Scanning_Results.class).putExtra("isbn", barcode.displayValue).putExtra("invoice", invoice).putExtra("vendor", vendor));
                        finish();
                        break;*/
                    default:
                        Toast.makeText(getApplicationContext(), "Invalid Result", Toast.LENGTH_SHORT).show();
                        Log.e("Invalid Result: ", barcode.displayValue);
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
        Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(getApplicationContext(),"Camera Permission Denied",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumeScanner() {
        barcodeReaderr.resumeScanning();
    }

    @Override
    public void resumeScan() {
        barcodeReaderr.resumeScanning();
    }
}
