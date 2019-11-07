package support.skipjack.adoi.matrix;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

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
import org.matrix.androidsdk.data.metrics.MetricsListener;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.client.ProfileRestClient;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.login.Credentials;
import org.matrix.androidsdk.ssl.Fingerprint;
import org.matrix.androidsdk.ssl.UnrecognizedCertificateException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import me.leolin.shortcutbadger.ShortcutBadger;
import support.skipjack.adoi.matrix.ssl_errorhandler.UnrecognizedCertHandler;
import support.skipjack.adoi.local_storage.AppSharedPreference;
import support.skipjack.adoi.repository.MatrixRepository;


public class MatrixService implements MetricsListener, MXDataHandler.RequestNetworkErrorListener {
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
        store.setMetricsListener(this);

        MXDataHandler dataHandler = new MXDataHandler(store, credentials);
        store.setDataHandler(dataHandler);
        dataHandler.setLazyLoadingEnabled(false);

        mxSession = new MXSession.Builder(homeServerConfig, dataHandler, context)
                .withPushServerUrl(context.getString(R.string.push_server_url))
                .withMetricsListener(this)
                .withFileEncryption(false)
                .build();

        dataHandler.setMetricsListener(this);
        dataHandler.setRequestNetworkErrorListener(this);

        if (!TextUtils.isEmpty(credentials.deviceId)) {
            mxSession.enableCryptoWhenStarting();
        }

        //insert MatrixRepository
        dataHandler.addListener(MatrixRepository.get());

        mxSession.setUseDataSaveMode(true);
    }

    public void updateBadgeCount(){
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
            ShortcutBadger.setBadge(context, badgeCount);

        } catch (Exception e) {
            MatrixUtility.LOG("## updateBadgeCount(): Exception Msg=" + e.getMessage());
        }
    }

    /**
     * MetricsListener methods
     * */
    @Override
    public void onInitialSyncFinished(long duration) {
//        MatrixUtility.LOG("onInitialSyncFinished :"+duration);
    }

    @Override
    public void onIncrementalSyncFinished(long duration) {
//        MatrixUtility.LOG("onIncrementalSyncFinished :"+duration );
    }

    @Override
    public void onStorePreloaded(long duration) {
//        MatrixUtility.LOG("onStorePreloaded :"+duration);

    }

    @Override
    public void onRoomsLoaded(int nbOfRooms) {
//        MatrixUtility.LOG("onRoomsLoaded :" + nbOfRooms);

    }

    /**
     * RequestNetworkErrorListener methods
     * */
    @Override
    public void onConfigurationError(String matrixErrorCode) {
//        MatrixUtility.LOG("onConfigurationError " + matrixErrorCode);

    }

    @Override
    public void onSSLCertificateError(UnrecognizedCertificateException unrecCertEx) {
//        MatrixUtility.LOG("## createSession() : onSSLCertificateError " + unrecCertEx);
//        UnrecognizedCertificateException unrecCertEx = CertUtil.getCertificateException(e);
        if (unrecCertEx != null) {
            final Fingerprint fingerprint = unrecCertEx.getFingerprint();

            UnrecognizedCertHandler.show(MatrixService.get().homeServerConfig, fingerprint, false, new UnrecognizedCertHandler.Callback() {
                @Override
                public void onAccept() {

                }

                @Override
                public void onIgnore() {
                    MatrixUtility.LOG("onIgnore");
                }

                @Override
                public void onReject() {
                    MatrixUtility.LOG("onReject");
                }
            });

        }

    }

    public RoomSummary getRoomSummary(String roomId){
        return mxSession.getDataHandler().getStore(roomId).getSummary(roomId);
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

    /**
     * Convert a time since epoch date to a string.
     *
     * @param context  the context.
     * @param ts       the time since epoch.
     * @param timeOnly true to return the time without the day.
     * @return the formatted date
     */
    public static final long MS_IN_DAY = 1000 * 60 * 60 * 24;
    /**
     * Reset the time of a date
     *
     * @param date the date with time to reset
     * @return the 0 time date.
     */
    public static Date zeroTimeDate(Date date) {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        gregorianCalendar.set(Calendar.MINUTE, 0);
        gregorianCalendar.set(Calendar.SECOND, 0);
        gregorianCalendar.set(Calendar.MILLISECOND, 0);
        return gregorianCalendar.getTime();
    }
    /**
     * Returns the 12/24 h preference display
     *
     * @param context the context
     * @return the preferred display format
     */
    private static int getTimeDisplay(Context context) {
        return DateUtils.FORMAT_12HOUR ;
    }

    public static String tsToString(Context context, long ts, boolean timeOnly) {
        long daysDiff = (new Date().getTime() - zeroTimeDate(new Date(ts)).getTime()) / MS_IN_DAY;

        String res;

        if (timeOnly) {
            res = DateUtils.formatDateTime(context, ts,
                    DateUtils.FORMAT_SHOW_TIME | getTimeDisplay(context));
        } else if (0 == daysDiff) {
            res = "Today" + " " + DateUtils.formatDateTime(context, ts,
                    DateUtils.FORMAT_SHOW_TIME | getTimeDisplay(context));
        } else if (1 == daysDiff) {
            res = "Yesterday" + " " + DateUtils.formatDateTime(context, ts,
                    DateUtils.FORMAT_SHOW_TIME | getTimeDisplay(context));
        } else if (7 > daysDiff) {
            res = DateUtils.formatDateTime(context, ts,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL | getTimeDisplay(context));
        } else if (365 > daysDiff) {
            res = DateUtils.formatDateTime(context, ts,
                    DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE);
        } else {
            res = DateUtils.formatDateTime(context, ts,
                    DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
        }

        return res;
    }



    /**
     * Provides the formatted timestamp for the room
     */
    public static long getTimestamp(final Event event) {
        return event.getOriginServerTs();
    }
    public static String getTimestampToString(final Event event) {
        String text = tsToString(MatrixService.get().getContext(), event.getOriginServerTs(), false);

        // don't display the today before the time
        String today = "Today" + " ";
        if (text.startsWith(today)) {
            text = text.substring(today.length());
        }

        return text;
    }
    public static String getTimestampToString(final long timeStamp) {
        String text = tsToString(MatrixService.get().getContext(), timeStamp, false);

        // don't display the today before the time
        String today = "Today" + " ";
        if (text.startsWith(today)) {
            text = text.substring(today.length());
        }

        return text;
    }
}
