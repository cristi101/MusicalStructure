package eu.baboi.cristian.musicalstructure;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import eu.baboi.cristian.musicalstructure.adapters.TracksAdapter;
import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.Picture;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class Album extends AppCompatActivity implements PagingCallbacks.Progress {
    private static String LOG = Artist.class.getName();

    private static final String TRACKS_LIMIT_KEY = "TRACKS_LIMIT_KEY";
    private static final String TRACKS_OFFSET_KEY = "TRACKS_OFFSET_KEY";

    private String idAlbum = null;

    private ProgressBar progress;

    @Override
    public ProgressBar getProgressBar() {
        return progress;
    }

    private ImageView picture;
    private TextView name;
    private TextView genres;
    private TracksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        picture = findViewById(R.id.picture);
        name = findViewById(R.id.name);
        genres = findViewById(R.id.genres);

        RecyclerView recyclerView = findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TracksAdapter(null, this);
        recyclerView.setAdapter(adapter);
        //todo check all 4 variants
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);

        Intent intent = getIntent();
        if (intent == null) return;

        idAlbum = intent.getStringExtra(Model.ID_KEY);
        if (TextUtils.isEmpty(idAlbum)) return;

        LoaderManager manager = getSupportLoaderManager();
        manager.initLoader(Loaders.Id.ALBUM.ordinal(), null, new AlbumCallbacks());
        //manager.initLoader(Loaders.Id.ALBUM_TRACKS.ordinal(), null, new TracksCallbacks(10, 0));
    }


    private void restoreData(Bundle in) {
        int data_limit = in.getInt(TRACKS_LIMIT_KEY, -1);
        int data_offset = in.getInt(TRACKS_OFFSET_KEY, -1);
        if (data_limit > 0 && data_offset >= 0) {
            TracksCallbacks callbacks = new TracksCallbacks(data_limit, data_offset);
            Loaders.startLoader(this, Loaders.Id.ALBUM_TRACKS, null, callbacks);
        }
    }

    private void saveData(Bundle out) {
        Model.TrackPaging data = adapter.data();
        if (data != null) {
            out.putInt(TRACKS_LIMIT_KEY, data.limit);
            out.putInt(TRACKS_OFFSET_KEY, data.offset);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreData(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveData(outState);
    }

    private class AlbumCallbacks implements LoaderManager.LoaderCallbacks<Loaders.AlbumResult> {

        @NonNull
        @Override
        public Loader<Loaders.AlbumResult> onCreateLoader(int id, Bundle args) {
            progress.setVisibility(View.VISIBLE);
            return new Loaders.Album(getApplicationContext(), idAlbum);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.AlbumResult> loader, Loaders.AlbumResult data) {
            progress.setVisibility(View.GONE);

            if (data == null) return; //no results

            if (data.error != null) {
                Toast.makeText(Album.this, String.format("Search error:\n%s", data.error.message), Toast.LENGTH_LONG).show();
                return;
            }
            if (data.aerror != null) {
                Toast.makeText(Album.this, String.format("Authentication error:\n%s", data.aerror.error_description), Toast.LENGTH_LONG).show();
                return;
            }

            Model.Album album = data.album;
            if (album == null) return;

            picture.post(new Runnable() {
                @Override
                public void run() {
                    int width = picture.getMeasuredWidth();
                    Picture.setImageUri(picture, album.imageUri, width, width);
                }
            });

            name.setText(album.name);

            if (album.genres != null) {
                StringBuilder builder = new StringBuilder();
                for (String genre : album.genres) {
                    builder.append(" * ");
                    builder.append(genre);
                }
                genres.setText(builder.toString());
            }
            adapter.update(album.tracks);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.AlbumResult> loader) {

        }
    }

    private class TracksCallbacks implements LoaderManager.LoaderCallbacks<Loaders.TracksResult> {

        private final int limit;
        private final int offset;

        TracksCallbacks(int limit, int offset) {
            this.limit = limit;
            this.offset = offset;
        }

        @NonNull
        @Override
        public Loader<Loaders.TracksResult> onCreateLoader(int id, Bundle args) {
            progress.setVisibility(View.VISIBLE);
            return new Loaders.AlbumTracks(getApplicationContext(), idAlbum, limit, offset);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.TracksResult> loader, Loaders.TracksResult data) {
            progress.setVisibility(View.GONE);
            if (data == null) return; //no results

            if (data.error != null) {
                Toast.makeText(Album.this, String.format("Search error:\n%s", data.error.message), Toast.LENGTH_LONG).show();
                return;
            }
            if (data.aerror != null) {
                Toast.makeText(Album.this, String.format("Authentication error:\n%s", data.aerror.error_description), Toast.LENGTH_LONG).show();
                return;
            }
            adapter.update(data.tracks);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.TracksResult> loader) {
            adapter.update(null);
        }
    }

}