package com.example.edubridge;

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
 * Admin Course Adapter for M6.3 Content Management.
 * 
 * Features:
 * - A6.3.1: Display list of courses
 * - A6.3.4: Show publish status
 */
public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.VH> {

    // Course data model for admin view
    public static class CourseItem {
        public String id;
        public String title;
        public String category;
        public String description;
        public boolean isPublished;
        public int moduleCount;

        public CourseItem(String id, String title, String category, String description,
                boolean isPublished, int moduleCount) {
            this.id = id;
            this.title = title != null ? title : "Untitled";
            this.category = category != null ? category : "General";
            this.description = description != null ? description : "";
            this.isPublished = isPublished;
            this.moduleCount = moduleCount;
        }
    }

    private List<CourseItem> allCourses = new ArrayList<>();
    private List<CourseItem> filteredCourses = new ArrayList<>();
    private OnCourseClickListener listener;

    private String currentFilter = "all"; // "all", "published", "drafts"

    public interface OnCourseClickListener {
        void onCourseClick(CourseItem course);

        void onEditClick(CourseItem course);
    }

    public AdminCourseAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<CourseItem> courses) {
        this.allCourses = new ArrayList<>(courses);
        applyFilter();
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        applyFilter();
    }

    private void applyFilter() {
        filteredCourses.clear();

        for (CourseItem course : allCourses) {
            if (currentFilter.equals("published") && !course.isPublished)
                continue;
            if (currentFilter.equals("drafts") && course.isPublished)
                continue;
            filteredCourses.add(course);
        }

        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredCourses.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CourseItem course = filteredCourses.get(position);

        h.tvTitle.setText(course.title);
        h.tvCategory.setText(course.category);
        h.tvModules.setText(course.moduleCount + " module" + (course.moduleCount != 1 ? "s" : ""));

        // Status badge
        if (course.isPublished) {
            h.tvStatus.setText("Published");
            h.tvStatus.setBackgroundResource(R.drawable.bg_badge_primary);
        } else {
            h.tvStatus.setText("Draft");
            h.tvStatus.setBackgroundColor(0xFF9E9E9E); // Gray
        }

        // Click handlers
        h.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onCourseClick(course);
        });

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null)
                listener.onEditClick(course);
        });
    }

    @Override
    public int getItemCount() {
        return filteredCourses.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon, btnEdit;
        TextView tvTitle, tvCategory, tvStatus, tvModules;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvModules = itemView.findViewById(R.id.tv_modules);
        }
    }
}
