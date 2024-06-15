package com.example.budgettracker;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 可选：实现点击列表项的操作，例如查看详细信息
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 长按删除记录
                showDeleteDialog(holder.getAdapterPosition());
                return true;
            }

            private void showDeleteDialog(int adapterPosition) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(holder.itemView.getContext());
                dialogBuilder.setTitle("Delete Record");
                dialogBuilder.setMessage("Are you sure you want to delete this record?");
                dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeItem(adapterPosition);
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", null);
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });
        holder.typeTextView.setText(record.getType());
        holder.amountTextView.setText(String.valueOf(record.getAmount()));
        holder.descriptionTextView.setText(record.getDescription());
        holder.dateTextView.setText(new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date(record.getDate())));
    }



    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public void removeItem(int position) {
        recordList.remove(position);
        notifyItemRemoved(position);
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
