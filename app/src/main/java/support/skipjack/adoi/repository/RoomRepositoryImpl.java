package support.skipjack.adoi.repository;

import android.annotation.SuppressLint;

import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.converter.RoomConverter;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.RoomEntry;
import support.skipjack.adoi.model.RoomTabType;
import support.skipjack.adoi.local_storage.AppDatabase;

public class RoomRepositoryImpl implements RoomRepositoryInterface {
    private static final RoomRepositoryInterface ourInstance = new RoomRepositoryImpl();

    public static RoomRepositoryInterface get() {
        return ourInstance;
    }

    private MXDataHandler mxDataHandler;
    private List<Room> roomList = new ArrayList<>();
    private RoomRepositoryImpl() {
        mxDataHandler = MatrixService.get().mxSession.getDataHandler();
    }
    @Override
    public void syncRooms() {
        if (MatrixUtility.isNetworkConnected()){
            Collection<Room> roomCollection = mxDataHandler.getStore().getRooms();
            roomList = new ArrayList<>(roomCollection);

            //save copy to local
            resetRoomDB();
            insertChatRoomsToDB(roomList);
        }
    }

    @Override
    public List<Room> getRoomsByType(RoomTabType roomTabType) {
        List<Room> newRoomList = new ArrayList<>();
        for (Room room: roomList){
            if (roomTabType == getRoomType(room))
                newRoomList.add(room);
        }
        sortRooms(newRoomList);
        return newRoomList;
    }

    @Override
    public List<Room> getAllRooms() {
        sortRooms(roomList);
        return roomList;
    }

    @Override
    public void joinRoom(Room room) {

    }

    @Override
    public void declineRoom(Room room) {

    }

    @Override
    public void addRoom(Room room) {

    }

    @Override
    public void updateRoom(Room room) {

    }

    @Override
    public void deleteRoom(Room room) {

    }

    private List<Room> sortRooms(List<Room> roomList){
        Collections.sort(roomList, new Comparator<Room>(){
            public int compare(Room obj1, Room obj2) {
                // ## Descending order
                return Long.valueOf(obj2.getRoomSummary().getLatestReceivedEvent().originServerTs)
                        .compareTo(obj1.getRoomSummary().getLatestReceivedEvent().originServerTs);
            }
        });

        return roomList;
    }

    private RoomTabType getRoomType(Room room){
        final Set<String> tags = room.getAccountData().getKeys();

        if (tags != null && tags.contains(RoomTag.ROOM_TAG_FAVOURITE))
            return RoomTabType.FAVORITES;
        else  if (tags != null && tags.contains(RoomTag.ROOM_TAG_LOW_PRIORITY))
            return RoomTabType.LOWPRIORITY;
        else  if (tags != null && tags.contains(RoomTag.ROOM_TAG_SERVER_NOTICE))
            return RoomTabType.SERVERNOTICE;
        else  if (mxDataHandler.getDirectChatRoomIdsList().contains(room.getRoomId()))
            return RoomTabType.FRIENDS;
        else
            return RoomTabType.OTHERS;
    }

    /**
     * Local Storage
     * */
    @SuppressLint("CheckResult")
    public void insertChatRoomsToDB(List<Room> rooms) {
        List<RoomEntry> roomEntries = new RoomConverter().convert(rooms);
        AppDatabase.GET.roomDao()
            .insert(roomEntries.toArray(new RoomEntry[roomEntries.size()]))
            ;
    }

    public void resetRoomDB(){
        AppDatabase.GET.roomDao().reset();
    }
}
