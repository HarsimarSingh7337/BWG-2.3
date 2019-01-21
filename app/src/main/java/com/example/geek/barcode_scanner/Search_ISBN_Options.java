package com.example.geek.barcode_scanner;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;


public class Search_ISBN_Options extends android.support.v4.app.DialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search__isbn__options, container, false);
        getDialog().setTitle("Choose an Option");
        getDialog().setCanceledOnTouchOutside(false);

        Button manualSearch = rootView.findViewById(R.id.manualSearch);
        Button scannerSearch = rootView.findViewById(R.id.scannerSearch);

        manualSearch.setOnClickListener(view -> {
            android.support.v4.app.FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
            Search_ISBN_Fragment searchOptionFragment = new Search_ISBN_Fragment();
            searchOptionFragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
            searchOptionFragment.show(fragmentManager, "");
        });

        scannerSearch.setOnClickListener(view -> startActivity(new Intent(getActivity(), Barcode_Scanner_WM.class).putExtra("loc", "search")));

        return rootView;
    }
}