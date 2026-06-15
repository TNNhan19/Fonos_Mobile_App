package com.example.login;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class FonosAudioService extends MediaSessionService {

    private ExoPlayer player;
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT,  true)
                .setHandleAudioBecomingNoisy(true)
                .build();

        // 2. Khởi tạo MediaSession
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("audio_url")) {
            String url = intent.getStringExtra("audio_url");
            String title = intent.getStringExtra("title");
            String author = intent.getStringExtra("author");

            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(author)
                    .build();

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .setMediaMetadata(metadata)
                    .build();

            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }
}
