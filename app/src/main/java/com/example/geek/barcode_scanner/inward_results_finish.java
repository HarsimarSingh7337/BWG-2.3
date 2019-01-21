package com.example.geek.barcode_scanner;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class inward_results_finish extends DialogFragment {

    private communicateWithParentActivity communicate;
    private String vendor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Select an Option");
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);

        View rootView = inflater.inflate(R.layout.fragment_inward_results_finish, container, false);
        vendor = getArguments().getString("vendor");

        Button showMyResult = rootView.findViewById(R.id.showmyresult);
        Button showMyList = rootView.findViewById(R.id.showmylist);
        this.communicate = (communicateWithParentActivity) getActivity();

        showMyList.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(),inward_results_showMyList.class).putExtra("vendor",vendor));
        });

        showMyResult.setOnClickListener(view -> {
          /*  new MyAsync().execute();
         //   startActivity(new Intent(getActivity(),inward));
            android.app.FragmentManager fragmentManager = getFragmentManager();
            inward_results_showMyResult inward_results_showMyResult = new inward_results_showMyResult();
            Bundle bundlee = new Bundle();
            bundlee.putString("invoice", invoice);
            inward_results_showMyResult.setArguments(bundlee);
            inward_results_showMyResult.show(fragmentManager, "");*/

        });
        return rootView;
    }

    public interface communicateWithParentActivity {
        void fragmentDetached();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.communicate = (communicateWithParentActivity) context;
            Log.e("Fragment", "Attached");
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement communicateWithParentActivity");
        }
    }

    @Override
    public void onDetach() {
        communicate.fragmentDetached();
        super.onDetach();
    }
}
