package com.example.edubridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.entity.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public NotificationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvBody, tvTime;
        View viewUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvBody = itemView.findViewById(R.id.tv_notification_body);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
        }

        public void bind(final Notification notification, final OnItemClickListener listener) {
            tvTitle.setText(notification.title != null ? notification.title : "Notification");
            tvBody.setText(notification.body != null ? notification.body : "");
            tvTime.setText(getTimeAgo(notification.timestamp));

            // Set icon based on type
            int iconRes = R.drawable.ic_notification;
            if (notification.type != null) {
                switch (notification.type) {
                    case "achievement":
                        iconRes = R.drawable.ic_achievement_medal;
                        break;
                    case "course_update":
                        iconRes = R.drawable.ic_nav_library;
                        break;
                    case "reminder":
                        iconRes = R.drawable.ic_home;
                        break;
                    case "announcement":
                        iconRes = R.drawable.ic_settings_gear;
                        break;
                }
            }
            ivIcon.setImageResource(iconRes);

            // Unread indicator
            viewUnreadDot.setVisibility(notification.isRead ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onItemClick(notification);
            });
        }

        private String getTimeAgo(long timestamp) {
            if (timestamp == 0)
                return "";

            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Just now";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                long mins = TimeUnit.MILLISECONDS.toMinutes(diff);
                return mins + " min ago";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return hours + " hr ago";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
