package com.example.budgettracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>{
    private List<Record> recordList;
    private Context context;
    private MainActivity mainActivity;
    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = recordList.get(position);
        holder.iconImageView.setImageResource(record.getIconResId());
        holder.typeTextView.setText(record.getType());
        holder.amountTextView.setText(String.valueOf(record.getAmount()));
        holder.descriptionTextView.setText(record.getDescription());
        holder.dateTextView.setText(new java.text.SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date(record.getDate())));
        //单击的操作
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecordDetailActivity.class);
            intent.putExtra("id", record.getId());
            intent.putExtra("iconResId", record.getIconResId());
            intent.putExtra("type", record.getType());
            intent.putExtra("amount", record.getAmount());
            intent.putExtra("description", record.getDescription());
            intent.putExtra("date", record.getDate());
            context.startActivity(intent);
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
                dialogBuilder.setTitle("删除");
                dialogBuilder.setMessage("删除后不可恢复，是否要删除?");
                dialogBuilder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeItem(adapterPosition);
                        //mainActivity.deleteRecordFromPreferences(record.getId());
                    }
                });
                dialogBuilder.setNegativeButton("取消", null);
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });
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
        public ImageView iconImageView;
        public TextView typeTextView;
        public TextView amountTextView;
        public TextView descriptionTextView;
        public TextView dateTextView;
        public RecordViewHolder(View v) {
            super(v);
            iconImageView = v.findViewById(R.id.iconImageView);
            typeTextView = v.findViewById(R.id.typeTextView);
            amountTextView = v.findViewById(R.id.amountTextView);
            descriptionTextView = v.findViewById(R.id.descriptionTextView);
            dateTextView = v.findViewById(R.id.dateTextView);
        }
    }
    public RecordAdapter(List<Record> recordList) {
        this.recordList = recordList;
    }

    public RecordAdapter(List<Record> recordList, Context context) {
        this.recordList = recordList;
        this.context = context;
    }



}
