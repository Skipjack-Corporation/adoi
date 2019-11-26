package com.skipjack.adoi.messaging.room.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.event.EventActivity;
import com.skipjack.adoi.messaging.room.details.RoomDetailActivity;
import com.skipjack.adoi.utility.AppUtility;
import com.skipjack.adoi.view.RoomNameDialog;
import com.skipjack.adoi.view.RoomTopicDialog;

import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomAccountData;
import org.matrix.androidsdk.data.RoomTag;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;

public class SettingsFragment extends BaseFragment implements SettingsAdapter.Callback {

    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    private List<SettingItem> settingItems = new ArrayList<>();
    private SettingsAdapter adapter;
    private Room mRoom;
    @Override
    public int getLayoutResource() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onCreateView() {
        if (getArguments() != null)
        {
            String roomId = getArguments().getString(Constants.KEY_ROOM_ID);
            if (roomId != null)mRoom = MatrixService.get().getRoom(roomId);
        }
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);
        DividerItemDecoration decor = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        decor.setDrawable(getActivity().getDrawable(R.drawable.shape_divider));
        recyclerView.addItemDecoration(decor);

        settingItems = new ArrayList<>();
        prepareSettingsItemList();
    }
    @Override
    public void onSelectedSettings(SettingType settingType) {
        switch (settingType){
            case RoomName:{
                RoomNameDialog.show(this.getActivity(), mRoom, new RoomNameDialog.Callback() {
                    @Override
                    public void onSuccess(String name) {
                        SettingItem nameItem  = new SettingItem();
                        nameItem.title = "Room Name";
                        nameItem.subTitle = name;
                        nameItem.type = SettingType.RoomName;
                        settingItems.set(1,nameItem);
                        adapter.update(settingItems);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailed(String message) {
                        AppUtility.toast(getActivity(),message);
                    }
                });

            }break;
            case RoomTopic:{
                RoomTopicDialog.show(this.getActivity(), mRoom, new RoomTopicDialog.Callback() {
                    @Override
                    public void onSuccess(String name) {
                        SettingItem topicItem  = new SettingItem();
                        topicItem.title = "Topic";
                        topicItem.subTitle = name;
                        topicItem.type = SettingType.RoomTopic;
                        settingItems.set(2,topicItem);
                        adapter.update(settingItems);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailed(String message) {
                        AppUtility.toast(getActivity(),message);
                    }
                });
            }break;
            case RoomTag:{
                popMenu(R.menu.menu_roomtag);
            }break;
            case Leave:{leaveRoomConfirmDialog();}break;
        }
    }
    public void leaveRoomConfirmDialog(){
        new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_DARK)
                .setTitle("Leave Room")
                .setMessage("Are you sure you want to leave the room.")
                .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog();

                        MatrixService.get().getRoom(mRoom.getRoomId()).leave(new MatrixCallback<Void>() {
                            @Override
                            public void onAPISuccess(Void data) {
                                getActivity().finish();
                            }

                            @Override
                            public void onAPIFailure(String errorMessage) {
                                hideProgressDialog();
                                if (errorMessage != null){
                                    AppUtility.toast(getActivity(),errorMessage);
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    private void prepareSettingsItemList(){

        SettingItem photoItem = new SettingItem();
        photoItem.title = "Room Photo";
        photoItem.url = mRoom.getAvatarUrl();
        photoItem.type = SettingType.RoomPhoto;

        SettingItem nameItem  = new SettingItem();
        nameItem.title = "Room Name";
        nameItem.subTitle = mRoom.getRoomDisplayName(this.getActivity());
        nameItem.type = SettingType.RoomName;

        SettingItem topicItem  = new SettingItem();
        topicItem.title = "Topic";
        topicItem.subTitle = mRoom.getTopic();
        topicItem.type = SettingType.RoomTopic;

        SettingItem tagItem  = new SettingItem();
        tagItem.title = "Room Tag";
        tagItem.subTitle = "None";
        tagItem.type = SettingType.RoomTag;

        SettingItem notifItem  = new SettingItem();
        notifItem.title = "Notifications";
        notifItem.subTitle = "All messages";
        notifItem.type = SettingType.RoomNotification;


        SettingItem leaveItem  = new SettingItem();
        leaveItem.title = "Leave";
        leaveItem.type = SettingType.Leave;


        settingItems.add(photoItem);
        settingItems.add(nameItem);
        settingItems.add(topicItem);
        settingItems.add(tagItem);
        settingItems.add(notifItem);
        settingItems.add(leaveItem);
        settingItems.add(getItemLine());
        settingItems.add(getItemTitle("Access and visibility"));
        settingItems.add(getItemSwitch(SettingType.RoomDirectory,"List this room in room directory",null,false));
        settingItems.add(getItemText(SettingType.RoomAccess,"Room Access","Only people who have been invited"));
        settingItems.add(getItemText(SettingType.RoomHistory,"Room History Readability",
                "Members only(since the point in time of selecting this option)"));
        settingItems.add(getItemLine());
        settingItems.add(getItemTitle("Address"));
        settingItems.add(getItemText(SettingType.NoLocalAddress,"This room has no local address",null));
        settingItems.add(getItemAdd(SettingType.AddNewAddress,"New Address"));
        settingItems.add(getItemLine());
        settingItems.add(getItemTitle("Flair"));
        settingItems.add(getItemText(SettingType.NoFlairCommunity,"This room is not showing flair for any communities",null));
        settingItems.add(getItemAdd(SettingType.AddNewCommunity,"New Community ID"));
        settingItems.add(getItemLine());
        settingItems.add(getItemTitle("Advanced"));
        settingItems.add(getItemText(SettingType.RoomInternalId,"This room's internal ID",mRoom.getRoomId()));
        settingItems.add(getItemSwitch(SettingType.Encrypt,"Encrypt to verified devices only",
                "Never send encrypted messages to unverified devices in this room from this device",false));

        adapter = new SettingsAdapter(settingItems, this);
        recyclerView.setAdapter(adapter);

    }

    private SettingItem getItemTitle(String title){
        SettingItem item = new SettingItem();
        item.title = title;
        item.type = SettingType.TitleHeader;
        return item;
    }
    private SettingItem getItemLine(){
        SettingItem item = new SettingItem();
        item.type = SettingType.LineSeparator;
        return item;
    }
    private SettingItem getItemText(SettingType type,String title, String subTitle){
        SettingItem item = new SettingItem();
        item.title = title;
        if (subTitle != null) item.subTitle = subTitle;
        item.type = type;
        return item;
    }
    private SettingItem getItemSwitch(SettingType type,String title, String subTitle, boolean checked){
        SettingItem item = new SettingItem();
        item.title = title;
        if(subTitle != null) item.subTitle = subTitle;
        item.checked = checked;
        item.type = type;
        return item;
    }
    private SettingItem getItemAdd(SettingType type,String title){
        SettingItem item = new SettingItem();
        item.title = title;
        item.type = type;
        return item;
    }

    private void popMenu(int menuResId){
        PopupMenu popup = new PopupMenu(getActivity(), recyclerView, Gravity.CENTER);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(menuResId, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuFavorite:{
                        boolean isSupportedTag = false;
                        String newTag = RoomTag.ROOM_TAG_FAVOURITE;
                        String currentTag = null;
                        Double tagOrder = 0.0;

                        // retrieve the tag from the room info
                        RoomAccountData accountData = mRoom.getAccountData();
                        if ((null != accountData) && accountData.hasTags()) {
                            currentTag = accountData.getKeys().iterator().next();
                        }

                        if (!newTag.equals(currentTag)) {
                            if (newTag.equals(RoomTag.ROOM_TAG_FAVOURITE)
                                    || newTag.equals(RoomTag.ROOM_TAG_LOW_PRIORITY)) {
                                isSupportedTag = true;
                            } else if (newTag.equals(RoomTag.ROOM_TAG_NO_TAG)) {
                                isSupportedTag = true;
                                newTag = null;
                            } else {
                                // unknown tag.. very unlikely
                            }
                        }

                        if (isSupportedTag) {
                            mRoom.replaceTag(currentTag,
                                    newTag, tagOrder, new MatrixCallback<Void>() {
                                        @Override
                                        public void onAPISuccess(Void data) {
                                            SettingItem tagItem  = new SettingItem();
                                            tagItem.title = "Room Tag";
                                            tagItem.subTitle = item.getTitle().toString();
                                            tagItem.type = SettingType.RoomTag;
                                            settingItems.set(3,tagItem);
                                            adapter.update(settingItems);
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onAPIFailure(String errorMessage) {
                                            AppUtility.toast(getActivity(),errorMessage);
                                        }
                                    });
                        }
                    }

                        return true;
                    case R.id.menuLeave:

                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }


}
