package eu.baboi.cristian.musicalstructure.adapters;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.ArtistsViewHolder;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;


public class SearchArtistsAdapter extends ListAdapter<Model.Artist, Model.ArtistSearch, ArtistsViewHolder, SearchArtistsAdapter> {

    public SearchArtistsAdapter(Model.ArtistSearch paging, AppCompatActivity context) {
        super(paging, context);
    }

    int empty() {
        return R.string.no_artists;
    }

    Loaders.Id loader() {
        return Loaders.Id.ARTISTS_PAGING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view;
        ItemType t = ItemType.values()[type];
        switch (t) {
            case ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_item, parent, false);
                return new ArtistsViewHolder(view, activity);
        }
        return super.onCreateViewHolder(parent, type);
    }
}

