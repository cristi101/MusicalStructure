package eu.baboi.cristian.musicalstructure.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// see https://developer.android.com/topic/performance/graphics/load-bitmap
public class Picture {
    private static final String LOG = Picture.class.getName();


    public static void setImageUri(ImageView v, Uri uri, int reqWidth, int reqHeight) {
        if (v == null) return;

        if (uri == null) v.setVisibility(View.GONE);
        else v.setVisibility(View.VISIBLE);

        Bitmap bitmap = decodeSampledBitmap(v.getContext(), uri, reqWidth, reqHeight);
        if (bitmap != null) v.setImageBitmap(bitmap);
    }

    private static InputStream uriToStream(Context context, Uri uri) {
        InputStream input = null;

        try {
            input = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.e(LOG, "File not found!", e);
        }
        return input;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Bitmap decodeSampledBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        if (context == null) return null;
        if (uri == null) return null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        InputStream is = uriToStream(context, uri);

        if (is != null)
            try {
                BitmapFactory.decodeStream(is, null, options);
                is.close();

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                is = uriToStream(context, uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                is.close();

                return bitmap;
            } catch (Exception e) {
                Log.e(LOG, "Failed to load picture.", e);
                return null;
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return null;
    }
}
