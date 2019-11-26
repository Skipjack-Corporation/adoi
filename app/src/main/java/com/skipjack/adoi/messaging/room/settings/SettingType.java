package com.skipjack.adoi.messaging.room.settings;

public enum SettingType {
    RoomPhoto,
    RoomName,
    RoomTopic,
    RoomTag,
    RoomNotification,
    Leave,
    RoomDirectory,
    RoomAccess,
    RoomHistory,
    NoLocalAddress,
    AddNewAddress,
    Address,
    NoFlairCommunity,
    AddNewCommunity,
    Community,
    RoomInternalId,
    Encrypt,
    LineSeparator,
    TitleHeader;


    public static SettingType getType(int ordinal){
        for (SettingType type: values())
            if (ordinal == type.ordinal())
                return type;
        return LineSeparator;
    }
}
