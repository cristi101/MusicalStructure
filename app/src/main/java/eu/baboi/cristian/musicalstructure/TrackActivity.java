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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.Picture;
import eu.baboi.cristian.musicalstructure.utils.SoundPlayer;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class TrackActivity extends AppCompatActivity implements PagingCallbacks.Progress {
    private static String LOG = ArtistActivity.class.getName();

    private String idTrack = null;

    private ProgressBar progress;

    @Override
    public ProgressBar getProgressBar() {
        return progress;
    }

    private ImageView picture;
    private TextView trackNo;
    private TextView name;
    private TextView duration;
    private TextView artists;
    private TextView tvAlbum;

    private ToggleButton play, pause, stop;
    private LinearLayout player;

    private String previewUrl;
    private SoundPlayer soundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        Model.setupActionBar(this, "- powered by");

        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        picture = findViewById(R.id.picture);
        trackNo = findViewById(R.id.track);
        name = findViewById(R.id.name);
        duration = findViewById(R.id.duration);
        tvAlbum = findViewById(R.id.album);
        artists = findViewById(R.id.artists);

        player = findViewById(R.id.player);
        player.setVisibility(View.GONE);

        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);

        previewUrl = null;

        Intent intent = getIntent();
        if (intent == null) return;

        idTrack = intent.getStringExtra(Model.ID_KEY);
        if (TextUtils.isEmpty(idTrack)) return;

        Loaders.initLoader(this, Loaders.Id.TRACK, null, new TrackCallbacks());
    }


    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (soundPlayer != null) {
            soundPlayer.releaseMediaPlayer();
            soundPlayer = null;
        }
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

            if (data == null) {
                Toast.makeText(TrackActivity.this, "There is something wrong with your Internet connection", Toast.LENGTH_LONG).show();
                return; //no results
            }

            if (data.error != null) {
                Toast.makeText(TrackActivity.this, String.format("Error status: %d\n%s", data.error.status, data.error.message), Toast.LENGTH_LONG).show();
                return;
            }

            if (data.aerror != null) {
                Toast.makeText(TrackActivity.this, String.format("Authentication error: %s\n%s", data.aerror.error, data.aerror.error_description), Toast.LENGTH_LONG).show();
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

            trackNo.setText(String.valueOf(track.track_number));
            name.setText(track.name);
            duration.setText(Model.milisToString(track.duration_ms));

            Model.SimplifiedAlbum album = track.album;
            tvAlbum.setText(String.format("%s * %s * %s", album.name, album.album_type, album.release_date));

            StringBuilder builder = new StringBuilder();
            builder.append("Disc ");
            builder.append(track.disc_number);

            if (track.artists != null) {
                for (Model.SimplifiedArtist artist : track.artists) {
                    builder.append(" * ");
                    builder.append(artist.name);
                }
            }
            artists.setText(builder.toString());

            previewUrl = track.preview_url;

            if (!TextUtils.isEmpty(previewUrl)) {
                soundPlayer = new SoundPlayer(TrackActivity.this, previewUrl, play, pause, stop);
                player.setVisibility(View.VISIBLE);
            } else player.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.TrackResult> loader) {

        }
    }
}

