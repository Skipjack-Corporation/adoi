package com.skipjack.adoi.messaging.call;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.permission.Permission;
import com.skipjack.adoi.permission.PermissionManager;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.call.CallSoundsManager;
import org.matrix.androidsdk.call.IMXCall;
import org.matrix.androidsdk.call.IMXCallListener;
import org.matrix.androidsdk.call.MXCallListener;
import org.matrix.androidsdk.call.VideoLayoutConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.local_storage.AppSharedPreference;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.repository.CallRespository;
import support.skipjack.adoi.service.EventStreamService;

public class CallActivity extends BaseAppCompatActivity {
    private static final String EXTRA_LOCAL_FRAME_LAYOUT = "EXTRA_LOCAL_FRAME_LAYOUT";
    private String LOG_TAG = "MITCH";

    @BindView(R.id.layoutCallView)RelativeLayout layoutCallView;
    @BindView(R.id.layoutIncomingCall)View layoutIncomingCall;
    @BindView(R.id.layoutOutGoingCall)View layoutOutGoingCall;
    @BindView(R.id.textActionbarTitle)TextView textTitle;
    @BindView(R.id.textActionbarElapseTime)TextView textElapseTime;
    @BindView(R.id.imgCallPic)CircleImageView imgCallPic;
    @BindView(R.id.imgBtnAcceptCall) ImageButton imgBtnAcceptCall;
    @BindView(R.id.imgBtnRejectCall) ImageButton imgBtnRejectCall;
    @BindView(R.id.imgBtnCallEnd)ImageButton imgBtnCallEnd;
    @BindView(R.id.imgBtnMessages)ImageButton imgBtnMessages;
    @BindView(R.id.imgBtnSpeaker)ImageButton imgBtnSpeaker;
    @BindView(R.id.imgBtnSwitchVideo)ImageButton imgBtnSwitchVideo;
    @BindView(R.id.imgBtnVideo)ImageButton imgBtnVideo;
    @BindView(R.id.imgBtnVideoOff)ImageButton imgBtnVideoOff;
    @BindView(R.id.imgBtnMic)ImageButton imgBtnMic;

    private static CallActivity sharedInstance = null;
    private IMXCall mxCall;
    private boolean isCaller = false;
    private View mxCallView;
    private VideoLayoutConfiguration mLocalVideoLayoutConfig;
    private Handler handler;

    // true when the user moves the preview
    private boolean mIsCustomLocalVideoLayoutConfig;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_call;
    }

    @Override
    public void onCreate() {
        sharedInstance = this;
        CallRespository.get().setCallActivity(this);
        mxCall = CallRespository.get().getActiveCall();
        if (mxCall == null){
            AppUtility.toast(this,"Call event has expired or ended.");
            finish();
        }
        mxCall.addListener(mxCallListener);
        if (mxCall.isVideo() && !PermissionManager.checkPermission(this,
                Permission.PERMISSION_READ_STORAGE,
                Permission.PERMISSION_WRITE_STORAGE,
                Permission.PERMISSION_RECORD_AUDIO,
                Permission.PERMISSION_CAMERA)){
            PermissionManager.askPermissions(this,
                    Permission.PERMISSION_READ_STORAGE,
                    Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_RECORD_AUDIO,
                    Permission.PERMISSION_CAMERA);
        }else if (!mxCall.isVideo() && !PermissionManager.checkPermission(this,
                Permission.PERMISSION_READ_STORAGE,
                Permission.PERMISSION_WRITE_STORAGE,
                Permission.PERMISSION_RECORD_AUDIO)){
            PermissionManager.askPermissions(this,
                    Permission.PERMISSION_READ_STORAGE,
                    Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_RECORD_AUDIO);
        }
        handler = new Handler(Looper.getMainLooper());

        if (saveInstance != null){
            mLocalVideoLayoutConfig = (VideoLayoutConfiguration) saveInstance.getSerializable(EXTRA_LOCAL_FRAME_LAYOUT);

            // check if the layout is not out of bounds
            if (null != mLocalVideoLayoutConfig) {
                boolean isPortrait = (Configuration.ORIENTATION_LANDSCAPE != getResources().getConfiguration().orientation);

                // do not keep the custom layout if the device orientation has been updated
                if (mLocalVideoLayoutConfig.mIsPortrait != isPortrait) {
                    mLocalVideoLayoutConfig = null;
                }
            }
            mIsCustomLocalVideoLayoutConfig = (null != mLocalVideoLayoutConfig);
        }else {
            mLocalVideoLayoutConfig = new VideoLayoutConfiguration();
        }


        if (mxCall.getSession().getMyUserId().equals(AppSharedPreference.get().getLoginCredential().getUserId())){
            isCaller = true;
        }

        textTitle.setText(mxCall.getRoom().getRoomDisplayName(this));

        int size = getResources().getDimensionPixelSize(R.dimen.more_profpic_size);
        Glide.with(this)
                .load(MatrixService.get().getDownloadableThumbnailUrl(mxCall.getRoom().getAvatarUrl(),size))
                .placeholder(R.drawable.ic_placeholder_fill)
                .into(imgCallPic);

        updateSwitchCameraButton();
        updateMicButton();
        updateSpeakerButton();
        updateVideoOffButton();

        updateCallElapseTime();
        updateCallViewLayout();

        if (mxCall.isIncoming()) {
            layoutIncomingCall.setVisibility(View.VISIBLE);
            layoutOutGoingCall.setVisibility(View.GONE);

        } else {
            layoutIncomingCall.setVisibility(View.GONE);
            layoutOutGoingCall.setVisibility(View.VISIBLE);
        }
        // the webview has been saved after a screen rotation
        // getParent() != null : the static value have been reused whereas it should not
        if ((null != CallRespository.get().getCallView()) && (null == CallRespository.get().getCallView().getParent())) {
            mxCallView = CallRespository.get().getCallView();
            insertCallView();

            if (null != CallRespository.get().getVideoLayoutConfiguration()) {
                boolean isPortrait = (Configuration.ORIENTATION_LANDSCAPE != getResources().getConfiguration().orientation);

                // do not keep the custom layout if the device orientation has been updated
                if (CallRespository.get().getVideoLayoutConfiguration().mIsPortrait == isPortrait) {
                    mLocalVideoLayoutConfig = CallRespository.get().getVideoLayoutConfiguration();
                    mIsCustomLocalVideoLayoutConfig = true;
                }
            }
        } else {
            // create the callview asap
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null != mxCall.getCallView()) {
                        mxCallView = mxCall.getCallView();
                        insertCallView();
                        computeVideoUiLayout();
                        mxCall.updateLocalVideoRendererPosition(mLocalVideoLayoutConfig);

                        // if the view is ready, launch the incoming call
                        if (TextUtils.equals(mxCall.getCallState(), IMXCall.CALL_STATE_READY) && mxCall.isIncoming()) {
                            mxCall.launchIncomingCall(mLocalVideoLayoutConfig);
                        }
                    } else if (!mxCall.isIncoming() && (TextUtils.equals(IMXCall.CALL_STATE_CREATED, mxCall.getCallState()))) {
                        mxCall.createCallView();
                    }
                }
            });
        }

    }
    /**
     * Save the call view before leaving the activity.
     */
    private void saveCallView() {
        if ((null != mxCall) && !mxCall.getCallState().equals(IMXCall.CALL_STATE_ENDED) && (null != mxCallView) && (null != mxCallView.getParent())) {

            // warn the call that the activity is going to be paused.
            // as the rendering is DSA, it saves time to close the activity while removing mCallView
            mxCall.onPause();

            ViewGroup parent = (ViewGroup) mxCallView.getParent();
            parent.removeView(mxCallView);
            CallRespository.get().setCallView(mxCallView);

            CallRespository.get().setVideoLayoutConfiguration(mLocalVideoLayoutConfig);

            // remove the call layout to avoid having a black screen

            layoutCallView.setVisibility(View.GONE);
            mxCallView = null;
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable(EXTRA_LOCAL_FRAME_LAYOUT, mLocalVideoLayoutConfig);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUESTCODE_ASK_PERMISSION){
            if (mxCall.isVideo()){
                if ( !PermissionManager.checkPermission(this,
                        Permission.PERMISSION_READ_STORAGE,
                        Permission.PERMISSION_WRITE_STORAGE,
                        Permission.PERMISSION_RECORD_AUDIO,
                        Permission.PERMISSION_CAMERA)){
                    finish();
                }
            }else {
                if ( !PermissionManager.checkPermission(this,
                        Permission.PERMISSION_READ_STORAGE,
                        Permission.PERMISSION_WRITE_STORAGE,
                        Permission.PERMISSION_RECORD_AUDIO)){
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        mxCall.removeListener(mxCallListener);
        saveCallView();
        onRejectCall();
        CallRespository.get().setCallActivity(null);
        CallRespository.get().callSoundsManager.removeAudioConfigurationListener(mAudioConfigListener);

        super.onDestroy();
    }
    // track the audio config updates
    private final CallSoundsManager.OnAudioConfigurationUpdateListener mAudioConfigListener = new CallSoundsManager.OnAudioConfigurationUpdateListener() {
        @Override
        public void onAudioConfigurationUpdate() {
            updateSpeakerButton();
            updateMicButton();
        }
    };
    public static CallActivity getInstance() {
        return sharedInstance;
    }

    @OnClick(R.id.imgBtnAcceptCall)
    public void onAcceptCall(){
         mxCall.createCallView();

    }

    @OnClick(R.id.imgBtnRejectCall)
    public void onRejectCall(){
        MatrixUtility.LOG("onRejectCall");
        CallRespository.get().rejectCall();
    }

    @OnClick(R.id.imgBtnCallEnd)
    public void onCallEnd(){
        CallRespository.get().onHangUp(null);
    }

    @OnClick(R.id.imgBtnSwitchVideo)
    public void onSwitchCamera(){
        boolean wasCameraSwitched = false;
        if ((null != mxCall) && mxCall.isVideo()) {
            wasCameraSwitched = mxCall.switchRearFrontCamera();
        }

        updateSwitchCameraButton();
    }
    @OnClick(R.id.imgBtnMic)
    public void onMicMute(){
        CallSoundsManager callSoundsManager = CallRespository.get().callSoundsManager;
        callSoundsManager.setMicrophoneMute(!callSoundsManager.isMicrophoneMute());
        updateMicButton();
    }
    @OnClick(R.id.imgBtnSpeaker)
    public void onSpeaker(){
        CallRespository.get().toggleSpeaker();
        updateSpeakerButton();
    }

    @OnClick(R.id.imgBtnVideoOff)
    public void onVideoOff(){
        if (null != mxCall) {
            if (mxCall.isVideo()) {
                boolean isMuted = mxCall.isVideoRecordingMuted();
                mxCall.muteVideoRecording(!isMuted);
                Log.w(LOG_TAG, "## toggleVideoMute(): camera record turned to " + !isMuted);
            }
        } else {
            Log.w(LOG_TAG, "## toggleVideoMute(): Failed");
        }
        updateVideoOffButton();
    }

    @OnClick(R.id.imgBtnVideo)
    public void onCallVideo(){
        if (null != mxCall) {
            if (!mxCall.isVideo()) {
            }
        }
    }
    private final IMXCallListener mxCallListener = new MXCallListener() {
            @Override
            public void onStateDidChange(String callState) {
                // other management
                updateCallElapseTime();
               updateCallViewLayout();
//                Log.d(LOG_TAG, "## manageSubViews(): OUT");
                if ((null != mxCall) && mxCall.isVideo() && mxCall.getCallState().equals(IMXCall.CALL_STATE_CONNECTED)) {
                    mxCall.updateLocalVideoRendererPosition(mLocalVideoLayoutConfig);
                }
            }

            @Override
            public void onCallViewCreated(View callView) {
                mxCallView = callView;
                insertCallView();
            }

            @Override
            public void onReady() {
                // update UI before displaying the video
                computeVideoUiLayout();
                if (!mxCall.isIncoming()) {
                    Log.d(LOG_TAG, "## onReady(): placeCall()");
                    mxCall.placeCall(mLocalVideoLayoutConfig);
                } else {
                    Log.d(LOG_TAG, "## onReady(): launchIncomingCall()");
                    mxCall.launchIncomingCall(mLocalVideoLayoutConfig);
                }
            }

            @Override
            public void onPreviewSizeChanged(int width, int height) {
                mSourceVideoWidth = width;
                mSourceVideoHeight = height;

                if ((null != mxCall) && mxCall.isVideo() && mxCall.getCallState().equals(IMXCall.CALL_STATE_CONNECTED)) {
                    computeVideoUiLayout();
                    mxCall.updateLocalVideoRendererPosition(mLocalVideoLayoutConfig);
                }
            }
    };
    /**
     * Insert the callView in the activity (above the other room member).
     */
    private void insertCallView() {
        if (null != mxCallView) {
            // insert the call view above the avatar

            RelativeLayout.LayoutParams params
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            layoutCallView.removeView(mxCallView);
            layoutCallView.setVisibility(View.VISIBLE);

            // add the call view only is the call is a video one
            if (mxCall.isVideo()) {
                // reported by a rageshake
                if (null != mxCallView.getParent()) {
                    ((ViewGroup) mxCallView.getParent()).removeView(mxCallView);
                }
                layoutCallView.addView(mxCallView, 1, params);
            }
            // init as GONE, will be displayed according to call states..
            mxCall.setVisibility(View.GONE);
        }
    }
    //
    private static SimpleDateFormat mHourMinSecFormat = null;
    private static SimpleDateFormat mMinSecFormat = null;

    /**
     * Format a time in seconds to a HH:MM:SS string.
     *
     * @param seconds the time in seconds
     * @return the formatted time
     */
    private static String formatSecondsToHMS(long seconds) {
        if (null == mHourMinSecFormat) {
            mHourMinSecFormat = new SimpleDateFormat("HH:mm:ss");
            mHourMinSecFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mMinSecFormat = new SimpleDateFormat("mm:ss");
            mMinSecFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        if (seconds < 3600) {
            return mMinSecFormat.format(new Date(seconds * 1000));
        } else {
            return mHourMinSecFormat.format(new Date(seconds * 1000));
        }
    }

    /**
     * Refresh the call status
     */
    private void updateCallElapseTime(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String callState = mxCall.getCallState();
                switch (callState) {
                    case IMXCall.CALL_STATE_CREATED:
                    case IMXCall.CALL_STATE_CREATING_CALL_VIEW:
                    case IMXCall.CALL_STATE_READY:
                    case IMXCall.CALL_STATE_WAIT_LOCAL_MEDIA:
                        if (mxCall.isIncoming()) {
                            if (mxCall.isVideo()) {
                                textElapseTime.setText("Incoming Video Call");
                            } else {
                                textElapseTime.setText("Incoming Voice Call");
                            }
                        }
                    case IMXCall.CALL_STATE_INVITE_SENT:
                    case IMXCall.CALL_STATE_CONNECTING:
                    case IMXCall.CALL_STATE_CREATE_ANSWER:
                    case IMXCall.CALL_STATE_WAIT_CREATE_OFFER: {
                        textElapseTime.setText("Call Connecting...");
                    }break;
                    case IMXCall.CALL_STATE_RINGING:
                        if (mxCall.isIncoming()) {
                            if (mxCall.isVideo()) {
                                textElapseTime.setText("Incoming Video Call");
                            } else {
                                textElapseTime.setText("Incoming Voice Call");
                            }
                        } else {
                            textElapseTime.setText("Calling...");
                        }
                        break;
                    case IMXCall.CALL_STATE_CONNECTED:
                        long elapsedTime = mxCall.getCallElapsedTime();

                        if (elapsedTime < 0) {
                            textElapseTime.setText("Call Connected");
                        } else {
                            textElapseTime.setText(formatSecondsToHMS(elapsedTime));
                        }
                        updateCallElapseTime();
                        break;
                    case IMXCall.CALL_STATE_ENDED:
                        textElapseTime.setText("Call ended");
                        break;
                }
            }
        }, 1000);
    }
    private void updateCallViewLayout(){
        MatrixUtility.LOG("MITCH_TEST","State: "+mxCall.getCallState());
        String callState = mxCall.getCallState();
        switch (callState) {
            case IMXCall.CALL_STATE_RINGING:{
                if (mxCall.isIncoming()) {
                    mxCall.answer();
                    layoutIncomingCall.setVisibility(View.VISIBLE);
                    layoutOutGoingCall.setVisibility(View.GONE);

                } else {
                    layoutIncomingCall.setVisibility(View.GONE);
                    layoutOutGoingCall.setVisibility(View.VISIBLE);
                }
            }break;
            case IMXCall.CALL_STATE_CONNECTED:{

                layoutIncomingCall.setVisibility(View.GONE);
                layoutOutGoingCall.setVisibility(View.VISIBLE);
            }break;
        }

    }

    /**
     * Update the switch camera icon.
     * Note that, this icon is only active if the device supports
     * camera switching (See {@link IMXCall#isSwitchCameraSupported()})
     */
    private void updateSwitchCameraButton(){
        if ((null != mxCall) && mxCall.isVideo() && mxCall.isSwitchCameraSupported()) {
            imgBtnSwitchVideo.setVisibility(View.VISIBLE);

            boolean isSwitched = mxCall.isCameraSwitched();
            Log.d(LOG_TAG, "## refreshSwitchRearFrontCameraButton(): isSwitched=" + isSwitched);

            // update icon
            if (isSwitched){
                imgBtnSwitchVideo.setImageResource(R.drawable.ic_media_switch_video_aacent);
            }else{
                imgBtnSwitchVideo.setImageResource(R.drawable.ic_media_switch_video);
            }

        } else {
            Log.d(LOG_TAG, "## refreshSwitchRearFrontCameraButton(): View.INVISIBLE");
            imgBtnSwitchVideo.setVisibility(View.GONE);
        }
    }
    private void updateSpeakerButton(){
        if (CallRespository.get().callSoundsManager.isSpeakerphoneOn()) {
            imgBtnSpeaker.setImageResource(R.drawable.ic_media_speaker_accent);
        } else {
            imgBtnSpeaker.setImageResource(R.drawable.ic_media_speaker);
        }
    }
    private void updateVideoOffButton(){
        if ((null != mxCall) && mxCall.isVideo()) {
            imgBtnVideoOff.setVisibility(View.VISIBLE);

            boolean isMuted = mxCall.isVideoRecordingMuted();
            Log.d(LOG_TAG, "## refreshMuteVideoButton(): isMuted=" + isMuted);

            // update icon
            // update icon
            if (isMuted) {
                imgBtnVideoOff.setImageResource(R.drawable.ic_media_video_off_aacent);
            } else {
                imgBtnVideoOff.setImageResource(R.drawable.ic_media_video_off);
            }
        }else {
            imgBtnVideoOff.setVisibility(View.GONE);
        }
    }
    private void updateMicButton(){
        if (CallRespository.get().callSoundsManager.isMicrophoneMute()){
            imgBtnMic.setImageResource(R.drawable.ic_media_mic_off_aacent);
        }else{
            imgBtnMic.setImageResource(R.drawable.ic_media_mic_off);
        }
    }
    //==============================================================================================================
    // UI items refresh
    //==============================================================================================================

    /**
     * Compute the top margin of the view that contains the video
     * of the local attendee of the call (the small video, where
     * the user sees himself).<br>
     * Ratios are taken from the UI specifications. The vertical space
     * between the video view and the container (call_menu_buttons_layout_container)
     * containing the buttons of the video menu, is specified as 4.3% of
     * the height screen.
     */
    private int mSourceVideoWidth = 0;
    private int mSourceVideoHeight = 0;
    private static final int PERCENT_LOCAL_USER_VIDEO_SIZE = 25;
    private static final float VIDEO_TO_BUTTONS_VERTICAL_SPACE = (float) (18.0 / 585.0);
    private void computeVideoUiLayout() {
        if (null == mLocalVideoLayoutConfig) {
            mLocalVideoLayoutConfig = new VideoLayoutConfiguration();
        }

        // get the height of the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;

        // compute action bar size: the video Y component starts below the action bar
        int actionBarHeight;
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
            screenHeight -= actionBarHeight;
        }

        ViewGroup.LayoutParams layout = imgBtnCallEnd.getLayoutParams();

        if (0 == mLocalVideoLayoutConfig.mWidth) {
            mLocalVideoLayoutConfig.mWidth = PERCENT_LOCAL_USER_VIDEO_SIZE;
        }

        if (0 == mLocalVideoLayoutConfig.mHeight) {
            mLocalVideoLayoutConfig.mHeight = PERCENT_LOCAL_USER_VIDEO_SIZE;
        }

        if ((0 != mSourceVideoWidth) && (0 != mSourceVideoHeight)) {
            int previewWidth = screenWidth * mLocalVideoLayoutConfig.mWidth / 100;
            int previewHeight = screenHeight * mLocalVideoLayoutConfig.mHeight / 100;

            int sourceRatio = mSourceVideoWidth * 100 / mSourceVideoHeight;
            int previewRatio = previewWidth * 100 / previewHeight;

            // there is an aspect ratio update
            if (sourceRatio != previewRatio) {
                int maxPreviewWidth = screenWidth * PERCENT_LOCAL_USER_VIDEO_SIZE / 100;
                int maxPreviewHeight = screenHeight * PERCENT_LOCAL_USER_VIDEO_SIZE / 100;

                if ((maxPreviewHeight * sourceRatio / 100) > maxPreviewWidth) {
                    mLocalVideoLayoutConfig.mHeight = maxPreviewWidth * 100 * 100 / sourceRatio / screenHeight;
                    mLocalVideoLayoutConfig.mWidth = PERCENT_LOCAL_USER_VIDEO_SIZE;
                } else {
                    mLocalVideoLayoutConfig.mWidth = maxPreviewHeight * sourceRatio / screenWidth;
                    mLocalVideoLayoutConfig.mHeight = PERCENT_LOCAL_USER_VIDEO_SIZE;
                }
            }
        } else {
            mLocalVideoLayoutConfig.mWidth = PERCENT_LOCAL_USER_VIDEO_SIZE;
            mLocalVideoLayoutConfig.mHeight = PERCENT_LOCAL_USER_VIDEO_SIZE;
        }

        if (!mIsCustomLocalVideoLayoutConfig) {
            int buttonsContainerHeight = (layoutIncomingCall.getVisibility() == View.VISIBLE) ? layout.height * 100 / screenHeight : 0;
            int bottomLeftMargin = (int) (VIDEO_TO_BUTTONS_VERTICAL_SPACE * screenHeight * 100 / screenHeight);

            mLocalVideoLayoutConfig.mX = bottomLeftMargin * screenHeight / screenWidth;
            mLocalVideoLayoutConfig.mY = 100 - bottomLeftMargin - buttonsContainerHeight - mLocalVideoLayoutConfig.mHeight;
        }

        mLocalVideoLayoutConfig.mIsPortrait = (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE);
        mLocalVideoLayoutConfig.mDisplayWidth = screenWidth;
        mLocalVideoLayoutConfig.mDisplayHeight = screenHeight;

        Log.d(LOG_TAG, "## computeVideoUiLayout() : x " + mLocalVideoLayoutConfig.mX + " y " + mLocalVideoLayoutConfig.mY);
        Log.d(LOG_TAG, "## computeVideoUiLayout() : mWidth " + mLocalVideoLayoutConfig.mWidth + " mHeight " + mLocalVideoLayoutConfig.mHeight);
    }

}
