package com.skipjack.adoi.messaging.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MessageGroup implements Parcelable {
    String roomId;
    int unreadCount;
    int status;
    String displayName;
    String date;
    String avatarUrl;
    String latestMessage;

    public MessageGroup() {

    }

    protected MessageGroup(Parcel in) {
        roomId = in.readString();
        unreadCount = in.readInt();
        status = in.readInt();
        displayName = in.readString();
        date = in.readString();
        avatarUrl = in.readString();
        latestMessage = in.readString();
    }

    public static final Creator<MessageGroup> CREATOR = new Creator<MessageGroup>() {
        @Override
        public MessageGroup createFromParcel(Parcel in) {
            return new MessageGroup(in);
        }

        @Override
        public MessageGroup[] newArray(int size) {
            return new MessageGroup[size];
        }
    };

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public int getStatus() {
        return status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDate() {
        return date;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomId);
        dest.writeInt(unreadCount);
        dest.writeInt(status);
        dest.writeString(displayName);
        dest.writeString(date);
        dest.writeString(avatarUrl);
        dest.writeString(latestMessage);
    }
}
