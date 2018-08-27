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

import eu.baboi.cristian.musicalstructure.adapters.AlbumsAdapter;
import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.Picture;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class ArtistActivity extends AppCompatActivity implements PagingCallbacks.Progress {
    private static String LOG = ArtistActivity.class.getName();

    private static final String ALBUMS_LIMIT_KEY = "ALBUMS_LIMIT_KEY";
    private static final String ALBUMS_OFFSET_KEY = "ALBUMS_OFFSET_KEY";

    private String idArtist = null;

    private ProgressBar progress;

    @Override
    public ProgressBar getProgressBar() {
        return progress;
    }

    private ImageView picture;
    private TextView name;
    private TextView genres;
    private AlbumsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        Model.setupActionBar(this, "- powered by");

        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        picture = findViewById(R.id.picture);
        name = findViewById(R.id.name);
        genres = findViewById(R.id.genres);

        RecyclerView recyclerView = findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumsAdapter(null, this);
        recyclerView.setAdapter(adapter);

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(false);

        Intent intent = getIntent();
        if (intent == null) return;

        idArtist = intent.getStringExtra(Model.ID_KEY);
        if (TextUtils.isEmpty(idArtist)) return;

        Loaders.initLoader(this, Loaders.Id.ARTIST, null, new ArtistCallbacks());
        Loaders.initLoader(this, Loaders.Id.ARTIST_ALBUMS, null, new AlbumsCallbacks(10, 0));
    }


    private void restoreData(Bundle in) {
        int data_limit = in.getInt(ALBUMS_LIMIT_KEY, -1);
        int data_offset = in.getInt(ALBUMS_OFFSET_KEY, -1);
        if (data_limit > 0 && data_offset >= 0) {
            AlbumsCallbacks callbacks = new AlbumsCallbacks(data_limit, data_offset);
            Loaders.startLoader(this, Loaders.Id.ARTIST_ALBUMS, null, callbacks);
        }
    }

    private void saveData(Bundle out) {
        Model.AlbumPaging data = adapter.data();
        if (data != null) {
            out.putInt(ALBUMS_LIMIT_KEY, data.limit);
            out.putInt(ALBUMS_OFFSET_KEY, data.offset);
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

    private class ArtistCallbacks implements LoaderManager.LoaderCallbacks<Loaders.ArtistResult> {

        @NonNull
        @Override
        public Loader<Loaders.ArtistResult> onCreateLoader(int id, Bundle args) {
            progress.setVisibility(View.VISIBLE);
            return new Loaders.Artist(getApplicationContext(), idArtist);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.ArtistResult> loader, Loaders.ArtistResult data) {
            progress.setVisibility(View.GONE);

            if (data == null) {
                Toast.makeText(ArtistActivity.this, "There is something wrong with your Internet connection", Toast.LENGTH_LONG).show();
                return; //no results
            }

            if (data.error != null) {
                Toast.makeText(ArtistActivity.this, String.format("Error status: %d\n%s", data.error.status, data.error.message), Toast.LENGTH_LONG).show();
                return;
            }
            if (data.aerror != null) {
                Toast.makeText(ArtistActivity.this, String.format("Authentication error: %s\n%s", data.aerror.error, data.aerror.error_description), Toast.LENGTH_LONG).show();
                return;
            }

            Model.Artist artist = data.artist;
            if (artist == null) return;

            picture.post(new Runnable() {
                @Override
                public void run() {
                    int width = picture.getMeasuredWidth();
                    Picture.setImageUri(picture, artist.imageUri, width, width);
                }
            });

            name.setText(artist.name);

            StringBuilder builder = new StringBuilder();
            if (artist.genres != null) {
                for (String genre : artist.genres) {
                    builder.append(" * ");
                    builder.append(genre);
                }
            }
            genres.setText(builder.toString());
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.ArtistResult> loader) {

        }
    }

    private class AlbumsCallbacks implements LoaderManager.LoaderCallbacks<Loaders.AlbumsResult> {

        private final int limit;
        private final int offset;

        AlbumsCallbacks(int limit, int offset) {
            this.limit = limit;
            this.offset = offset;
        }

        @NonNull
        @Override
        public Loader<Loaders.AlbumsResult> onCreateLoader(int id, Bundle args) {
            progress.setVisibility(View.VISIBLE);
            return new Loaders.ArtistAlbums(getApplicationContext(), idArtist, limit, offset);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.AlbumsResult> loader, Loaders.AlbumsResult data) {
            progress.setVisibility(View.GONE);
            if (data == null) {
                Toast.makeText(ArtistActivity.this, "There is something wrong with your Internet connection", Toast.LENGTH_LONG).show();
                return; //no results
            }

            if (data.error != null) {
                Toast.makeText(ArtistActivity.this, String.format("Error status: %d\n%s", data.error.status, data.error.message), Toast.LENGTH_LONG).show();
                return;
            }
            if (data.aerror != null) {
                Toast.makeText(ArtistActivity.this, String.format("Authentication error: %s\n%s", data.aerror.error, data.aerror.error_description), Toast.LENGTH_LONG).show();
                return;
            }
            adapter.update(data.albums);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.AlbumsResult> loader) {
            adapter.update(null);
        }
    }

}
