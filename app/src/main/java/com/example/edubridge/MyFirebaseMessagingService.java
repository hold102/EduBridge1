package com.example.edubridge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "edubridge_general";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannelIfNeeded();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "EduBridge";
        String body = "You have a new notification.";

        // 1) Notification payload
        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null) body = message.getNotification().getBody();
        }

        // 2) Data payload override (更灵活)
        String screen = null; // dashboard / leaderboard / community / badges ...
        if (message.getData() != null) {
            if (message.getData().containsKey("title")) title = message.getData().get("title");
            if (message.getData().containsKey("body")) body = message.getData().get("body");
            if (message.getData().containsKey("screen")) screen = message.getData().get("screen");
        }

        showNotification(title, body, screen);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        saveTokenToFirestore(token);
    }

    private void showNotification(String title, String body, String screen) {
        createNotificationChannelIfNeeded();

        // 默认点开回 Dashboard
        Intent intent = new Intent(this, DashboardActivity.class);

        // 根据 screen 跳转（你可以继续加）
        if (screen != null) {
            switch (screen) {
                case "leaderboard":
                    intent = new Intent(this, LeaderboardActivity.class);
                    break;
                case "community":
                    intent = new Intent(this, CommunityActivity.class);
                    break;
                case "badges":
                    intent = new Intent(this, BadgesActivity.class);
                    break;
                case "dashboard":
                default:
                    intent = new Intent(this, DashboardActivity.class);
                    break;
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // ✅ 建议你后续换成白色单色小图标 drawable/ic_notification_small
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            nm.notify(id, builder.build());
        }
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) return;

            if (nm.getNotificationChannel(CHANNEL_ID) != null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "EduBridge Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("General notifications");
            nm.createNotificationChannel(channel);
        }
    }

    private void saveTokenToFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);
        update.put("fcmUpdatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid).set(update, SetOptions.merge());
    }
}
