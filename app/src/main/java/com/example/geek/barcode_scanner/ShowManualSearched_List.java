package com.example.geek.barcode_scanner;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ShowManualSearched_List extends DialogFragment {

    private RecyclerView MyRecyclerView;
    private ArrayList lst = new ArrayList();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setTitle("Search Results");

        View rootView = inflater.inflate(R.layout.fragment_show_manual_searched__list, container, false);
        lst = Search_ISBN_Fragment.lisbn;
        MyRecyclerView = rootView.findViewById(R.id.card_recycle);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        RecyclerView.Adapter mAdapter = new MyAdapter(lst);
        MyRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> list;

        MyAdapter(ArrayList<String> Data) {
            this.list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_searched_data, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            MyRecyclerView = recyclerView;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        MyViewHolder(View v) {
            super(v);
            displayshelfvalue = v.findViewById(R.id.shelf_value);
            displayisbnvalue = v.findViewById(R.id.isbn_value);
            displayqtyvalue = v.findViewById(R.id.qty_value);
        }
    }

}
