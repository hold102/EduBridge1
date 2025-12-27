package com.example.edubridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

    private final Context context;
    private final List<Leader> data;
    private final String currentUid;

    public LeaderboardAdapter(Context context, List<Leader> data) {
        this.context = context;
        this.data = data;

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        this.currentUid = (u != null) ? u.getUid() : null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_leaderboard_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Leader leader = data.get(position);
        int rank = position + 1;

        h.tvRank.setText("#" + rank);
        h.tvName.setText((leader.name == null || leader.name.trim().isEmpty()) ? "User" : leader.name);
        h.tvPoints.setText(String.valueOf(leader.totalPoints));

        // ✅ highlight current user
        boolean isMe = (currentUid != null && leader.uid != null && leader.uid.equals(currentUid));
        h.root.setBackgroundResource(isMe ? R.drawable.bg_leaderboard_row_me : R.drawable.bg_leaderboard_row);

        // ✅ badge icon
        if (leader.badgeCount > 0) {
            h.ivBadge.setVisibility(View.VISIBLE);
            h.tvBadgeCount.setVisibility(View.VISIBLE);
            h.tvBadgeCount.setText(String.valueOf(leader.badgeCount));
        } else {
            h.ivBadge.setVisibility(View.GONE);
            h.tvBadgeCount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View root;
        TextView tvRank, tvName, tvPoints, tvBadgeCount;
        ImageView ivBadge;

        VH(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root_row);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPoints = itemView.findViewById(R.id.tv_points);

            ivBadge = itemView.findViewById(R.id.iv_badge);
            tvBadgeCount = itemView.findViewById(R.id.tv_badge_count);
        }
    }
}

