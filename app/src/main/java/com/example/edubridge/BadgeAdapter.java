package com.example.edubridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.VH> {

    private final List<Badge> data;

    public BadgeAdapter(List<Badge> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Badge b = data.get(position);

        h.tvTitle.setText(b.getTitle());
        h.tvDesc.setText(b.getDesc());

        if (b.isUnlocked()) {
            h.tvStatus.setText("UNLOCKED ✅");
            h.ivIcon.setImageResource(android.R.drawable.star_big_on);
        } else {
            h.tvStatus.setText("LOCKED 🔒");
            h.ivIcon.setImageResource(android.R.drawable.star_big_off);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDesc, tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_badge_icon);
            tvTitle = itemView.findViewById(R.id.tv_badge_title);
            tvDesc = itemView.findViewById(R.id.tv_badge_desc);
            tvStatus = itemView.findViewById(R.id.tv_badge_status);
        }
    }
}