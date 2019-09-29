package com.skipjack.adoi.register;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.skipjack.adoi.register.fragments.AddConnectionFragment;
import com.skipjack.adoi.register.fragments.AddFriendFragment;
import com.skipjack.adoi.register.fragments.CameraFragment;
import com.skipjack.adoi.register.fragments.SetBioFragment;
import com.skipjack.adoi.register.fragments.VerifyCodeFragment;
import com.skipjack.adoi.register.fragments.VerifyMobileFragment;

public class RegisterProfileViewPagerAdapter extends FragmentStatePagerAdapter {
    public RegisterProfileViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) return new VerifyMobileFragment();
        else if (position == 1) return new VerifyCodeFragment();
        else if (position == 2) return new SetBioFragment();
        else if (position == 3) return new CameraFragment();
        else if (position == 4) return new AddFriendFragment();
        else if (position == 5) return new AddConnectionFragment();

        return null;
    }

    @Override
    public int getCount() {
        return 6;
    }
}
