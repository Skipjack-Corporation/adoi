package support.skipjack.adoi.converter;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomTag;

import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.RoomEntry;
import support.skipjack.adoi.model.RoomTabType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoomConverter extends BaseConverter<Room, RoomEntry> {


    @Override
    public RoomEntry convert(Room room) {
        RoomEntry roomEntry = new RoomEntry();
        roomEntry.id = room.getRoomId();


        final Set<String> tags = room.getAccountData().getKeys();
        final boolean isFavorite = tags != null && tags.contains(RoomTag.ROOM_TAG_FAVOURITE);
        final boolean isLowPriority = tags != null && tags.contains(RoomTag.ROOM_TAG_LOW_PRIORITY);
        final boolean isServerNotice = tags != null && tags.contains(RoomTag.ROOM_TAG_SERVER_NOTICE);
        final boolean isDirectChat = MatrixService.get().mxSession.getDataHandler()
                .getDirectChatRoomIdsList().contains(room.getRoomId());

        if (isFavorite) roomEntry.tabType = RoomTabType.FAVORITES.getType();
        else if (isLowPriority) roomEntry.tabType = RoomTabType.LOWPRIORITY.getType();
        else if (isServerNotice) roomEntry.tabType = RoomTabType.SERVERNOTICE.getType();
        else if (isDirectChat) roomEntry.tabType = RoomTabType.FRIENDS.getType();
        else roomEntry.tabType = RoomTabType.OTHERS.getType();

        return roomEntry;
    }

    @Override
    public List<RoomEntry> convert(List<Room> rooms) {
        List<RoomEntry> list = new ArrayList<>();
        for (Room room: rooms){
            list.add(convert(room));
        }
        return list;
    }

    @Override
    public Room revert(RoomEntry roomEntry) {
        Room room = mxDataHandler.getRoom(roomEntry.id);
        return room;
    }

    @Override
    public List<Room> revert(List<RoomEntry> roomEntries) {
        List<Room> roomList = new ArrayList<>();
        for (RoomEntry roomEntry: roomEntries){
            roomList.add(revert(roomEntry));
        }
        return roomList;
    }
}
