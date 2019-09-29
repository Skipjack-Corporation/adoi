package mx.skipjack.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.skipjack.adoi.R;
import com.skipjack.adoi.main.MainActivity;

public class EventStreamService extends Service implements MxSessionManager.Callback {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    MxSessionManager mxSessionManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mxSessionManager = new MxSessionManager(this);
        mxSessionManager.startSession();
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText("Listening for events")
                .setSmallIcon(R.drawable.ic_logo_small)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(61, notification);
        return START_STICKY;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if (mxSessionManager != null) mxSessionManager.stopSession();

    }

    @Override
    public void onStartingNow() {

    }
}
