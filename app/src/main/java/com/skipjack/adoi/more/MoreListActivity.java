package com.skipjack.adoi.more;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.view.AppImageView;

import butterknife.BindView;

public class MoreListActivity extends BaseAppCompatActivity {

    @BindView(R.id.imgProfPic)
    AppImageView imgProfPic;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_more_list;
    }

    @Override
    public void onCreate() {
        imgProfPic.setImageCircularDrawable(getDrawable(R.drawable.img_example_2_small));
    }
}
