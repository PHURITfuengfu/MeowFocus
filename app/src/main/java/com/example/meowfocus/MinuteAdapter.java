package com.example.meowfocus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MinuteAdapter extends RecyclerView.Adapter<MinuteAdapter.ViewHolder> {
    private int[] minutes;
    private OnItemClickListener listener;
    private int selectedPosition = 15; // Default 15 minutes

    public interface OnItemClickListener {
        void onItemClick(int minute);
    }

    public MinuteAdapter(int[] minutes, OnItemClickListener listener) {
        this.minutes = minutes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_minute, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int minute = minutes[position];
        holder.minuteText.setText(String.valueOf(minute));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(minute);
            }
        });
    }

    @Override
    public int getItemCount() {
        return minutes.length;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView minuteText;

        ViewHolder(View itemView) {
            super(itemView);
            minuteText = itemView.findViewById(R.id.minuteText);
        }
    }
}
