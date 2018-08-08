package eu.baboi.cristian.musicalstructure.fragments;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import eu.baboi.cristian.musicalstructure.R;

public class CategoryAdapter extends FragmentPagerAdapter {
    private final int[] categories = {R.string.category_artists, R.string.category_albums, R.string.category_tracks};
    private final Context mContext;

    public CategoryAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ArtistsFragment();
            case 1:
                return new AlbumsFragment();
            case 2:
                return new TracksFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return (position < 3 && position >= 0) ? mContext.getString(categories[position]) : null;
    }
}
