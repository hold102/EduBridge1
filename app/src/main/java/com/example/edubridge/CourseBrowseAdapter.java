package com.example.edubridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.entity.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * M3.1 Course Browsing - RecyclerView Adapter
 * 
 * Displays courses with thumbnail, title, description,
 * category badge, and difficulty indicator.
 */
public class CourseBrowseAdapter extends RecyclerView.Adapter<CourseBrowseAdapter.CourseViewHolder> {

    private final Context context;
    private List<Course> courses;
    private final OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseBrowseAdapter(Context context, List<Course> courses, OnCourseClickListener listener) {
        this.context = context;
        this.courses = courses != null ? courses : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_browse, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    /**
     * Update the course list and refresh display.
     */
    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses != null ? newCourses : new ArrayList<>();
        notifyDataSetChanged();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgThumbnail;
        private final TextView tvDifficulty;
        private final TextView tvCategory;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvLessons;
        private final TextView tvDuration;

        CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvLessons = itemView.findViewById(R.id.tv_lessons);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }

        void bind(Course course) {
            tvTitle.setText(course.title);
            tvDescription.setText(course.description);
            tvCategory.setText(course.category);
            tvDifficulty.setText(course.difficulty);

            // Lesson count
            int lessonCount = course.totalLessons;
            tvLessons.setText(lessonCount + (lessonCount == 1 ? " lesson" : " lessons"));

            // Duration
            tvDuration.setText(course.duration != null ? course.duration : "Self-paced");

            // Set difficulty badge color based on level
            int badgeColor = getDifficultyColor(course.difficulty);
            tvDifficulty.getBackground().setTint(badgeColor);

            // Set thumbnail based on category
            int thumbnailRes = getCategoryThumbnail(course.category);
            imgThumbnail.setImageResource(thumbnailRes);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }

        private int getDifficultyColor(String difficulty) {
            if (difficulty == null)
                return 0xFF4CAF50; // Green default

            switch (difficulty.toLowerCase()) {
                case "beginner":
                    return 0xFF4CAF50; // Green
                case "intermediate":
                    return 0xFFFF9800; // Orange
                case "advanced":
                    return 0xFFF44336; // Red
                default:
                    return 0xFF4CAF50;
            }
        }

        private int getCategoryThumbnail(String category) {
            if (category == null)
                return R.drawable.ic_nav_library;

            switch (category.toLowerCase()) {
                case "mathematics":
                    return R.drawable.ic_subject_math;
                case "science":
                    return R.drawable.ic_subject_science;
                case "coding":
                case "computer science":
                    return R.drawable.ic_subject_coding;
                case "art":
                case "art & design":
                    return R.drawable.ic_subject_art;
                case "geography":
                    return R.drawable.ic_subject_geo;
                default:
                    return R.drawable.ic_nav_library;
            }
        }
    }
}
