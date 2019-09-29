package mx.skipjack.service;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.skipjack.adoi.R;
import com.skipjack.adoi.messaging.model.MessageGroup;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.EventDisplay;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.data.RoomTag;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

public class MxRoomManager {
    private ArrayList<MessageGroup> favoriteList = new ArrayList<>();
    private ArrayList<MessageGroup> directChatList = new ArrayList<>();
    private ArrayList<MessageGroup> otherRoomList = new ArrayList<>();
    private ArrayList<MessageGroup> lowPriorityList = new ArrayList<>();
    private ArrayList<MessageGroup> serverNoticeList = new ArrayList<>();
    private Callback callback;
    public MxRoomManager(@NonNull Callback callback) {
        this.callback = callback;
    }

    public void getRooms(){
        Collection<Room> rooms = MatrixService.get().mxSession.getDataHandler().getStore().getRooms();

        int cnt = 0;
        for (Room room : rooms) {
            final Set<String> tags = room.getAccountData().getKeys();
            final boolean isFavorite = tags != null && tags.contains(RoomTag.ROOM_TAG_FAVOURITE);
            final boolean isLowPriority = tags != null && tags.contains(RoomTag.ROOM_TAG_LOW_PRIORITY);
            final boolean isServerNotice = tags != null && tags.contains(RoomTag.ROOM_TAG_SERVER_NOTICE);
            final boolean isDirectChat = isDirectChat(room.getRoomId());

            if (isFavorite) favoriteList.add(convert(room));
            else if (isLowPriority) lowPriorityList.add(convert(room));
            else if (isServerNotice) serverNoticeList.add(convert(room));
            else if (isDirectChat)directChatList.add(convert(room));
            else otherRoomList.add(convert(room));

            cnt++;
        }

        callback.onGetRooms(favoriteList,directChatList,otherRoomList,lowPriorityList,serverNoticeList);
    }
    private  boolean isDirectChat(final String roomId) {
        return (null != roomId) && MatrixService.get().mxSession.getDataHandler()
                .getDirectChatRoomIdsList().contains(roomId);
    }
    public static CharSequence getRoomMessageToDisplay(RoomSummary roomSummary) {

        CharSequence messageToDisplay = null;
        EventDisplay eventDisplay;




        if (null != roomSummary) {
            if (roomSummary.getLatestReceivedEvent() != null) {
                eventDisplay = new EventDisplay(MatrixService.get().getContext());
                eventDisplay.setPrependMessagesWithAuthor(true);
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = MatrixService.get().getContext().getTheme();
                theme.resolveAttribute(R.attr.color_last_display_message, typedValue, true);
                @ColorInt int color = typedValue.data;
                messageToDisplay = eventDisplay.getTextualDisplay(color,
                        roomSummary.getLatestReceivedEvent(),
                        roomSummary.getLatestRoomState());
            }

            // check if this is an invite
            if (roomSummary.isInvited() && (null != roomSummary.getInviterUserId())) {
                // TODO Re-write this algorithm, it's so complicated to understand for nothing...
                RoomState latestRoomState = roomSummary.getLatestRoomState();
                String inviterUserId = roomSummary.getInviterUserId();
                String myName = roomSummary.getUserId();

                if (null != latestRoomState) {
                    inviterUserId = latestRoomState.getMemberName(inviterUserId);
                    myName = latestRoomState.getMemberName(myName);
                } else {
                    inviterUserId = getMemberDisplayNameFromUserId(MatrixService.get().getContext(), roomSummary.getUserId(), inviterUserId);
                    myName = getMemberDisplayNameFromUserId(MatrixService.get().getContext(), roomSummary.getUserId(), myName);
                }

                if (TextUtils.equals(MatrixService.get().mxSession.getMyUserId(), roomSummary.getUserId())) {
                    messageToDisplay = MatrixService.get().getContext().getString(org.matrix.androidsdk.R.string.notice_room_invite_you, inviterUserId);
                } else {
                    messageToDisplay = MatrixService.get().getContext().getString(org.matrix.androidsdk.R.string.notice_room_invite, inviterUserId, myName);
                }
            }
        }

        return messageToDisplay;
    }
    /**
     * Provides the formatted timestamp for the room
     *
     * @param latestEvent the latest event.
     * @return the formatted timestamp to display.
     */
    public static String getRoomTimestamp(final Event latestEvent) {
        String text = tsToString(MatrixService.get().getContext(), latestEvent.getOriginServerTs(), false);

        // don't display the today before the time
        String today = "Today" + " ";
        if (text.startsWith(today)) {
            text = text.substring(today.length());
        }

        return text;
    }
    public static String getRoomTimestamp(final long timeStamp) {
        String text = tsToString(MatrixService.get().getContext(), timeStamp, false);

        // don't display the today before the time
        String today = "Today" + " ";
        if (text.startsWith(today)) {
            text = text.substring(today.length());
        }

        return text;
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
     * Get the displayable name of the user whose ID is passed in aUserId.
     *
     * @param context
     * @param matrixId matrix ID
     * @param userId   user ID
     * @return the user display name
     */
    private static String getMemberDisplayNameFromUserId(final Context context,
                                                         final String matrixId,
                                                         final String userId) {
        String displayNameRetValue;
        MXSession session;

        if (null == matrixId || null == userId) {
            displayNameRetValue = null;
        } else if ((null == (session = MatrixService.get().mxSession)) || (!session.isAlive())) {
            displayNameRetValue = null;
        } else {
            User user = session.getDataHandler().getStore().getUser(userId);

            if ((null != user) && !TextUtils.isEmpty(user.displayname)) {
                displayNameRetValue = user.displayname;
            } else {
                displayNameRetValue = userId;
            }
        }

        return displayNameRetValue;
    }



    public MessageGroup convert(Room room){
        MessageGroup group = new MessageGroup();
        group.setRoomId(room.getRoomId());
        group.setDisplayName(room.getRoomDisplayName(MatrixService.get().getContext()));
        group.setAvatarUrl(room.getAvatarUrl());
        group.setUnreadCount(room.getNotificationCount());
        final RoomSummary roomSummary = MatrixService.get().mxSession.getDataHandler()
                .getStore().getSummary(room.getRoomId());
        group.setLatestMessage(String.valueOf(getRoomMessageToDisplay(roomSummary)));
        group.setDate(getRoomTimestamp(roomSummary.getLatestReceivedEvent()));


//        for (Event event: events){
//            event.get
//        }

       return group;
    }


    public interface Callback{
        void onGetRooms(ArrayList<MessageGroup> favoriteList,
                        ArrayList<MessageGroup> directChatList,
                        ArrayList<MessageGroup> otherRoomList,
                        ArrayList<MessageGroup> lowPriorityList,
                        ArrayList<MessageGroup> serverNoticeList);
    }
}
