package com.skipjack.adoi.messaging.group;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.room.RoomAdapter;
import com.skipjack.adoi.messaging.room.RoomViewModel;
import com.skipjack.adoi.messaging.room.RoomViewModelFactory;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.group.Group;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.RoomTabType;

public class GroupFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.layoutNoResult) View noResultView;
    @BindView(R.id.textDummyTitle) TextView textNoResultTitle;

    private GroupAdapter adapter;

    @Override
    public int getLayoutResource() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onCreateView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        populateView();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public void populateView() {
        ArrayList<Group> groupList = new ArrayList<>(MatrixService.get().mxSession.getGroupsManager().getJoinedGroups());
        if (groupList == null){
            setNoResultView(true);
            return;
        }else if (groupList.size() == 0){
            setNoResultView(false);
            return;
        }

        setNoResultView(false);
        if (adapter == null){
            adapter = new GroupAdapter(groupList);
            recyclerView.setAdapter(adapter);
        }else {
            adapter.update(groupList);
        }
    }
    private void setNoResultView(boolean noResult){
        if (!noResult){
            noResultView.setVisibility(View.GONE);
            return;
        }
        noResultView.setVisibility(View.VISIBLE);
        textNoResultTitle.setText("No Group(s)");
    }


}
