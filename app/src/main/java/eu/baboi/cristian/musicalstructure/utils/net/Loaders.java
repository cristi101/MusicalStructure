package eu.baboi.cristian.musicalstructure.utils.net;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;

import eu.baboi.cristian.musicalstructure.utils.secret.DataStore;

public class Loaders {
    private static final String LOG = Loaders.class.getName();

    public enum Id {
        TOKENS,
        TOKEN,

        SEARCH,
        SEARCH_ARTISTS,
        SEARCH_ALBUMS,
        SEARCH_TRACKS,

        ARTISTS_PAGING,
        ALBUMS_PAGING,
        TRACKS_PAGING,

        ARTIST,
        ARTIST_ALBUMS,
        ARTIST_ALBUMS_PAGING,

        ALBUM,
        ALBUM_TRACKS,
        ALBUM_TRACKS_PAGING,

        TRACK
    }

    private Loaders() {
    }

    //check if network available
    public static boolean noNetwork(Context context) {
        //check network connectivity
        if (!Model.hasNetwork(context)) {
            Toast toast = Toast.makeText(context, "No network!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return true;
        }
        return false;
    }

    //starts a loader - force loading data
    public static <T> void startLoader(AppCompatActivity activity, Id id, Bundle args, LoaderManager.LoaderCallbacks<T> callbacks) {
        if (noNetwork(activity)) return;

        int which = id.ordinal();
        LoaderManager manager = activity.getSupportLoaderManager();
        Loader loader = manager.getLoader(which);
        if (loader == null) manager.initLoader(which, args, callbacks);
        else manager.restartLoader(which, args, callbacks);
    }

    public static <T> void initLoader(AppCompatActivity activity, int id, Bundle args, LoaderManager.LoaderCallbacks<T> callbacks) {
        LoaderManager manager = activity.getSupportLoaderManager();
        manager.initLoader(id, args, callbacks);
    }

    //destroy given loader
    public static void destroyLoader(AppCompatActivity activity, int id) {
        LoaderManager manager = activity.getSupportLoaderManager();
        manager.destroyLoader(id);
    }


    // download the pictures
    private static <T, P extends Model.Paging<T>> void getPictures(File cacheDir, P paging) {
        if (cacheDir == null) return;
        if (paging == null) return;

        //get pictures
        if (paging instanceof Model.ImageItems) {
            Model.ImageItems items = (Model.ImageItems) paging;
            Model.ImageItem[] itemArray = items.getImageItems();
            if (itemArray != null)
                for (Model.ImageItem item : itemArray) {
                    Model.Image image = item.getImage();
                    if (image != null) {
                        Uri picture = HTTP.getPicture(cacheDir, image.url);
                        item.setImageUri(picture);
                    }
                }
        }
    }

    // force access token refresh
    private static String refreshToken(DataStore dataStore) throws Model.AuthenticationError {
        Model.Token token;

        String refresh_token = dataStore.getString(Model.REFRESH_TOKEN, null);
        if (TextUtils.isEmpty(refresh_token)) return null; //no refresh token

        token = Model.refreshToken(refresh_token, dataStore);
        if (token == null) return null;

        //save the new access token
        dataStore.putString(Model.ACCESS_TOKEN, token.access_token);
        dataStore.putLong(Model.EXPIRES_AT, token.expires_at);

        return token.access_token;
    }

    // refresh access token if necessary
    private static String getToken(DataStore dataStore) throws Model.AuthenticationError {
        String access_token = null;// current access token
        Model.Token token = null;  // new access token

        String refresh_token = dataStore.getString(Model.REFRESH_TOKEN, null);
        if (TextUtils.isEmpty(refresh_token)) return null;//no refresh token

        long expires_at = dataStore.getLong(Model.EXPIRES_AT, 0);
        if (expires_at > 0) {//found token
            long now = System.currentTimeMillis();
            if (now < expires_at) access_token = dataStore.getString(Model.ACCESS_TOKEN, null);
            else token = Model.refreshToken(refresh_token, dataStore);
        } else token = Model.refreshToken(refresh_token, dataStore);

        if (token == null) return access_token;// no new access token

        //save the new access token
        dataStore.putString(Model.ACCESS_TOKEN, token.access_token);
        dataStore.putLong(Model.EXPIRES_AT, token.expires_at);

        return token.access_token;
    }

    // fetch new authorization tokens from Spotify - tokens saved in loader callbacks
    public static class GetTokens extends AsyncTaskLoader<TokensResult> {
        private final String code;// authorization code
        TokensResult data = null;

        public GetTokens(Context context, String code) {
            super(context);
            this.code = code;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public TokensResult loadInBackground() {
            Model.Tokens tokens;
            try {
                //get both tokens
                DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
                tokens = Model.getTokens(code, dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new TokensResult(null, error);
            }
            return new TokensResult(tokens, null);
        }

        @Override
        public void deliverResult(TokensResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class TokensResult {
        public final Model.Tokens tokens;
        public final Model.AuthenticationError error;

        TokensResult(Model.Tokens tokens, Model.AuthenticationError error) {
            this.tokens = tokens;
            this.error = error;
        }
    }

    // fetch a new access token from Spotify - token saved in loader callbacks
    public static class GetToken extends AsyncTaskLoader<TokenResult> {
        TokenResult data = null;

        public GetToken(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public TokenResult loadInBackground() {
            Model.Token token;
            try {
                DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
                String refresh_token = dataStore.getString(Model.REFRESH_TOKEN, null);
                token = Model.refreshToken(refresh_token, dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new TokenResult(null, error);
            }
            return new TokenResult(token, null);
        }

        @Override
        public void deliverResult(TokenResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class TokenResult {
        public final Model.Token token;
        public final Model.AuthenticationError error;

        TokenResult(Model.Token token, Model.AuthenticationError error) {
            this.token = token;
            this.error = error;
        }
    }

    // perform search for artists, albums, tracks on background thread
    public static class Search extends AsyncTaskLoader<SearchResult> {
        final private File cacheDir; // the folder to load pictures

        SearchResult data = null;

        private final String query;
        private final int limit;
        private final int offset;
        private final int which;

        public Search(Context context, String query, int which, int limit, int offset) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.query = query;
            this.limit = limit;
            this.offset = offset;
            this.which = which;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }


        @Override
        public SearchResult loadInBackground() {
            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new SearchResult(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token))
                    return null; //no token found - no search result
                Model.setToken(access_token);

                Model.Ref<Model.TrackSearch> tracks = null;
                Model.Ref<Model.AlbumSearch> albums = null;
                Model.Ref<Model.ArtistSearch> artists = null;

                int d = which;
                if (d >= 4) {
                    d -= 4;
                    tracks = new Model.Ref<>();
                }
                if (d >= 2) {
                    d -= 2;
                    albums = new Model.Ref<>();
                }
                if (d >= 1) {
                    d -= 1;
                    artists = new Model.Ref<>();
                }

                try {
                    Model.search(query, artists, albums, tracks, limit, offset);
                    if (artists != null) getPictures(cacheDir, artists.ref);
                    if (albums != null) getPictures(cacheDir, albums.ref);
                    return new SearchResult(artists, albums, tracks);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new SearchResult(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new SearchResult(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(SearchResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class SearchResult {
        public final Model.ArtistSearch artists;
        public final Model.AlbumSearch albums;
        public final Model.TrackSearch tracks;
        public final Model.Error error;
        public final Model.AuthenticationError aerror;

        SearchResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.artists = null;
            this.albums = null;
            this.tracks = null;
        }

        SearchResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.artists = null;
            this.albums = null;
            this.tracks = null;
        }

        SearchResult(Model.Ref<Model.ArtistSearch> artists, Model.Ref<Model.AlbumSearch> albums, Model.Ref<Model.TrackSearch> tracks) {
            this.aerror = null;
            this.error = null;
            this.artists = artists != null ? artists.ref : null;
            this.albums = albums != null ? albums.ref : null;
            this.tracks = tracks != null ? tracks.ref : null;
        }
    }

    // perform search for artists, albums, tracks on background thread
    public static class Paging<T, P extends Model.Paging<T>> extends AsyncTaskLoader<PagingResult<T, P>> {
        final private File cacheDir; // the folder to load pictures
        PagingResult<T, P> data = null;

        private final Model.Paging.Direction dir;
        private final P paging;

        public Paging(Context context, Model.Paging.Direction dir, P paging) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.dir = dir;
            this.paging = paging;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public PagingResult<T, P> loadInBackground() {
            if (paging == null) return null;

            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new PagingResult<>(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token))
                    return null; //no token found - no search result
                Model.setToken(access_token);
                try {
                    P result = null;
                    switch (dir) {
                        case PREV:
                            result = (P) paging.prev();
                            break;
                        case NEXT:
                            result = (P) paging.next();
                            break;
                        default:
                    }
                    //get pictures
                    getPictures(cacheDir, result);
                    return new PagingResult<>(result);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new PagingResult<>(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new PagingResult<>(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(PagingResult<T, P> data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class PagingResult<T, P extends Model.Paging<T>> {
        public final P paging;
        public final Model.Error error;
        public final Model.AuthenticationError aerror;

        PagingResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.paging = null;
        }

        PagingResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.paging = null;
        }

        PagingResult(P paging) {
            this.aerror = null;
            this.error = null;
            this.paging = paging;
        }
    }


    // fetch given artist data
    public static class Artist extends AsyncTaskLoader<ArtistResult> {
        final private File cacheDir; // the folder to load pictures
        ArtistResult data = null;

        final String id;

        public Artist(Context context, String id) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.id = id;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public ArtistResult loadInBackground() {
            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new ArtistResult(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token)) return null; //no token found - no result
                Model.setToken(access_token);

                try {
                    Model.Artist result = Model.Artist.get(id);
                    if (result != null) {
                        if (result.images != null) {
                            Uri picture = HTTP.getPicture(cacheDir, result.images[0].url);
                            result.setImageUri(picture);
                        }
                    }
                    return new ArtistResult(result);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new ArtistResult(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new ArtistResult(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(ArtistResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class ArtistResult {
        public final Model.Error error;
        public final Model.AuthenticationError aerror;
        public final Model.Artist artist;

        ArtistResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.artist = null;
        }

        ArtistResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.artist = null;
        }

        ArtistResult(Model.Artist artist) {
            this.aerror = null;
            this.error = null;
            this.artist = artist;
        }
    }

    // fetch given artist albums
    public static class ArtistAlbums extends AsyncTaskLoader<AlbumsResult> {
        final private File cacheDir; // the folder to load pictures
        AlbumsResult data = null;

        private final String id;
        private final int limit;
        private final int offset;

        public ArtistAlbums(Context context, String id, int limit, int offset) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.id = id;
            this.limit = limit;
            this.offset = offset;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public AlbumsResult loadInBackground() {
            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new AlbumsResult(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token)) return null; //no token found - no result
                Model.setToken(access_token);

                try {
                    Model.AlbumPaging result = Model.Artist.getAlbums(id, limit, offset);
                    getPictures(cacheDir, result);
                    return new AlbumsResult(result);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new AlbumsResult(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new AlbumsResult(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(AlbumsResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class AlbumsResult {
        public final Model.Error error;
        public final Model.AuthenticationError aerror;
        public final Model.AlbumPaging albums;

        AlbumsResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.albums = null;
        }

        AlbumsResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.albums = null;
        }

        AlbumsResult(Model.AlbumPaging albums) {
            this.aerror = null;
            this.error = null;
            this.albums = albums;
        }
    }

    // fetch given album data
    public static class Album extends AsyncTaskLoader<AlbumResult> {
        final private File cacheDir; // the folder to load pictures
        AlbumResult data = null;

        final String id;

        public Album(Context context, String id) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.id = id;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public AlbumResult loadInBackground() {
            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new AlbumResult(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token)) return null; //no token found - no result
                Model.setToken(access_token);

                try {
                    Model.Album result = Model.Album.get(id);
                    if (result != null) {
                        if (result.images != null) {
                            Uri picture = HTTP.getPicture(cacheDir, result.images[0].url);
                            result.setImageUri(picture);
                        }
                    }
                    return new AlbumResult(result);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new AlbumResult(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new AlbumResult(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(AlbumResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class AlbumResult {
        public final Model.Error error;
        public final Model.AuthenticationError aerror;
        public final Model.Album album;

        AlbumResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.album = null;
        }

        AlbumResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.album = null;
        }

        AlbumResult(Model.Album album) {
            this.aerror = null;
            this.error = null;
            this.album = album;
        }
    }

    // fetch given album tracks
    public static class AlbumTracks extends AsyncTaskLoader<TracksResult> {
        final private File cacheDir; // the folder to load pictures
        TracksResult data = null;

        private final String id;
        private final int limit;
        private final int offset;

        public AlbumTracks(Context context, String id, int limit, int offset) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.id = id;
            this.limit = limit;
            this.offset = offset;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public TracksResult loadInBackground() {
            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new TracksResult(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token)) return null; //no token found - no result
                Model.setToken(access_token);

                try {
                    Model.TrackPaging result = Model.Album.getTracks(id, limit, offset);
                    getPictures(cacheDir, result);
                    return new TracksResult(result);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new TracksResult(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new TracksResult(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(TracksResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class TracksResult {
        public final Model.Error error;
        public final Model.AuthenticationError aerror;
        public final Model.TrackPaging tracks;

        TracksResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.tracks = null;
        }

        TracksResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.tracks = null;
        }

        TracksResult(Model.TrackPaging tracks) {
            this.aerror = null;
            this.error = null;
            this.tracks = tracks;
        }
    }

    //todo aici =============================================================
    // fetch given track data
    public static class Track extends AsyncTaskLoader<TrackResult> {
        final private File cacheDir; // the folder to load pictures
        TrackResult data = null;

        final String id;

        public Track(Context context, String id) {
            super(context);
            cacheDir = context.getDir("cachedPictures", 0);
            this.id = id;
        }

        @Override
        protected void onStartLoading() {
            if (data != null) {
                if (takeContentChanged()) data = null;
                else deliverResult(data); // use cached data
            }
            if (data == null) forceLoad();
        }

        @Override
        public TrackResult loadInBackground() {
            DataStore dataStore = new DataStore(getContext(), Model.PASSWORD);
            String access_token;

            try {
                access_token = getToken(dataStore);
            } catch (Model.AuthenticationError error) {
                Log.e(LOG, String.format("error=%s desc=%s", error.error, error.error_description), error);
                return new TrackResult(error); // the refresh token not valid
            }

            for (int count = 2; count > 0; count--) {// try 2 times
                if (TextUtils.isEmpty(access_token)) return null; //no token found - no result
                Model.setToken(access_token);

                try {
                    Model.Track result = Model.Track.get(id);
                    if (result != null) {
                        if (result.album != null) {
                            Uri picture = HTTP.getPicture(cacheDir, result.album.images[0].url);
                            result.album.setImageUri(picture);
                        }
                    }
                    return new TrackResult(result);
                } catch (Model.Error error) {
                    Log.e(LOG, String.format("status=%d message=%s", error.status, error.message), error);
                    if (error.status == 401 && count > 1) {//authorization error - refresh token
                        try {
                            access_token = refreshToken(dataStore);//new access token
                        } catch (Model.AuthenticationError aerror) {
                            Log.e(LOG, String.format("error=%s desc=%s", aerror.error, aerror.error_description), error);
                            return new TrackResult(aerror); // the refresh token not valid
                        }
                        //fresh access token
                        continue;//try again
                    }
                    return new TrackResult(error);
                }
            }
            return null;//not reached - only if two refreshed access tokens give authorization error which is impossible
        }

        @Override
        public void deliverResult(TrackResult data) {
            this.data = data; // save the data for later
            super.deliverResult(data);
        }
    }

    public static class TrackResult {
        public final Model.Error error;
        public final Model.AuthenticationError aerror;
        public final Model.Track track;

        TrackResult(Model.AuthenticationError error) {
            this.aerror = error;
            this.error = null;
            this.track = null;
        }

        TrackResult(Model.Error error) {
            this.aerror = null;
            this.error = error;
            this.track = null;
        }

        TrackResult(Model.Track track) {
            this.aerror = null;
            this.error = null;
            this.track = track;
        }
    }

}
