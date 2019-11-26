package com.skipjack.adoi._repository;

import org.matrix.androidsdk.data.Room;

import java.util.List;

import support.skipjack.adoi.model.RoomTabType;

public interface RoomRepositoryInterface {
    void syncRooms();
    List<Room> getRoomsByType(RoomTabType roomTabType);
    List<Room> getAllRooms();
    void joinRoom(Room room);
    void declineRoom(Room room);
    void addRoom(Room room);
    void updateRoom(Room room);
    void deleteRoom(Room room);

}
