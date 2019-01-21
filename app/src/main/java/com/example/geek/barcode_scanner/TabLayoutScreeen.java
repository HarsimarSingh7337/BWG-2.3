package com.example.geek.barcode_scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabLayoutScreeen extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean deviceHasFlash=false;
    private ViewPager viewPager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout_screeen);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar1);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();

        sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);

        TextView appname = view.findViewById(R.id.custom_actionbar1_appname);
        appname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        ImageView nav_dropdown = view.findViewById(R.id.actionbar_dropdown);
        ImageView option = view.findViewById(R.id.actionbar_option);
        nav_dropdown.setOnClickListener(view12 -> {
            PopupMenu pp = new PopupMenu(getApplicationContext(), view12);
            pp.getMenuInflater().inflate(R.menu.popmenu, pp.getMenu());
            pp.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.logout:
                        editor = sharedPreferences.edit();
                        editor.remove("password");
                        editor.remove("isLoggedIn");
                        editor.apply();
                        startActivity(new Intent(getApplicationContext(), Login_Screen.class));
                        finishAffinity();
                        break;
                }
                return true;
            });
            pp.show();
        });

        option.setOnClickListener((View view1) -> {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view1);
            popupMenu.getMenuInflater().inflate(R.menu.menu1, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.setip:
                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        IP_Fragment ip_fragment = new IP_Fragment();
                        ip_fragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                        ip_fragment.show(fragmentManager, "");
                        break;
                    case R.id.flashlight:
                        // flashlight code here
                        checkFlashlightSupport();
                        if(!deviceHasFlash){
                            // flashlight not supportable in device
                            AlertDialog.Builder ab=new AlertDialog.Builder(TabLayoutScreeen.this);
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
                    /*case R.id.file_upload:
                        android.support.v4.app.FragmentManager fragmentManager1 =getSupportFragmentManager();
                        Upload_File_To_Server upload_file_to_server = new Upload_File_To_Server();
                        upload_file_to_server.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                        upload_file_to_server.show(fragmentManager1,"");
                        break;*/
                }
                return true;
            });
            popupMenu.show();
        });

        viewPager=findViewById(R.id.viewPagerContainer);
        setUpViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        @SuppressLint("InflateParams") TextView tabOne=(TextView)getLayoutInflater().inflate(R.layout.customtablayoutfile,null);
        tabOne.setText("Catalog Scanner");
        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(tabOne);

        @SuppressLint("InflateParams") TextView tabTwo=(TextView)getLayoutInflater().inflate(R.layout.customtablayoutfile,null);
        tabTwo.setText("Sieve");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(tabTwo);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Inward_Scanning_Results.notMatchedISBNList.clear();

        try{
            WifiManager wifiManager=(WifiManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.WIFI_SERVICE));
            if (wifiManager != null) {
                if (!wifiManager.isWifiEnabled()) {
                    AlertDialog.Builder ab=new AlertDialog.Builder(TabLayoutScreeen.this);
                    ab.setCancelable(false);
                    ab.setTitle("Alert!!!");
                    ab.setMessage("Kindly Turn On Wi-fi on device ");
                    ab.setNeutralButton("Ok", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    ab.show();
                }
            }
        }
        catch(Exception ignored){ }
    }

    public void setUpViewPager(ViewPager viewPager){

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFrag(new Home_Screen());
        viewPagerAdapter.addFrag(new BWG_Sieve_HomeScreen());
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(viewPagerAdapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(android.support.v4.app.FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add("");
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
            BWG_Sieve_Scan_New_Results.list.clear();
            Inward_Scanning_Results.notMatchedISBNList.clear();
            finishAffinity();
    }

    public void checkFlashlightSupport(){
        deviceHasFlash = getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}