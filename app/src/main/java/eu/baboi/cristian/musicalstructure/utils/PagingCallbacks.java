package eu.baboi.cristian.musicalstructure.utils;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import eu.baboi.cristian.musicalstructure.adapters.ListAdapter;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.ViewHolder;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class PagingCallbacks<T, P extends Model.Paging<T>, VH extends ViewHolder<T>, A extends ListAdapter<T, P, VH, A>> implements LoaderManager.LoaderCallbacks<Loaders.PagingResult<T, P>> {
    private static final String LOG = PagingCallbacks.class.getName();
    public interface Progress {
        ProgressBar getProgressBar();
    }

    private final AppCompatActivity activity;
    private ProgressBar progress;
    private final Model.Paging.Direction dir;
    private final P paging;
    private final A adapter;

    public PagingCallbacks(A adapter, Model.Paging.Direction dir, P paging) {
        this.dir = dir;
        this.paging = paging;
        this.adapter = adapter;
        activity = adapter.activity;
        if (activity instanceof Progress)
            progress = ((Progress) activity).getProgressBar();
    }

    @Override
    public Loader<Loaders.PagingResult<T, P>> onCreateLoader(int id, Bundle args) {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        return new Loaders.Paging<>(activity.getApplicationContext(), dir, paging);
    }

    @Override
    public void onLoadFinished(Loader<Loaders.PagingResult<T, P>> loader, Loaders.PagingResult<T, P> data) {
        if (progress != null) progress.setVisibility(View.GONE);

        if (data == null) return;
        if (data.error != null) {
            Toast.makeText(activity, String.format("Search error:\n%s", data.error.message), Toast.LENGTH_LONG).show();
            return;
        }
        if (data.aerror != null) {
            Toast.makeText(activity, String.format("Authentication error:\n%s", data.aerror.error_description), Toast.LENGTH_LONG).show();
            return;
        }
        adapter.update(data.paging);
        adapter.scrollToTop();
    }

    @Override
    public void onLoaderReset(Loader<Loaders.PagingResult<T, P>> loader) {
        adapter.update(null);
    }
}
