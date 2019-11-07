package support.skipjack.adoi.model;

public enum RoomTabType {
    FRIENDS(1),
    OTHERS(2),
    COMMUNITY(3),
    FAVORITES(4),
    LOWPRIORITY(5),
    SERVERNOTICE(6);

    int type;
    RoomTabType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
