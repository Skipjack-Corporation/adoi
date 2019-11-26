package com.skipjack.adoi.messaging.room.people;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.RoomMember;

import java.util.List;

import butterknife.BindView;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;

public class PeopleFragment  extends BaseFragment {

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.layoutNoResult) View layoutNoResult;

    @Override
    public int getLayoutResource() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onCreateView() {
        String roomId = getArguments().getString(Constants.KEY_ROOM_ID);
        Room room = MatrixService.get().getRoom(roomId);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);

        room.getActiveMembersAsync(new MatrixCallback<List<RoomMember>>() {
            @Override
            public void onAPISuccess(List<RoomMember> data) {
                if (data == null) layoutNoResult.setVisibility(View.VISIBLE);
                else if (data.size() == 0) layoutNoResult.setVisibility(View.VISIBLE);
                else recyclerView.setAdapter(new PeopleAdapter(data));
            }

            @Override
            public void onAPIFailure(String errorMessage) {
                layoutNoResult.setVisibility(View.VISIBLE);
                AppUtility.toast(getActivity(),errorMessage);
            }
        });
    }
}
