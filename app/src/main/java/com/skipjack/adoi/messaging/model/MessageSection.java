package com.skipjack.adoi.messaging.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageSection implements Parcelable {

    String eventId;
    String senderId;
    String senderName;
    String avatarUrl;
    String senderState;
    long timeStamp;

    public MessageSection() {
    }

    public MessageSection(String eventId, String senderId, String senderName, String avatarUrl, long timeStamp) {
        this.eventId = eventId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.avatarUrl = avatarUrl;
        this.timeStamp = timeStamp;
    }

    protected MessageSection(Parcel in) {
        eventId = in.readString();
        senderId = in.readString();
        senderName = in.readString();
        avatarUrl = in.readString();
        senderState = in.readString();
        timeStamp = in.readLong();
    }

    public static final Creator<MessageSection> CREATOR = new Creator<MessageSection>() {
        @Override
        public MessageSection createFromParcel(Parcel in) {
            return new MessageSection(in);
        }

        @Override
        public MessageSection[] newArray(int size) {
            return new MessageSection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventId);
        dest.writeString(senderId);
        dest.writeString(senderName);
        dest.writeString(avatarUrl);
        dest.writeString(senderState);
        dest.writeLong(timeStamp);
    }

    public String getEventId() {
        return eventId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getSenderState() {
        return senderState;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
