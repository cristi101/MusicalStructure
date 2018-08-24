package eu.baboi.cristian.musicalstructure;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.Picture;
import eu.baboi.cristian.musicalstructure.utils.SoundPlayer;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class Track extends AppCompatActivity implements PagingCallbacks.Progress, View.OnClickListener {
    private static String LOG = Artist.class.getName();


    private String idTrack = null;

    private ProgressBar progress;

    @Override
    public ProgressBar getProgressBar() {
        return progress;
    }

    private ImageView picture;
    private TextView name;

    private String previewUrl;
    private SoundPlayer soundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        picture = findViewById(R.id.picture);
        name = findViewById(R.id.name);

        previewUrl = null;
        picture.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent == null) return;

        idTrack = intent.getStringExtra(Model.ID_KEY);
        if (TextUtils.isEmpty(idTrack)) return;

        Loaders.initLoader(this, Loaders.Id.TRACK, null, new TrackCallbacks());
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (soundPlayer != null) {
            soundPlayer.releaseMediaPlayer();
            soundPlayer = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(previewUrl)) return;
        releaseMediaPlayer();
        soundPlayer = new SoundPlayer(this, previewUrl);
    }

    private class TrackCallbacks implements LoaderManager.LoaderCallbacks<Loaders.TrackResult> {

        @NonNull
        @Override
        public Loader<Loaders.TrackResult> onCreateLoader(int id, Bundle args) {
            progress.setVisibility(View.VISIBLE);
            return new Loaders.Track(getApplicationContext(), idTrack);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.TrackResult> loader, Loaders.TrackResult data) {
            progress.setVisibility(View.GONE);

            if (data == null) return; //no results

            if (data.error != null) {
                Toast.makeText(Track.this, String.format("Search error:\n%s", data.error.message), Toast.LENGTH_LONG).show();
                return;
            }
            if (data.aerror != null) {
                Toast.makeText(Track.this, String.format("Authentication error:\n%s", data.aerror.error_description), Toast.LENGTH_LONG).show();
                return;
            }

            Model.Track track = data.track;
            if (track == null) return;

            picture.post(new Runnable() {
                @Override
                public void run() {
                    int width = picture.getMeasuredWidth();
                    Picture.setImageUri(picture, track.album.imageUri, width, width);
                }
            });

            name.setText(track.name);
            previewUrl = track.preview_url;
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.TrackResult> loader) {

        }
    }

}

