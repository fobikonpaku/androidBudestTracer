package com.example.budgettracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>{
    private List<Record> recordList;

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = recordList.get(position);
        holder.typeTextView.setText(record.getType());
        holder.amountTextView.setText(String.valueOf(record.getAmount()));
        holder.descriptionTextView.setText(record.getDescription());
        holder.dateTextView.setText(new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date(record.getDate())));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder{
        public TextView typeTextView;
        public TextView amountTextView;
        public TextView descriptionTextView;
        public TextView dateTextView;
        public RecordViewHolder(View v) {
            super(v);
            typeTextView = v.findViewById(R.id.typeTextView);
            amountTextView = v.findViewById(R.id.amountTextView);
            descriptionTextView = v.findViewById(R.id.descriptionTextView);
            dateTextView = v.findViewById(R.id.dateTextView);
        }
    }
    public RecordAdapter(List<Record> recordList) {
        this.recordList = recordList;
    }




}
