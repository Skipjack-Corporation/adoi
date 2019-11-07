package com.skipjack.adoi.messaging.event;

import org.matrix.androidsdk.rest.model.message.Message;

public enum EventMsgType {
    MSGTYPE_TEXT(Message.MSGTYPE_TEXT),
    MSGTYPE_EMOTE(Message.MSGTYPE_EMOTE),
    MSGTYPE_NOTICE(Message.MSGTYPE_NOTICE),
    MSGTYPE_IMAGE(Message.MSGTYPE_IMAGE),
    MSGTYPE_AUDIO(Message.MSGTYPE_AUDIO),
    MSGTYPE_VIDEO(Message.MSGTYPE_VIDEO),
    MSGTYPE_LOCATION(Message.MSGTYPE_LOCATION),
    MSGTYPE_FILE(Message.MSGTYPE_FILE);
    public String msType;
    EventMsgType(String msgtype) {
        this.msType = msgtype;
    }

    public static EventMsgType getType(String msType) {
        for (EventMsgType eventMsgType : values()){
            if (eventMsgType.msType.equals(msType)){
                return eventMsgType;
            }
        }
        return MSGTYPE_TEXT;
    }

}
