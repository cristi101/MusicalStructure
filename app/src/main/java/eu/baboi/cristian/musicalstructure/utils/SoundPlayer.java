package eu.baboi.cristian.musicalstructure.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.IOException;

public class SoundPlayer implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
    private static final String LOG = SoundPlayer.class.getName();

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private ToggleButton button; // the current checked button
    private ToggleButton play, pause, stop; // all the buttons

    public SoundPlayer(Context context, String url, ToggleButton play, ToggleButton pause, ToggleButton stop) {
        if (context == null) return;
        if (TextUtils.isEmpty(url)) return;

        this.play = play;
        this.pause = pause;
        this.stop = stop;

        Uri uri = Uri.parse(url);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        mediaPlayer = MediaPlayer.create(context, uri);
        if (mediaPlayer == null) return;

        enableButtons();
    }

    // turn off all the buttons and attach listeners
    private void enableButtons() {
        button = null;
        setChecked(play, false);
        setChecked(pause, false);
        setChecked(stop, false);

        setListener(play, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == (button == buttonView)) return;

                if (isChecked) play();
                else setChecked(button, true);//force current button to on state
            }
        });

        setListener(pause, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == (button == buttonView)) return;

                if (isChecked) pause();
                else setChecked(button, true);//force current button to on state
            }
        });

        setListener(stop, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == (button == buttonView)) return;

                if (isChecked) stop();
                else setChecked(button, true);//force current button to on state
            }
        });
    }

    // detach listeners and turn off all the buttons
    private void disableButtons() {
        button = null;

        setListener(play, null);
        setListener(pause, null);
        setListener(stop, null);

        setChecked(play, false);
        setChecked(pause, false);
        setChecked(stop, false);
    }

    // attach a listener to a button
    private void setListener(ToggleButton button, CompoundButton.OnCheckedChangeListener listener) {
        if (button != null) button.setOnCheckedChangeListener(listener);
    }

    // toggle a button
    private void setChecked(ToggleButton button, boolean checked) {
        if (button != null) button.setChecked(checked);
    }

    // turn on the button
    private void turnOn(ToggleButton which) {
        ToggleButton old = button; // save the previous button
        button = which; // new current button
        setChecked(button, true); // turn it on
        setChecked(old, false);    // turn off the previous button
    }

    // the actions for the media player buttons
    public void pause() {
        if (button == null || button == stop) {
            setChecked(pause, false); // cannot pause after stop
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.pause();
            audioManager.abandonAudioFocus(this);
        }
        turnOn(pause);
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            audioManager.abandonAudioFocus(this);
        }
        turnOn(stop);
    }

    public void play() {
        if (mediaPlayer != null) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.setOnCompletionListener(this);

                if (button == stop) {
                    try {
                        mediaPlayer.prepare();// play after stop requires calling prepare first
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mediaPlayer.start();
                turnOn(play);
            }
        } else turnOn(play);

    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            disableButtons();
            mediaPlayer.stop();
            mediaPlayer.release();
            audioManager.abandonAudioFocus(this);
            mediaPlayer = null;
        }
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stop();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pause();
                break;
        }
    }

    public void onCompletion(MediaPlayer mp) {
        audioManager.abandonAudioFocus(this);
        button = null;
        setChecked(play, false);
        setChecked(pause, false);
        setChecked(stop, false);
    }
}
