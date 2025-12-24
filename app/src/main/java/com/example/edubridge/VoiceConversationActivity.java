package com.example.edubridge;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_conversation);

        TextView tvStatus = findViewById(R.id.tv_status);

        // Simulate Connection
        new Handler().postDelayed(() -> {
            tvStatus.setText("Listening...");
        }, 1500);

        // Mute Button
        findViewById(R.id.btn_mute).setOnClickListener(v -> {
            Toast.makeText(this, "Mute toggled", Toast.LENGTH_SHORT).show();
        });

        // End Call
        findViewById(R.id.btn_end_call).setOnClickListener(v -> {
            Toast.makeText(this, "Call Ended", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}