package com.skipjack.adoi.post;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.view.AppImageView;

import butterknife.BindView;

public class AddPostActivity extends BaseAppCompatActivity {

    @BindView(R.id.imgPostAvatar) AppImageView imgPostAvatar;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_add_post;
    }

    @Override
    public void onCreate() {
        imgPostAvatar.setImageCircularDrawable(getDrawable(R.drawable.img_example_2_small));
    }
}
