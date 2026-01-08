package com.example.edubridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * M3.2.1 Syllabus Adapter
 * Displays course syllabus items with chapter numbers.
 */
public class SyllabusAdapter extends RecyclerView.Adapter<SyllabusAdapter.SyllabusViewHolder> {

    private final Context context;
    private List<String> topics;
    private int completedCount;

    public SyllabusAdapter(Context context, List<String> topics, int completedCount) {
        this.context = context;
        this.topics = topics != null ? topics : new ArrayList<>();
        this.completedCount = completedCount;
    }

    @NonNull
    @Override
    public SyllabusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_syllabus, parent, false);
        return new SyllabusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SyllabusViewHolder holder, int position) {
        String topic = topics.get(position);
        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvTopic.setText(topic);

        // Mark completed items
        boolean isCompleted = position < completedCount;
        if (isCompleted) {
            holder.tvNumber.setBackgroundResource(R.drawable.bg_bubble_user);
            holder.imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
            holder.imgStatus.setColorFilter(0xFF4CAF50); // Green
        } else {
            holder.tvNumber.setBackgroundResource(R.drawable.bg_input_outline);
            holder.imgStatus.setImageResource(android.R.drawable.ic_media_play);
            holder.imgStatus.setColorFilter(0xFFBDBDBD); // Gray
        }
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public void updateTopics(List<String> newTopics, int completed) {
        this.topics = newTopics != null ? newTopics : new ArrayList<>();
        this.completedCount = completed;
        notifyDataSetChanged();
    }

    static class SyllabusViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvTopic;
        ImageView imgStatus;

        SyllabusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number);
            tvTopic = itemView.findViewById(R.id.tv_topic);
            imgStatus = itemView.findViewById(R.id.img_status);
        }
    }
}
