package eu.baboi.cristian.musicalstructure.adapters;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.ButtonViewHolder;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.EmptyViewHolder;
import eu.baboi.cristian.musicalstructure.adapters.viewholders.ViewHolder;
import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public abstract class ListAdapter<T, P extends Model.Paging<T>, VH extends ViewHolder<T>, A extends ListAdapter<T, P, VH, A>> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    enum ItemType {
        EMPTY,
        PREVIOUS,
        NEXT,
        ITEM
    }

    private P paging;
    public final AppCompatActivity activity;

    private RecyclerView recyclerView = null;

    ListAdapter(P paging, AppCompatActivity context) {
        this.paging = paging;
        activity = context;
    }

    abstract int empty();// return the resource id for the empty state string

    abstract Loaders.Id loader();// return the loader id

    @Override
    public int getItemViewType(int position) {
        if (paging == null || paging.items == null) return ItemType.EMPTY.ordinal();
        if (position == 0 && paging.hasPrevious()) return ItemType.PREVIOUS.ordinal();
        if (position == getItemCount() - 1 && paging.hasNext()) return ItemType.NEXT.ordinal();
        return ItemType.ITEM.ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view;
        ItemType t = ItemType.values()[type];
        switch (t) {
            case EMPTY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty, parent, false);
                return new EmptyViewHolder(view);
            case PREVIOUS:
            case NEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button, parent, false);
                return new ButtonViewHolder<T, P, VH, A>(view, activity);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemType t = ItemType.values()[holder.getItemViewType()];
        switch (t) {
            case EMPTY:
                EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
                emptyViewHolder.bind(empty());
                break;
            case PREVIOUS: // nothing to do - can use one type of button and change labels here
                PagingCallbacks<T, P, VH, A> callbacks = new PagingCallbacks<>((A) this, Model.Paging.Direction.PREV, paging);
                ButtonViewHolder<T, P, VH, A> pvh = (ButtonViewHolder<T, P, VH, A>) holder;
                pvh.bind(loader(), ButtonViewHolder.Label.PREV, callbacks);
                break;
            case NEXT:
                PagingCallbacks<T, P, VH, A> callback = new PagingCallbacks<>((A) this, Model.Paging.Direction.NEXT, paging);
                ButtonViewHolder<T, P, VH, A> nvh = (ButtonViewHolder<T, P, VH, A>) holder;
                nvh.bind(loader(), ButtonViewHolder.Label.NEXT, callback);
                break;
            case ITEM:
                // adjust the position
                if (paging.hasPrevious()) position--;

                T item = paging.get(position);

                VH viewHolder = (VH) holder;
                viewHolder.bind(item);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (paging == null || paging.items == null) return 1;

        int count = paging.items.length;
        if (paging.hasPrevious()) count++;
        if (paging.hasNext()) count++;
        return count;
    }

    //called from loader callback
    public void update(P data) {
        paging = data;
        notifyDataSetChanged();
    }

    //return a reference to the data
    public P data() {
        return paging;
    }

    public void scrollToTop() {
        if (recyclerView == null) return;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        layoutManager.scrollToPosition(0);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

}

