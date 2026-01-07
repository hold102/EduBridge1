package com.example.edubridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.VH> {

    public interface OnReplyClickListener {
        void onReplyClick(Reply reply);
    }

    private final Context context;
    private final List<Reply> list;
    private final String currentUid;
    private final String postId;
    private final OnReplyClickListener listener;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ✅ 注意：这里是 5 个参数（你截图里“Expected 5 arguments”就是因为你工程里构造函数不一致）
    public ReplyAdapter(Context context, List<Reply> list, String currentUid, String postId, OnReplyClickListener listener) {
        this.context = context;
        this.list = list;
        this.currentUid = currentUid;
        this.postId = postId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_reply, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Reply r = list.get(position);

        h.tvName.setText(safe(r.getUserName()));
        h.tvTime.setText(formatTimestamp(r.getCreatedAt()));

        // ✅ 方案A：如果 replyToName 有值，就显示 “@xxx  + content”
        String prefix = "";
        if (!isEmpty(r.getReplyToName())) {
            prefix = "@" + r.getReplyToName() + " ";
        }
        h.tvContent.setText(prefix + safe(r.getContent()));

        // ✅ 只显示自己的删除按钮（不管是不是自己发的帖子）
        boolean isOwner = currentUid != null && currentUid.equals(r.getAuthorId());
        h.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        // 点击整条回复 → 进入“回复此人”模式
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReplyClick(r);
        });

        // 删除自己的回复
        h.btnDelete.setOnClickListener(v -> {
            if (!isOwner) return;

            String replyId = r.getId();
            if (isEmpty(postId) || isEmpty(replyId)) {
                Toast.makeText(context, "Missing postId/replyId", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("posts")
                    .document(postId)
                    .collection("replies")
                    .document(replyId)
                    .delete()
                    .addOnSuccessListener(x ->
                            Toast.makeText(context, "Reply deleted", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvContent;
        ImageView btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_reply_name);
            tvTime = itemView.findViewById(R.id.tv_reply_time);
            tvContent = itemView.findViewById(R.id.tv_reply_content);
            btnDelete = itemView.findViewById(R.id.btn_delete_reply);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(ts.toDate());
    }
}
