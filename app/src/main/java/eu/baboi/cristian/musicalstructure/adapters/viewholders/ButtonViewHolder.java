package eu.baboi.cristian.musicalstructure.adapters.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.adapters.ListAdapter;
import eu.baboi.cristian.musicalstructure.utils.PagingCallbacks;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class ButtonViewHolder<T, P extends Model.Paging<T>, VH extends ViewHolder<T>, A extends ListAdapter<T, P, VH, A>> extends RecyclerView.ViewHolder implements View.OnClickListener {
    public enum Label {
        PREV(R.string.prev_page),
        NEXT(R.string.next_page);
        public final int res;

        Label(int resId) {
            res = resId;
        }
    }

    final private Button button;
    final private AppCompatActivity activity;

    private Loaders.Id id;
    private PagingCallbacks<T, P, VH, A> callbacks = null;

    public ButtonViewHolder(@NonNull View itemView, AppCompatActivity context) {
        super(itemView);
        activity = context;
        button = itemView.findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    public void bind(Loaders.Id id, Label label, PagingCallbacks<T, P, VH, A> callbacks) {
        this.id = id;
        this.callbacks = callbacks;
        button.setText(label.res);
    }

    public void onClick(View v) {
        if (callbacks != null)
            Loaders.startLoader(activity, id, null, callbacks);
    }
}

