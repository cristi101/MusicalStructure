package eu.baboi.cristian.musicalstructure.fragments;

import android.support.v7.app.AppCompatActivity;

import eu.baboi.cristian.musicalstructure.adapters.SearchTracksAdapter;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.TracksViewHolder;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class TracksFragment extends CategoryFragment<Model.Track, Model.TrackSearch, TracksViewHolder, SearchTracksAdapter> {

    public interface ITracks {
        void setTracksAdapter(SearchTracksAdapter adapter);
    }

    @Override
    public SearchTracksAdapter getAdapter() {
        if (adapter == null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            adapter = new SearchTracksAdapter(null, activity);
            if (activity instanceof ITracks)
                ((ITracks) activity).setTracksAdapter(adapter);
        }
        return adapter;
    }
}
