package support.skipjack.adoi.model;

public enum RoomItemType {
    DEFAULT(1),
    INVITE_DIRECT(2),
    INVITE_ROOM(3);

    int type;
    RoomItemType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static RoomItemType getRoomItemType(int type) {
        for (RoomItemType itemType: values()){
            if (itemType.getType() == type)
                return itemType;
        }
        return DEFAULT;
    }
}
