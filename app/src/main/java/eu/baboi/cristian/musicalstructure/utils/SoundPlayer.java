package eu.baboi.cristian.musicalstructure.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;


public class SoundPlayer implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
    private static final String LOG = SoundPlayer.class.getName();

    private MediaPlayer mediaPlayer;
    private final AudioManager audioManager;

    public SoundPlayer(AppCompatActivity activity, String url) {
        Uri uri = Uri.parse(url);
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = MediaPlayer.create(activity, uri);
        if (mediaPlayer == null) return;

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
        }
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            audioManager.abandonAudioFocus(this);
            mediaPlayer = null;
        }
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mediaPlayer.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                releaseMediaPlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                break;
        }
    }

    public void onCompletion(MediaPlayer mp) {
        releaseMediaPlayer();
    }

}
