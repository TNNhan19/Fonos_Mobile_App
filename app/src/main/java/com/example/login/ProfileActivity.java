package com.example.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "profile_settings";

    private String username;
    private SharedPreferences prefs;
    private TextView tvName;
    private TextView tvAvatar;
    private TextView tvEmailStatus;
    private TextView tvLanguageStatus;
    private TextView tvThemeStatus;
    private TextView tvPersonalInfoSummary;
    private TextView tvOfflineCount;
    private TextView tvFavoriteCount;
    private TextView tvHistoryCount;
    private SupabaseBookService supabaseBookService;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        supabaseBookService = new SupabaseBookService();
        username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            username = prefs.getString("username", "Bạn đọc Fonos");
        }

        bindViews();
        renderProfile();
        setupActions();
        loadContentStats();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvProfileName);
        tvAvatar = findViewById(R.id.tvProfileAvatar);
        tvEmailStatus = findViewById(R.id.tvEmailStatus);
        tvLanguageStatus = findViewById(R.id.tvLanguageStatus);
        tvThemeStatus = findViewById(R.id.tvThemeStatus);
        tvPersonalInfoSummary = findViewById(R.id.tvPersonalInfoSummary);
        tvOfflineCount = findViewById(R.id.tvOfflineCount);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        tvHistoryCount = findViewById(R.id.tvHistoryCount);
    }

    private void renderProfile() {
        tvName.setText(username);
        tvAvatar.setText(makeInitial(username));
        tvPersonalInfoSummary.setText(username + " · fonos.user@example.com");
        tvEmailStatus.setText(prefs.getBoolean("email_enabled", true) ? "Đang bật" : "Đang tắt");
        tvLanguageStatus.setText(LocaleHelper.getSavedLanguage(this).equals("vi") ? "Tiếng Việt" : "English");
        tvThemeStatus.setText(prefs.getString("theme", "Tối"));
    }

    private void setupActions() {
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.rowPersonalInfo).setOnClickListener(v -> showPersonalInfoDialog());
        findViewById(R.id.rowEmailNotify).setOnClickListener(v -> toggleEmailNotification());
        findViewById(R.id.rowLanguage).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.rowTheme).setOnClickListener(v -> showThemeDialog());

        findViewById(R.id.rowOfflineBooks).setOnClickListener(v -> openLibrary("Offline"));
        findViewById(R.id.rowFavoriteBooks).setOnClickListener(v -> openLibrary("Favorite"));
        findViewById(R.id.rowHistory).setOnClickListener(v -> openLibrary("History"));
        findViewById(R.id.statOffline).setOnClickListener(v -> openLibrary("Offline"));
        findViewById(R.id.statFavorite).setOnClickListener(v -> openLibrary("Favorite"));
        findViewById(R.id.statHistory).setOnClickListener(v -> openLibrary("History"));

        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("username", username);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navExplore).setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navLibrary).setOnClickListener(v -> openLibrary("All"));

        findViewById(R.id.btnProfileLogout).setOnClickListener(v -> {
            stopService(new Intent(this, FonosAudioService.class));
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadContentStats() {
        supabaseBookService.getBooks(new SupabaseBookService.BooksCallback() {
            @Override
            public void onSuccess(List<Book> books) {
                int offline = 0;
                int favorite = 0;
                int history = 0;

                for (Book book : books) {
                    if (book.isDownloaded()) offline++;
                    if (book.isFavorite()) favorite++;
                    if (book.getProgress() > 0) history++;
                }

                tvOfflineCount.setText(String.valueOf(offline));
                tvFavoriteCount.setText(String.valueOf(favorite));
                tvHistoryCount.setText(String.valueOf(history));
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this,
                        "Không tải được thống kê cá nhân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setSingleLine(true);
        input.setText(username);
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle("Edit profile")
                .setMessage("Đổi tên hiển thị trong app.")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    username = value;
                    prefs.edit().putString("username", username).apply();
                    renderProfile();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPersonalInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thông tin cá nhân")
                .setMessage("Tên: " + username + "\nEmail: fonos.user@example.com\nVai trò: Thành viên")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void toggleEmailNotification() {
        boolean enabled = !prefs.getBoolean("email_enabled", true);
        prefs.edit().putBoolean("email_enabled", enabled).apply();
        renderProfile();
        Toast.makeText(this,
                enabled ? "Đã bật thông báo email" : "Đã tắt thông báo email",
                Toast.LENGTH_SHORT).show();
    }

    private void showLanguageDialog() {
        String[] names = {"Tiếng Việt", "English"};
        String[] values = {"vi", "en"};
        String current = LocaleHelper.getSavedLanguage(this);
        int checked = current.equals("vi") ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle("Ngôn ngữ")
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    LocaleHelper.saveLanguage(this, values[which]);
                    dialog.dismiss();
                    recreate();
                })
                .show();
    }

    private void showThemeDialog() {
        String[] themes = {"Tối", "Sáng", "Theo hệ thống"};
        String current = prefs.getString("theme", "Tối");
        int checked = 0;
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(current)) checked = i;
        }

        new AlertDialog.Builder(this)
                .setTitle("Giao diện")
                .setSingleChoiceItems(themes, checked, (dialog, which) -> {
                    prefs.edit().putString("theme", themes[which]).apply();
                    renderProfile();
                    dialog.dismiss();
                    Toast.makeText(this, "Đã chọn giao diện: " + themes[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void openLibrary(String filter) {
        Intent intent = new Intent(this, LibraryActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("initial_filter", filter);
        startActivity(intent);
    }

    private String makeInitial(String value) {
        if (value == null || value.trim().isEmpty()) return "B";
        return value.trim().substring(0, 1).toUpperCase();
    }
}
