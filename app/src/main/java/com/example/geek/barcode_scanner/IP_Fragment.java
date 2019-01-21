package com.example.geek.barcode_scanner;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

public class IP_Fragment extends android.support.v4.app.DialogFragment {

    private TextInputLayout wrapperIP;
    private TextInputEditText IPAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Set IP Address");
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_ip_, container, false);

        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        wrapperIP = rootView.findViewById(R.id.wrapperIpAddress);
        IPAddress = rootView.findViewById(R.id.ipaddress);
        Button submit = rootView.findViewById(R.id.submit);
        Button cancel = rootView.findViewById(R.id.cancel);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
        IPAddress.setText(sharedPreferences.getString("ip", null));

        cancel.setOnClickListener(view -> getDialog().dismiss());

        submit.setOnClickListener(view -> {

            if (IPAddress.getText().toString().trim().length() == 0) {
                wrapperIP.setError("Invalid Field");
            } else {
                Toast.makeText(getActivity(), "IP set Successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putString(Splash_Screen.IP, IPAddress.getText().toString());
                editor.apply();
                getDialog().dismiss();
            }
        });
        return rootView;
    }

}
