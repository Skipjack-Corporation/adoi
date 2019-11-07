package support.skipjack.adoi.repository;

import android.app.NotificationManager;
import android.content.Context;

import com.google.gson.Gson;
import com.skipjack.adoi.base.BaseApplication;

import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;

import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.service.EventStreamService;

public class MatrixRepository extends MXEventListener {
    private static final MatrixRepository ourInstance = new MatrixRepository();

    public static MatrixRepository get() {
        return ourInstance;
    }

    private MatrixRepository() {
    }

    @Override
    public void onLiveEventsChunkProcessed(String fromToken, String toToken) {
//        MatrixUtility.LOG(getClass().getSimpleName()+" onLiveEventsChunkProcessed");
        MatrixService.get().updateBadgeCount();
        CallRespository.get().addListener();
    }

    @Override
    public void onLiveEvent(Event event, RoomState roomState) {
        MatrixService.get().updateBadgeCount();

        if (event.getType().equals(Event.EVENT_TYPE_CALL_INVITE)){
            if (EventStreamService.getInstance() != null) {
                EventStreamService.getInstance().launchIncomingCallNotification(
                        BaseApplication.getContext(),MatrixService.get().getRoom(event.roomId)
                                .getRoomDisplayName(BaseApplication.getContext()));
            }
        } else if (event.getType().equals(Event.EVENT_TYPE_CALL_HANGUP)){
            try {
                NotificationManager mNotificationManager = (NotificationManager) BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(EventStreamService.SERVICE_CALL_ID);
                mNotificationManager.deleteNotificationChannel(EventStreamService.CALL_NOTIFICATION_CHANNEL_ID);
            }catch (Exception e){}
            EventStreamService.getInstance().createNotificationChannel();
            EventStreamService.getInstance().startForegroundNotification();
        }
    }

    @Override
    public void onRoomInternalUpdate(String roomId) {
//        MatrixUtility.LOG(getClass().getSimpleName()+" onRoomInternalUpdate :" +roomId);
    }
}
