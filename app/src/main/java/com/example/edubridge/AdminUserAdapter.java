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
import java.util.Locale;

/**
 * Admin User Adapter for M6.2 User Management.
 * 
 * Features:
 * - A6.2.1: Display list of users
 * - A6.2.2: Filter by name/email/status
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    // User data model
    public static class UserItem {
        public String uid;
        public String name;
        public String email;
        public long totalPoints;
        public int level;
        public boolean isSuspended;
        public int badgeCount;

        public UserItem(String uid, String name, String email, long totalPoints, boolean isSuspended, int badgeCount) {
            this.uid = uid;
            this.name = name != null ? name : "Unknown";
            this.email = email != null ? email : "";
            this.totalPoints = totalPoints;
            this.level = LevelManager.getLevelForXp(totalPoints);
            this.isSuspended = isSuspended;
            this.badgeCount = badgeCount;
        }
    }

    private List<UserItem> allUsers = new ArrayList<>();
    private List<UserItem> filteredUsers = new ArrayList<>();
    private OnUserClickListener listener;

    private String currentSearchQuery = "";
    private String currentStatusFilter = "all"; // "all", "active", "suspended"

    public interface OnUserClickListener {
        void onUserClick(UserItem user);
    }

    public AdminUserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserItem> users) {
        this.allUsers = new ArrayList<>(users);
        applyFilters();
    }

    public void setSearchQuery(String query) {
        this.currentSearchQuery = query.toLowerCase(Locale.getDefault());
        applyFilters();
    }

    public void setStatusFilter(String filter) {
        this.currentStatusFilter = filter;
        applyFilters();
    }

    private void applyFilters() {
        filteredUsers.clear();

        for (UserItem user : allUsers) {
            // Status filter
            if (currentStatusFilter.equals("active") && user.isSuspended)
                continue;
            if (currentStatusFilter.equals("suspended") && !user.isSuspended)
                continue;

            // Search filter
            if (!currentSearchQuery.isEmpty()) {
                boolean matchesName = user.name.toLowerCase(Locale.getDefault()).contains(currentSearchQuery);
                boolean matchesEmail = user.email.toLowerCase(Locale.getDefault()).contains(currentSearchQuery);
                if (!matchesName && !matchesEmail)
                    continue;
            }

            filteredUsers.add(user);
        }

        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredUsers.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserItem user = filteredUsers.get(position);

        h.tvName.setText(user.name);
        h.tvEmail.setText(user.email);
        h.tvLevel.setText("Lv." + user.level);
        h.tvXp.setText(user.totalPoints + " XP");

        // Status
        if (user.isSuspended) {
            h.tvStatus.setText("Suspended");
            h.tvStatus.setTextColor(0xFFD32F2F); // Red
            h.viewStatus.setBackgroundColor(0xFFD32F2F);
        } else {
            h.tvStatus.setText("Active");
            h.tvStatus.setTextColor(0xFF4CAF50); // Green
            h.viewStatus.setBackgroundColor(0xFF4CAF50);
        }

        // Click handler
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View viewStatus;
        TextView tvName, tvEmail, tvLevel, tvXp, tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            viewStatus = itemView.findViewById(R.id.view_status);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvXp = itemView.findViewById(R.id.tv_xp);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
