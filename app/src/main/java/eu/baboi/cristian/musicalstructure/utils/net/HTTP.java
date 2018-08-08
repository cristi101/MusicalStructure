package eu.baboi.cristian.musicalstructure.utils.net;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public final class HTTP {
    private static final String LOG = HTTP.class.getName();

    private HTTP() {
    }

    // make a GET request to Spotify web api
    public static Model.Result getData(String url, String token) {
        URL lUrl = createUrl(url);

        Model.Result response = null;
        try {
            response = makeHttpRequest(lUrl, token);
        } catch (IOException e) {
            Log.e(LOG, "Error closing HTTP connection!", e);
        }
        return response;
    }

    // make a POST HTTP request to Spotify token api
    public static Model.Result postData(String url, String token, String data) {
        URL lUrl = createUrl(url);

        Model.Result response = null;
        try {
            response = makeHttpPostRequest(lUrl, token, data);
        } catch (IOException e) {
            Log.e(LOG, "Error closing HTTP connection!", e);
        }
        return response;
    }

    // make a HTTP GET request to retrieve a picture
    public static Uri getPicture(File dir, String url) {
        URL lUrl = createUrl(url);
        return getPicture(dir, lUrl);
    }

    // transform a string to an URL
    private static URL createUrl(String url) {
        if (url == null || url.trim().length() == 0) return null;
        URL lUrl = null;
        try {
            lUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG, String.format("Error creating URL:%s", url), e);
        }
        return lUrl;
    }

    //make a POST HTTP request given Basic Authorization token & application/x-www-form-urlencoded data
    private static Model.Result makeHttpPostRequest(URL url, String token, String data) throws IOException {
        if (url == null) return null;
        if (data == null) return null;

        Model.Result response = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
            // setup the connection
            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

            if (token != null)
                connection.setRequestProperty("Authorization", "Basic " + token);

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bytes);
            outputStream.close();

            connection.connect();

            response = new Model.Result();
            response.code = connection.getResponseCode();

            if (response.code == HttpURLConnection.HTTP_OK)
                inputStream = connection.getInputStream();
            else inputStream = connection.getErrorStream();

            response.data = readData(inputStream);

        } catch (IOException e) {
            Log.e(LOG, "Problem retrieving data.", e);
        } finally {
            if (connection != null)
                connection.disconnect();
            if (inputStream != null)
                inputStream.close();
        }
        return response;
    }


    // get the response of a HTTP request
    private static Model.Result makeHttpRequest(URL url, String token) throws IOException {
        if (url == null) return null;

        Model.Result response = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            // setup the connection
            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            if (token != null)
                connection.setRequestProperty("Authorization", "Bearer " + token);

            connection.connect();

            response = new Model.Result();
            response.code = connection.getResponseCode();

            if (response.code == HttpURLConnection.HTTP_OK)
                inputStream = connection.getInputStream();
            else inputStream = connection.getErrorStream();

            response.data = readData(inputStream);

        } catch (IOException e) {
            Log.e(LOG, "Problem retrieving data.", e);
        } finally {
            if (connection != null)
                connection.disconnect();
            if (inputStream != null)
                inputStream.close();
        }
        return response;
    }

    // read data from an InputStream
    private static String readData(InputStream stream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

            // read line by line
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    // save a local copy of the picture at url and return the Uri of it
    private static Uri getPicture(File dir, URL url) {
        if (url == null) return null;
        if (dir == null) return null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            File file = new File(dir, url.getPath());
            if (file.exists()) return Uri.fromFile(file); // if the file exists return it
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                Log.e(LOG, "Cannot create cache file.");
                return null;
            }

            file.deleteOnExit();
            outputStream = new FileOutputStream(file);

            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                inputStream = connection.getInputStream();
                byte[] buffer = new byte[4096];// the transfer buffer
                int n; // the byte count

                while ((n = inputStream.read(buffer, 0, 4096)) != -1)
                    outputStream.write(buffer, 0, n);

                return Uri.fromFile(file);
            } else {
                Log.e(LOG, String.format("Error connecting to news code: %d url: %s", connection.getResponseCode(), url.toString()));
            }

        } catch (IOException e) {
            Log.e(LOG, "Error saving thumbnail.", e);
        } finally {

            if (connection != null) connection.disconnect();

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(LOG, "Error closing picture connection.", e);
                }
            }

            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(LOG, "Error closing cache file.", e);
                }
        }
        return null;
    }
}
