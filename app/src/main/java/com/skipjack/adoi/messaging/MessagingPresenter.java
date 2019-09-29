package com.skipjack.adoi.messaging;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.skipjack.adoi.messaging.model.MessageGroup;

import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;

import mx.skipjack.service.MatrixUtility;
import mx.skipjack.service.MxRoomManager;

public class MessagingPresenter implements MxRoomManager.Callback {
    private MessagingView view;
    private MxRoomManager mxRoomManager;
    public MessagingPresenter(@NonNull MessagingView view) {
        this.view = view;
        mxRoomManager = new MxRoomManager(this);
    }

    public void getRooms(){
        view.onStartProgress();
        mxRoomManager.getRooms();
    }

    @Override
    public void onGetRooms(ArrayList<MessageGroup> favoriteList, ArrayList<MessageGroup> directChatList,
                           ArrayList<MessageGroup> otherRoomList, ArrayList<MessageGroup> lowPriorityList,
                           ArrayList<MessageGroup> serverNoticeList) {



        view.onStopProgress();
        view.onGetRooms(favoriteList,directChatList,otherRoomList,lowPriorityList,serverNoticeList);
    }
}
