package com.skipjack.adoi.register.fragments;

import android.content.Intent;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.main.MainActivity;
import com.skipjack.adoi.startup.StartUpActivity;

import butterknife.OnClick;

public class AddConnectionFragment extends BaseFragment {
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_add_connection;
    }

    @Override
    public void onCreateView() {

    }

    @OnClick(R.id.btnNext)
    public void onNextClick(){
        getActivity().startActivity(new Intent(getActivity(), StartUpActivity.class));
    }
}
