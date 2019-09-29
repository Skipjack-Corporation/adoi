package com.skipjack.adoi.messaging;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.gson.Gson;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.model.MessageGroup;

import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;
import java.util.List;

import mx.skipjack.service.MatrixUtility;

public class MessagingPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<MessageGroup> favoriteList = new ArrayList<>();
    private ArrayList<MessageGroup> directChatList = new ArrayList<>();
    private ArrayList<MessageGroup> otherRoomList = new ArrayList<>();
    private ArrayList<MessageGroup> lowPriorityList = new ArrayList<>();
    private ArrayList<MessageGroup> serverNoticeList = new ArrayList<>();
    public MessagingPagerAdapter(FragmentManager fm, ArrayList<MessageGroup> favoriteList,
                                 ArrayList<MessageGroup> directChatList,
                                 ArrayList<MessageGroup> otherRoomList,
                                 ArrayList<MessageGroup> lowPriorityList,
                                 ArrayList<MessageGroup> serverNoticeList) {
        super(fm);
        this.favoriteList = favoriteList;
        this.directChatList = directChatList;
        this.otherRoomList = otherRoomList;
        this.lowPriorityList = lowPriorityList;
        this.serverNoticeList = serverNoticeList;

        MatrixUtility.LOG("Favorites : "+new Gson().toJson(favoriteList));
        MatrixUtility.LOG("DirectChat : "+new Gson().toJson(directChatList));
        MatrixUtility.LOG("OtherRooms : "+new Gson().toJson(otherRoomList));
        MatrixUtility.LOG("LowPriorities : "+new Gson().toJson(lowPriorityList));
        MatrixUtility.LOG("ServerNotices : "+new Gson().toJson(serverNoticeList));
    }

    @Override
    public Fragment getItem(int position) {
        MessagingTabFragment fragment = new MessagingTabFragment();

        Bundle bundle = new Bundle();
        if (position == 0) bundle.putParcelableArrayList(Constants.ARG_MESSAGE_GROUP_LIST,directChatList);
        else if (position == 1) bundle.putParcelableArrayList(Constants.ARG_MESSAGE_GROUP_LIST,lowPriorityList);
        else if (position == 2) bundle.putParcelableArrayList(Constants.ARG_MESSAGE_GROUP_LIST,otherRoomList);
        else if (position == 3) bundle.putParcelableArrayList(Constants.ARG_MESSAGE_GROUP_LIST,favoriteList);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
