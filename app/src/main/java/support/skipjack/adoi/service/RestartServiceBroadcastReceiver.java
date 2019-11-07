package support.skipjack.adoi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import org.matrix.androidsdk.core.Log;

import support.skipjack.adoi.local_storage.AppSharedPreference;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    public static final String RESTART_SERVICE = "support.skipjack.adoi.restartservice";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ADOI_LOG","Service stops.. restart again...");
        if (AppSharedPreference.get().hasLoginCredentials()){
            Intent serviceIntent =  new Intent(context, EventStreamService.class);
//            context.startService(serviceIntent);
            ContextCompat.startForegroundService(context, serviceIntent);

        }
    }
}
