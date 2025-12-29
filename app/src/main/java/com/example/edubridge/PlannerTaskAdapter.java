package com.example.edubridge;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.entity.PlannerTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying planner tasks.
 */
public class PlannerTaskAdapter extends RecyclerView.Adapter<PlannerTaskAdapter.TaskViewHolder> {

    private List<PlannerTask> tasks;
    private final TaskClickListener listener;

    public interface TaskClickListener {
        void onTaskChecked(PlannerTask task, boolean isChecked);

        void onTaskDelete(PlannerTask task);
    }

    public PlannerTaskAdapter(List<PlannerTask> tasks, TaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void setTasks(List<PlannerTask> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planner_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        PlannerTask task = tasks.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbTask;
        private final TextView tvTitle;
        private final TextView tvDue;
        private final ImageView btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTask = itemView.findViewById(R.id.cb_task);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDue = itemView.findViewById(R.id.tv_task_due);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
        }

        void bind(PlannerTask task, TaskClickListener listener) {
            tvTitle.setText(task.title);
            cbTask.setChecked(task.isCompleted);

            // Format due date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            String dueText = task.isCompleted ? "Done" : "Due: " + sdf.format(new Date(task.dueDate));
            tvDue.setText(dueText);

            // Strikethrough for completed tasks
            if (task.isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
            }

            // Overdue indicator
            if (task.isOverdue()) {
                tvDue.setText("OVERDUE");
                tvDue.setTextColor(0xFFE53935); // Red color
            }

            cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskChecked(task, isChecked);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskDelete(task);
                }
            });
        }
    }
}
