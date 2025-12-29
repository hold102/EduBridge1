package com.example.edubridge;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    private final Context context;
    private final List<CommunityPost> posts;

    public CommunityPostAdapter(Context context, List<CommunityPost> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = posts.get(position);

        holder.tvName.setText(safe(post.getUserName()));
        holder.tvContent.setText(safe(post.getContent()));

        final int avatar = (post.getAvatarRes() == 0)
                ? R.drawable.img_hero_student
                : post.getAvatarRes();
        holder.imgAvatar.setImageResource(avatar);

        final String timeText = formatTimestamp(post.getCreatedAt());
        holder.tvTime.setText(timeText);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);

            // ✅新增：传 docId + authorId，用于判断是否本人
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, safe(post.getId()));
            intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_ID, safe(post.getAuthorId()));

            // 原有传参
            intent.putExtra(PostDetailActivity.EXTRA_NAME, safe(post.getUserName()));
            intent.putExtra(PostDetailActivity.EXTRA_CONTENT, safe(post.getContent()));
            intent.putExtra(PostDetailActivity.EXTRA_TIME, timeText);
            intent.putExtra(PostDetailActivity.EXTRA_AVATAR, avatar);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts == null ? 0 : posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvTime, tvContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(ts.toDate());
    }
}