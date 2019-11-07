package com.skipjack.adoi.messaging.media;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.group.GroupFragment;
import com.skipjack.adoi.messaging.room.RoomFragment;

import java.util.ArrayList;
import java.util.List;

import support.skipjack.adoi.model.RoomTabType;

public class MediaPreviewAdapter extends FragmentStatePagerAdapter {
    private List<SlidableMediaInfo> list = new ArrayList<>();
    public MediaPreviewAdapter(FragmentManager fm, List<SlidableMediaInfo> list) {
        super(fm);
        this.list = list;

    }
    @Override
    public Fragment getItem(int position) {
        MediaPreviewFragment fragment = new MediaPreviewFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.ARG_MEDIA_DATA, list.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
