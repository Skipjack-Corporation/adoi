package com.skipjack.adoi.messaging;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.skipjack.adoi.R;

import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;

import java.util.List;

import mx.skipjack.service.MatrixUtility;

public class MessagingDetailAdapter extends AbstractMessagesAdapter {


    public MessagingDetailAdapter(Context context, int view) {
        super(context, view);

        MatrixUtility.LOG("MessagingDetailAdapter "+getCount());
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_message_sender,parent,false);
        }

        MatrixUtility.LOG("getView : "+position);
        return convertView;
    }

    @Override
    public void add(MessageRow row, boolean refresh) {

    }

    @Override
    public void addToFront(MessageRow row) {

    }

    @Override
    public MessageRow getMessageRow(String eventId) {
        return null;
    }

    @Override
    public MessageRow getClosestRow(Event event) {
        return null;
    }

    @Override
    public MessageRow getClosestRowFromTs(String eventId, long eventTs) {
        return null;
    }

    @Override
    public MessageRow getClosestRowBeforeTs(String eventId, long eventTs) {
        return null;
    }

    @Override
    public void updateEventById(Event event, String oldEventId) {

    }

    @Override
    public void removeEventById(String eventId) {

    }

    @Override
    public void setIsPreviewMode(boolean isPreviewMode) {

    }

    @Override
    public void setIsUnreadViewMode(boolean isUnreadViewMode) {

    }

    @Override
    public boolean isUnreadViewMode() {
        return false;
    }

    @Override
    public void setSearchPattern(String pattern) {

    }

    @Override
    public void resetReadMarker() {

    }

    @Override
    public void updateReadMarker(String readMarkerEventId, String readReceiptEventId) {

    }

    @Override
    public int getMaxThumbnailWidth() {
        return 0;
    }

    @Override
    public int getMaxThumbnailHeight() {
        return 0;
    }

    @Override
    public void onBingRulesUpdate() {

    }

    @Override
    public void setLiveRoomMembers(List<RoomMember> roomMembers) {
    }



}
