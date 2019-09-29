package com.skipjack.adoi.register;

import android.content.Intent;

import androidx.viewpager.widget.ViewPager;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.main.MainActivity;

import butterknife.BindView;

public class RegisterProfileActivity extends BaseAppCompatActivity implements ViewPager.OnPageChangeListener {

    @BindView(R.id.viewPager)ViewPager viewPager;
    @Override
    public int getLayoutResource() {
        return R.layout.activity_viewpager;
    }

    @Override
    public void onCreate() {
        viewPager.setAdapter(new RegisterProfileViewPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
