package support.skipjack.adoi.repository;

import android.annotation.SuppressLint;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomTag;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import support.skipjack.adoi.converter.RoomConverter;
import support.skipjack.adoi.local_storage.AppDatabase;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.model.RoomEntry;
import support.skipjack.adoi.model.RoomTabType;

public class RoomRepository {
    public MutableLiveData<List<RoomEntry>> roomEntries = new MutableLiveData<>();
    private RoomTabType roomTabType;
    private Callback callback;
    public RoomRepository(RoomTabType roomTabType, Callback callback) {
        this.roomTabType = roomTabType;
        this.callback = callback;
        MatrixService.get().mxSession.getDataHandler().addListener(mxEventListener);
    }
    private MXEventListener mxEventListener = new MXEventListener(){
        @Override
        public void onLiveEvent(Event event, RoomState roomState) {
            getRooms();
        }
        @Override
        public void onNewRoom(String roomId) {
            super.onNewRoom(roomId);
            callback.onNewRoomAdded(MatrixService.get().getRoom(roomId));
        }
        @Override
        public void onJoinRoom(String roomId) {
            super.onJoinRoom(roomId);
            callback.onNewRoomAdded(MatrixService.get().getRoom(roomId));
        }

        @Override
        public void onLeaveRoom(String roomId) {
            super.onLeaveRoom(roomId);
            callback.onRoomDeleted(MatrixService.get().getRoom(roomId));
        }

        @Override
        public void onRoomKick(String roomId) {
            super.onRoomKick(roomId);
            callback.onRoomDeleted(MatrixService.get().getRoom(roomId));
        }

        @Override
        public void onRoomFlush(String roomId) {
            super.onRoomFlush(roomId);
            callback.onRoomUpdated(MatrixService.get().getRoom(roomId));
        }

        @Override
        public void onNewGroupInvitation(String groupId) {
            super.onNewGroupInvitation(groupId);
        }

        @Override
        public void onJoinGroup(String groupId) {
            super.onJoinGroup(groupId);
        }

        @Override
        public void onLeaveGroup(String groupId) {
            super.onLeaveGroup(groupId);
        }
    };

    private boolean hasCallback(){ return callback != null;}

    public void removeListener(){
        MatrixService.get().mxSession.getDataHandler().removeListener(mxEventListener);
    }
    /**
     * Live Stream queries
     * */
    public void getRooms(){
        if (MatrixUtility.isNetworkConnected()){
            List<Room> roomList = getRoomsLive();
            callback.onGetRooms(roomList);

            //save copy to local
            insertRoomsToDB(roomList);
        }else{
            getRoomsFromDB();
        }
    }
    private List<Room> getRoomsLive(){
        Collection<Room> rooms = MatrixService.get().mxSession.getDataHandler().getStore().getRooms();
        return filterRoomsByType(new ArrayList<>(rooms), roomTabType);
    }

    public void updatedRoom(String roomId){
        Room room = MatrixService.get().mxSession.getDataHandler().getStore().getRoom(roomId);
        if (hasCallback()) callback.onRoomUpdated(room);
    }

    /**
     * Execute room database queries
     * */
    @SuppressLint("CheckResult")
    public void getRoomsFromDB(){
        AppDatabase.GET.roomDao()
                .getRoomsByType(roomTabType.getType())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(roomEntryList -> {
                    callback.onGetRooms(convertToRooms(roomEntryList));
                },throwable -> {
                    throwable.printStackTrace();
                });
    }
    @SuppressLint("CheckResult")
    public void insertRoomsToDB(List<Room> rooms) {
        List<RoomEntry> roomEntries = new RoomConverter().convert(rooms);
        AppDatabase.GET.roomDao()
                .insert(roomEntries.toArray(new RoomEntry[roomEntries.size()]))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                },throwable -> {
                    throwable.printStackTrace();
                });
    }
    @SuppressLint("CheckResult")
    public static void insertRoomsToDB(Room room) {
        RoomEntry roomEntry = new RoomConverter().convert(room);
        AppDatabase.GET.roomDao()
                .insert(roomEntry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                },throwable -> {
                    throwable.printStackTrace();
                });
    }
    private List<Room> convertToRooms(List<RoomEntry> roomEntries){
        return sort(new RoomConverter().revert(roomEntries));
    }
    private List<Room> filterRoomsByType(List<Room> roomList, RoomTabType roomTabType){
        List<Room> newRoomList = new ArrayList<>();
        for (Room room: roomList){
            final Set<String> tags = room.getAccountData().getKeys();
            final boolean isFavorite = tags != null && tags.contains(RoomTag.ROOM_TAG_FAVOURITE);
            final boolean isLowPriority = tags != null && tags.contains(RoomTag.ROOM_TAG_LOW_PRIORITY);
            final boolean isServerNotice = tags != null && tags.contains(RoomTag.ROOM_TAG_SERVER_NOTICE);
            final boolean isDirectChat = MatrixService.get().mxSession.getDataHandler()
                    .getDirectChatRoomIdsList().contains(room.getRoomId());


            RoomTabType tabType;
            if (isFavorite) tabType = RoomTabType.FAVORITES;
            else if (isLowPriority) tabType = RoomTabType.LOWPRIORITY;
            else if (isServerNotice) tabType = RoomTabType.SERVERNOTICE;
            else if (isDirectChat) tabType = RoomTabType.FRIENDS;
            else tabType = RoomTabType.OTHERS;

            if (tabType == roomTabType)
                newRoomList.add(room);

        }
        return sort(newRoomList);
    }

    private List<Room> sort(List<Room> roomList){
        Collections.sort(roomList, new Comparator<Room>(){
            public int compare(Room obj1, Room obj2) {
                // ## Ascending order
                // return Long.valueOf(obj1.date).compareTo(Long.valueOf(obj2.date));

                // ## Descending order
                return Long.valueOf(obj2.getRoomSummary().getLatestReceivedEvent().originServerTs)
                        .compareTo(obj1.getRoomSummary().getLatestReceivedEvent().originServerTs);
            }

        });
        return roomList;
    }
    public interface Callback{
        void onGetRooms(List<Room> roomList);
        void onNewRoomAdded(Room room);
        void onRoomUpdated(Room room);
        void onRoomInvitation(Room room);
        void onRoomDeleted(Room room);

    }

}
