package support.skipjack.adoi.repository;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseApplication;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.main.MainActivity;
import com.skipjack.adoi.messaging.call.CallActivity;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.call.CallSoundsManager;
import org.matrix.androidsdk.call.HeadsetConnectionReceiver;
import org.matrix.androidsdk.call.IMXCall;
import org.matrix.androidsdk.call.IMXCallListener;
import org.matrix.androidsdk.call.IMXCallsManagerListener;
import org.matrix.androidsdk.call.MXCallListener;
import org.matrix.androidsdk.call.MXCallsManagerListener;
import org.matrix.androidsdk.call.VideoLayoutConfiguration;
import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.crypto.data.MXDeviceInfo;
import org.matrix.androidsdk.crypto.data.MXUsersDevicesMap;

import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.service.EventStreamService;

public class CallRespository {
    private static final String LOG_TAG = "MITCH CallRespository";
    private static final CallRespository ourInstance = new CallRespository();

    public static CallRespository get() {
        return ourInstance;
    }

    private static final String RING_TONE_START_RINGING = "ring.ogg";
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    private IMXCall activeCall;
    private View mCallView = null;
    public CallSoundsManager callSoundsManager;
    private CallActivity callActivity;

    private String mPrevCallState;
    private boolean mIsStoppedByUser;
    public boolean isAppAlive = false;
    private VideoLayoutConfiguration mLocalVideoLayoutConfig = null;
    private CallRespository() {
        callSoundsManager = CallSoundsManager.getSharedInstance(BaseApplication.getContext());
    }

    public View getCallView() {
        return mCallView;
    }

    public void setCallView(View mCallView) {
        this.mCallView = mCallView;
    }

    public void setCallActivity(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    public void setActiveCall(IMXCall activeCall) {
        this.activeCall = activeCall;
    }
    /**
     * Save the layout conffig
     *
     * @param aLocalVideoLayoutConfig the video config
     */
    public void setVideoLayoutConfiguration(VideoLayoutConfiguration aLocalVideoLayoutConfig) {
        mLocalVideoLayoutConfig = aLocalVideoLayoutConfig;
    }

    /**
     * @return the layout config
     */
    public VideoLayoutConfiguration getVideoLayoutConfiguration() {
        return mLocalVideoLayoutConfig;
    }
    public IMXCall getActiveCall() {
        return activeCall;
    }

    public void addListener(){
        MatrixService.get().mxSession.getDataHandler().getCallsManager().addListener(callsManagerListener);
    }
    public void removeListener(){
        MatrixService.get().mxSession.getDataHandler().getCallsManager().removeListener(callsManagerListener);
    }

    private IMXCallListener callListener = new MXCallListener(){
        @Override
        public void onStateDidChange(String state) {
            super.onStateDidChange(state);
            if (null == activeCall) {
                Log.d(LOG_TAG, "## onStateDidChange() : no more active call");
                return;
            }

            Log.d(LOG_TAG, "dispatchOnStateDidChange " + activeCall.getCallId() + " : " + state);

            switch (state) {
                case IMXCall.CALL_STATE_CREATED:
                    if (activeCall.isIncoming()) {
                        if (EventStreamService.getInstance() != null) {
                            EventStreamService.getInstance().launchIncomingCallNotification(
                                    BaseApplication.getContext(),activeCall.getRoom().getRoomDisplayName(BaseApplication.getContext()));
                        }
                        startRinging();
                    }
                    break;
                case IMXCall.CALL_STATE_CREATING_CALL_VIEW:
                case IMXCall.CALL_STATE_CONNECTING:
                case IMXCall.CALL_STATE_CREATE_ANSWER:
                case IMXCall.CALL_STATE_WAIT_LOCAL_MEDIA:
                case IMXCall.CALL_STATE_WAIT_CREATE_OFFER:
                    if (activeCall.isIncoming()) {
                        callSoundsManager.stopSounds();
                    } // else ringback
                    break;

                case IMXCall.CALL_STATE_CONNECTED:
                    if (EventStreamService.getInstance() != null) {
                        EventStreamService.getInstance().launchProgressCallNotification(
                                BaseApplication.getContext(),activeCall.getRoom().getRoomDisplayName(BaseApplication.getContext()),
                                activeCall.isVideo());
                    }
//                    }
                    callSoundsManager.stopSounds();
                    callSoundsManager.requestAudioFocus();

                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != activeCall) {
                                setCallSpeakerphoneOn(activeCall.isVideo()
                                        && !HeadsetConnectionReceiver.isHeadsetPlugged(BaseApplication.getContext()));
                                callSoundsManager.setMicrophoneMute(false);
                            } else {
                                Log.e(LOG_TAG, "## onStateDidChange() : no more active call");
                            }
                        }
                    });

                    break;

                case IMXCall.CALL_STATE_RINGING:
                    if (!activeCall.isIncoming()) {
                        startRingBackSound();
                    }
                    break;

                case IMXCall.CALL_STATE_ENDED: {
                    if (((TextUtils.equals(IMXCall.CALL_STATE_RINGING, mPrevCallState) && !activeCall.isIncoming())
                            || TextUtils.equals(IMXCall.CALL_STATE_INVITE_SENT, mPrevCallState))) {
                        if (!mIsStoppedByUser) {
                            // display message only if the caller originated the hang up
                            AppUtility.toast(BaseApplication.getContext(),"User is not responding...");
                        }

                        endCall(true);
                    } else {
                        endCall(false);
                    }
//                    try {
//                        NotificationManager mNotificationManager = (NotificationManager) BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                        mNotificationManager.cancel(EventStreamService.SERVICE_CALL_ID);
//                    }catch (Exception e){}
                    break;
                }
            }

            mPrevCallState = state;
        }

        @Override
        public void onCallError(String error) {
            super.onCallError(error);
            if (null == activeCall) {
                Log.d(LOG_TAG, "## onCallError() : no more active call");
                return;
            }

            Log.d(LOG_TAG, "## onCallError(): error=" + error);
            if (IMXCall.CALL_ERROR_USER_NOT_RESPONDING.equals(error)) {
                AppUtility.toast(BaseApplication.getContext(),"The remote side failed to pick up.");
            } else if (IMXCall.CALL_ERROR_ICE_FAILED.equals(error)) {
                AppUtility.toast(BaseApplication.getContext(),"Media connection failed.");
            } else if (IMXCall.CALL_ERROR_CAMERA_INIT_FAILED.equals(error)) {
                AppUtility.toast(BaseApplication.getContext(),"Cannot initialize camera.");
            } else {
                AppUtility.toast(BaseApplication.getContext(),error);
            }

            endCall(IMXCall.CALL_ERROR_USER_NOT_RESPONDING.equals(error));
        }

        @Override
        public void onCallAnsweredElsewhere() {
            super.onCallAnsweredElsewhere();
            if (null == activeCall) {
                Log.d(LOG_TAG, "## onCallError() : no more active call");
                return;
            }

            Log.d(LOG_TAG, "onCallAnsweredElsewhere " + activeCall.getCallId());
            AppUtility.toast(BaseApplication.getContext(),"Call answered elsewhere.");
            releaseCall();
        }

        @Override
        public void onCallEnd(int aReasonId) {
            super.onCallEnd(aReasonId);
            if (null == activeCall) {
                Log.d(LOG_TAG, "## onCallEnd() : no more active call");
                return;
            }
            endCall(false);

        }
    };
    private IMXCallsManagerListener callsManagerListener = new MXCallsManagerListener(){
        @Override
        public void onIncomingCall(IMXCall call, MXUsersDevicesMap<MXDeviceInfo> unknownDevices) {
            super.onIncomingCall(call, unknownDevices);
            Log.e(LOG_TAG, "## onIncomingCall");
            //receiving a call
            Context context = BaseApplication.getContext();
            Log.d(LOG_TAG, "## onIncomingCall () :" + call.getCallId());
            int currentCallState = TelephonyManager.CALL_STATE_IDLE;

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (null != telephonyManager && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                currentCallState = telephonyManager.getCallState();
            }

            Log.d(LOG_TAG, "## onIncomingCall () : currentCallState(GSM) = " + currentCallState);

            if (currentCallState == TelephonyManager.CALL_STATE_OFFHOOK || currentCallState == TelephonyManager.CALL_STATE_RINGING) {
                Log.d(LOG_TAG, "## onIncomingCall () : rejected because GSM Call is in progress");
                call.hangup(null);
            } else if (null != activeCall) {
                Log.d(LOG_TAG, "## onIncomingCall () : rejected because " + activeCall + " is in progress");
                call.hangup(null);
            } else {
                mPrevCallState = null;
                mIsStoppedByUser = false;

                activeCall = call;
                CallActivity callActivity = CallActivity.getInstance();

                // if the home activity does not exist : the application has been woken up by a notification)
                if (null == callActivity) {
                    Log.d(LOG_TAG, "onIncomingCall : the home activity does not exist -> launch it");

                    // clear the activity stack to home activity
                    Intent intent = new Intent(context, CallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra(MainActivity.EXTRA_CALL_SESSION_ID, mActiveCall.getSession().getMyUserId());
                    intent.putExtra(Constants.KEY_CALL_ID, activeCall.getCallId());
//                    if (null != unknownDevices) {
//                        intent.putExtra(VectorHomeActivity.EXTRA_CALL_UNKNOWN_DEVICES, unknownDevices);
//                    }
                    context.startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "onIncomingCall : the home activity exists : but permissions have to be checked before");
                    // check incoming call required permissions, before allowing the call..
                    context.startActivity(callActivity.getIntent());
                }

                startRinging();
                activeCall.addListener(callListener);
            }
        }

        @Override
        public void onOutgoingCall(IMXCall call) {
            super.onOutgoingCall(call);
            Log.e(LOG_TAG, "## onOutgoingCall");
            //you make a call
            mPrevCallState = null;
            mIsStoppedByUser = false;
            activeCall = call;
            activeCall.addListener(callListener);
            startRingBackSound();

        }

        @Override
        public void onCallHangUp(IMXCall call) {
            super.onCallHangUp(call);
            Log.e(LOG_TAG, "## onCallHangUp");
            //end of call
            if (activeCall == null) {
                return;
            }
            endCall(false);
        }
    };

    /**
     * Update the speaker status
     *
     * @param isSpeakerPhoneOn true to turn on the loud speaker.
     */
    private void setCallSpeakerphoneOn(boolean isSpeakerPhoneOn) {
        if (null != activeCall) {
            callSoundsManager.setCallSpeakerphoneOn(isSpeakerPhoneOn);
        } else {
            Log.w(LOG_TAG, "## toggleSpeaker(): no active call");
        }
    }
    /**
     * Manage end of call
     */
    private void endCall(boolean isBusy) {
        if (null != activeCall) {
            final IMXCall call = activeCall;
            activeCall = null;

            if (callSoundsManager.isRinging()) {
                releaseCall(call);
            } else {
                callSoundsManager.startSound(isBusy ? R.raw.busy : R.raw.callend, false, new CallSoundsManager.OnMediaListener() {
                    @Override
                    public void onMediaReadyToPlay() {
                        if (null != callActivity) {
                            callActivity.finish();
                            callActivity = null;
                        }
                    }

                    @Override
                    public void onMediaPlay() {
                    }

                    @Override
                    public void onMediaCompleted() {
                        releaseCall(call);
                    }
                });
            }
        }
    }

    /**
     * Toggle the speaker
     */
    public void toggleSpeaker() {
        if (null != activeCall) {
            callSoundsManager.toggleSpeaker();
        } else {
            Log.w(LOG_TAG, "## toggleSpeaker(): no active call");
        }
    }
    /**
     * Reject the incoming call
     */
    public void rejectCall() {
        if (null != activeCall) {
            activeCall.hangup(null);
            releaseCall();
        }
    }
    /**
     * Release the active call.
     */
    public void releaseCall() {
        if (null != activeCall) {
            releaseCall(activeCall);
            activeCall = null;
        }
    }
    /**
     * hangup the call.
     */
    public void onHangUp(String hangUpMsg) {
        if (null != activeCall) {
            mIsStoppedByUser = true;
            activeCall.hangup(hangUpMsg);
            endCall(false);
        }
    }
    /**
     * Release a call.
     */
    private void releaseCall(IMXCall call) {
        if (null != call) {
            call.removeListener(callListener);

            callSoundsManager.stopSounds();
            callSoundsManager.releaseAudioFocus();

            if (null != callActivity) {
                callActivity.finish();
                callActivity = null;
            }
            mCallView = null;
            mLocalVideoLayoutConfig = null;

        }
    }
    /**
     * Start the ringing tone, if the phone is not in "do not disturb" mode
     */
    private void startRinging() {
//        if (NotificationUtils.INSTANCE.isDoNotDisturbModeOn(mContext)) {
//            Log.w(LOG_TAG, "Do not ring because DO NOT DISTURB MODE is on");
//            mCallSoundsManager.startRingingSilently();
//            return;
//        }

        callSoundsManager.requestAudioFocus();
        callSoundsManager.startRinging(R.raw.ring, RING_TONE_START_RINGING);

    }
    /**
     * Play the ringback sound
     */
    private void startRingBackSound() {
        callSoundsManager.startSound(R.raw.ringback, true, new CallSoundsManager.OnMediaListener() {
            @Override
            public void onMediaReadyToPlay() {
                Log.e(LOG_TAG, "## onMediaReadyToPlay");
                if (null != activeCall) {
                    callSoundsManager.releaseAudioFocus();
                    callSoundsManager.setSpeakerphoneOn(true, activeCall.isVideo()
                            && !HeadsetConnectionReceiver.isHeadsetPlugged(BaseApplication.getContext()));
                } else {
                    Log.e(LOG_TAG, "## startSound() : null activeCall");
                }
            }

            @Override
            public void onMediaPlay() {
                Log.e(LOG_TAG, "## onMediaPlay");
            }

            @Override
            public void onMediaCompleted() {
                Log.e(LOG_TAG, "## onMediaCompleted");
            }
        });
    }
}
