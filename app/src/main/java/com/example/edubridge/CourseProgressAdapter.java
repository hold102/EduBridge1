package com.example.edubridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.entity.Course;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of course progress items in the Dashboard.
 * Binds {@link Course} data to the `item_course_progress` layout.
 */
public class CourseProgressAdapter extends RecyclerView.Adapter<CourseProgressAdapter.ViewHolder> {

    private List<Course> courses = new ArrayList<>();

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvFraction, tvPercent;
        LinearProgressIndicator pbProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_course_title);
            tvStatus = itemView.findViewById(R.id.tv_course_status);
            tvFraction = itemView.findViewById(R.id.tv_progress_fraction);
            tvPercent = itemView.findViewById(R.id.tv_progress_percent);
            pbProgress = itemView.findViewById(R.id.pb_course_progress);
        }

        public void bind(Course course) {
            tvTitle.setText(course.title != null ? course.title : "Unknown Course");

            // Status logic
            if (course.status != null) {
                tvStatus.setText(course.status);
            } else {
                tvStatus.setText(course.progress > 0 ? "In Progress" : "Not Started");
            }

            int total = course.totalLessons > 0 ? course.totalLessons : 10; // Default fallback
            int current = course.progress;

            tvFraction.setText(current + "/" + total + " Lessons");

            int percent = (int) (((float) current / total) * 100);
            tvPercent.setText(percent + "%");

            pbProgress.setMax(total);
            pbProgress.setProgress(current);
        }
    }
}
