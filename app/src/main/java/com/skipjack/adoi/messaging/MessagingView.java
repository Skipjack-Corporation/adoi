package com.skipjack.adoi.messaging;

import com.skipjack.adoi.messaging.model.MessageGroup;

import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;

public interface MessagingView {
    void onStartProgress();
    void onStopProgress();
    void onGetRooms(ArrayList<MessageGroup> favoriteList, ArrayList<MessageGroup> directChatList,
                    ArrayList<MessageGroup> otherRoomList, ArrayList<MessageGroup> lowPriorityList,
                    ArrayList<MessageGroup> serverNoticeList);
}
