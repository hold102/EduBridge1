package com.example.edubridge;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Badge Adapter for M4.3 Digital Badges.
 * 
 * Displays badges with:
 * - Category label (M4.3.3)
 * - Locked/unlocked visual state
 * - Unlock conditions for locked badges (M4.3.4)
 */
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

        // Category (M4.3.3)
        h.tvCategory.setText(b.getCategoryDisplayName().toUpperCase());

        // Title
        h.tvTitle.setText(b.getTitle());

        if (b.isUnlocked()) {
            // UNLOCKED state
            h.tvDesc.setText(b.getDescription());
            h.tvStatus.setText("✓ EARNED");
            h.tvStatus.setBackgroundResource(R.drawable.bg_badge_primary);
            h.tvStatus.setTextColor(0xFFFFFFFF); // White text for visibility

            // Hide unlock condition
            h.tvUnlockCondition.setVisibility(View.GONE);

            // Show colored icon
            h.ivIcon.setImageResource(b.getIconRes() != 0 ? b.getIconRes() : R.drawable.ic_achievement_medal);
            h.ivIcon.setColorFilter(null);
            h.ivIcon.setAlpha(1.0f);

        } else {
            // LOCKED state (M4.3.4)
            h.tvDesc.setText(b.getDescription());
            h.tvStatus.setText("LOCKED");
            h.tvStatus.setBackgroundResource(R.drawable.bg_badge_locked);

            // Show unlock condition (M4.3.4)
            h.tvUnlockCondition.setVisibility(View.VISIBLE);
            h.tvUnlockCondition.setText("🔒 " + b.getUnlockCondition());

            // Grayscale icon
            h.ivIcon.setImageResource(b.getIconRes() != 0 ? b.getIconRes() : R.drawable.ic_achievement_medal);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            h.ivIcon.setColorFilter(new ColorMatrixColorFilter(matrix));
            h.ivIcon.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCategory, tvTitle, tvDesc, tvStatus, tvUnlockCondition;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_badge_icon);
            tvCategory = itemView.findViewById(R.id.tv_badge_category);
            tvTitle = itemView.findViewById(R.id.tv_badge_title);
            tvDesc = itemView.findViewById(R.id.tv_badge_desc);
            tvStatus = itemView.findViewById(R.id.tv_badge_status);
            tvUnlockCondition = itemView.findViewById(R.id.tv_unlock_condition);
        }
    }
}