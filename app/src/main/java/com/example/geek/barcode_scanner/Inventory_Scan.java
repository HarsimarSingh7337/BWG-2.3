package com.example.geek.barcode_scanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class Inventory_Scan extends AppCompatActivity {

    private TextInputEditText shelfNumber;
    private TextInputLayout wrapperSelfNumber;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_scan);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        View view = getSupportActionBar().getCustomView();

        wrapperSelfNumber = findViewById(R.id.wrapperselfnumber);
        shelfNumber = findViewById(R.id.selfnumber);

        TextView welcometag = findViewById(R.id.welcome_tag);
        TextView loggedEmpId = findViewById(R.id.logedempid);
        TextView appname = view.findViewById(R.id.actionbar_appname);
        appname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        ImageView nav_back = view.findViewById(R.id.actionbar_backbutton);
        ImageView nav_dropdown = view.findViewById(R.id.actionbar_dropdown);
        TextView copyrightText = findViewById(R.id.copyright_text);
        TextView allRightReservedText = findViewById(R.id.allrightsreserved_text);
        TextView version = findViewById(R.id.version_text);

        copyrightText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        allRightReservedText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        version.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));

        sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        loggedEmpId.setText(sharedPreferences.getString("empId", null));
        loggedEmpId.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Medium.ttf"));
        welcometag.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Medium.ttf"));

        nav_back.setOnClickListener(view1 -> this.finish());
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
    }

    public void scanBarcode(View v) {

        if (shelfNumber.getText().toString().trim().length() == 0) {
            wrapperSelfNumber.setError("Field Required");
        } else {
            if(shelfNumber.getText().toString().length() > 10){
                Toast.makeText(getApplicationContext(),"Invalid Length",Toast.LENGTH_LONG).show();
            }
            else{
                startActivity(new Intent(getApplicationContext(), Barcode_Scanner_WM.class).putExtra("shelfNumber", shelfNumber.getText().toString()).putExtra("loc", "inventory"));
            }
        }
    }
    //End of Class
}