package com.skipjack.adoi.messaging;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.group.GroupFragment;
import com.skipjack.adoi.messaging.room.RoomFragment;

import support.skipjack.adoi.model.RoomTabType;

public class MessagingAdapter extends FragmentStatePagerAdapter {
    public MessagingAdapter(FragmentManager fm) {
        super(fm);

    }
    public void update() {
    }
    @Override
    public Fragment getItem(int position) {
        RoomFragment fragment = new RoomFragment();

        Bundle bundle = new Bundle();
        if (position == 0) bundle.putSerializable(Constants.ARG_ROOM_TYPE, RoomTabType.FRIENDS);
        else if (position == 1) bundle.putSerializable(Constants.ARG_ROOM_TYPE, RoomTabType.OTHERS);
        else if (position == 2){
            return new GroupFragment();
        }
        else if (position == 3) bundle.putSerializable(Constants.ARG_ROOM_TYPE, RoomTabType.FAVORITES);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
