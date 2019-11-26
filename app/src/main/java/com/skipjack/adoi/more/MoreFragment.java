package com.skipjack.adoi.more;

import android.view.View;
import android.widget.TextView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;

import butterknife.BindView;

public class MoreFragment extends BaseFragment {
    @BindView(R.id.textDummyTitle) TextView textTitle;
    @BindView(R.id.textDummySubTitle) TextView textSubTitle;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_dummy;
    }

    @Override
    public void onCreateView() {
        textTitle.setVisibility(View.GONE);
        textSubTitle.setVisibility(View.GONE);
    }
}
