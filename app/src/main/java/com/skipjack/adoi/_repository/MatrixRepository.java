package com.skipjack.adoi._repository;

import com.skipjack.adoi.base.BaseApplication;
import com.skipjack.adoi.main.MainActivity;

import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.metrics.MetricsListener;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.ssl.Fingerprint;
import org.matrix.androidsdk.ssl.UnrecognizedCertificateException;

import support.skipjack.adoi.local_storage.AppSharedPreference;
import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.ssl_errorhandler.UnrecognizedCertHandler;
import support.skipjack.adoi.matrix.service.MatrixEventService;

public class MatrixRepository extends MXEventListener implements MetricsListener, MXDataHandler.RequestNetworkErrorListener {
    private static final MatrixRepository ourInstance = new MatrixRepository();

    public static MatrixRepository get() {
        return ourInstance;
    }

    private MatrixRepository() {
    }

    @Override
    public void onLiveEventsChunkProcessed(String fromToken, String toToken) {
//        if (!MatrixEventService.getInstance().launchLiveListener){
//            MatrixEventService.getInstance().launchLiveListener = true;
//            MatrixEventService.getInstance().startLiveForegroundService();
//
//        }
        MatrixService.get().setMessagingNotificationCount();
        CallRespository.get().addListener();
    }

    @Override
    public void onLiveEvent(Event event, RoomState roomState) {
        MatrixService.get().setMessagingNotificationCount();
        if (BaseApplication.getCurrentActivity() instanceof MainActivity){
            ((MainActivity) BaseApplication.getCurrentActivity()).updateMessageCount();
        }
        if (event.getType().equals(Event.EVENT_TYPE_CALL_INVITE)){
            if (MatrixEventService.getInstance() != null) {
                MatrixEventService.getInstance().launchIncomingCallNotification(
                        MatrixService.get().getRoom(event.roomId).getRoomDisplayName(BaseApplication.getContext()));
            }
        } else if (event.getType().equals(Event.EVENT_TYPE_CALL_HANGUP)){
            MatrixEventService.getInstance().cancelNotification(MatrixEventService.CALL_NOTIFICATION_ID);
        }else if (event.getType().equals(Event.EVENT_TYPE_MESSAGE)){
            String title = MatrixService.get().getRoomDisplayName(event.roomId);
            String message = MatrixService.get().getRoomMessageDisplay(event.roomId);
            MatrixEventService.getInstance().launchMessageNotification(title,message,event.roomId);
        }
    }

    @Override
    public void onRoomInternalUpdate(String roomId) {
//        MatrixHelper.LOG(getClass().getSimpleName()+" onRoomInternalUpdate :" +roomId);
    }


    /**
     * MetricsListener
     * */
    @Override
    public void onInitialSyncFinished(long duration) {

    }

    @Override
    public void onIncrementalSyncFinished(long duration) {

    }

    @Override
    public void onStorePreloaded(long duration) {

    }

    @Override
    public void onRoomsLoaded(int nbOfRooms) {

    }


    /**
     * MXDataHandler.RequestNetworkErrorListener
     * */
    @Override
    public void onConfigurationError(String matrixErrorCode) {

    }

    @Override
    public void onSSLCertificateError(UnrecognizedCertificateException unrecCertEx) {
//        MatrixHelper.LOG("## createSession() : onSSLCertificateError " + unrecCertEx);
//        UnrecognizedCertificateException unrecCertEx = CertUtil.getCertificateException(e);
        if (unrecCertEx != null) {
            final Fingerprint fingerprint = unrecCertEx.getFingerprint();

            UnrecognizedCertHandler.show(MatrixService.get().homeServerConfig, fingerprint, false, new UnrecognizedCertHandler.Callback() {
                @Override
                public void onAccept() {

                }

                @Override
                public void onIgnore() {
                    MatrixHelper.LOG("onIgnore");
                }

                @Override
                public void onReject() {
                    MatrixHelper.LOG("onReject");
                }
            });

        }
    }
}
