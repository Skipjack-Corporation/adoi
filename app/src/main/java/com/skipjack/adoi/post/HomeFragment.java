package com.skipjack.adoi.post;

import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_main_home;
    }

    @Override
    public void onCreateView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new PostRecyclerAdapter());
    }

    @OnClick(R.id.imgbtnAddPost)
    public void onAddPostClick(){
        startActivity(new Intent(getActivity(), AddPostActivity.class));
    }
}
