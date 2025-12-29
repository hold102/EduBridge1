package com.example.edubridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.entity.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying recommended courses in a horizontal RecyclerView.
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<Course> recommendations = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public RecommendationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRecommendations(List<Course> recommendations) {
        this.recommendations = recommendations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = recommendations.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDifficulty, tvDuration;
        Button btnStart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_rec_title);
            tvDifficulty = itemView.findViewById(R.id.tv_rec_difficulty);
            tvDuration = itemView.findViewById(R.id.tv_rec_duration);
            btnStart = itemView.findViewById(R.id.btn_rec_start);
        }

        public void bind(final Course course, final OnItemClickListener listener) {
            tvTitle.setText(course.title);
            // Null safety
            tvDifficulty.setText(course.difficulty != null ? course.difficulty : "General");
            tvDuration.setText(course.duration != null ? course.duration : "Self-Paced");

            View.OnClickListener clickAction = v -> {
                if (listener != null) listener.onItemClick(course);
            };

            btnStart.setOnClickListener(clickAction);
            itemView.setOnClickListener(clickAction);
        }
    }
}
