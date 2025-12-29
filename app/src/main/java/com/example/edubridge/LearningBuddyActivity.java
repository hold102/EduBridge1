package com.example.edubridge;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.AppDatabase;
import com.example.edubridge.data.local.dao.ChatMessageDao;
import com.example.edubridge.data.local.entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Learning Buddy Activity - AI Chat Interface
 * 
 * Features:
 * - Chat with Gemini AI
 * - Persistent chat history (Room database)
 * - Delete chat history
 */
public class LearningBuddyActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatMessageDao chatDao;
    private EditText etMessage;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_buddy);

        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize database
        chatDao = AppDatabase.getInstance(this).chatMessageDao();

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Delete history button
        ImageView btnDeleteHistory = findViewById(R.id.btn_delete_history);
        if (btnDeleteHistory != null) {
            btnDeleteHistory.setOnClickListener(v -> showDeleteConfirmDialog());
        }

        // Setup RecyclerView
        rvChat = findViewById(R.id.rv_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(messages);
        rvChat.setAdapter(chatAdapter);

        // Load chat history
        loadChatHistory();

        // Send button
        etMessage = findViewById(R.id.et_message);
        ImageView btnSend = findViewById(R.id.btn_send);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }
    }

    /**
     * Show confirmation dialog before deleting chat history.
     */
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Chat History")
                .setMessage("Are you sure you want to delete all chat messages? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteChatHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete all chat history from Room database.
     */
    private void deleteChatHistory() {
        new Thread(() -> {
            chatDao.deleteAll();
            mainHandler.post(() -> {
                messages.clear();
                chatAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Chat history deleted", Toast.LENGTH_SHORT).show();

                // Add welcome message again
                addWelcomeMessage();
            });
        }).start();
    }

    /**
     * Load chat history from database.
     * If empty, add a welcome message.
     */
    private void loadChatHistory() {
        chatDao.getAllMessages().observe(this, chatMessages -> {
            if (chatMessages != null) {
                messages.clear();
                messages.addAll(chatMessages);
                chatAdapter.notifyDataSetChanged();

                // Scroll to bottom
                if (!messages.isEmpty()) {
                    rvChat.scrollToPosition(messages.size() - 1);
                }
            }

            // Add welcome message if chat is empty
            if (messages.isEmpty()) {
                addWelcomeMessage();
            }
        });
    }

    /**
     * Add a welcome message from the AI.
     */
    private void addWelcomeMessage() {
        ChatMessage welcome = new ChatMessage(
                UUID.randomUUID().toString(),
                "Hi! I'm your Learning Buddy. How can I help you today? 📚",
                false,
                System.currentTimeMillis());
        saveMessage(welcome);
    }

    /**
     * Send user message and get AI response.
     */
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        // Clear input
        etMessage.setText("");

        // Create and save user message
        ChatMessage userMessage = new ChatMessage(
                UUID.randomUUID().toString(),
                text,
                true,
                System.currentTimeMillis());
        saveMessage(userMessage);

        // Show typing indicator (simple approach)
        Toast.makeText(this, "Learning Buddy is thinking...", Toast.LENGTH_SHORT).show();

        // Get AI response
        GeminiHelper.generateResponse(text, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                mainHandler.post(() -> {
                    ChatMessage aiMessage = new ChatMessage(
                            UUID.randomUUID().toString(),
                            response,
                            false,
                            System.currentTimeMillis());
                    saveMessage(aiMessage);
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    ChatMessage errorMessage = new ChatMessage(
                            UUID.randomUUID().toString(),
                            "Sorry, I'm having trouble responding right now. Please try again later.",
                            false,
                            System.currentTimeMillis());
                    saveMessage(errorMessage);
                    Toast.makeText(LearningBuddyActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Save message to database.
     */
    private void saveMessage(ChatMessage message) {
        new Thread(() -> chatDao.insert(message)).start();
    }
}