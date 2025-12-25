package com.example.edubridge;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_AVATAR = "extra_avatar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Back button (if exists)
        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        ImageView imgAvatar = findViewById(R.id.img_avatar);
        TextView tvName = findViewById(R.id.tv_user_name);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvContent = findViewById(R.id.tv_content);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String time = getIntent().getStringExtra(EXTRA_TIME);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        int avatarRes = getIntent().getIntExtra(EXTRA_AVATAR, R.drawable.img_hero_student);

        if (tvName != null) tvName.setText(name != null ? name : "Unknown");
        if (tvTime != null) tvTime.setText(time != null ? time : "");
        if (tvContent != null) tvContent.setText(content != null ? content : "");
        if (imgAvatar != null) imgAvatar.setImageResource(avatarRes);
    }
}