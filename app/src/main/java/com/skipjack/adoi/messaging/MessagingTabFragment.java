package com.skipjack.adoi.messaging;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.model.MessageGroup;

import java.util.ArrayList;

import butterknife.BindView;

public class MessagingTabFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    private  ArrayList<MessageGroup> messageGroupList = new ArrayList<>();
    @Override
    public int getLayoutResource() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onCreateView() {
        if (getArguments() != null){
            messageGroupList = getArguments().getParcelableArrayList(Constants.ARG_MESSAGE_GROUP_LIST);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MessagingRecyclerAdapter(messageGroupList));
    }
}
