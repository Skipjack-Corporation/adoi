package com.skipjack.adoi.messaging.room;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.matrix.androidsdk.data.Room;
import java.util.List;
import support.skipjack.adoi.model.RoomTabType;
import com.skipjack.adoi._repository.RoomRepository;

public class RoomViewModel extends ViewModel implements RoomRepository.Callback {
    public MutableLiveData<List<Room>> liveRoomList = new MutableLiveData<>();
    public RoomRepository roomRepository;
    public RoomViewModel(RoomTabType roomTabType) {
        roomRepository = new RoomRepository(roomTabType,this);
    }

    @Override
    protected void onCleared() {
        roomRepository.removeListener();
        super.onCleared();

    }

    public void loadRooms(){
        roomRepository.getRooms();
    }


    @Override
    public void onGetRooms(List<Room> roomList) {
        liveRoomList.setValue(roomList);
    }

    @Override
    public void onNewRoomAdded(Room room) {
        if (!liveRoomList.getValue().contains(room))
            liveRoomList.getValue().add(0,room);
    }

    @Override
    public void onRoomUpdated(Room room) {
        int cnt = 0;
        for (Room r: liveRoomList.getValue()){
            if (r.getRoomId().equals(room.getRoomId())){
                liveRoomList.getValue().set(cnt,r);
            }
            cnt++;
        }
    }

    @Override
    public void onRoomInvitation(Room room) {
        if (!liveRoomList.getValue().contains(room))
            liveRoomList.getValue().add(0,room);
    }

    @Override
    public void onRoomDeleted(Room room) {
        if (liveRoomList.getValue().contains(room))
            liveRoomList.getValue().remove(room);
    }
}
