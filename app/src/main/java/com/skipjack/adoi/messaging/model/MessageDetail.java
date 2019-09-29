package com.skipjack.adoi.messaging.model;

import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;
import java.util.List;

public class MessageDetail {
    String eventId;
    String senderId;
    String senderName;
    String avatarUrl;
    String senderState;
    long timeStamp;
    List<Event> events = new ArrayList<>();

    public MessageDetail(String eventId, String senderId, String senderName, String avatarUrl, long timeStamp) {
        this.eventId = eventId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.avatarUrl = avatarUrl;

        this.timeStamp = timeStamp;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public void addEvent(Event event){
        this.events.add(event);
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

    public List<Event> getEvents() {
        return events;
    }
}
