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

import java.util.List;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = posts.get(position);

        holder.tvName.setText(post.getName());
        holder.tvTime.setText(post.getTime());
        holder.tvContent.setText(post.getContent());
        holder.imgAvatar.setImageResource(post.getAvatarResId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_NAME, post.getName());
            intent.putExtra(PostDetailActivity.EXTRA_TIME, post.getTime());
            intent.putExtra(PostDetailActivity.EXTRA_CONTENT, post.getContent());
            intent.putExtra(PostDetailActivity.EXTRA_AVATAR, post.getAvatarResId());
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
}