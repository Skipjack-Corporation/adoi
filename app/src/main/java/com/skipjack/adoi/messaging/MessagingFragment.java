package com.skipjack.adoi.messaging;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.event.EventActivity;

import org.matrix.androidsdk.core.MXPatterns;
import org.matrix.androidsdk.data.Room;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;
import com.skipjack.adoi._repository.RoomRepository;
import com.skipjack.adoi.view.InviteUserDialog;

public class MessagingFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {

    @BindView(R.id.radioGroupMessages) RadioGroup radioGroupMessages;
    @BindView(R.id.rbtnFriends) RadioButton rbtnFriends;
    @BindView(R.id.rbtnRooms) RadioButton rbtnRooms;
    @BindView(R.id.rbtnCommunity) RadioButton rbtnCommunity;
    @BindView(R.id.rbtnFavorites) RadioButton rbtnFavorites;
    @BindView(R.id.viewPager) ViewPager viewPager;
    private MessagingAdapter adapter;

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_main_message;
    }

    @Override
    public void onCreateView() {
        radioGroupMessages.setOnCheckedChangeListener(this);
//        if (adapter == null){
            adapter = new MessagingAdapter(getChildFragmentManager());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(this);
//        }else {
//            adapter.update();
//            adapter.notifyDataSetChanged();
//        }



    }


    @Override
    public void onResume() {
        super.onResume();
        int currentItem = viewPager.getCurrentItem();
        if (currentItem == 0){
            rbtnFriends.setChecked(true);
        }else if (currentItem == 1){
            rbtnRooms.setChecked(true);

        }else if (currentItem == 2){
            rbtnCommunity.setChecked(true);

        }else if (currentItem == 3){
            rbtnFavorites.setChecked(true);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @OnClick(R.id.fabCreateRoom)
    public void onCreateRoom(){
        showProgressDialog();
        MatrixService.get().mxSession.createRoom(new MatrixCallback<String>() {
            @Override
            public void onAPISuccess(String data) {
                Room room = MatrixService.get().mxSession.getDataHandler()
                        .getStore().getRoom(data);
                RoomRepository.insertRoomsToDB(room);

                Intent intent = new Intent(getActivity(), EventActivity.class);
                intent.putExtra(Constants.KEY_ROOM_ID,room.getRoomId());
                hideProgressDialog();
                getActivity().startActivity(intent);
            }

            @Override
            public void onAPIFailure(String errorMessage) {
                hideProgressDialog();
                Toast.makeText(getActivity(),"Failed to create room.",Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick(R.id.fabStartChat)
    public void onCreateDirectChat(){

        InviteUserDialog.show(this.getActivity(), new InviteUserDialog.Callback() {
            @Override
            public void onSuccess(Room room) {
                Intent intent = new Intent(getActivity(), EventActivity.class);
                intent.putExtra(Constants.KEY_ROOM_ID,room.getRoomId());
                intent.putExtra(Constants.KEY_ROOM_NAME,room.getRoomDisplayName(MatrixService.get().getContext()));
                hideProgressDialog();
                getActivity().startActivity(intent);
            }

            @Override
            public void onFailed(String message) {
                Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
            }
        });
//        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppDialogTheme);
//        View view = getLayoutInflater().inflate(R.layout.layout_invite_user,null);
//        final EditText edittext = view.findViewById(R.id.editText);
//        alert.setTitle("Invite User");
//
//        alert.setView(view);
//
//        alert.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                showProgressDialog();
//                String text = edittext.getText().toString();
//                if (!text.substring(0,1).equals("@")){
//                    text = "@" + text;
//                }
//
//                if (!text.contains(":adoichat.com")){
//                    text = text+":adoichat.com";
//                }
//                List<Pattern> patterns = Arrays.asList(MXPatterns.PATTERN_CONTAIN_MATRIX_USER_IDENTIFIER, android.util.Patterns.EMAIL_ADDRESS);
//
//                for (Pattern pattern : patterns) {
//                    Matcher matcher = pattern.matcher(text);
//                    while (matcher.find()) {
//                        try {
//                            String userId = text.substring(matcher.start(0), matcher.end(0));
//                            MatrixService.get().mxSession.createDirectMessageRoom(userId, new MatrixCallback<String>() {
//                                @Override
//                                public void onAPISuccess(String data) {
//                                    Room room = MatrixService.get().mxSession.getDataHandler()
//                                            .getStore().getRoom(data);
//                                    RoomRepository.insertRoomsToDB(room);
//
//                                    Intent intent = new Intent(getActivity(), EventActivity.class);
//                                    intent.putExtra(Constants.KEY_ROOM_ID,room.getRoomId());
//                                    intent.putExtra(Constants.KEY_ROOM_NAME,room.getRoomDisplayName(MatrixService.get().getContext()));
//                                    hideProgressDialog();
//                                    getActivity().startActivity(intent);
//                                }
//
//                                @Override
//                                public void onAPIFailure(String errorMessage) {
//                                    dialog.dismiss();
//                                    hideProgressDialog();
//                                    Toast.makeText(getActivity(),errorMessage,Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        } catch (Exception e) {
//                            MatrixHelper.LOG("## displayInviteByUserId() " + e.getMessage());
//                            hideProgressDialog();
//                            Toast.makeText(getActivity(),"Failed to find user.",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }
//            }
//        });
//
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.dismiss();
//                // what ever you want to do with No option.
//            }
//        });
//
//        alert.show();
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
}
