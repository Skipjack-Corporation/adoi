package support.skipjack.adoi.matrix;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.skipjack.adoi.R;

import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.BingRulesManager;
import org.matrix.androidsdk.core.ContentManager;
import org.matrix.androidsdk.core.EventDisplay;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.client.ProfileRestClient;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.login.Credentials;

import java.util.Collection;

import me.leolin.shortcutbadger.ShortcutBadger;
import support.skipjack.adoi.local_storage.AppSharedPreference;
import com.skipjack.adoi._repository.MatrixRepository;


public class MatrixService {
    private static final MatrixService ourInstance = new MatrixService();

    public static MatrixService get() {
        return ourInstance;
    }

    private MatrixService() {
    }

    public HomeServerConnectionConfig homeServerConfig;
    public LoginRestClient loginRestClient;
    public ProfileRestClient profileRestClient;

    private Context context;
    public Context getContext() {
        return context;
    }
    public void init(Context context) {
        this.context = context;
        homeServerConfig = new HomeServerConnectionConfig.Builder()
                .withHomeServerUri(Uri.parse(context.getString(R.string.HOST_SERVER)))
                .withIdentityServerUri(Uri.parse(context.getString(R.string.HOST_SERVER)))
                .build();

        loginRestClient = new LoginRestClient(homeServerConfig);
        profileRestClient = new ProfileRestClient(homeServerConfig);
    }

    public MXSession mxSession;
    public void startSession(){
        createSession();
        startEventStream();
    }
    public void  stopSession(){
        if (mxSession == null)
            return;

        if (mxSession.isAlive()){
            stopEventStream();
            mxSession.getDataHandler().removeListener(MatrixRepository.get());

        }
    }
    private void startEventStream(){

        mxSession.getDataHandler().getStore().open();

        final IMXStore imxStore = mxSession.getDataHandler().getStore();
        mxSession.startEventStream(imxStore.getEventStreamToken());

    }

    private void stopEventStream(){
        mxSession.stopEventStream();
    }

    private void createSession() {
        Credentials loginCredentials = AppSharedPreference.get().getLoginCredential();
        homeServerConfig.setCredentials(loginCredentials);
        Credentials credentials = homeServerConfig.getCredentials();

        MXFileStore store = new MXFileStore(homeServerConfig, false, context);
        store.setMetricsListener(MatrixRepository.get());

        MXDataHandler dataHandler = new MXDataHandler(store, credentials);
        store.setDataHandler(dataHandler);
        dataHandler.setLazyLoadingEnabled(false);

        mxSession = new MXSession.Builder(homeServerConfig, dataHandler, context)
                .withPushServerUrl(context.getString(R.string.push_server_url))
                .withMetricsListener(MatrixRepository.get())
                .withFileEncryption(false)
                .build();

        dataHandler.setMetricsListener(MatrixRepository.get());
        dataHandler.setRequestNetworkErrorListener(MatrixRepository.get());

        if (!TextUtils.isEmpty(credentials.deviceId)) {
            mxSession.enableCryptoWhenStarting();
        }

        //insert MatrixRepository
        dataHandler.addListener(MatrixRepository.get());

        mxSession.setUseDataSaveMode(true);
    }

    public void setMessagingNotificationCount(){
        BingRulesManager bingRulesManager = mxSession.getDataHandler().getBingRulesManager();
        Collection<Room> rooms = mxSession.getDataHandler().getStore().getRooms();
        int badgeCount = 0;
        for (Room room : rooms) {
            //NotifCounts for badge
            if (room.isInvited()) {
                badgeCount++;
            } else {
                int notificationCount = room.getNotificationCount();

                if (bingRulesManager.isRoomMentionOnly(room.getRoomId())) {
                    notificationCount = room.getHighlightCount();
                }

                if (notificationCount > 0) {
                    badgeCount++;
                }
            }

        }
        //update badge
        try {
            AppSharedPreference.get().setMessagingCount(badgeCount);
            ShortcutBadger.setBadge(context, badgeCount);

        } catch (Exception e) {
            MatrixHelper.LOG("## setMessagingNotificationCount(): Exception Msg=" + e.getMessage());
        }
    }
    public RoomSummary getRoomSummary(String roomId){
        return mxSession.getDataHandler().getStore(roomId).getSummary(roomId);
    }
    public String getRoomDisplayName(String roomId){
        return getRoom(roomId).getRoomDisplayName(MatrixService.get().getContext());
    }
    public RoomState getRoomState(String roomId){
        return mxSession.getDataHandler().getStore().getRoom(roomId).getState();
    }
    public String getDownloadableThumbnailUrl(String url, int size){
        return MatrixService.get().mxSession.getContentManager()
                .getDownloadableThumbnailUrl(url,size,size, ContentManager.METHOD_SCALE);
    }
    public Event getEvent(String eventId, String roomId){
        return mxSession.getDataHandler().getStore().getEvent(eventId,roomId);
    }
    public Room getRoom(String roomId){
        return mxSession.getDataHandler().getRoom(roomId);
    }
    public String getUserAvatarUrl(String userId){
        if (userId != null){
            User user = mxSession.getDataHandler().getUser(userId);
            if (user != null)
                return mxSession.getDataHandler().getUser(userId).avatar_url;
        }
        return null;
    }
    public  boolean isDirectChat(String roomId) {
        return (null != roomId)
                && mxSession.getDataHandler()
                .getDirectChatRoomIdsList().contains(roomId);
    }

    public String getRoomMessageDisplay(String roomId){
        RoomSummary roomSummary = MatrixService.get().getRoomSummary(roomId);

        if (roomSummary != null){
            if (roomSummary.getLatestReceivedEvent() != null){
                EventDisplay eventDisplay =  new EventDisplay(MatrixService.get().getContext());
                eventDisplay.setPrependMessagesWithAuthor(isDirectChat(roomId)?false:true);
                return   String.valueOf(eventDisplay.getTextualDisplay(
                        roomSummary.getLatestReceivedEvent(),
                        roomSummary.getLatestRoomState()));
            }
        }
        return "";

    }
    public String getRoomMessageDisplay(Room room){
        RoomSummary roomSummary = MatrixService.get().getRoomSummary(room.getRoomId());

        if (roomSummary != null){
            if (roomSummary.getLatestReceivedEvent() != null){
                EventDisplay eventDisplay =  new EventDisplay(MatrixService.get().getContext());
                eventDisplay.setPrependMessagesWithAuthor(isDirectChat(room.getRoomId())?false:true);
                return   String.valueOf(eventDisplay.getTextualDisplay(
                        roomSummary.getLatestReceivedEvent(),
                        roomSummary.getLatestRoomState()));
            }
        }
        return "";

    }

    public User getUser(String userId){
        return mxSession.getDataHandler().getStore().getUser(userId);
    }
}
