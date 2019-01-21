package com.example.geek.barcode_scanner;


import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

public class BWG_Sieve_Insert_Tracking_ID extends android.support.v4.app.DialogFragment {

    private TextInputLayout wrapperId;
    private TextInputEditText trackingId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_bwg__sieve__insert__tracking__id, container, false);

        getDialog().setTitle("Insert Tracking ID");
        getDialog().setCanceledOnTouchOutside(false);
        wrapperId=rootView.findViewById(R.id.wrappertrackingid);
        trackingId=rootView.findViewById(R.id.trackingid);
        Button submit = rootView.findViewById(R.id.submitbtn);
        Button cancel = rootView.findViewById(R.id.cancelbtn);

        submit.setOnClickListener(v -> {

            if(trackingId.getText().toString().trim().length()==0){
                wrapperId.setError("Invalid Field");
            }
            else{
                if(trackingId.getText().toString().length() > 15){
                    Toast.makeText(getActivity(),"Invalid Length",Toast.LENGTH_LONG).show();
                }
                else{
                    FragmentManager fragmentManager=Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                    BWG_Sieve_Cancelled_ID bwg_sieve_cancelled_id=new BWG_Sieve_Cancelled_ID();
                    Bundle bundle=new Bundle();
                    bundle.putString("val",trackingId.getText().toString());
                    bwg_sieve_cancelled_id.setArguments(bundle);
                    bwg_sieve_cancelled_id.show(fragmentManager,"");
                    //startActivity(new Intent(getActivity(),BWG_Sieve_Cancelled_ID.class).putExtra("val",trackingId.getText().toString()));
                    trackingId.setText("");
                }
            }
        });
        cancel.setOnClickListener(v -> getDialog().dismiss());
        return  rootView;
    }
}
