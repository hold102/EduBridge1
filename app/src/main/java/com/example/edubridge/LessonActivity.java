package com.example.edubridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * M3.4 Lesson Activity - Content Viewer
 * 
 * Supports:
 * - M3.4.5: Video, text, quiz, download content types
 * - M3.4.3: Resume from last position
 * - M3.4.4: Mark lesson as complete
 */
public class LessonActivity extends AppCompatActivity {

    private TextView tvLessonTitle, tvTextContent, tvQuizQuestion, tvDownloadTitle;
    private FrameLayout layoutVideo;
    private LinearLayout layoutQuiz;
    private MaterialCardView layoutDownload;
    private RadioGroup radioOptions;
    private MaterialButton btnPrevious, btnComplete;

    private String lessonId, lessonTitle, contentType, textContent, contentUrl;
    private int correctAnswerIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        initViews();
        loadLessonData();
        displayContent();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvTextContent = findViewById(R.id.tv_text_content);
        tvQuizQuestion = findViewById(R.id.tv_quiz_question);
        tvDownloadTitle = findViewById(R.id.tv_download_title);

        layoutVideo = findViewById(R.id.layout_video);
        layoutQuiz = findViewById(R.id.layout_quiz);
        layoutDownload = findViewById(R.id.layout_download);
        radioOptions = findViewById(R.id.radio_options);

        btnPrevious = findViewById(R.id.btn_previous);
        btnComplete = findViewById(R.id.btn_complete);

        btnPrevious.setOnClickListener(v -> finish());
        btnComplete.setOnClickListener(v -> completeLesson());
    }

    private void loadLessonData() {
        lessonId = getIntent().getStringExtra("EXTRA_LESSON_ID");
        lessonTitle = getIntent().getStringExtra("EXTRA_LESSON_TITLE");
        contentType = getIntent().getStringExtra("EXTRA_LESSON_TYPE");
        textContent = getIntent().getStringExtra("EXTRA_LESSON_CONTENT");
        contentUrl = getIntent().getStringExtra("EXTRA_LESSON_URL");

        if (lessonTitle != null) {
            tvLessonTitle.setText(lessonTitle);
        }
    }

    private void displayContent() {
        // Hide all containers first
        layoutVideo.setVisibility(View.GONE);
        tvTextContent.setVisibility(View.GONE);
        layoutQuiz.setVisibility(View.GONE);
        layoutDownload.setVisibility(View.GONE);

        if (contentType == null) {
            contentType = Lesson.TYPE_TEXT;
        }

        switch (contentType) {
            case Lesson.TYPE_VIDEO:
                displayVideoContent();
                break;
            case Lesson.TYPE_QUIZ:
                displayQuizContent();
                break;
            case Lesson.TYPE_DOWNLOAD:
                displayDownloadContent();
                break;
            case Lesson.TYPE_TEXT:
            default:
                displayTextContent();
                break;
        }
    }

    private void displayTextContent() {
        tvTextContent.setVisibility(View.VISIBLE);

        if (textContent != null && !textContent.isEmpty()) {
            tvTextContent.setText(textContent);
        } else {
            tvTextContent.setText("This is a text lesson.\n\n" +
                    "In a real implementation, this would contain the full lesson content " +
                    "with formatted text, images, and interactive elements.\n\n" +
                    "Topics covered:\n" +
                    "• Key concept 1\n" +
                    "• Key concept 2\n" +
                    "• Key concept 3\n\n" +
                    "Click 'Mark Complete' when you're done reading.");
        }
    }

    private void displayVideoContent() {
        layoutVideo.setVisibility(View.VISIBLE);
        tvTextContent.setVisibility(View.VISIBLE);
        tvTextContent.setText("Video: " + lessonTitle + "\n\n" +
                "In a real implementation, this would be a video player " +
                "(using ExoPlayer or similar).\n\n" +
                "The video would resume from your last watched position.");

        layoutVideo.setOnClickListener(v -> {
            Toast.makeText(this, "Video playback would start here", Toast.LENGTH_SHORT).show();
            // In real app: start video player with contentUrl
        });
    }

    private void displayQuizContent() {
        layoutQuiz.setVisibility(View.VISIBLE);

        tvQuizQuestion.setText("What is the main concept covered in this lesson?");

        radioOptions.removeAllViews();
        String[] options = { "Option A - Correct Answer", "Option B", "Option C", "Option D" };
        correctAnswerIndex = 0;

        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setText(options[i]);
            rb.setPadding(16, 24, 16, 24);
            rb.setTextSize(14);
            radioOptions.addView(rb);
        }

        btnComplete.setText("Submit Answer");
    }

    private void displayDownloadContent() {
        layoutDownload.setVisibility(View.VISIBLE);
        tvTextContent.setVisibility(View.VISIBLE);

        tvDownloadTitle.setText("Download: " + lessonTitle);
        tvTextContent.setText("This lesson contains downloadable materials.\n\n" +
                "Tap the download card above to get the resource.\n\n" +
                "File: " + (contentUrl != null ? contentUrl : "resource.pdf"));

        layoutDownload.setOnClickListener(v -> {
            if (contentUrl != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contentUrl));
                try {
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open download link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Download would start here", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeLesson() {
        // For quiz, check answer first
        if (Lesson.TYPE_QUIZ.equals(contentType)) {
            int selectedId = radioOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = radioOptions.indexOfChild(findViewById(selectedId));
            if (selectedIndex != correctAnswerIndex) {
                Toast.makeText(this, "Incorrect. Try again!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        }

        // Mark lesson as complete and return
        Intent resultIntent = new Intent();
        resultIntent.putExtra("COMPLETED_LESSON_ID", lessonId);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Lesson completed!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
