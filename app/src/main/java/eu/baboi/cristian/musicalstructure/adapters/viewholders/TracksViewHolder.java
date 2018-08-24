package eu.baboi.cristian.musicalstructure.adapters.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.Track;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

public class TracksViewHolder extends ViewHolder<Model.Track> implements View.OnClickListener {
    private final TextView trackNo;
    private final TextView name;
    private final TextView duration;
    private final TextView artists;
    private final TextView tvAlbum;

    private final Context mContext;

    private String idTrack = null;

    public TracksViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;

        trackNo = itemView.findViewById(R.id.track);
        name = itemView.findViewById(R.id.name);
        duration = itemView.findViewById(R.id.duration);
        tvAlbum = itemView.findViewById(R.id.album);
        artists = itemView.findViewById(R.id.artists);

        LinearLayout layout = itemView.findViewById(R.id.list_item);
        layout.setOnClickListener(this);
    }

    public void bind(Model.Track track) {
        idTrack = track.id;

        trackNo.setText(String.valueOf(track.track_number));
        name.setText(track.name);
        duration.setText(milisToString(track.duration_ms));
        Model.SimplifiedAlbum album = track.album;
        tvAlbum.setText(String.format("%s * %s * %s", album.name, album.album_type, album.release_date));
        if (track.artists != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("disc ");
            builder.append(track.disc_number);
            for (Model.SimplifiedArtist artist : track.artists) {
                builder.append(" * ");
                builder.append(artist.name);
            }
            artists.setText(builder.toString());
        }
    }

    public void onClick(View v) {
        if (TextUtils.isEmpty(idTrack)) return;
        Intent intent = new Intent(mContext, Track.class);
        intent.putExtra(Model.ID_KEY, idTrack);
        mContext.startActivity(intent);
    }

    private String milisToString(long milis) {
        long s = milis / 1000;
        long m = s / 60;
        long h = m / 60;
        long ss = s % 60;
        long mm = m % 60;
        long hh = h % 24;
        if (hh == 0)
            return String.format("%02d:%02d", mm, ss);
        else
            return String.format("%d:%02d:%02d", hh, mm, ss);
    }
}
