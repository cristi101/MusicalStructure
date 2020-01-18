package eu.baboi.cristian.musicalstructure;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import eu.baboi.cristian.musicalstructure.adapters.SearchAlbumsAdapter;
import eu.baboi.cristian.musicalstructure.adapters.SearchArtistsAdapter;
import eu.baboi.cristian.musicalstructure.adapters.SearchTracksAdapter;
import eu.baboi.cristian.musicalstructure.fragments.AlbumsFragment;
import eu.baboi.cristian.musicalstructure.fragments.ArtistsFragment;
import eu.baboi.cristian.musicalstructure.fragments.CategoryAdapter;
import eu.baboi.cristian.musicalstructure.fragments.TracksFragment;
import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.activities.SettingsActivity;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;
import eu.baboi.cristian.musicalstructure.utils.secret.DataStore;

public class MainActivity extends AppCompatActivity
        implements
        TextView.OnEditorActionListener,
        View.OnClickListener,
        ArtistsFragment.IArtists,
        AlbumsFragment.IAlbums,
        TracksFragment.ITracks,
        PagingCallbacks.Progress {

    private static final String LOG = MainActivity.class.getName();

    private static final String CODE_KEY = "code";
    private static final String QUERY_KEY = "query";

    private static final String ARTISTS_LIMIT_KEY = "ARTISTS_LIMIT_KEY";
    private static final String ARTISTS_OFFSET_KEY = "ARTISTS_OFFSET_KEY";
    private static final String ALBUMS_LIMIT_KEY = "ALBUMS_LIMIT_KEY";
    private static final String ALBUMS_OFFSET_KEY = "ALBUMS_OFFSET_KEY";
    private static final String TRACKS_LIMIT_KEY = "TRACKS_LIMIT_KEY";
    private static final String TRACKS_OFFSET_KEY = "TRACKS_OFFSET_KEY";

    private static final int TRACKS = 4;
    private static final int ALBUMS = 2;
    private static final int ARTISTS = 1;

    private enum Request {
        LOGIN,
        LOGOUT
    }

    private AlertDialog dialog = null;
    private DataStore dataStore = null;

    private String state = "123";// random state
    private boolean locked = true;

    private EditText tvQuery = null;
    private ImageView search = null;

    private ProgressBar progress = null;

    @Override
    public ProgressBar getProgressBar() {
        return progress;
    }

    // set from the fragment constructors
    private SearchArtistsAdapter artistsAdapter;
    private SearchAlbumsAdapter albumsAdapter;
    private SearchTracksAdapter tracksAdapter;

    public void setArtistsAdapter(SearchArtistsAdapter adapter) {
        artistsAdapter = adapter;
    }

    public void setAlbumsAdapter(SearchAlbumsAdapter adapter) {
        albumsAdapter = adapter;
    }

    public void setTracksAdapter(SearchTracksAdapter adapter) {
        tracksAdapter = adapter;
    }


    //logout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Model.setupActionBar(this, "&");

        tvQuery = findViewById(R.id.query);
        tvQuery.setOnEditorActionListener(this);
        tvQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard(v);
            }
        });

        search = findViewById(R.id.search);
        search.setOnClickListener(this);

        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        ViewPager viewPager = findViewById(R.id.view_pager);
        FragmentPagerAdapter fragmentAdapter = new CategoryAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2);

        prepareLocking();
    }

    // ask for password then init screen
    private void prepareLocking() {
        dataStore = new DataStore(this, Model.PASSWORD);
        String password = dataStore.getString(Model.PASSWORD_KEY, null);
        if (TextUtils.isEmpty(password)) showPasswordDialog();
        else initLocking();
    }

    // ask for the password, save the password
    private void showPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_message)
                .setView(dialogView);
        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dlg, int which) {
                if (dialog != null) {
                    EditText text = dialog.findViewById(R.id.password);
                    String password = text.getText().toString().trim();
                    //Here you can encode the secret api key
                    //String scrt=Key.encodeApiKey(password, Model.RSECRET);
                    //Log.e("===SECRET===",scrt);
                    dialog.dismiss();
                    dialog = null;

                    if (TextUtils.isEmpty(password)) showPasswordDialog();
                    else {//save the password into preferences
                        if (dataStore != null)
                            dataStore.putString(Model.PASSWORD_KEY, password);
                        initLocking();
                    }
                }
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    // detect the state of the lock
    // ends by calling either finish_locking() or finish_unlocking()
    private void initLocking() {
        // detect if logged in
        String refresh_token = dataStore.getString(Model.REFRESH_TOKEN, null);
        if (TextUtils.isEmpty(refresh_token)) finish_locking();//no refresh token - lock the screen
        else { //have refresh token
            long expires_at = dataStore.getLong(Model.EXPIRES_AT, 0);
            if (expires_at > 0) {
                long now = System.currentTimeMillis();
                if (now < expires_at) finish_unlocking();
                else getToken();//token expired - try to get a new one
            } else getToken();// no token - try to get a new one
        }
    }

    //start a loader to get an access token
    private void getToken() {
        TokenCallback callback = new TokenCallback();
        Loaders.startLoader(this, Loaders.Id.TOKEN, null, callback);
    }

    //  menu handling
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem;
        if (locked) { // add mode
            menuItem = menu.findItem(R.id.locked);
            menuItem.setVisible(true);
            menuItem = menu.findItem(R.id.unlocked);
            menuItem.setVisible(false);
        } else {
            menuItem = menu.findItem(R.id.locked);
            menuItem.setVisible(false);
            menuItem = menu.findItem(R.id.unlocked);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.locked:
                unlock();
                return true;
            case R.id.unlocked:
                lock();
                return true;
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //restore the data
    private void restoreData(Bundle in) {

        hideKeyboard(tvQuery);
        if (locked) return;

        int artists_limit = in.getInt(ARTISTS_LIMIT_KEY, -1);
        int artists_offset = in.getInt(ARTISTS_OFFSET_KEY, -1);


        int albums_limit = in.getInt(ALBUMS_LIMIT_KEY, -1);
        int albums_offset = in.getInt(ALBUMS_OFFSET_KEY, -1);


        int tracks_limit = in.getInt(TRACKS_LIMIT_KEY, -1);
        int tracks_offset = in.getInt(TRACKS_OFFSET_KEY, -1);

        SearchCallbacks callbacks;

        if (tracks_offset >= 0 && tracks_limit > 0) {
            callbacks = new SearchCallbacks(TRACKS, tracks_limit, tracks_offset);
            Loaders.startLoader(this, Loaders.Id.SEARCH_TRACKS, null, callbacks);
        }

        if (albums_offset >= 0 && albums_limit > 0) {
            callbacks = new SearchCallbacks(ALBUMS, albums_limit, albums_offset);
            Loaders.startLoader(this, Loaders.Id.SEARCH_ALBUMS, null, callbacks);
        }

        if (artists_offset >= 0 && artists_limit > 0) {
            callbacks = new SearchCallbacks(ARTISTS, artists_limit, artists_offset);
            Loaders.startLoader(this, Loaders.Id.SEARCH_ARTISTS, null, callbacks);
        }
    }

    //save the data
    private void saveData(Bundle out) {
        Model.ArtistSearch artists = artistsAdapter == null ? null : artistsAdapter.data();
        Model.AlbumSearch albums = albumsAdapter == null ? null : albumsAdapter.data();
        Model.TrackSearch tracks = tracksAdapter == null ? null : tracksAdapter.data();
        if (artists != null) {
            out.putInt(ARTISTS_LIMIT_KEY, artists.limit);
            out.putInt(ARTISTS_OFFSET_KEY, artists.offset);
        }
        if (albums != null) {
            out.putInt(ALBUMS_LIMIT_KEY, albums.limit);
            out.putInt(ALBUMS_OFFSET_KEY, albums.offset);
        }
        if (tracks != null) {
            out.putInt(TRACKS_LIMIT_KEY, tracks.limit);
            out.putInt(TRACKS_OFFSET_KEY, tracks.offset);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (dialog != null) {
            dialog.dismiss();//prevent leak
            dialog = null;
        }
        outState.putString(Model.STATE_KEY, state);
        saveData(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        state = savedInstanceState.getString(Model.STATE_KEY);
        restoreData(savedInstanceState);
    }

    // perform a logout from Spotify. Continues with call to onActivityResult()
    private void lock() {
        Model.logout(MainActivity.this, Request.LOGOUT.ordinal());
    }

    // finish locking the app
    private void finish_locking() {
        // disable interface
        enableSearch(false);

        //forget tokens
        dataStore.putString(Model.REFRESH_TOKEN, null);
        dataStore.putLong(Model.EXPIRES_AT, 0);
        dataStore.putString(Model.ACCESS_TOKEN, null);

        locked = true;
        invalidateOptionsMenu();//refresh the menu
    }

    private void enableSearch(boolean enabled) {
        tvQuery.setEnabled(enabled);
        search.setEnabled(enabled);
    }


    // perform a Spotify login. Continues with call to onActivityResult()
    private void unlock() {
        state = Model.getRandomString(8);
        Model.login(this, Request.LOGIN.ordinal(), state);
    }

    // finish unlocking the app
    private void finish_unlocking() {
        locked = false;
        invalidateOptionsMenu();//refresh the menu
        enableSearch(true);
        String query = dataStore.getString(QUERY_KEY, null);
        if (!TextUtils.isEmpty(query)) {
            tvQuery.setText(query);
            search();
        }
    }

    // return from lock()/unlock() calls
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //return from logout
        if (requestCode == Request.LOGOUT.ordinal()) {

            if (resultCode == RESULT_OK) {
                finish_locking();//logout successful
                return;
            }
            Toast.makeText(this, "Logout failed!", Toast.LENGTH_LONG).show();
            return;// do nothing if not successful - stay in unlocked state
        }


        // use loader to retrieve the authorization tokens
        if (requestCode != Request.LOGIN.ordinal() || resultCode != RESULT_OK || data == null) {
            Toast.makeText(this, "Authentication failed!", Toast.LENGTH_LONG).show();
            return;// do nothing if login not successful - stay in locked state
        }

        //return from login
        Uri uri = data.getData();
        if (uri == null) return;
        String rstate = uri.getQueryParameter(Model.STATE);
        if (!(state.equals(rstate))) {
            return;
        }

        String error = uri.getQueryParameter(Model.ERROR);
        String code = uri.getQueryParameter(Model.CODE);

        if (error == null) {
            TokensCallback callback = new TokensCallback();
            Loaders.startLoader(this, Loaders.Id.TOKENS, newCode(code), callback);
        } else {
            Log.e(LOG, error);
            Toast.makeText(this, String.format("There was an error during authentication:\n%s", error), Toast.LENGTH_LONG).show();
        }
    }


    // search related methods
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, 0);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void search() {
        SearchCallbacks callbacks = new SearchCallbacks(TRACKS + ALBUMS + ARTISTS, 10, 0);
        Loaders.startLoader(this, Loaders.Id.SEARCH, null, callbacks);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        hideKeyboard(v);
        String query = tvQuery.getText().toString().trim();
        dataStore.putString(QUERY_KEY, query);
        search();
        return true;
    }

    @Override
    public void onClick(View v) {
        hideKeyboard(v);
        String query = tvQuery.getText().toString().trim();
        dataStore.putString(QUERY_KEY, query);
        search();
    }


    //all loader callbacks

    private Bundle newCode(String code) {
        Bundle bundle = new Bundle(1);
        bundle.putString(CODE_KEY, code);
        return bundle;
    }

    // callbacks for receiving authorization tokens - only started by calling unlock()
    private class TokensCallback implements LoaderManager.LoaderCallbacks<Loaders.TokensResult> {
        @NonNull
        @Override
        public Loader<Loaders.TokensResult> onCreateLoader(int id, Bundle args) {
            if (id == Loaders.Id.TOKENS.ordinal()) {
                String code = args.getString(CODE_KEY);
                return new Loaders.GetTokens(getApplicationContext(), code);
            }
            return null;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.TokensResult> loader, Loaders.TokensResult data) {
            Loaders.destroyLoader(MainActivity.this, loader.getId());

            if (data == null) {
                Toast.makeText(MainActivity.this, "Error during authentication!", Toast.LENGTH_LONG).show();
                finish_locking();
                return;
            }

            if (data.error != null) {
                Toast.makeText(MainActivity.this, String.format("Error during authentication:\n%s\n%s", data.error.error, data.error.error_description), Toast.LENGTH_LONG).show();
                finish_locking();
                return;
            }
            if (data.tokens == null) {
                Toast.makeText(MainActivity.this, "Error during authentication!", Toast.LENGTH_LONG).show();
                finish_locking();
                return;
            }

            //store the token & unlock the app
            dataStore.putString(Model.ACCESS_TOKEN, data.tokens.access_token);
            dataStore.putString(Model.REFRESH_TOKEN, data.tokens.refresh_token);
            dataStore.putLong(Model.EXPIRES_AT, data.tokens.expires_at);
            finish_unlocking();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.TokensResult> loader) {

        }
    }

    // callbacks for receiving access token
    private class TokenCallback implements LoaderManager.LoaderCallbacks<Loaders.TokenResult> {

        @NonNull
        @Override
        public Loader<Loaders.TokenResult> onCreateLoader(int id, Bundle args) {
            if (id == Loaders.Id.TOKEN.ordinal())
                return new Loaders.GetToken(getApplicationContext());
            return null;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.TokenResult> loader, Loaders.TokenResult data) {
            Loaders.destroyLoader(MainActivity.this, loader.getId());

            if (data == null) {
                Toast.makeText(MainActivity.this, "Error getting access token!", Toast.LENGTH_LONG).show();
                finish_locking();
                return;
            }
            if (data.error != null) {
                Toast.makeText(MainActivity.this, String.format("Error getting access token:\n%s\n%s", data.error.error, data.error.error_description), Toast.LENGTH_LONG).show();
                finish_locking();
                return;
            }
            if (data.token == null) {
                Toast.makeText(MainActivity.this, "Error getting access token!", Toast.LENGTH_LONG).show();
                finish_locking();
                return;
            }
            //store the token &  unlock the app
            dataStore.putString(Model.ACCESS_TOKEN, data.token.access_token);
            dataStore.putLong(Model.EXPIRES_AT, data.token.expires_at);
            finish_unlocking();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.TokenResult> loader) {

        }
    }

    //todo all callbacks authentication error should display error in addition to error_description

    // callbacks for receiving search results
    private class SearchCallbacks implements LoaderManager.LoaderCallbacks<Loaders.SearchResult> {
        private final int which;
        private final int limit;
        private final int offset;

        SearchCallbacks(int which, int limit, int offset) {
            this.which = which;
            this.limit = limit;
            this.offset = offset;
        }

        @NonNull
        @Override
        public Loader<Loaders.SearchResult> onCreateLoader(int id, Bundle args) {
            progress.setVisibility(View.VISIBLE);
            String query = tvQuery.getText().toString().trim();
            return new Loaders.Search(getApplicationContext(), query, which, limit, offset);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Loaders.SearchResult> loader, Loaders.SearchResult data) {
            progress.setVisibility(View.GONE);

            if (data == null) {
                Toast.makeText(MainActivity.this, "There is something wrong with your Internet connection", Toast.LENGTH_LONG).show();
                return; //no search results
            }
            if (data.error != null) {
                Toast.makeText(MainActivity.this, String.format("Error status: %d\n%s", data.error.status, data.error.message), Toast.LENGTH_LONG).show();
                return;
            }
            if (data.aerror != null) {
                Toast.makeText(MainActivity.this, String.format("Authentication error: %s\n%s", data.aerror.error, data.aerror.error_description), Toast.LENGTH_LONG).show();
                return;
            }

            int d = which;
            if (d >= TRACKS) {
                d -= TRACKS;
                if (tracksAdapter != null) {
                    tracksAdapter.update(data.tracks);
                    tracksAdapter.scrollToTop();
                }
            }

            if (d >= ALBUMS) {
                d -= ALBUMS;
                if (albumsAdapter != null) {
                    albumsAdapter.update(data.albums);
                    albumsAdapter.scrollToTop();
                }
            }

            if (d >= ARTISTS) {
                d -= ARTISTS;
                if (artistsAdapter != null) {
                    artistsAdapter.update(data.artists);
                    artistsAdapter.scrollToTop();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Loaders.SearchResult> loader) {
            int d = which;
            if (d >= TRACKS) {
                d -= TRACKS;
                if (tracksAdapter != null)
                    tracksAdapter.update(null);
            }

            if (d >= ALBUMS) {
                d -= ALBUMS;
                if (albumsAdapter != null)
                    albumsAdapter.update(null);
            }

            if (d >= ARTISTS) {
                d -= ARTISTS;
                if (artistsAdapter != null)
                    artistsAdapter.update(null);
            }
        }
    }
}
