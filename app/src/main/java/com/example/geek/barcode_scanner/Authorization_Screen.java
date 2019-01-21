package com.example.geek.barcode_scanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;
import java.util.Random;

public class Authorization_Screen extends DialogFragment {

    private TextInputLayout wrapperOtpField;
    private TextInputEditText otpField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_otp, container, false);
        getDialog().setTitle("Authenticate New User");
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);

        wrapperOtpField = rootView.findViewById(R.id.wrapperotpfield);
        otpField = rootView.findViewById(R.id.otpfield);
        Button submitOTP = rootView.findViewById(R.id.submitOTP);

        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Login_Screen.MY_SHARED_PREFERENCES, 0);
        String empId = sharedPreferences.getString("empId", null);

        Random random = new Random();
        int x = random.nextInt(10000);
        String message = "The One-TimePassword(OTP) for Emp_Id " + empId + " is: " + x;

        //Creating com.example.geek.barcode_scanner.SendMail object
        String subject = "OTP Bookswagon App";
        String email = "srecommerce@gmail.com";


        submitOTP.setOnClickListener(view -> {
            if (otpField.getText().toString().trim().length() == 0) {
                wrapperOtpField.setError("Invalid Field");
            } else {
                //matching OTP and then proceed to next Screen
                String OTP = otpField.getText().toString();

                ProgressDialog pd = new ProgressDialog(getActivity());
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setIndeterminate(true);
                pd.setMessage("Checking OTP...");
                pd.setCancelable(false);
                pd.show();

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (OTP.equals(String.valueOf(x))) {
                        //OTP Matched here
                        pd.dismiss();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                        startActivity(new Intent(getActivity(), Home_Screen.class));
                    } else {
                        //OTP not matched
                        Toast.makeText(getActivity(), "Wrong OTP", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }, 2000);
            }
        });

        return rootView;
    }

}