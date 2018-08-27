package eu.baboi.cristian.musicalstructure.adapters.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.baboi.cristian.musicalstructure.ArtistActivity;
import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.utils.Picture;
import eu.baboi.cristian.musicalstructure.utils.net.Loaders;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class ArtistsViewHolder extends ViewHolder<Model.Artist> implements View.OnClickListener {
    private final ImageView picture;
    private final TextView name;
    private final TextView genres;

    final private Context mContext;

    private String idArtist = null;

    public ArtistsViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;

        picture = itemView.findViewById(R.id.picture);
        name = itemView.findViewById(R.id.name);
        genres = itemView.findViewById(R.id.genres);

        LinearLayout layout = itemView.findViewById(R.id.list_item);
        layout.setOnClickListener(this);
    }

    public void bind(Model.Artist artist) {
        idArtist = artist.id;

        picture.post(new Runnable() {
            @Override
            public void run() {
                int width = picture.getMeasuredWidth();
                Picture.setImageUri(picture, artist.imageUri, width, width);
            }
        });

        name.setText(artist.name);

        StringBuilder builder = new StringBuilder();
        if (artist.genres != null) {
            for (String genre : artist.genres) {
                builder.append(" * ");
                builder.append(genre);
            }
        }
        genres.setText(builder.toString());
    }

    public void onClick(View v) {
        if (TextUtils.isEmpty(idArtist)) return;
        if (Loaders.noNetwork(mContext)) return;
        Intent intent = new Intent(mContext, ArtistActivity.class);
        intent.putExtra(Model.ID_KEY, idArtist);
        mContext.startActivity(intent);
    }
}
