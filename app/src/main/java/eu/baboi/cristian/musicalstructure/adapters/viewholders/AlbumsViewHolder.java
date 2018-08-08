package eu.baboi.cristian.musicalstructure.adapters.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.baboi.cristian.musicalstructure.Album;
import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.utils.Picture;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class AlbumsViewHolder extends ViewHolder<Model.SimplifiedAlbum> implements View.OnClickListener {
    private final ImageView picture;
    private final TextView name;
    private final TextView artists;

    private final Context mContext;

    private String idAlbum = null;

    public AlbumsViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;

        picture = itemView.findViewById(R.id.picture);
        name = itemView.findViewById(R.id.name);
        artists = itemView.findViewById(R.id.artists);

        LinearLayout layout = itemView.findViewById(R.id.list_item);
        layout.setOnClickListener(this);
    }

    public void bind(Model.SimplifiedAlbum album) {
        idAlbum = album.id;

        if (album.imageUri != null) {
            picture.setVisibility(View.VISIBLE);

            picture.post(new Runnable() {
                @Override
                public void run() {
                    int width = picture.getMeasuredWidth();
                    Picture.setImageUri(picture, album.imageUri, width, width);
                }
            });

        } else picture.setVisibility(View.GONE);

        name.setText(album.name);

        if (album.artists != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(album.album_type);
            builder.append(" * ");
            builder.append(album.release_date);
            for (Model.SimplifiedArtist artist : album.artists) {
                builder.append(" * ");
                builder.append(artist.name);
            }
            artists.setText(builder.toString());
        }
    }

    public void onClick(View v) {
        if (idAlbum == null) return;
        Intent intent = new Intent(mContext, Album.class);
        intent.putExtra(Model.ID_KEY, idAlbum);
        mContext.startActivity(intent);
    }
}