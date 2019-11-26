package support.skipjack.adoi.matrix.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseApplication;
import com.skipjack.adoi.main.MainActivity;
import com.skipjack.adoi.messaging.call.CallActivity;
import com.skipjack.adoi.messaging.event.EventActivity;

import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixService;
import com.skipjack.adoi._repository.CallRespository;

public class MatrixEventService extends Service {
    private static final String LIVE_CHANNEL_ID = "LiveChannel";
    private static final String MESSAGING_CHANNEL_ID = "MessagingChannel";
    public static final String CALLS_CHANNEL_ID = "CallsChannel";
    private static final int LIVE_NOTIFICATION_ID = 61;
    public static final int CALL_NOTIFICATION_ID = 62;
    public static final int MESSAGING_NOTIFICATION_ID = 63;
    public boolean launchLiveListener;

    public static final String INTENT_EXTRA_SHOW_MESSAGING_TAB = "showMessagingTab";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * static instance
     */
    @Nullable
    public static MatrixEventService instance = null;
    /**
     * @return the event stream instance
     */
    @Nullable
    public static MatrixEventService getInstance() {
        return instance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // initialize instance
        if (null == instance) {
            instance = this;
            launchLiveListener = false;
        }

        //initialize notification channels
        createNotificationChannel(LIVE_CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_HIGH);
        createNotificationChannel(MESSAGING_CHANNEL_ID, "Messages Channel", NotificationManager.IMPORTANCE_DEFAULT);
        createNotificationChannel(CALLS_CHANNEL_ID, "Calls Channel", NotificationManager.IMPORTANCE_DEFAULT);

        //start matrix session
        MatrixService.get().startSession();
        startLiveForegroundService();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MatrixHelper.LOG("MatrixEventService onDestroy");
        CallRespository.get().removeListener();
        MatrixService.get().stopSession();
        if (instance != null){
            instance.stopSelf();
            instance = null;
        }

        Intent broadcastIntent = new Intent(this, RestartServiceBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        MatrixHelper.LOG("MatrixEventService onTaskRemoved");
        MatrixService.get().stopSession();
        Intent broadcastIntent = new Intent(this, RestartServiceBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

    }

    public void createNotificationChannel(String channelId, String name, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    channelId,
                    name,
                    importance);
            serviceChannel.setSound(null,null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public Notification createNotification(String channelId, String title, String message, PendingIntent pendingIntent, boolean isAutoCancel){
        return createNotification(channelId,title,message,R.drawable.ic_logo_small,pendingIntent,isAutoCancel);
    }
    public Notification createNotification(String channelId, String title, String message, int iconResId, PendingIntent pendingIntent, boolean isAutoCancel){
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(iconResId)
                .setContentIntent(pendingIntent)
                .setVibrate(null)
                .setAutoCancel(isAutoCancel)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        return notification;
    }
    public void cancelNotification(int notifId){
        try {
            NotificationManager mNotificationManager = (NotificationManager) BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notifId);
        }catch (Exception e){}
    }

    public void startLiveForegroundService(){
        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(INTENT_EXTRA_SHOW_MESSAGING_TAB,true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, 0);

        Notification notification = createNotification(LIVE_CHANNEL_ID,
                getApplicationContext().getString(R.string.app_name),
                "Listening for events", pendingIntent, false);
        startForeground(LIVE_NOTIFICATION_ID, notification);
    }

    public void launchIncomingCallNotification(String title){
        Intent notificationIntent = new Intent(this, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = createNotification(CALLS_CHANNEL_ID,
                title.isEmpty()?this.getString(R.string.app_name):title, "Call request.",
                R.drawable.ic_media_call,pendingIntent,true);

        startForeground(CALL_NOTIFICATION_ID, notification);

    }
    public void launchProgressCallNotification(Context context,String title, boolean isVideo){

        Intent notificationIntent = new Intent(context, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);

        Notification notification = createNotification(CALLS_CHANNEL_ID,
                title.isEmpty()?this.getString(R.string.app_name):title,
                isVideo?"Video call in progress...":"Call in progress...",
                R.drawable.ic_media_call,pendingIntent,true);

        startForeground(CALL_NOTIFICATION_ID, notification);

    }


    public void launchMessageNotification(String title, String message, String roomId){
        Intent intent = new Intent(getApplicationContext(), EventActivity.class);
        intent.putExtra("KEY_ROOM_ID",roomId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, 0);

        Notification notification = createNotification(MESSAGING_CHANNEL_ID,
                title.isEmpty()?this.getString(R.string.app_name):title,
                message,
                R.drawable.selector_main_messages,pendingIntent,true);

        NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGING_NOTIFICATION_ID, notification);

    }


}
