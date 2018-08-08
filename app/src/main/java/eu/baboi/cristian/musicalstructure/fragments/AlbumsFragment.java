package eu.baboi.cristian.musicalstructure.fragments;

import android.support.v7.app.AppCompatActivity;

import eu.baboi.cristian.musicalstructure.adapters.SearchAlbumsAdapter;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.AlbumsViewHolder;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class AlbumsFragment extends CategoryFragment<Model.SimplifiedAlbum, Model.AlbumSearch, AlbumsViewHolder, SearchAlbumsAdapter> {

    public interface IAlbums {
        void setAlbumsAdapter(SearchAlbumsAdapter adapter);
    }

    @Override
    SearchAlbumsAdapter getAdapter() {
        if (adapter == null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            adapter = new SearchAlbumsAdapter(null, activity);
            if (activity instanceof IAlbums)
                ((IAlbums) activity).setAlbumsAdapter(adapter);
        }
        return adapter;
    }
}
