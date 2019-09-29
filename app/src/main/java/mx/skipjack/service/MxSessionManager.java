package mx.skipjack.service;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.skipjack.adoi.R;
import com.skipjack.adoi.database.AppSharedPreference;

import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomTag;
import org.matrix.androidsdk.data.metrics.MetricsListener;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.login.Credentials;
import org.matrix.androidsdk.ssl.UnrecognizedCertificateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class MxSessionManager extends MXEventListener implements MetricsListener {


    private Callback callback;

    public MxSessionManager(@NonNull Callback callback) {
        this.callback = callback;
    }
    public void  startSession(){
        createSession();
        startEventStream();
    }
    public void  stopSession(){
        if (MatrixService.get().mxSession.isAlive()){
            stopEventStream();
        }
    }
    private void startEventStream(){
        MatrixService.get().mxSession.getDataHandler().getStore().open();

        final IMXStore imxStore = MatrixService.get().mxSession.getDataHandler().getStore();
        MatrixService.get().mxSession.startEventStream(imxStore.getEventStreamToken());
    }

    private void stopEventStream(){
        MatrixService.get().mxSession.stopEventStream();
    }

    private void createSession() {
        Credentials loginCredentials = AppSharedPreference.get().getLoginCredential();
        MatrixService.get().homeServerConfig.setCredentials(loginCredentials);
        MXFileStore store;
        final Credentials credentials = MatrixService.get().homeServerConfig.getCredentials();

        /*if (true) {*/
        store = new MXFileStore(MatrixService.get().homeServerConfig, false,MatrixService.get().getContext());
        store.setMetricsListener(this);

        /*} else {
            store = new MXMemoryStore(hsConfig.getCredentials(), context);
        }*/

        final MXDataHandler dataHandler = new MXDataHandler(store, credentials);
        store.setDataHandler(dataHandler);
        dataHandler.setLazyLoadingEnabled(true);

        MatrixService.get().mxSession = new MXSession.Builder(MatrixService.get().homeServerConfig, dataHandler, MatrixService.get().getContext())
                .withPushServerUrl(MatrixService.get().getContext().getString(R.string.push_server_url))
                .withMetricsListener(this)
                .withFileEncryption(false)
                .build();

        dataHandler.setMetricsListener(this);
        dataHandler.setRequestNetworkErrorListener(new MXDataHandler.RequestNetworkErrorListener() {

            @Override
            public void onConfigurationError(String matrixErrorCode) {
                MatrixUtility.LOG("## createSession() : onConfigurationError " + matrixErrorCode);

            }

            @Override
            public void onSSLCertificateError(UnrecognizedCertificateException unrecCertEx) {
                MatrixUtility.LOG("## createSession() : onSSLCertificateError " + unrecCertEx);

            }
        });

        if (!TextUtils.isEmpty(credentials.deviceId)) {
            MatrixService.get().mxSession.enableCryptoWhenStarting();
        }

        dataHandler.addListener(this);

        MatrixService.get().mxSession.setUseDataSaveMode(true);

    }
    private  boolean isDirectChat(String roomId) {
        return (null != roomId)
                && MatrixService.get().mxSession.getDataHandler()
                .getDirectChatRoomIdsList().contains(roomId);
    }

    /**
     * MetricsListener methods
     * */
    @Override
    public void onPresenceUpdate(Event event, User user) {
        super.onPresenceUpdate(event, user);
        MatrixUtility.LOG("## createSession() : mxEventListener  onPresenceUpdate");


    }

    @Override
    public void onLiveEvent(Event event, RoomState roomState) {
        super.onLiveEvent(event, roomState);
        MatrixUtility.LOG("## createSession() : mxEventListener  onLiveEvent");
    }

    @Override
    public void onIgnoredUsersListUpdate() {
        // the application cache will be cleared at next launch if the application is not yet launched
        MatrixUtility.LOG("## createSession() : mxEventListener  onIgnoredUsersListUpdate");

    }


    @Override
    public void onInitialSyncComplete(String toToken) {
        MatrixUtility.LOG("## createSession() : MXEventListener onInitialSyncComplete" + toToken);
    }

    @Override
    public void onLiveEventsChunkProcessed(String fromToken, String toToken) {
        // when the client does not use FCM (ie. FDroid),
        // we need to compute the application badge values
        MatrixUtility.LOG("## createSession() : mxEventListener  onLiveEventsChunkProcessed");

        callback.onStartingNow();
    }


    /**
     * MetricsListener methods
     * */
    @Override
    public void onInitialSyncFinished(long duration) {
        MatrixUtility.LOG("## createSession() : MetricsListener  onInitialSyncFinished :"+duration);
    }

    @Override
    public void onIncrementalSyncFinished(long duration) {
        MatrixUtility.LOG("## createSession() : MetricsListener  onIncrementalSyncFinished :"+duration );
    }

    @Override
    public void onStorePreloaded(long duration) {
        MatrixUtility.LOG("## createSession() : MetricsListener  onStorePreloaded :"+duration);

    }

    @Override
    public void onRoomsLoaded(int nbOfRooms) {
        MatrixUtility.LOG("## createSession() : MetricsListener  onRoomsLoaded :"+nbOfRooms);

    }

    public interface Callback{
        void onStartingNow();
    }
}
