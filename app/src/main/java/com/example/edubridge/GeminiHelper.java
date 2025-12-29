package com.example.edubridge;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utility class for interacting with Google Gemini AI via REST API.
 * Uses OkHttp (bundled with Retrofit) for HTTP requests.
 */
public class GeminiHelper {

    private static final String TAG = "GeminiHelper";

    // API Key - Replace with your own from https://makersuite.google.com/app/apikey
    private static final String API_KEY = "AIzaSyD_0ffzW9n6Mow-yBNW3lYTOeeYkO_mtGI";

    // Use gemini-2.0-flash (latest stable)
    private static final String MODEL_NAME = "gemini-2.5-flash";

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/" + MODEL_NAME
            + ":generateContent?key=" + API_KEY;

    private static final String SYSTEM_PROMPT = "You are Learning Buddy, a friendly and helpful AI tutor for students. "
            +
            "You help with homework, explain concepts clearly, and encourage learning. " +
            "Keep responses concise and suitable for students. " +
            "Be encouraging and supportive.";

    private static final OkHttpClient client = new OkHttpClient();
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * Callback interface for AI responses.
     */
    public interface GeminiCallback {
        void onSuccess(String response);

        void onError(String error);
    }

    /**
     * Generate a response from Gemini AI.
     * 
     * @param userMessage The user's message/question
     * @param callback    Callback for success/error
     */
    public static void generateResponse(String userMessage, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                // Build the request body
                String fullPrompt = SYSTEM_PROMPT + "\n\nUser: " + userMessage + "\n\nAssistant:";

                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();

                part.put("text", fullPrompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                RequestBody body = RequestBody.create(JSON, requestBody.toString());

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "API call failed", e);
                        callback.onError("Network error. Please check your internet connection.");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "API error: " + response.code() + " - " + errorBody);
                            callback.onError("API error: " + response.code());
                            return;
                        }

                        try {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);

                            // Extract the generated text
                            JSONArray candidates = json.optJSONArray("candidates");
                            if (candidates != null && candidates.length() > 0) {
                                JSONObject candidate = candidates.getJSONObject(0);
                                JSONObject contentObj = candidate.optJSONObject("content");
                                if (contentObj != null) {
                                    JSONArray partsArr = contentObj.optJSONArray("parts");
                                    if (partsArr != null && partsArr.length() > 0) {
                                        String text = partsArr.getJSONObject(0).optString("text", "");
                                        if (!text.isEmpty()) {
                                            callback.onSuccess(text.trim());
                                            return;
                                        }
                                    }
                                }
                            }

                            callback.onError("Empty response from AI");
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error", e);
                            callback.onError("Failed to parse AI response");
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Request build error", e);
                callback.onError("Failed to send request");
            }
        });
    }
}
