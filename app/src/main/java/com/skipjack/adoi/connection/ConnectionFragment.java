package com.skipjack.adoi.connection;

import android.content.Intent;
import android.widget.TextView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.main.MainActivity;
import com.skipjack.adoi.startup.StartUpActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class ConnectionFragment extends BaseFragment {
    @BindView(R.id.textDummyTitle) TextView textView;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_dummy;
    }

    @Override
    public void onCreateView() {
        textView.setText("Request Screen");
    }

}
