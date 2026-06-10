package com.example.login;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SupabaseBookService {

    private static final String SUPABASE_URL = "https://ezjtsaarixdoyofkdcob.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_HLTqeHnsdBPleQZEVrum9w_8Le7YCvH";

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface BooksCallback {
        void onSuccess(List<Book> books);
        void onError(String error);
    }
    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    public void getBooks(BooksCallback callback) {
        String url = SUPABASE_URL
                + "/rest/v1/books"
                + "?select=*"
                + "&order=title.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    mainHandler.post(() -> callback.onError(
                            "HTTP " + response.code() + ": " + errorBody
                    ));
                    return;
                }

                String body = response.body() != null ? response.body().string() : "[]";

                try {
                    JSONArray array = new JSONArray(body);
                    List<Book> result = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        Book book = new Book(
                                obj.optInt("id"),
                                obj.optString("title"),
                                obj.optString("author"),
                                obj.optString("genre"),
                                obj.optInt("year", 2024),
                                obj.optString("type", "Audiobook"),
                                obj.optString("topic", ""),
                                obj.optInt("duration", 3600),
                                obj.optInt("progress", 0),
                                obj.optBoolean("is_favorite", false),
                                obj.optBoolean("is_downloaded", false),
                                obj.optDouble("rating", 0.0)
                        );
                        book.setDescription(cleanString(obj.optString("description", "")));
                        book.setCoverUrl(cleanString(obj.optString("cover_url", "")));
                        book.setAudioUrl(cleanString(obj.optString("audio_url", "")));

                        result.add(book);
                    }

                    mainHandler.post(() -> callback.onSuccess(result));

                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }
    public void updateFavorite(int bookId, boolean isFavorite, UpdateCallback callback) {
        String url = SUPABASE_URL
                + "/rest/v1/books"
                + "?id=eq." + bookId;

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String json = "{"
                + "\"is_favorite\":" + isFavorite
                + "}";

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    mainHandler.post(() -> callback.onError(
                            "HTTP " + response.code() + ": " + errorBody
                    ));
                    return;
                }

                mainHandler.post(callback::onSuccess);
            }
        });
    }
    public void updateProgress(int bookId, int progress, UpdateCallback callback) {
        String url = SUPABASE_URL
                + "/rest/v1/books"
                + "?id=eq." + bookId;

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String json = "{"
                + "\"progress\":" + progress
                + "}";

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    mainHandler.post(() -> callback.onError(
                            "HTTP " + response.code() + ": " + errorBody
                    ));
                    return;
                }

                mainHandler.post(callback::onSuccess);
            }
        });
    }
    private String cleanString(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "";
        }
        return value;
    }
}