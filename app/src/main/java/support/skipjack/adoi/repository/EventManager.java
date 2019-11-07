package support.skipjack.adoi.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseApplication;

import org.matrix.androidsdk.core.callback.SimpleApiCallback;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomMediaMessage;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.timeline.EventTimeline;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.listeners.MXMediaUploadListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.TokensChunkEvents;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.message.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import support.skipjack.adoi.model.PageTokenEntity;
import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.converter.EventConverter;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.model.EventEntry;
import support.skipjack.adoi.local_storage.AppDatabase;
import support.skipjack.adoi.local_storage.AppSharedPreference;

public class EventManager extends MXEventListener {

    private List<Event> eventList = new ArrayList<>();
    private EventCallback callback;
    private IMXStore imxStore;
    private String roomId;

    private Room room;
    private String startToken;
    private String endToken;

    public EventManager(@NonNull String roomId, @NonNull EventCallback callback) {
        this.roomId = roomId;
        this.callback = callback;

        eventList = new ArrayList<>();
        imxStore = MatrixService.get().mxSession.getDataHandler().getStore(this.roomId);

        room = MatrixService.get().mxSession.getDataHandler().getRoom(this.roomId,false);
        startToken = null;
        endToken = null;

        room.addEventListener(new MXEventListener(){
            @Override
            public void onLiveEvent(Event event, RoomState roomState) {

            }
        });
    }
    public void addListener(){
        room.addEventListener(this);
    }
    public void removeListener(){
    }
    public interface EventCallback{
        void onEventListing(List<Event> eventList);
        void onEventListingError(String message);
        void onMemberTyping(String message);
        void onUploadMediaStart(String uploadId,Event event, String mimeType);
        void onUploadMediaProgress(String uploadId, Event event, String mimeType, int progress);
        void onUploadMediaFinish(String uploadId, boolean success, String failedMessage);
    }

    @Override
    public void onPresenceUpdate(Event event, User user) {
        MatrixUtility.LOG("onPresenceUpdate");
    }

    @Override
    public void onLiveEvent(Event event, RoomState roomState) {
        String eventType = event.getType();

        switch (eventType) {
            case Event.EVENT_TYPE_MESSAGE:{
                boolean exist = false;
                for (Event e: eventList){
                    if (e.getEventId().equals(event.getEventId())) {
                        exist = true;
                        break;
                    }
                }
                if (!exist)eventList.add(0,event);
                callback.onEventListing(eventList);
                insertMessagesToDB(event);
                } break;
            case Event.EVENT_TYPE_TYPING:
                onRoomMemberTyping();
                break;
//            case Event.EVENT_TYPE_STATE_ROOM_NAME:
//            case Event.EVENT_TYPE_STATE_ROOM_ALIASES:
//            case Event.EVENT_TYPE_STATE_ROOM_MEMBER:
////                setTitle();
////                updateRoomHeaderMembersStatus();
////                updateRoomHeaderAvatar();
//                break;
//            case Event.EVENT_TYPE_STATE_ROOM_TOPIC:
////                StateEvent stateEvent = JsonUtils.toStateEvent(event.getContent());
////                setTopic(stateEvent.topic);
//                break;
//            case Event.EVENT_TYPE_STATE_ROOM_POWER_LEVELS:
////                checkSendEventStatus();
//                break;
//
//            case Event.EVENT_TYPE_STATE_ROOM_AVATAR:
////                updateRoomHeaderAvatar();
//                break;
//            case Event.EVENT_TYPE_MESSAGE_ENCRYPTION:
////                boolean canSendEncryptedEvent = mRoom.isEncrypted() && mSession.isCryptoEnabled();
////                mE2eImageView.setImageResource(canSendEncryptedEvent ? R.drawable.e2e_verified : R.drawable.e2e_unencrypted);
////                mVectorMessageListFragment.setIsRoomEncrypted(mRoom.isEncrypted());
//                break;
//            case Event.EVENT_TYPE_STATE_ROOM_TOMBSTONE:
////                checkSendEventStatus();
//                break;
            default:
                break;
        }
    }

    public void sendReadMarker(){
        if (room.getNotificationCount() == 0) return;
        room.markAllAsRead(new MatrixCallback<Void>() {
            @Override
            public void onAPISuccess(Void data) {

            }

            @Override
            public void onAPIFailure(String errorMessage) {

            }
        });

    }
    public void getEventListing(){
        startToken = null;
        endToken = null;
        eventList = new ArrayList<>();
        if (MatrixUtility.isNetworkConnected()){
            pullEvents(endToken, EventTimeline.Direction.BACKWARDS);
        }else{
            getMessagesToDB(new GetCallback() {
                @Override
                public void onGetMessages(List<EventEntry> messageEntries) {
                    if (messageEntries.size() > 0){
                        for (EventEntry entry: messageEntries){
                            eventList.add(MatrixService.get().getEvent(entry.eventId,roomId));
                        }
                        callback.onEventListing(filterMessageByDate(eventList));
                    }else
                        callback.onEventListing(filterMessageByDate(eventList));
                }

                @Override
                public void onGetFailed() {
                    callback.onEventListingError("No record found.");
                }
            });
        }

    }

    public void pullPreviousEvents(){
        pullEvents(endToken, EventTimeline.Direction.BACKWARDS);

    }

    public void pullPresentEvents(){
        pullEvents(startToken, EventTimeline.Direction.FORWARDS);

    }

    private void pullEvents(String token,EventTimeline.Direction direction){
        MatrixService.get().mxSession.getDataHandler().getDataRetriever()
        .paginate(imxStore, roomId, token, direction,
            true, new MatrixCallback<TokensChunkEvents>() {
                @Override
                public void onAPISuccess(TokensChunkEvents data) {
                    startToken = data.start;
                    endToken = data.end;
                    if (data.chunk.size() > 0) {
                        eventList.addAll(data.chunk);
                        callback.onEventListing(filterMessageByDate(eventList));

                        List<EventEntry> entryList = new EventConverter().convert(data.chunk);
                        insertMessagesToDB(entryList);
                    }else
                        callback.onEventListing(new ArrayList<>());
                    //next token
                    saveToken(roomId,startToken, endToken);
                }

                @Override
                public void onAPIFailure(String errorMessage) {
                    callback.onEventListingError(errorMessage);
                }
            });
    }

    private List<Event> filterMessageByDate(List<Event> eventList){
        List<Event> newList = new ArrayList<>();
        for (Event event: eventList){

            if (event.getType().equals(Event.EVENT_TYPE_MESSAGE) ||
            event.getType().equals(Event.EVENT_TYPE_CALL_ANSWER) ||
            event.getType().equals(Event.EVENT_TYPE_CALL_HANGUP) ||
            event.getType().equals(Event.EVENT_TYPE_CALL_INVITE) ||
            event.getType().equals(Event.EVENT_TYPE_STICKER)){

                newList.add(event);
            }

        }
        Collections.sort(newList, new Comparator<Event>(){
            public int compare(Event obj1, Event obj2) {
                // ## Ascending order
                // return Long.valueOf(obj1.date).compareTo(Long.valueOf(obj2.date));

                // ## Descending order
                 return Long.valueOf(obj2.originServerTs).compareTo(Long.valueOf(obj1.originServerTs));
            }
        });
        return newList;
    }

    /**
     * Get Saved Messages
     * */
    @SuppressLint("CheckResult")
    public void getMessagesToDB(GetCallback callback){
        AppDatabase.GET.messageDao()
                .getList(roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventEntries -> {
                    callback.onGetMessages(eventEntries);
                },throwable -> {
                    throwable.printStackTrace();
                    callback.onGetFailed();
                });
    }
    @SuppressLint("CheckResult")
    public void insertMessagesToDB(List<EventEntry> eventEntryList, PullCallback callback){
        AppDatabase.GET.messageDao()
                .add(eventEntryList.toArray(new EventEntry[eventEntryList.size()]))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                },throwable -> {
                    throwable.printStackTrace();
                    callback.onPullFailed_DirectListing(eventEntryList);
                });
    }
    @SuppressLint("CheckResult")
    public void insertMessagesToDB(List<EventEntry> eventEntryList){
        AppDatabase.GET.messageDao()
                .add(eventEntryList.toArray(new EventEntry[eventEntryList.size()]))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                },throwable -> {
                    throwable.printStackTrace();
                });
    }
    @SuppressLint("CheckResult")
    public void insertMessagesToDB(Event event){
        EventEntry eventEntry = new EventConverter().convert(event);
        AppDatabase.GET.messageDao()
                .add(eventEntry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                },throwable -> {
                    throwable.printStackTrace();
                });
    }
//    /**
//     * Room end Token Saved
//     * */
//
//    @SuppressLint("CheckResult")
//    public void getToken(String roomId,TokenCallback callback){
//        AppDatabase.GET.roomTokenDao()
//                .get(roomId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(roomToken -> {
//                    callback.onEndToken(roomToken);
//                }, throwable -> {
//                    callback.onEndToken(null);
//                });
//    }
    @SuppressLint("CheckResult")
    public void saveToken(String roomId, String start,String end){
        AppDatabase.GET.roomTokenDao()
                .add(new PageTokenEntity(roomId,start,end))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {});
    }
    public interface PullCallback{
        void onPullFailed_DirectListing(List<EventEntry> eventEntryList);
    }
    public interface GetCallback{
        void onGetMessages(List<EventEntry> messageEntries);
        void onGetFailed();
    }
//    public void sendTextMessage(String body) {
//        this.sendTextMessage("m.text", body, (String)null);
//    }

    public void onRoomMemberTyping(){

        List<String> typingUsers = room.getTypingUsers();
        String typingMessage = null;
        if (!typingUsers.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < typingUsers.size(); i++) {
                RoomMember member = room.getMember(typingUsers.get(i));

                // check if the user is known and not oneself
                if ((null != member) && !TextUtils.equals(AppSharedPreference.get().getLoginCredential().userId,
                        member.getUserId()) && (null != member.displayname)) {
                    names.add(member.displayname);
                }
            }

            if (names.isEmpty()) {
                // nothing to display
                typingMessage = null;
            } else if (names.size() == 1) {
                typingMessage = BaseApplication.getContext().getString(R.string.room_one_user_is_typing, names.get(0));
            } else if (names.size() == 2) {
                typingMessage = BaseApplication.getContext().getString(R.string.room_two_users_are_typing, names.get(0), names.get(1));
            } else {
                typingMessage = BaseApplication.getContext().getString(R.string.room_many_users_are_typing, names.get(0), names.get(1));
            }
        }

        callback.onMemberTyping(typingMessage);

    }
    public void sendTypingNotification(boolean typing){
        room.sendTypingNotification(typing, typing ? 10000 : -1, new SimpleApiCallback<Void>() {
            @Override
            public void onSuccess(Void info) {

            }
        });
    }
    private RoomMediaMessage.EventCreationListener eventCreationListener = new RoomMediaMessage.EventCreationListener() {
        @Override
        public void onEventCreated(RoomMediaMessage roomMediaMessage) {

        }

        @Override
        public void onEventCreationFailed(RoomMediaMessage roomMediaMessage, String errorMessage) {

        }

        @Override
        public void onEncryptionFailed(RoomMediaMessage roomMediaMessage) {

        }
    };
    public void sendTextMessage(String body) {
        room.sendTextMessage(body, body, Message.FORMAT_MATRIX_HTML, eventCreationListener);
    }
    public void sendMediaMessage(final RoomMediaMessage roomMediaMessage) {
        Point size = new Point(0, 0);
        getScreenSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;
        int mMaxImageWidth;
        int  mMaxImageHeight;
        // landscape / portrait
        if (screenWidth < screenHeight) {
           mMaxImageWidth = Math.round(screenWidth * 0.6f);
            mMaxImageHeight = Math.round(screenHeight * 0.4f);
        } else {
            mMaxImageWidth = Math.round(screenWidth * 0.4f);
            mMaxImageHeight = Math.round(screenHeight * 0.6f);
        }
        room.sendMediaMessage(roomMediaMessage, mMaxImageWidth, mMaxImageHeight, eventCreationListener );

        roomMediaMessage.setMediaUploadListener(new MXMediaUploadListener() {
            @Override
            public void onUploadStart(String uploadId) {
                callback.onUploadMediaStart(uploadId,roomMediaMessage.getEvent(),
                        roomMediaMessage.getMimeType(BaseApplication.getContext()));
            }
            @Override
            public void onUploadCancel(String uploadId) {
                callback.onUploadMediaFinish(uploadId,false,"Upload has been cancelled.");
            }
            @Override
            public void onUploadError(String uploadId, int serverResponseCode, String serverErrorMessage) {
                callback.onUploadMediaFinish(uploadId,false,""+serverErrorMessage);
            }
            @Override
            public void onUploadComplete(String uploadId, String contentUri) {
                callback.onUploadMediaFinish(uploadId,true,null);

            }

            @Override
            public void onUploadProgress(String uploadId, UploadStats uploadStats) {
//                int progress = (uploadStats.mProgress * 10 / 100);
                callback.onUploadMediaProgress(uploadId, roomMediaMessage.getEvent(),roomMediaMessage.getMimeType(BaseApplication.getContext())
                        ,uploadStats.mProgress);

            }
    });
    }
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void getScreenSize(Point size) {
        WindowManager wm = (WindowManager) BaseApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);
    }
//
//    public void sendTextMessage(String body, String formattedBody, @Nullable Event replyToEvent, String format) {
//        this.mRoom.sendTextMessage(body, formattedBody, format, replyToEvent, this.mEventCreationListener);
//    }
//
//    public void sendEmote(String emote, String formattedEmote, String format) {
//        this.mRoom.sendEmoteMessage(emote, formattedEmote, format, this.mEventCreationListener);
//    }
//
//    public void sendStickerMessage(Event event) {
//        this.mRoom.sendStickerMessage(event, this.mEventCreationListener);
//    }

//    private final RoomMediaMessage.EventCreationListener mEventCreationListener = new RoomMediaMessage.EventCreationListener() {
//        public void onEventCreated(RoomMediaMessage roomMediaMessage) {
//            MatrixMessageListFragment.this.insert(roomMediaMessage);
//        }
//
//        public void onEventCreationFailed(RoomMediaMessage roomMediaMessage, String errorMessage) {
//            MatrixMessageListFragment.this.displayMessageSendingFailed(errorMessage);
//        }
//
//        public void onEncryptionFailed(RoomMediaMessage roomMediaMessage) {
//            MatrixMessageListFragment.this.displayEncryptionAlert();
//        }
//    };


//
//    public void deleteUnsentEvents() {
//        List<Event> unsent = this.mRoom.getUnsentEvents();
//        this.mRoom.deleteEvents(unsent);
//        Iterator var2 = unsent.iterator();
//
//        while(var2.hasNext()) {
//            Event event = (Event)var2.next();
//            this.mAdapter.removeEventById(event.eventId);
//        }
//
//        this.mAdapter.notifyDataSetChanged();
//    }
//
//    public void resendUnsentMessages() {
//        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//                public void run() {
//                    MatrixMessageListFragment.this.resendUnsentMessages();
//                }
//            });
//        } else {
//            List<Event> unsent = this.mRoom.getUnsentEvents();
//            Iterator var2 = unsent.iterator();
//
//            while(var2.hasNext()) {
//                Event unsentMessage = (Event)var2.next();
//                this.resend(unsentMessage);
//            }
//
//        }
//    }
//
//    protected void resend(final Event event) {
//        if (null == event.eventId) {
//            Log.e("MatrixMsgsListFrag", "resend : got an event with a null eventId");
//        } else if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//                public void run() {
//                    MatrixMessageListFragment.this.resend(event);
//                }
//            });
//        } else {
//            event.originServerTs = System.currentTimeMillis();
//            this.getSession().getDataHandler().deleteRoomEvent(event);
//            this.mAdapter.removeEventById(event.eventId);
//            this.mPendingRelaunchTimersByEventId.remove(event.eventId);
//            org.matrix.androidsdk.rest.model.message.EventEntry message = JsonUtils.toMessage(event.getContent());
//            RoomMediaMessage roomMediaMessage = new RoomMediaMessage(new Event(message, this.mSession.getMyUserId(), this.mRoom.getRoomId()));
//            roomMediaMessage.getEvent().eventId = event.eventId;
//            if (message instanceof MediaMessage) {
//                this.sendMediaMessage(roomMediaMessage);
//            } else {
//                this.mRoom.sendMediaMessage(roomMediaMessage, this.getMaxThumbnailWidth(), this.getMaxThumbnailHeight(), this.mEventCreationListener);
//            }
//
//        }
//    }

}
