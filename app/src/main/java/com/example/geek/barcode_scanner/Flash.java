package com.example.geek.barcode_scanner;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Objects;

public class Flash extends DialogFragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Select Flash Mode");
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_flash, container, false);

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
        Switch flashlight=rootView.findViewById(R.id.flash_text);

        try{
            boolean flashstatus = sharedPreferences.getBoolean("flash",false);

            if(flashstatus){
                flashlight.setChecked(true);
                flashlight.setText("Flash is ON");
            }
            else{
                flashlight.setChecked(false);
                flashlight.setText("Flash is OFF");
            }
        }
        catch(Exception e){
            flashlight.setChecked(false);
            flashlight.setText("Flash is OFF");
        }

        flashlight.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if(isChecked){
                Toast.makeText(getActivity(),"Flash is ON ",Toast.LENGTH_LONG).show();
                flashlight.setText("Flash is ON");
                editor=sharedPreferences.edit();
                editor.putBoolean("flash",true);
                editor.apply();
            }
            else{
                Toast.makeText(getActivity(),"Flash is OFF ",Toast.LENGTH_LONG).show();
                flashlight.setText("Flash is OFF");
                editor=sharedPreferences.edit();
                editor.putBoolean("flash",false);
                editor.apply();
            }
        });
        return rootView;
    }

}
