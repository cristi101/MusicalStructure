package eu.baboi.cristian.musicalstructure.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.adapters.ListAdapter;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.ViewHolder;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public abstract class CategoryFragment<T, P extends Model.Paging<T>, VH extends ViewHolder<T>, A extends ListAdapter<T, P, VH, A>> extends Fragment {

    A adapter = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.list, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(getAdapter());

        return rootView;
    }

    abstract A getAdapter();
}
