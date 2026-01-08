package com.example.edubridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.entity.ChatMessage;

import java.util.List;

/**
 * RecyclerView adapter for displaying chat messages.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAiAvatar;
        private final TextView tvUserMessage;
        private final TextView tvAiMessage;
        private final View viewAvatarSpacer;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAiAvatar = itemView.findViewById(R.id.iv_ai_avatar);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
            viewAvatarSpacer = itemView.findViewById(R.id.view_avatar_spacer);
        }

        void bind(ChatMessage message) {
            if (message.isUser) {
                // User message - right side, dark bubble
                tvUserMessage.setVisibility(View.VISIBLE);
                tvUserMessage.setText(message.content);
                tvAiMessage.setVisibility(View.GONE);
                ivAiAvatar.setVisibility(View.GONE);
                viewAvatarSpacer.setVisibility(View.VISIBLE);
            } else {
                // AI message - left side with avatar
                tvAiMessage.setVisibility(View.VISIBLE);
                tvAiMessage.setText(message.content);
                tvUserMessage.setVisibility(View.GONE);
                ivAiAvatar.setVisibility(View.VISIBLE);
                viewAvatarSpacer.setVisibility(View.GONE);
            }
        }
    }
}
