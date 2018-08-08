package eu.baboi.cristian.musicalstructure.fragments;

import android.support.v7.app.AppCompatActivity;

import eu.baboi.cristian.musicalstructure.adapters.SearchArtistsAdapter;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.ArtistsViewHolder;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class ArtistsFragment extends CategoryFragment<Model.Artist, Model.ArtistSearch, ArtistsViewHolder, SearchArtistsAdapter> {

    public interface IArtists {
        void setArtistsAdapter(SearchArtistsAdapter adapter);
    }

    @Override
    SearchArtistsAdapter getAdapter() {
        if (adapter == null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            adapter = new SearchArtistsAdapter(null, activity);
            if (activity instanceof IArtists)
                ((IArtists) activity).setArtistsAdapter(adapter);
        }
        return adapter;
    }
}
