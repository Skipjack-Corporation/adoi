package support.skipjack.adoi.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.skipjack.adoi.R;
import com.skipjack.adoi.main.MainActivity;
import com.skipjack.adoi.messaging.call.CallActivity;

import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;

import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.repository.CallRespository;

public class EventStreamService extends Service {
    private static final String CHANNEL_ID = "ForegroundServiceChannelAdoi";
    public static final String CALL_NOTIFICATION_CHANNEL_ID = "CallNotifChannelAdoi";
    private static final int SERVICE_BACKGROUND_ID = 61;
    public static final int SERVICE_CALL_ID = 62;
    private Handler handler = new Handler();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * static instance
     */
    @Nullable
    private static EventStreamService mActiveEventStreamService = null;
    /**
     * @return the event stream instance
     */
    @Nullable
    public static EventStreamService getInstance() {
        return mActiveEventStreamService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MatrixUtility.LOG("EventStreamService onStartCommand");
        MatrixService.get().startSession();
        createNotificationChannel();
        startForegroundNotification();
        // release previous instance
        if (null == mActiveEventStreamService) {
            mActiveEventStreamService = this;
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MatrixUtility.LOG("EventStreamService onDestroy");
        CallRespository.get().removeListener();
        MatrixService.get().stopSession();
        if (mActiveEventStreamService != null){
            mActiveEventStreamService.stopSelf();
            mActiveEventStreamService = null;
        }

        Intent broadcastIntent = new Intent(this, RestartServiceBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        MatrixUtility.LOG("EventStreamService onTaskRemoved");
        MatrixService.get().stopSession();
        Intent broadcastIntent = new Intent(this, RestartServiceBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

    }



    public void startForegroundNotification(){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

       Notification notification = launchNotification("Listening for events", pendingIntent);
       startForeground(SERVICE_BACKGROUND_ID, notification);
    }


    private Notification launchNotification(String message, PendingIntent pendingIntent){
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText("Listening for events")
                .setSmallIcon(R.drawable.ic_logo_small)
                .setContentIntent(pendingIntent)
                .setVibrate(null)
                .build();

        // hide the notification from the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.priority = NotificationCompat.PRIORITY_HIGH;
        }
        return notification;
    }
    public void launchIncomingCallNotification(Context context,String title){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CALL_NOTIFICATION_CHANNEL_ID,
                    "Call Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        Intent notificationIntent = new Intent(context, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context, CALL_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title.isEmpty()?context.getString(R.string.app_name):title)
                .setSmallIcon(R.drawable.ic_media_call)
                .setContentText("Call request.")
                .setContentIntent(pendingIntent)
                .setLights(Color.GREEN, 500, 500)
                .setVibrate(null)
                .build();
        startForeground(SERVICE_CALL_ID, notification);

    }
    public void launchProgressCallNotification(Context context,String title, boolean isVideo){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CALL_NOTIFICATION_CHANNEL_ID,
                    "Call Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        Intent notificationIntent = new Intent(context, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(context, CALL_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title.isEmpty()?context.getString(R.string.app_name):title)
                .setSmallIcon(R.drawable.ic_media_call)
                .setContentText(isVideo?"Video call in progress...":"Call in progress...")
                .setContentIntent(pendingIntent)
                .setLights(Color.GREEN, 500, 500)
                .setVibrate(null)
                .build();
        startForeground(SERVICE_CALL_ID, notification);

    }
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setSound(null,null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void launchSimpleNotification(String title, String message){
            //Get an instance of NotificationManager//
        Intent notificationIntent = new Intent(getApplicationContext(), CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent, 0);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_logo_small)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setContentIntent(pendingIntent);


            // Gets an instance of the NotificationManager service//

            NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(101, mBuilder.build());

    }
}
