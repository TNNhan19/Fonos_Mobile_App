package com.example.login;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

public class PlayerActivity extends AppCompatActivity {

    private ImageView ivCover;
    private TextView tvTitle, tvAuthor, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnRewind, btnForward, btnBack;

    private MediaController mediaController;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initViews();
        setupMediaController();

        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void initViews() {
        ivCover = findViewById(R.id.ivPlayerCover);
        tvTitle = findViewById(R.id.tvPlayerTitle);
        tvAuthor = findViewById(R.id.tvPlayerAuthor);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.playerSeekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);
        btnBack = findViewById(R.id.btnPlayerBack);

        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (mediaController != null) {
                if (mediaController.isPlaying()) {
                    mediaController.pause();
                } else {
                    mediaController.play();
                }
            }
        });

        btnRewind.setOnClickListener(v -> {
            if (mediaController != null) mediaController.seekBack();
        });

        btnForward.setOnClickListener(v -> {
            if (mediaController != null) mediaController.seekForward();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaController != null) {
                    mediaController.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupMediaController() {
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, FonosAudioService.class));
        ListenableFuture<MediaController> controllerFuture =
                new MediaController.Builder(this, sessionToken).buildAsync();

        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                onConnected();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    private void onConnected() {
        mediaController.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                btnPlayPause.setImageResource(isPlaying
                        ? android.R.drawable.ic_media_pause
                        : android.R.drawable.ic_media_play);
                if (isPlaying) {
                    handler.post(updateProgressRunnable);
                } else {
                    handler.removeCallbacks(updateProgressRunnable);
                }
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                updateUI();
            }
        });

        updateUI();
        if (mediaController.isPlaying()) {
            handler.post(updateProgressRunnable);
        }
    }

    private void updateUI() {
        if (mediaController == null) return;

        MediaItem currentItem = mediaController.getCurrentMediaItem();
        if (currentItem != null && currentItem.mediaMetadata != null) {
            tvTitle.setText(currentItem.mediaMetadata.title);
            tvAuthor.setText(currentItem.mediaMetadata.artist);
        }

        // Cập nhật ảnh bìa từ intent nếu Media3 chưa có metadata đầy đủ
        String coverUrl = getIntent().getStringExtra("cover_url");
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this).load(coverUrl).placeholder(R.color.galaxy_card_2).into(ivCover);
        }

        btnPlayPause.setImageResource(mediaController.isPlaying()
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);

        updateSeekBar();
    }

    private void updateSeekBar() {
        if (mediaController == null) return;

        long currentPos = mediaController.getCurrentPosition();
        long duration = mediaController.getDuration();

        seekBar.setMax((int) duration);
        seekBar.setProgress((int) currentPos);

        tvCurrentTime.setText(formatTime(currentPos));
        tvTotalTime.setText(formatTime(duration));
    }

    private String formatTime(long ms) {
        if (ms < 0) return "0:00";
        int totalSeconds = (int) (ms / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateProgressRunnable);
        if (mediaController != null) {
            mediaController.release();
        }
    }
}
