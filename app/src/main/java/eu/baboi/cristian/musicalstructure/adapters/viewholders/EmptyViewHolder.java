package eu.baboi.cristian.musicalstructure.adapters.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import eu.baboi.cristian.musicalstructure.R;

//Empty state
public class EmptyViewHolder extends RecyclerView.ViewHolder {
    final private TextView textView;

    public EmptyViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.empty);
    }

    public void bind(int res) {
        textView.setText(res);
    }
}
