package com.example.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestNotificationPermission();

        String username = getIntent().getStringExtra("username");

        // ── Greeting cá nhân hóa ──
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        if (username != null && !username.isEmpty()) {
            tvGreeting.setText("Hello, " + username + "! 👋");
        } else {
            tvGreeting.setText("Hello, bạn! 👋");
        }

        // ── Logout (icon 👤 header) ──
        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            stopService(new Intent(this, FonosAudioService.class));
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // ── CTA "Khám phá ngay" → ExploreActivity ──
        findViewById(R.id.btnExplore).setOnClickListener(v -> openExplore(username));

        // ── Category cards → ExploreActivity với filter tương ứng ──
        findViewById(R.id.cardAudiobook).setOnClickListener(v -> openExplore(username));
        findViewById(R.id.cardPodcast).setOnClickListener(v -> openExplore(username));
        findViewById(R.id.cardMeditation).setOnClickListener(v -> openExplore(username));

        // ── Bottom nav ──
        findViewById(R.id.navExplore).setOnClickListener(v -> openExplore(username));

        findViewById(R.id.navLibrary).setOnClickListener(v -> {
            Intent intent = new Intent(this, LibraryActivity.class);
            if (username != null) intent.putExtra("username", username);
            startActivity(intent);
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> openProfile(username));
    }

    private void openExplore(String username) {
        Intent intent = new Intent(this, ExploreActivity.class);
        if (username != null) intent.putExtra("username", username);
        startActivity(intent);
    }

    private void openProfile(String username) {
        Intent intent = new Intent(this, ProfileActivity.class);
        if (username != null) intent.putExtra("username", username);
        startActivity(intent);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }
}
