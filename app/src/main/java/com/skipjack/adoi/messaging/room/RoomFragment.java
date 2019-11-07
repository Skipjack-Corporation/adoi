package com.skipjack.adoi.messaging.room;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;

import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.RoomTabType;

public class RoomFragment extends BaseFragment implements Observer<List<Room>> {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.layoutNoResult) View noResultView;
    @BindView(R.id.textDummyTitle) TextView textNoResultTitle;
    private RoomTabType roomTabType;
    private RoomAdapter adapter;

    private RoomViewModel roomViewModel;


    @Override
    public int getLayoutResource() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onCreateView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (getArguments() != null){
            roomTabType = (RoomTabType) getArguments().getSerializable(Constants.ARG_ROOM_TYPE);
        }
        roomViewModel = ViewModelProviders.of(this, new RoomViewModelFactory(roomTabType))
                .get(RoomViewModel.class);
        roomViewModel.loadRooms();
        roomViewModel.liveRoomList.observe(this,this);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onChanged(List<Room> roomList) {
        if (roomList == null){
            setNoResultView(true);
            return;
        }else if (roomList.size() == 0){
            setNoResultView(false);
            return;
        }

        setNoResultView(false);
        if (adapter == null){
            adapter = new RoomAdapter(new ArrayList<>(roomList));
            recyclerView.setAdapter(adapter);
        }else {
            adapter.update(roomList);
        }
    }
    private void setNoResultView(boolean noResult){
        if (!noResult){
            noResultView.setVisibility(View.GONE);
            return;
        }
        noResultView.setVisibility(View.VISIBLE);
        textNoResultTitle.setText(roomTabType == RoomTabType.FRIENDS? "No Friend(s)":
                roomTabType == RoomTabType.OTHERS?"No Room(s)":roomTabType == RoomTabType.FAVORITES?
                "No Favorite(s)":"No Result(s)");

    }


}
