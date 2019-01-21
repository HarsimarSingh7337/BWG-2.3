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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewFragment_ShowListData extends DialogFragment {

    private RecyclerView MyRecyclerView;
    private ArrayList<String> list_isbn = new ArrayList<>();
    private ArrayList<String> list_qty = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recycler_view_fragment__show_list_data, container, false);
        getDialog().setTitle("Scanned Results");
        getList();

        MyRecyclerView = rootView.findViewById(R.id.card_recycle);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        MyRecyclerView.setLayoutManager(mLayoutManager);
        MyRecyclerView.setHasFixedSize(true);
        RecyclerView.Adapter mAdapter = new MyAdapter(list_isbn);
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_showscanned_data, parent, false);
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
            holder.displayisbnvalue.setText(list_isbn.get(position));
            holder.displayqtyvalue.setText(list_qty.get(position));
        }

        @Override
        public int getItemCount() {
            return list_isbn.size();
        }
    }

     class MyViewHolder extends RecyclerView.ViewHolder {

        TextView displayisbnvalue, displayqtyvalue;
        ImageView removeBtn;

        MyViewHolder(View v) {
            super(v);
            displayisbnvalue = v.findViewById(R.id.isbn_value);
            displayqtyvalue = v.findViewById(R.id.qty_value);
            removeBtn = v.findViewById(R.id.removebtn);

            removeBtn.setOnClickListener(view -> {

                try {
                    getDialog().dismiss();
                } catch (Exception ignored) {

                }
            });
        }
    }

    private void getList() {

        try {
            String query = "select * from scandata";
            Home_Screen.cursor = Home_Screen.database.rawQuery(query, null);
            while (Home_Screen.cursor.moveToNext()) {

                list_isbn.add(Home_Screen.cursor.getString(Home_Screen.cursor.getColumnIndex("ISBN")));
                list_qty.add(String.valueOf(Home_Screen.cursor.getInt(Home_Screen.cursor.getColumnIndex("QTY"))));
            }
        } catch (Exception ignored) {
        } finally {
            Home_Screen.cursor.close();
        }
    }
}