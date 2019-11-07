package com.skipjack.adoi.messaging.room;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import support.skipjack.adoi.model.RoomTabType;

public class RoomViewModelFactory implements ViewModelProvider.Factory {
    private RoomTabType roomTabType;

    public RoomViewModelFactory(RoomTabType roomTabType) {
        this.roomTabType = roomTabType;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RoomViewModel(roomTabType);
    }
}
