package eu.baboi.cristian.musicalstructure.adapters.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

        picture.post(new Runnable() {
            @Override
            public void run() {
                int width = picture.getMeasuredWidth();
                Picture.setImageUri(picture, album.imageUri, width, width);
            }
        });

        name.setText(album.name);

        StringBuilder builder = new StringBuilder();
        builder.append(album.album_type);
        builder.append(" * ");
        builder.append(album.release_date);

        if (album.artists != null) {
            for (Model.SimplifiedArtist artist : album.artists) {
                builder.append(" * ");
                builder.append(artist.name);
            }
        }

        artists.setText(builder.toString());
    }

    public void onClick(View v) {
        if (TextUtils.isEmpty(idAlbum)) return;
        Intent intent = new Intent(mContext, Album.class);
        intent.putExtra(Model.ID_KEY, idAlbum);
        mContext.startActivity(intent);
    }
}