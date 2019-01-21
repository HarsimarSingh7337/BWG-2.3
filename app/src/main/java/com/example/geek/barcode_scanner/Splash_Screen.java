package com.example.geek.barcode_scanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Objects;

public class Splash_Screen extends AppCompatActivity {

    public static final String IP = "ip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        TextView copyrightText = findViewById(R.id.copyright_text);
        TextView allRightReservedText = findViewById(R.id.allrightsreserved_text);
        TextView version = findViewById(R.id.version_text);

        copyrightText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        allRightReservedText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));
        version.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf"));

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            SharedPreferences sharedPreferences = getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
            boolean isLoogedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            try {
                if (isLoogedIn || Objects.requireNonNull(sharedPreferences.getString("ip", null)).equals("")) {
                    startActivity(new Intent(getApplicationContext(), TabLayoutScreeen.class));
                    this.finish();
                } else {
                    startActivity(new Intent(getApplicationContext(), Login_Screen.class));
                    this.finish();
                }
            } catch (NullPointerException npe) {
                startActivity(new Intent(getApplicationContext(), Login_Screen.class));
                this.finish();
            }
        }, 3500);
    }
}