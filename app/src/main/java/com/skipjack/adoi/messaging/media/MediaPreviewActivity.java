package com.skipjack.adoi.messaging.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.TextView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.base.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MediaPreviewActivity extends BaseAppCompatActivity implements ViewPager.OnPageChangeListener {

    @BindView(R.id.textActionbarTitle) TextView textActiobarTitle;

    @BindView(R.id.previewViewPager)PreviewViewPager previewViewPager;
    private   List<SlidableMediaInfo> mediaInfoList = new ArrayList<>();
    @Override
    public int getLayoutResource() {
        return R.layout.activity_media_preview;
    }

    @Override
    public void onCreate() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaInfoList = (List<SlidableMediaInfo>) getIntent().getSerializableExtra(Constants.KEY_MEDIALIST_DATA);
        String currentId = getIntent().getStringExtra(Constants.KEY_CURRENT_MEDIA_ID);

        previewViewPager.setAdapter(new MediaPreviewAdapter(getSupportFragmentManager(),mediaInfoList));
        previewViewPager.setOnPageChangeListener(this);

        int cnt = 0;
        for (SlidableMediaInfo info: mediaInfoList){
            if (info.id.equals(currentId)){
                textActiobarTitle.setText(info.mFileName);
                previewViewPager.setCurrentItem(cnt);
                break;
            }
            cnt++;
        }


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        textActiobarTitle.setText(mediaInfoList.get(position).mFileName);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
