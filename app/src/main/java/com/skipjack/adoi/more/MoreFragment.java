package com.skipjack.adoi.more;

import android.widget.TextView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;

import butterknife.BindView;

public class MoreFragment extends BaseFragment {
    @BindView(R.id.textDummyTitle) TextView textView;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_dummy;
    }

    @Override
    public void onCreateView() {
        textView.setText("More Screen");
    }
}
