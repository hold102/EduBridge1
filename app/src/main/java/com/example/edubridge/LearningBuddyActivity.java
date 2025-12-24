package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LearningBuddyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_buddy);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // NEW: Voice Call Button Logic
        ImageView btnVoice = findViewById(R.id.btn_voice_call);
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> {
                Intent intent = new Intent(LearningBuddyActivity.this, VoiceConversationActivity.class);
                startActivity(intent);
            });
        }

        // Camera Action
        ImageView btnCamera = findViewById(R.id.btn_camera);
        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                Intent intent = new Intent(LearningBuddyActivity.this, HomeworkHelperActivity.class);
                startActivity(intent);
            });
        }

        // Send Action
        EditText etMessage = findViewById(R.id.et_message);
        ImageView btnSend = findViewById(R.id.btn_send);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (!etMessage.getText().toString().isEmpty()) {
                    etMessage.setText("");
                    Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}