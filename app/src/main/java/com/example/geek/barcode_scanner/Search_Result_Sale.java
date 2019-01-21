package com.example.geek.barcode_scanner;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class Search_Result_Sale extends android.support.v4.app.DialogFragment {

    private communicateWithParentActivity communicate;
    private RecyclerView MyRecyclerView;
    private ArrayList<String> lst = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Select a Rack");
        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.fragment_search__result__sale, container, false);
        this.communicate = (communicateWithParentActivity) getActivity();
        lst = Search_ISBN_Fragment.lisbn;
        MyRecyclerView = rootView.findViewById(R.id.card_recycle);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        RecyclerView.Adapter mAdapter = new MyAdapter(lst);
        MyRecyclerView.setAdapter(mAdapter);

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

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> list;

        MyAdapter(ArrayList<String> Data) {
            this.list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_search_result_sale, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            MyRecyclerView = recyclerView;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.displayisbnvalue.setText(Search_ISBN_Fragment.lisbn.get(position));
            holder.displayqtyvalue.setText(Search_ISBN_Fragment.lqty.get(position));
            holder.displayshelfvalue.setText(Search_ISBN_Fragment.lshelf.get(position));
        }

        @Override
        public int getItemCount() {
            return lst.size();
        }
    }

     class MyViewHolder extends RecyclerView.ViewHolder {

        TextView displayshelfvalue, displayisbnvalue, displayqtyvalue;
        RelativeLayout relativeLayout;

        MyViewHolder(View v) {
            super(v);
            relativeLayout = v.findViewById(R.id.relativelayout);
            displayshelfvalue = v.findViewById(R.id.shelf_value);
            displayisbnvalue = v.findViewById(R.id.isbn_value);
            displayqtyvalue = v.findViewById(R.id.qty_value);

            relativeLayout.setOnClickListener(view -> {

                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putString("shelf", displayshelfvalue.getText().toString());
                bundle.putString("isbn", displayisbnvalue.getText().toString());
                Qty_For_Sale qtyForSale = new Qty_For_Sale();
                qtyForSale.setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                qtyForSale.setArguments(bundle);
                qtyForSale.show(fragmentManager, "");
            });
        }
    }
}