package com.example.edubridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * M3.4 Module Adapter with expandable lessons
 */
public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private final Context context;
    private List<CourseModule> modules;
    private final OnLessonClickListener listener;
    private Set<Integer> expandedPositions = new HashSet<>();

    public interface OnLessonClickListener {
        void onLessonClick(CourseModule module, Lesson lesson);
    }

    public ModuleAdapter(Context context, List<CourseModule> modules, OnLessonClickListener listener) {
        this.context = context;
        this.modules = modules != null ? modules : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        CourseModule module = modules.get(position);
        boolean isExpanded = expandedPositions.contains(position);
        holder.bind(module, position, isExpanded);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    public void updateModules(List<CourseModule> newModules) {
        this.modules = newModules != null ? newModules : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ModuleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvModuleNumber, tvTitle, tvLessonCount;
        private final ImageView imgStatus, imgExpand;
        private final LinearLayout layoutHeader, layoutLessons;

        ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModuleNumber = itemView.findViewById(R.id.tv_module_number);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvLessonCount = itemView.findViewById(R.id.tv_lesson_count);
            imgStatus = itemView.findViewById(R.id.img_status);
            imgExpand = itemView.findViewById(R.id.img_expand);
            layoutHeader = itemView.findViewById(R.id.layout_header);
            layoutLessons = itemView.findViewById(R.id.layout_lessons);
        }

        void bind(CourseModule module, int position, boolean isExpanded) {
            tvModuleNumber.setText(String.valueOf(position + 1));
            tvTitle.setText(module.getTitle());

            int lessonCount = module.getLessons() != null ? module.getLessons().size() : 0;
            tvLessonCount.setText(lessonCount + " lessons");

            // Status icon
            if (module.isLocked()) {
                imgStatus.setImageResource(android.R.drawable.ic_lock_lock);
                imgStatus.setColorFilter(0xFFBDBDBD);
                tvTitle.setAlpha(0.5f);
            } else if (module.isCompleted()) {
                imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
                imgStatus.setColorFilter(0xFF4CAF50);
                tvTitle.setAlpha(1f);
            } else {
                imgStatus.setImageResource(android.R.drawable.checkbox_off_background);
                imgStatus.setColorFilter(0xFFBDBDBD);
                tvTitle.setAlpha(1f);
            }

            // Expand/collapse
            layoutLessons.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            imgExpand.setRotation(isExpanded ? 180 : 0);

            // Build lesson views
            if (isExpanded && !module.isLocked()) {
                layoutLessons.removeAllViews();
                for (Lesson lesson : module.getLessons()) {
                    View lessonView = createLessonView(module, lesson);
                    layoutLessons.addView(lessonView);
                }
            }

            // Header click to expand/collapse
            layoutHeader.setOnClickListener(v -> {
                if (module.isLocked())
                    return;

                int pos = getAdapterPosition();
                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos);
                } else {
                    expandedPositions.add(pos);
                }
                notifyItemChanged(pos);
            });
        }

        private View createLessonView(CourseModule module, Lesson lesson) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_lesson, layoutLessons, false);

            TextView tvTitle = view.findViewById(R.id.tv_title);
            TextView tvDuration = view.findViewById(R.id.tv_duration);
            ImageView imgType = view.findViewById(R.id.img_type);
            ImageView imgStatus = view.findViewById(R.id.img_status);

            tvTitle.setText(lesson.getTitle());
            tvDuration.setText(lesson.getDurationMinutes() + " min");

            // Content type icon
            imgType.setImageResource(lesson.getTypeIcon());

            // Completion status
            if (lesson.isLocked()) {
                imgStatus.setImageResource(android.R.drawable.ic_lock_lock);
                imgStatus.setColorFilter(0xFFBDBDBD);
                tvTitle.setAlpha(0.5f);
            } else if (lesson.isCompleted()) {
                imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
                imgStatus.setColorFilter(0xFF4CAF50);
                tvTitle.setAlpha(1f);
            } else {
                imgStatus.setImageResource(android.R.drawable.checkbox_off_background);
                imgStatus.setColorFilter(0xFFBDBDBD);
                tvTitle.setAlpha(1f);
            }

            // Click listener
            view.setOnClickListener(v -> {
                if (!lesson.isLocked() && listener != null) {
                    listener.onLessonClick(module, lesson);
                }
            });

            return view;
        }
    }
}
