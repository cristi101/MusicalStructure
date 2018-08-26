package eu.baboi.cristian.musicalstructure.utils.net;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.utils.activities.LoginCallback;
import eu.baboi.cristian.musicalstructure.utils.secret.DataStore;
import eu.baboi.cristian.musicalstructure.utils.secret.Key;

public class Model {
    private static final String LOG = Model.class.getName();

    public static final String ID_KEY = "id";

    public static final String REDIRECT_URI = "eu.baboi.cristian.musicalstructure://callback";
    public static final String AUTHORIZE_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    public static final String LOGOUT_URL = "https://accounts.spotify.com/en/logout";

    public static final String CLIENT = "05865a6984e2407f980620ee17a2368e";
    private static final String SECRET = "{zQ%NE0EC'{$)zyORTzH5Fw,L+)Gz $T";
    public static final String PASSWORD = "once upon a time";
    public static final String PASSWORD_KEY = "password";

    public static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String REDIRECT = "redirect_uri";
    public static final String STATE = "state";
    private static final String SCOPE = "scope";
    public static final String CODE = "code";
    public static final String ERROR = "error";

    private static final String GRANT_TYPE = "grant_type";
    private static final String CODE_GRANT = "authorization_code";
    private static final String REFRESH_GRANT = "refresh_token";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String EXPIRES_AT = "expires_at";

    //login & logout parameters
    public static final String STATE_KEY = "state";
    public static final String CODE_KEY = "code";
    public static final int LOGIN = 1;
    public static final int LOGOUT = 2;

    private static String TOKEN = "123";

    private static final String BASE_URL = "https://api.spotify.com/v1";

    private static final String ARTISTS = "artists";
    private static final String ALBUMS = "albums";
    private static final String TRACKS = "tracks";

    // data urls
    private static final String ARTISTS_URL = BASE_URL + "/" + ARTISTS;
    private static final String ALBUMS_URL = BASE_URL + "/" + ALBUMS;
    private static final String TRACKS_URL = BASE_URL + "/" + TRACKS;
    private static final String SEARCH_URL = BASE_URL + "/search";

    // url query parameters
    private static final String IDS = "ids";
    private static final String QUERY = "q";
    private static final String TYPE = "type";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";

    //no constructor
    private Model() {
        throw new AssertionError();
    }

    // data    model classes

    public static class Context {
        String type;
        String href;
        ExternalUrls external_urls;
        String uri;

        private Context(JSONObject json) {
            type = getString("type", json);
            href = getString("href", json);
            uri = getString("uri", json);
            external_urls = genericParseObject(ExternalUrls.class, "external_urls", json);
        }
    }

    public static class LinkedTrack extends Context {
        public String id;

        private LinkedTrack(JSONObject json) {
            super(json);
            id = getString("id", json);
        }
    }

    private static class Common extends LinkedTrack {
        public String name;

        private Common(JSONObject json) {
            super(json);
            name = getString("name", json);
        }
    }


    // simplified classes

    public static class SimplifiedArtist extends Common {
        private SimplifiedArtist(JSONObject json) {
            super(json);
        }
    }


    private static class AlbumTrackCommon extends Common {
        public SimplifiedArtist[] artists;

        private AlbumTrackCommon(JSONObject json) {
            super(json);
            artists = genericParseArray(SimplifiedArtist.class, "artists", json);
        }
    }


    public static class SimplifiedAlbum extends AlbumTrackCommon implements ImageItem {
        String album_group;//optional
        public String album_type;
        public String release_date;
        String release_date_precision;
        Image[] images;
        public Uri imageUri = null;

        private SimplifiedAlbum(JSONObject json) {
            super(json);
            album_group = getString("album_group", json);
            album_type = getString("album_type", json);
            release_date = getString("release_date", json);
            release_date_precision = getString("release_date_precision", json);
            images = genericParseArray(Image.class, "images", json);
        }

        @Override
        public Image getImage() {
            return images == null ? null : images[0];
        }

        @Override
        public void setImageUri(Uri uri) {
            this.imageUri = uri;
        }
    }


    public static class SimplifiedTrack extends AlbumTrackCommon {
        public int disc_number;
        public int track_number;
        public long duration_ms;

        boolean explicit;
        boolean is_playable;

        LinkedTrack linked_from;
        public String preview_url;

        private SimplifiedTrack(JSONObject json) {
            super(json);
            preview_url = getString("preview_url", json);
            linked_from = genericParseObject(LinkedTrack.class, "linked_from", json);

            try {
                disc_number = json.getInt("disc_number");
                track_number = json.getInt("track_number");
                duration_ms = json.getLong("duration_ms");

                explicit = json.getBoolean("explicit");
                is_playable = json.optBoolean("is_playable");

            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong JSON object", e);
            }
        }
    }

    // full version classes with factory methods

    public static class Artist extends SimplifiedArtist implements ImageItem {
        public String[] genres;

        int popularity;

        public Image[] images;
        public Uri imageUri = null;

        private Artist(JSONObject json) {
            super(json);
            images = genericParseArray(Image.class, "images", json);
            try {
                JSONArray genres = json.getJSONArray("genres");
                int length = genres.length();
                if (length > 0) {
                    this.genres = new String[length];
                    for (int i = 0; i < length; i++)
                        this.genres[i] = genres.getString(i);
                }
                popularity = json.getInt("popularity");
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong JSON object", e);
            }
        }

        // factory methods
        public static Artist get(String id) throws Error {
            Result result = getData(ARTISTS_URL, id);
            return result == null ? null : genericParseObject(Artist.class, result.data);
        }

        public static Artist[] get(String[] ids) throws Error {
            Result result = getData(ARTISTS_URL, ids, 50);
            return result == null ? null : genericParseArray(Artist.class, ARTISTS, result.data);
        }

        public static AlbumPaging getAlbums(String id, int limit, int offset) throws Error {
            Result result = getData(ARTISTS_URL, id, ALBUMS, limit, offset);
            return result == null ? null : genericParseObject(AlbumPaging.class, result.data);
        }

        @Override
        public Image getImage() {
            return images == null ? null : images[0];
        }

        @Override
        public void setImageUri(Uri uri) {
            this.imageUri = uri;
        }
    }

    public static class Album extends SimplifiedAlbum {
        public String[] genres;

        int popularity;
        ExternalIds external_ids;

        public String label;
        Copyright[] copyrights;

        public TrackPaging tracks;

        private Album(JSONObject json) {
            super(json);
            label = getString("label", json);
            external_ids = genericParseObject(ExternalIds.class, "external_ids", json);
            copyrights = genericParseArray(Copyright.class, "copyrights", json);
            tracks = genericParseObject(TrackPaging.class, "tracks", json);

            try {
                JSONArray genres = json.getJSONArray("genres");
                int length = genres.length();
                if (length > 0) {
                    this.genres = new String[length];
                    for (int i = 0; i < length; i++)
                        this.genres[i] = genres.getString(i);
                }
                popularity = json.getInt("popularity");
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong JSON object", e);
            }
        }

        // factory methods
        public static Album get(String id) throws Error {
            Result result = getData(ALBUMS_URL, id);
            return result == null ? null : genericParseObject(Album.class, result.data);
        }

        public static Album[] get(String[] ids) throws Error {
            Result result = getData(ALBUMS_URL, ids, 20);
            return result == null ? null : genericParseArray(Album.class, ALBUMS, result.data);
        }

        public static TrackPaging getTracks(String id, int limit, int offset) throws Error {
            Result result = getData(ALBUMS_URL, id, TRACKS, limit, offset);
            return result == null ? null : genericParseObject(TrackPaging.class, result.data);
        }
    }


    public static class Track extends SimplifiedTrack {
        int popularity;
        ExternalIds external_ids;

        public SimplifiedAlbum album;

        private Track(JSONObject json) {
            super(json);
            external_ids = genericParseObject(ExternalIds.class, "external_ids", json);
            album = genericParseObject(SimplifiedAlbum.class, "album", json);

            try {
                popularity = json.getInt("popularity");
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong JSON object", e);
            }
        }

        // factory methods
        public static Track get(String id) throws Error {
            Result result = getData(TRACKS_URL, id);
            return result == null ? null : genericParseObject(Track.class, result.data);
        }

        public static Track[] get(String[] ids) throws Error {
            Result result = getData(TRACKS_URL, ids, 50);
            return result == null ? null : genericParseArray(Track.class, TRACKS, result.data);
        }
    }

    // Main entry point

    //search for an artist, album, track
    public static void search(String query, Ref<ArtistSearch> artists, Ref<AlbumSearch> albums, Ref<TrackSearch> tracks, int limit, int offset) throws Error {
        String type = getQueryType(artists, albums, tracks);
        Result result = getData(query, type, limit, offset); //can throw error

        ArtistSearch artists_ref = null;
        AlbumSearch albums_ref = null;
        TrackSearch tracks_ref = null;

        if (result != null) {
            try {
                JSONObject root = new JSONObject(result.data);
                artists_ref = genericParseObject(ArtistSearch.class, ARTISTS, root);
                albums_ref = genericParseObject(AlbumSearch.class, ALBUMS, root);
                tracks_ref = genericParseObject(TrackSearch.class, TRACKS, root);
            } catch (Exception e) {
                Log.e(LOG, "Error parsing search results", e);
            }
        }

        if (artists != null) artists.ref = artists_ref;
        if (albums != null) albums.ref = albums_ref;
        if (tracks != null) tracks.ref = tracks_ref;
    }

    // various classes

    // reference to an object
    public static class Ref<T> {
        public T ref;

        public Ref() {
            ref = null;
        }

        public Ref(T ref) {
            this.ref = ref;
        }
    }

    // HTTP request result
    public static class Result {
        public int code;//error code
        public String data;//HTTP response
    }

    // initial tokens
    public static class Tokens {
        public String access_token;
        public String refresh_token;
        public long expires_at;

        private Tokens(JSONObject json) {
            try {
                expires_at = System.currentTimeMillis();
                expires_at += Long.valueOf(json.getString("expires_in")) * 1000 - 1000;
                access_token = getString("access_token", json);
                refresh_token = getString("refresh_token", json);
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong JSON object", e);
            }
        }
    }

    // on refresh
    public static class Token {
        public String access_token;
        public long expires_at;

        private Token(JSONObject json) {
            try {
                expires_at = System.currentTimeMillis();
                expires_at += Long.valueOf(json.getString("expires_in")) * 1000 - 1000;
                access_token = getString("access_token", json);
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong JSON object", e);
            }
        }
    }

    //Error objects
    public static class Error extends Exception {
        public int status;
        public String message;

        private Error(JSONObject json) {
            try {
                JSONObject error = json.getJSONObject("error");
                status = error.getInt("status");
                message = getString("message", error);
            } catch (Exception e) {
                status = 0;
                message = e.getMessage();
            }
        }
    }

    public static class AuthenticationError extends Exception {
        public String error;
        public String error_description;

        private AuthenticationError(JSONObject json) {
            try {
                this.error = getString("error", json);
                this.error_description = getString("error_description", json);
            } catch (Exception e) {
                this.error_description = e.getMessage();
            }
        }
    }


    // search result paging root class
    public static class Paging<T> {

        public enum Kind {
            ARTIST(ARTISTS),
            ALBUM(ALBUMS),
            TRACK(TRACKS),
            NONE(null);
            public final String key;

            Kind(String key) {
                this.key = key;
            }
        }

        public enum Direction {
            PREV,
            NEXT
        }

        public String href;

        public int limit;
        public int total;
        public int offset;

        String previous;//url
        String next;//url
        public T[] items;

        private Paging(JSONObject json) {
            try {
                href = getString("href", json);
                limit = json.getInt("limit");
                total = json.getInt("total");
                offset = json.getInt("offset");

                previous = getString("previous", json);
                next = getString("next", json);
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong Paging JSON object", e);
            }
        }

        // different json: search paging has key vs regular paging (no key)
        private Paging parse(String data) {
            String key = kind().key;
            if (key != null) return genericParseObject(getClass(), key, data);
            return genericParseObject(getClass(), data);
        }

        // return next page of data
        public Paging next() throws Error {
            if (next == null) return this;
            Result result = getData(next);
            if (result == null) return null;
            return parse(result.data);
        }

        // return previous page of data
        public Paging prev() throws Error {
            if (previous == null) return this;
            Result result = getData(previous);
            if (result == null) return null;
            return parse(result.data);
        }

        public boolean hasPrevious() {
            return previous != null;
        }

        public boolean hasNext() {
            return next != null;
        }

        public T get(int position) {
            return items == null ? null : ((position >= 0 && position < items.length) ? items[position] : null);
        }

        public Kind kind() {
            return Kind.NONE;
        }
    }

    //interfaces for accessing pictures
    public interface ImageItem {
        Image getImage();

        void setImageUri(Uri uri);
    }

    public interface ImageItems {
        ImageItem[] getImageItems();
    }

    //search paging objects
    public static class ArtistSearch extends Paging<Artist> implements ImageItems {

        private ArtistSearch(JSONObject json) {
            super(json);
            items = genericParseArray(Artist.class, "items", json);
        }

        public Kind kind() {
            return Kind.ARTIST;
        }

        @Override
        public ImageItem[] getImageItems() {
            return items;
        }
    }

    public static class AlbumSearch extends Paging<SimplifiedAlbum> implements ImageItems {

        private AlbumSearch(JSONObject json) {
            super(json);
            items = genericParseArray(SimplifiedAlbum.class, "items", json);
        }

        public Kind kind() {
            return Kind.ALBUM;
        }

        @Override
        public ImageItem[] getImageItems() {
            return items;
        }
    }

    public static class TrackSearch extends Paging<Track> {

        private TrackSearch(JSONObject json) {
            super(json);
            items = genericParseArray(Track.class, "items", json);
        }

        public Kind kind() {
            return Kind.TRACK;
        }
    }

    //regular paging objects - no key present
    public static class AlbumPaging extends Paging<SimplifiedAlbum> implements ImageItems {

        private AlbumPaging(JSONObject json) {
            super(json);
            items = genericParseArray(SimplifiedAlbum.class, "items", json);
        }

        public Kind kind() {
            return Kind.NONE;
        }

        @Override
        public ImageItem[] getImageItems() {
            return items;
        }
    }

    public static class TrackPaging extends Paging<SimplifiedTrack> {

        private TrackPaging(JSONObject json) {
            super(json);
            items = genericParseArray(SimplifiedTrack.class, "items", json);
        }

        public Kind kind() {
            return Kind.NONE;
        }
    }


    public static class ExternalUrls {

        Map<String, String> urls;

        private ExternalUrls(JSONObject json) {
            urls = get(json);
        }
    }

    public static class ExternalIds {

        Map<String, String> ids;

        private ExternalIds(JSONObject json) {
            ids = get(json);
        }
    }


    public static class Copyright {
        String text;
        String type;

        private Copyright(JSONObject json) {
            text = getString("text", json);
            type = getString("type", json);
        }
    }

    public static class Image {
        public int height;
        public int width;
        public String url;

        private Image(JSONObject image) {
            try {
                height = image.getInt("height");
                width = image.getInt("width");
                url = getString("url", image);
            } catch (JSONException e) {
                throw new IllegalArgumentException("Wrong Image JSON object", e);
            }
        }
    }

    // utility methods

    //get a JSON object into a Map<String,String>
    private static Map<String, String> get(JSONObject json) {
        if (json == null) return null;

        Map<String, String> map = null;
        int length = json.length();
        if (length > 0) {
            map = new HashMap<>(length);
            Iterator<String> iterator = json.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = getString(key, json);
                map.put(key, value);
            }
        }
        return map;
    }

    // build the type search argument
    private static String getQueryType(Ref<ArtistSearch> artists, Ref<AlbumSearch> albums, Ref<TrackSearch> tracks) {
        StringBuilder type = new StringBuilder();
        int which = 0;

        if (artists != null) which += 1;
        if (which > 0) type.append("artist");

        if (albums != null) which += 2;
        if (which > 2) type.append(',');
        if (which >= 2) type.append("album");

        if (tracks != null) which += 4;
        if (which > 4) type.append(',');
        if (which >= 4) type.append("track");

        if (which == 0) return null;

        return type.toString();
    }

    // various ways to build an uri
    private static Uri buildUri(String url) {
        if (TextUtils.isEmpty(url)) return null;
        return Uri.parse(url);
    }

    // build a search url
    private static Uri buildUri(String query, String type, int limit, int offset) {
        if (TextUtils.isEmpty(query)) return null;
        if (TextUtils.isEmpty(type)) return null;
        if (limit < 1 || limit > 50) return null;
        if (offset < 0) return null;
        Uri.Builder builder = Uri.parse(SEARCH_URL).buildUpon();
        builder.appendQueryParameter(QUERY, query);
        builder.appendQueryParameter(TYPE, type);
        builder.appendQueryParameter(LIMIT, String.valueOf(limit));
        builder.appendQueryParameter(OFFSET, String.valueOf(offset));
        return builder.build();
    }

    // append id and key to url and query parameters limit and offset
    private static Uri buildUri(String url, String id, String key, int limit, int offset) {
        if (TextUtils.isEmpty(url)) return null;
        if (TextUtils.isEmpty(id)) return null;
        if (limit < 1 || limit > 50) return null;
        if (offset < 0) return null;

        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendPath(id);
        builder.appendPath(key);
        builder.appendQueryParameter(LIMIT, String.valueOf(limit));
        builder.appendQueryParameter(OFFSET, String.valueOf(offset));
        return builder.build();
    }

    // append id to the end
    private static Uri buildUri(String url, String id) {
        if (TextUtils.isEmpty(url)) return null;
        if (TextUtils.isEmpty(id)) return null;
        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendPath(id);
        return builder.build();
    }

    // append query parameter with a list of ids
    private static Uri buildUri(String url, String[] ids, int max) {
        if (TextUtils.isEmpty(url)) return null;
        if (ids == null) return null;

        int length = ids.length;
        if (length < 1) return null;

        Uri.Builder builder = Uri.parse(url).buildUpon();
        StringBuilder stringBuilder = new StringBuilder();

        int pos;
        int count;
        for (count = pos = 0; pos < length; pos++) {
            String id = ids[pos];
            if (!TextUtils.isEmpty(id)) {
                count++;
                stringBuilder.append(id);
                break;
            }
        }
        for (pos++; pos < length; pos++) {
            String id = ids[pos];
            if (!TextUtils.isEmpty(id)) {
                count++;
                if (count > max) break;
                stringBuilder.append(',');
                stringBuilder.append(id);
            }
        }
        if (count == 0) return null;
        builder.appendQueryParameter(IDS, stringBuilder.toString());
        return builder.build();
    }

    // various ways to request data via HTTP

    // given a base url and parameters, build uri and get data
    private static Result getData(String query, String type, int limit, int offset) throws Error {
        Uri uri = buildUri(query, type, limit, offset);
        if (uri == null) return null;
        return getData(uri);
    }

    // given a base url and parameters, build uri and get data
    private static Result getData(String url, String[] ids, int max) throws Error {
        Uri uri = buildUri(url, ids, max);
        if (uri == null) return null;
        return getData(uri);
    }

    // given a base url and parameters, build uri and get data
    private static Result getData(String url, String id) throws Error {
        Uri uri = buildUri(url, id);
        if (uri == null) return null;
        return getData(uri);
    }

    // given a base url and parameters, build uri and get data
    private static Result getData(String url, String id, String key, int limit, int offset) throws Error {
        Uri uri = buildUri(url, id, key, limit, offset);
        if (uri == null) return null;
        return getData(uri);
    }

    private static Result getData(String url) throws Error {
        Uri uri = buildUri(url);
        if (uri == null) return null;
        return getData(uri);
    }

    // given an Uri, gets the JSON data
    private static Result getData(Uri uri) throws Error {
        Result result = HTTP.getData(uri.toString(), TOKEN);
        if (result == null) return null;
        if (result.code != HttpURLConnection.HTTP_OK) {
            Error error = null;
            if (!TextUtils.isEmpty(result.data))
                error = genericParseObject(Error.class, result.data);
            if (error != null) throw error;
            return null;
        }
        return result;
    }

    // various ways to construct data model objects given type and json data

    //convert for string to json parse tree
    private static <T> JSONObject jsonObject(Class<T> which, String json) {
        if (which == null) return null;
        if (TextUtils.isEmpty(json)) return null;
        JSONObject result = null;
        try {
            result = new JSONObject(json);
        } catch (Exception e) {
            Log.e(LOG, String.format("Error parsing %s JSON object", which.getSimpleName()), e);
        }
        return result;
    }

    private static <T> JSONArray jsonArray(Class<T> which, String json) {
        if (which == null) return null;
        if (TextUtils.isEmpty(json)) return null;
        JSONArray result = null;
        try {
            result = new JSONArray(json);
        } catch (Exception e) {
            Log.e(LOG, String.format("Error parsing %s JSON array", which.getSimpleName()), e);
        }
        return result;
    }

    // return field with specified key
    private static <T> JSONObject getObject(String key, JSONObject json) {
        if (json == null) return null;
        if (TextUtils.isEmpty(key)) return null;
        return json.optJSONObject(key);
    }

    private static <T> JSONArray getArray(String key, JSONObject json) {
        if (json == null) return null;
        if (TextUtils.isEmpty(key)) return null;
        return json.optJSONArray(key);
    }

    // return the constructor for given class
    private static <T> Constructor<T> getConstructor(Class<T> which) {
        Constructor<T> constructor;
        try {
            constructor = which.getDeclaredConstructor(JSONObject.class);
            constructor.setAccessible(true);
        } catch (Exception e) {
            Log.e(LOG, String.format("No suitable constructor found for %s!", which.getSimpleName()), e);
            return null;
        }
        return constructor;
    }

    // object parsing
    private static <T> T genericParseObject(Class<T> which, String json) {
        JSONObject object = jsonObject(which, json);
        if (object == null) return null;
        return genericParseObject(which, object);
    }

    private static <T> T genericParseObject(Class<T> which, String key, String json) {
        JSONObject object = jsonObject(which, json);
        if (object == null) return null;
        return genericParseObject(which, key, object);
    }

    private static <T> T genericParseObject(Class<T> which, String key, JSONObject json) {
        JSONObject object = getObject(key, json);
        if (object == null) return null;
        return genericParseObject(which, object);
    }

    private static <T> T genericParseObject(Class<T> which, JSONObject json) {
        if (which == null) return null;
        if (json == null) return null;

        Constructor<T> constructor = getConstructor(which);
        if (constructor == null) return null;

        T result = null;
        try {
            result = constructor.newInstance(json);
        } catch (Exception e) {
            Log.e(LOG, String.format("Error parsing %s JSON object", which.getSimpleName()), e);
        }
        return result;
    }

    // array parsing
    private static <T> T[] genericParseArray(Class<T> which, String json) {
        JSONArray array = jsonArray(which, json);
        if (array == null) return null;
        return genericParseArray(which, array);
    }

    private static <T> T[] genericParseArray(Class<T> which, String key, String json) {
        JSONObject object = jsonObject(which, json);
        if (object == null) return null;
        return genericParseArray(which, key, object);
    }

    private static <T> T[] genericParseArray(Class<T> which, String key, JSONObject json) {
        JSONArray array = getArray(key, json);
        if (array == null) return null;
        return genericParseArray(which, array);
    }

    private static <T> T[] genericParseArray(Class<T> which, JSONArray array) {
        if (which == null) return null;
        if (array == null) return null;

        Constructor<T> constructor = getConstructor(which);
        if (constructor == null) return null;

        T[] result = null;

        try {
            int length = array.length();

            if (length > 0) result = (T[]) Array.newInstance(which, length);
            for (int i = 0; i < length; i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null)
                    result[i] = constructor.newInstance(item);
            }
        } catch (Exception e) {
            Log.e(LOG, String.format("Error parsing %s JSON array", which.getSimpleName()), e);
            return null;
        }
        return result;
    }

    // access & refresh tokens request & refresh methods

    //generate random string of given length
    public static String getRandomString(int length) {
        if (length < 1) return null;
        SecureRandom random = new SecureRandom();
        byte[] buffer = new byte[length];
        random.nextBytes(buffer);
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    // add an URL encoded parameter
    private static void addURLParameter(StringBuilder builder, String key, String value) {
        if (builder == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) return;
        try {
            builder.append(URLEncoder.encode(key, "UTF-8"));
            builder.append('=');
            builder.append(URLEncoder.encode(value, "UTF-8"));
        } catch (Exception e) {
            Log.e(LOG, "No UTF-8 encoding found!", e);
        }
    }

    // get password from shared preferences
    private static String getPassword(DataStore dataStore) {
        if (dataStore == null) return null;
        return dataStore.getString(PASSWORD_KEY, null);
    }

    // construct Basic Authorization token
    private static String getAuthorizationCode(DataStore dataStore) {
        String s = String.format("%s:%s", CLIENT, Key.decodeApiKey(getPassword(dataStore), SECRET));
        return Base64.encodeToString(s.getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);
    }

    // prepare HTTP POST parameters for requesting initial tokens
    private static String getRequestTokenParams(String code) {
        //application/x-www-form-urlencoded
        StringBuilder builder = new StringBuilder();
        addURLParameter(builder, GRANT_TYPE, CODE_GRANT);
        builder.append('&');
        addURLParameter(builder, CODE, code);
        builder.append('&');
        addURLParameter(builder, REDIRECT, REDIRECT_URI);
        return builder.toString();
    }

    // prepare HTTP POST parameters for refreshing access token
    private static String getRefreshTokenParams(String token) {
        StringBuilder builder = new StringBuilder();
        addURLParameter(builder, GRANT_TYPE, REFRESH_GRANT);
        builder.append('&');
        addURLParameter(builder, REFRESH_TOKEN, token);
        return builder.toString();
    }

    // perform HTTP POST request for tokens
    private static Result getTokenData(String url, String tokenParams, DataStore dataStore) throws AuthenticationError {
        String token = getAuthorizationCode(dataStore);
        Result result = HTTP.postData(url, token, tokenParams);
        if (result == null) return null;
        if (result.code != HttpURLConnection.HTTP_OK) {
            AuthenticationError error = null;
            if (!TextUtils.isEmpty(result.data))
                error = genericParseObject(AuthenticationError.class, result.data);
            if (error != null) throw error;
            return null;
        }
        return result;
    }

    // the two main methods for getting tokens

    // get the initial tokens
    public static Tokens getTokens(String code, DataStore dataStore) throws AuthenticationError {
        String tokenParams = getRequestTokenParams(code);
        Result result = getTokenData(Model.TOKEN_URL, tokenParams, dataStore);
        return result == null ? null : genericParseObject(Tokens.class, result.data);
    }

    // refresh the access token
    public static Token refreshToken(String token, DataStore dataStore) throws AuthenticationError {
        String tokenParams = getRefreshTokenParams(token);
        Result result = getTokenData(Model.TOKEN_URL, tokenParams, dataStore);
        return result == null ? null : genericParseObject(Token.class, result.data);
    }

    // Check if there is network connectivity
    public static boolean hasNetwork(android.content.Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // calls the LoginCallback activity
    public static void login(AppCompatActivity activity, int code, String state) {
        if (activity == null) return;
        Intent intent = new Intent(activity, LoginCallback.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra(CODE_KEY, LOGIN);
        intent.putExtra(STATE_KEY, state);
        activity.startActivityForResult(intent, code);
    }

    // calls
    public static void logout(AppCompatActivity activity, int code) {
        if (activity == null) return;
        Intent intent = new Intent(activity, LoginCallback.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra(CODE_KEY, LOGOUT);
        activity.startActivityForResult(intent, code);
    }

    //set the access token
    public static void setToken(String token) {
        TOKEN = token;
    }


    private static String getString(String key, JSONObject json) {
        if (TextUtils.isEmpty(key)) return null;
        if (json == null) return null;
        if (json.isNull(key)) return null;
        return json.optString(key, null);
    }

    public static void setupActionBar(AppCompatActivity activity, String info) {
        ActionBar bar = activity.getSupportActionBar();
        if (bar == null) return;

        bar.setCustomView(R.layout.logo);
        bar.setDisplayShowCustomEnabled(true);

        View customView = bar.getCustomView();
        TextView title = customView.findViewById(R.id.title);
        title.setText(String.format("%s %s", bar.getTitle(), info));
    }

}
