package com.skipjack.adoi.messaging.event;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;

import butterknife.OnClick;
import support.skipjack.adoi.matrix.MatrixHelper;

import com.skipjack.adoi._repository.CallRespository;
import com.skipjack.adoi._repository.EventManager;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.call.CallActivity;
import com.skipjack.adoi.messaging.media.MediaPreviewActivity;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;
import com.skipjack.adoi.messaging.room.details.RoomDetailActivity;
import com.skipjack.adoi.permission.Permission;
import com.skipjack.adoi.permission.PermissionManager;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.call.IMXCall;
import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.data.RoomMediaMessage;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.message.FileMessage;
import org.matrix.androidsdk.rest.model.message.ImageInfo;
import org.matrix.androidsdk.rest.model.message.ImageMessage;
import org.matrix.androidsdk.rest.model.message.Message;
import org.matrix.androidsdk.rest.model.message.VideoMessage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class EventActivity extends BaseAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, EventManager.EventCallback, EventsAdapter.Callback {

    private static final int SEND_FILE_REQUEST_CODE = 23;

    private static final int SEND_STICKER_REQUEST_CODE = 24;
    private static final int SEND_AUDIO_REQUEST_CODE = 25;
    private static final int SEND_IMAGE_VIDEO_REQUEST_CODE = 26;
    private static final int SEND_FILE_REQUEST_CODE_CONFIRM = 27;

    @BindView(R.id.textActionbarTitle) TextView textActionbarTitle;
    @BindView(R.id.textTyping) TextView textTyping;
    @BindView(R.id.editWrite) EditText editWrite;
    @BindView(R.id.swipeTop) SwipeRefreshLayout swipeTop;
    @BindView(R.id.recyclerViewMessageGroup) RecyclerView recyclerView;
    @BindView(R.id.imgBtnSticker) ImageButton imgBtnSticker;
    @BindView(R.id.imgBtnFile) ImageButton imgBtnFile;
    @BindView(R.id.imgBtnAudio) ImageButton imgBtnAudio;
    @BindView(R.id.layoutFileUploading) LinearLayout layoutFileUploading;
    @BindView(R.id.textNoResult) TextView textNoResult;


    private String roomName = "Chat room";
    private String roomId;
    private EventsAdapter adapter;
    private EventManager eventManager;
    @Override
    public int getLayoutResource() {
        return R.layout.activity_events;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);

        swipeTop.setOnRefreshListener(this);

        //Intent extras
        if (getIntent() != null){
            roomId = getIntent().getStringExtra(Constants.KEY_ROOM_ID);
            roomName = MatrixService.get().getRoomDisplayName(roomId);
        }

        textActionbarTitle.setText(roomName);

        eventManager = new EventManager(roomId,this);

        textActionbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        eventManager.sendReadMarker();

        editWrite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                imgBtnSticker.setVisibility(View.GONE);
                imgBtnFile.setVisibility(View.GONE);
                imgBtnAudio.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editWrite.getText().toString().replace(" ","").length() > 0){
                    editWrite.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_close_small,0);
                }else {
                    editWrite.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                eventManager.sendTypingNotification(editWrite.length() > 0);
                if (editWrite.getText().toString().replace(" ","").length() == 0){
                    imgBtnSticker.setVisibility(View.VISIBLE);
                    imgBtnFile.setVisibility(View.VISIBLE);
                    imgBtnAudio.setVisibility(View.VISIBLE);
                }

            }
        });
        editWrite.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        imgBtnSticker.setVisibility(View.VISIBLE);
                        imgBtnFile.setVisibility(View.VISIBLE);
                        imgBtnAudio.setVisibility(View.VISIBLE);
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SEND_FILE_REQUEST_CODE:{
                List<RoomMediaMessage> sharedDataItems = new ArrayList<>(RoomMediaMessage.listRoomMediaMessages(data));
                if (sharedDataItems.size() > 0)
                    eventManager.sendMediaMessage(sharedDataItems.get(0));
            }break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionManager.REQUESTCODE_ASK_PERMISSION){
            AppUtility.toast(this,"Request granted. Make a call again.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (eventManager != null)
        {
            eventManager.addListener();
            eventManager.sendReadMarker();
            eventManager.getEventListing();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (eventManager != null)
            eventManager.removeListener();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    @OnClick(R.id.imgBtnMore)
    public void onMore(View v ){
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_events, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuRoomDetails:
                        Log.e("Menu","Roomdetails");
                        Intent intent = new Intent(EventActivity.this, RoomDetailActivity.class);
                        intent.putExtra(Constants.KEY_ROOM_ID, roomId);
                        startActivity(intent);
                        return true;
                    case R.id.menuLeave:
                        leaveRoomConfirmDialog();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    @OnClick(R.id.btnSend)
    public void onSend(){
        String message = editWrite.getText().toString().trim();
        if (message.length() != 0)
         eventManager.sendTextMessage(message);

        editWrite.setText("");
    }
    @Override
    public void onRefresh() {
        MatrixHelper.LOG("onRefresh ");
        eventManager.pullPreviousEvents();
    }

    @OnClick(R.id.imgBtnFile)
    public void sendFile(){
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        fileIntent.setType("*/*");

        try {
            startActivityForResult(fileIntent, SEND_FILE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    @OnClick(R.id.imgBtnSticker)
    public void sendSticker(){
        Toast.makeText(this,"Under development.",Toast.LENGTH_LONG).show();
    }
    @OnClick(R.id.imgBtnAudio)
    public void sendAudio(){
        Toast.makeText(this,"Under development.",Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.imgBtnPhoneCall)
    public void phoneCall(){
        startCallInRoom(false);
    }

    @OnClick(R.id.imgBtnVideoCall)
    public void onVideoCall(){
        startCallInRoom(true);
    }

    public void leaveRoomConfirmDialog(){
        new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK)
                .setTitle("Leave Room")
                .setMessage("Are you sure you want to leave the room.")
                .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog();

                        MatrixService.get().getRoom(roomId).leave(new MatrixCallback<Void>() {
                            @Override
                            public void onAPISuccess(Void data) {
                                 finish();
                            }

                            @Override
                            public void onAPIFailure(String errorMessage) {
                                hideProgressDialog();
                                if (errorMessage != null){
                                    AppUtility.toast(EventActivity.this,errorMessage);
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startCallInRoom(boolean isVideoCall){

        if (isVideoCall && !PermissionManager.checkPermission(this,
                Permission.PERMISSION_READ_STORAGE,
                Permission.PERMISSION_WRITE_STORAGE,
                Permission.PERMISSION_RECORD_AUDIO,
                Permission.PERMISSION_CAMERA)){
            PermissionManager.askPermissions(this,
                    Permission.PERMISSION_READ_STORAGE,
                    Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_RECORD_AUDIO,
                    Permission.PERMISSION_CAMERA);
            return;
        }else if (!isVideoCall && !PermissionManager.checkPermission(this,
                Permission.PERMISSION_READ_STORAGE,
                Permission.PERMISSION_WRITE_STORAGE,
                Permission.PERMISSION_RECORD_AUDIO)){
            PermissionManager.askPermissions(this,
                    Permission.PERMISSION_READ_STORAGE,
                    Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_RECORD_AUDIO);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK)
                .setTitle("Confirmation");

        if (isVideoCall) {
            builder.setMessage("Are you sure you want to start a video call?");
        } else {
            builder.setMessage("Are you sure you want to start a phone call?");
        }

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog();
                        MatrixService.get().mxSession.mCallsManager.createCallInRoom(roomId, isVideoCall, new MatrixCallback<IMXCall>() {
                                @Override
                                public void onAPISuccess(IMXCall data) {
                                    hideProgressDialog();
                                    CallRespository.get().setActiveCall(data);
                                    Intent intent = new Intent(EventActivity.this, CallActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onAPIFailure(String errorMessage) {
                                    hideProgressDialog();
                                    AppUtility.toast(EventActivity.this,"Failed to proceed call request. "+errorMessage);
                                }
                        });

                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private boolean checkAudioPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
             // Permission is not granted
            return false;
        }
        return true;
    }
    private boolean checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
             // Permission is not granted
            return false;
        }
        return true;
    }

    @Override
    public void onEventListing(List<Event> eventList) {
        if (swipeTop.isRefreshing())
            swipeTop.setRefreshing(false);

        textNoResult.setVisibility(View.GONE);
        if (adapter == null){
            if (eventList == null){
                textNoResult.setVisibility(View.VISIBLE);
                return;
            }
            if (eventList.size() == 0){
                textNoResult.setVisibility(View.VISIBLE);
                return;
            }
            adapter = new EventsAdapter(eventList, MatrixService.get().getRoomState(roomId),this);
            recyclerView.setAdapter(adapter);

            if (eventList.size() < 10 && eventList.size() > 0)
                onRefresh();
        }else {
            if (eventList.size() > 0)
                adapter.update(eventList, MatrixService.get().getRoomState(roomId));
        }


    }

    @Override
    public void onEventListingError(String message) {

    }
    @Override
    public void onMediaContentClick(Event currentEvent) {
        ArrayList<SlidableMediaInfo> mediaInfoList = new ArrayList<>();

        for (Event event: adapter.getList()){
            Message message = JsonUtils.toMessage(event.getContent());
            if (Message.MSGTYPE_IMAGE.equals(message.msgtype)) {
                ImageMessage imageMessage = (ImageMessage) message;
                SlidableMediaInfo info = new SlidableMediaInfo();
                info.id = event.eventId;
                info.mMessageType = Message.MSGTYPE_IMAGE;
                info.mFileName = imageMessage.body;
                info.mMediaUrl = imageMessage.getUrl();
                info.mRotationAngle = imageMessage.getRotation();
                info.mOrientation = imageMessage.getOrientation();
                info.mMimeType = imageMessage.getMimeType();
                info.mEncryptedFileInfo = imageMessage.file;
                mediaInfoList.add(info);
            } else if (Message.MSGTYPE_VIDEO.equals(message.msgtype)) {
                VideoMessage videoMessage = (VideoMessage) message;
                SlidableMediaInfo info = new SlidableMediaInfo();
                info.id = event.eventId;
                info.mMessageType = Message.MSGTYPE_VIDEO;
                info.mFileName = videoMessage.body;
                info.mMediaUrl = videoMessage.getUrl();
                info.mThumbnailUrl = (null != videoMessage.info) ? videoMessage.info.thumbnail_url : null;
                info.mMimeType = videoMessage.getMimeType();
                info.mEncryptedFileInfo = videoMessage.file;
                mediaInfoList.add(info);
            }
        }


        Intent intent = new Intent(this, MediaPreviewActivity.class);
        intent.putExtra(Constants.KEY_MEDIALIST_DATA,mediaInfoList);
        intent.putExtra(Constants.KEY_CURRENT_MEDIA_ID,currentEvent.eventId);
        startActivity(intent);
    }
    @Override
    public void onMemberTyping(String message) {
        if (message == null) textTyping.setVisibility(View.GONE);
        else {
            textTyping.setText(message);
            textTyping.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUploadMediaStart(String uploadId, Event event, String mimeType) {
        startUploadMedia(uploadId,mimeType,event);
    }

    @Override
    public void onUploadMediaProgress(String uploadId, Event event, String mimeType, int progress) {
//        progressUploadMedia(uploadId,event,mimeType,progress);
    }

    @Override
    public void onUploadMediaFinish(String uploadId, boolean success, String failedMessage) {
        finishUploadMedia(uploadId);
        if (!success){
            Toast.makeText(this,failedMessage,Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Send the medias defined in the intent.
     * They are listed, checked and sent when it is possible.
     */
    @SuppressLint("NewApi")
    private void sendMediasIntent(Intent intent) {
        List<RoomMediaMessage> sharedDataItems = new ArrayList<>();

        if (null != intent) {
            sharedDataItems = new ArrayList<>(RoomMediaMessage.listRoomMediaMessages(intent));
        }

        // check the extras
        if ((0 == sharedDataItems.size()) && (null != intent)) {
            Bundle bundle = intent.getExtras();
            // sanity checks
            if (null != bundle) {
                if (bundle.containsKey(Intent.EXTRA_TEXT)) {
                    editWrite.append(bundle.getString(Intent.EXTRA_TEXT));

                    editWrite.post(new Runnable() {
                        @Override
                        public void run() {
                            editWrite.setSelection(editWrite.getText().length());
                        }
                    });
                }
            }
        }
        boolean hasItemToShare = !sharedDataItems.isEmpty();


        if (hasItemToShare) {
            intent.setExtrasClassLoader(RoomMediaMessage.class.getClassLoader());
            intent.putExtra( Constants.KEY_ROOM_NAME, roomName);
            startActivityForResult(intent, SEND_FILE_REQUEST_CODE_CONFIRM);
        }
    }


    private void startUploadMedia(String uploadId,String mimeType, Event event){
        View view = null;
        boolean exist = false;
        for (int i = 0; i < layoutFileUploading.getChildCount(); i ++){
            View v = layoutFileUploading.getChildAt(i);
           if (v.getTag().equals(uploadId)){
               exist = true;
               view = v;
               break;
           }
        }
        if (!exist){
            if (mimeType.contains("image/") || mimeType.contains("video/")){
                view = getLayoutInflater().inflate(R.layout.layout_upload_image_video_view,null);
            }else{
                view = getLayoutInflater().inflate(R.layout.layout_upload_image_video_view,null);
            }
            view.setTag(uploadId);

            layoutFileUploading.addView(view);
        }

        View finalView = view;
        view.findViewById(R.id.imgCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatrixService.get().mxSession.getDataHandler().getRoom(roomId)
                        .cancelEventSending(event);
                layoutFileUploading.removeView(finalView);
            }
        });

        if (mimeType.contains("image/")){
            ImageMessage imageMessage = JsonUtils.toImageMessage(event.getContent());
            String url = imageMessage.getThumbnailUrl();
            if (url == null)
                url = imageMessage.getUrl();
            int size = getResources().getDimensionPixelSize(R.dimen.img_msg_filw_size);
            Glide.with(this)
                    .load(MatrixService.get().getDownloadableThumbnailUrl(url,size))
                    .into(((ImageView)view.findViewById(R.id.imgPrePic)));

        }else if (mimeType.contains("video/")){
            VideoMessage videoMessage = JsonUtils.toVideoMessage(event.getContent());videoMessage.checkMediaUrls();

            String thumbUrl = videoMessage.getThumbnailUrl();
            int size = getResources().getDimensionPixelSize(R.dimen.img_msg_filw_size);
            Glide.with(this)
                    .load(MatrixService.get().getDownloadableThumbnailUrl(thumbUrl,size))
                    .into(((ImageView)view.findViewById(R.id.imgPrePic)));

        }else{
            FileMessage fileMessage = JsonUtils.toFileMessage(event.getContent());
            ((ImageView)view.findViewById(R.id.imgPrePic)).setImageResource(Message.MSGTYPE_AUDIO.equals(fileMessage.msgtype)
                    ? R.drawable.filetype_audio : R.drawable.filetype_attachment);
        }

        layoutFileUploading.setVisibility(View.VISIBLE);
    }

    private void progressUploadMedia(String uploadId,Event event,String mimeType,int progress){
        View view = null;
        for (int i = 0; i < layoutFileUploading.getChildCount(); i ++){
            View v = layoutFileUploading.getChildAt(i);
            if (v.getTag().equals(uploadId)){
                view = v;
                break;
            }
        }

        MatrixHelper.LOG("progress: "+progress);
        if (view != null){
            ((ProgressBar)view.findViewById(R.id.progressBar)).setProgress(progress);
            if (mimeType.contains("image/")){
                ImageMessage imageMessage = JsonUtils.toImageMessage(event.getContent());
                String url = imageMessage.getThumbnailUrl();
                if (url == null)
                    url = imageMessage.getUrl();
                ImageInfo imageInfo = imageMessage.info;
                Glide.with(this)
                        .load(MatrixService.get().getDownloadableThumbnailUrl(url,imageInfo.w))
                        .into(((ImageView)view.findViewById(R.id.imgPrePic)));

            }else if (mimeType.contains("video/")){
//            view.findViewById(R.id.imgPlay).setVisibility(View.VISIBLE);
//            ((ImageView)view.findViewById(R.id.imgPrePic)).setImageBitmap(bitmap);
            }

        }
    }

    private void finishUploadMedia(String uploadId){
        View view = null;
        for (int i = 0; i < layoutFileUploading.getChildCount(); i ++){
            View v = layoutFileUploading.getChildAt(i);
            if (v.getTag().equals(uploadId)){
                view = v;
                break;
            }
        }

        if (view != null){
            layoutFileUploading.removeView(view);
        }

        if (layoutFileUploading.getChildCount() == 0)
            layoutFileUploading.setVisibility(View.GONE);
    }


}
