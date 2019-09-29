package com.skipjack.adoi.messaging;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.viewpager.widget.ViewPager;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.messaging.model.MessageGroup;

import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;

import butterknife.BindView;

public class MessagingFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener, MessagingView {

    @BindView(R.id.radioGroupMessages) RadioGroup radioGroupMessages;
    @BindView(R.id.rbtnFriends) RadioButton rbtnFriends;
    @BindView(R.id.rbtnRooms) RadioButton rbtnRooms;
    @BindView(R.id.rbtnCommunity) RadioButton rbtnCommunity;
    @BindView(R.id.rbtnFavorites) RadioButton rbtnFavorites;
    @BindView(R.id.viewPager) ViewPager viewPager;
    private MessagingPresenter messagingPresenter;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_main_message;
    }

    @Override
    public void onCreateView() {

        radioGroupMessages.setOnCheckedChangeListener(this);
        messagingPresenter = new MessagingPresenter(this);
        messagingPresenter.getRooms();


    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rbtnFriends) {
            viewPager.setCurrentItem(0,true);
        }else  if (checkedId == R.id.rbtnRooms) {
            viewPager.setCurrentItem(1,true);
        }else  if (checkedId == R.id.rbtnCommunity) {
            viewPager.setCurrentItem(2,true);
        }else  if (checkedId == R.id.rbtnFavorites) {
            viewPager.setCurrentItem(3,true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0){
            rbtnFriends.setChecked(true);
        }else if (position == 1){
            rbtnRooms.setChecked(true);

        }else if (position == 2){
            rbtnCommunity.setChecked(true);

        }else if (position == 3){
            rbtnFavorites.setChecked(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onStartProgress() {
        showProgressDialog();
    }

    @Override
    public void onStopProgress() {
        hideProgressDialog();
    }

    @Override
    public void onGetRooms(ArrayList<MessageGroup> favoriteList, ArrayList<MessageGroup> directChatList,
                           ArrayList<MessageGroup> otherRoomList, ArrayList<MessageGroup> lowPriorityList,
                           ArrayList<MessageGroup> serverNoticeList) {


        viewPager.setAdapter(new MessagingPagerAdapter(getChildFragmentManager(),
                favoriteList,directChatList,otherRoomList,lowPriorityList,serverNoticeList));
        viewPager.addOnPageChangeListener(this);
    }
}
