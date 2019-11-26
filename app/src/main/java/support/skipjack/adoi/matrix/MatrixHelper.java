package support.skipjack.adoi.matrix;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.skipjack.adoi.base.BaseApplication;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.RoomItemType;

public class MatrixHelper {
    public static final String LOG_TAG = "API_MATRIX_TAG";

    public static final long MS_IN_DAY = 1000 * 60 * 60 * 24;




    public static void LOG(String message){
        Log.e(LOG_TAG,"" +message);
    }
    public static void LOG(String title,String message){
        Log.e(title,"" +message);
    }

    public static Date zeroTimeDate(Date date) {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        gregorianCalendar.set(Calendar.MINUTE, 0);
        gregorianCalendar.set(Calendar.SECOND, 0);
        gregorianCalendar.set(Calendar.MILLISECOND, 0);
        return gregorianCalendar.getTime();
    }
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
    public static String getTimestampToString(final long timeStamp) {
        String text = tsToString(MatrixService.get().getContext(), timeStamp, false);

        // don't display the today before the time
        String today = "Today" + " ";
        if (text.startsWith(today)) {
            text = text.substring(today.length());
        }

        return text;
    }

    public static String getTimestampToString(final Event latestEvent) {
        String text = tsToString(MatrixService.get().getContext(), latestEvent.getOriginServerTs(), false);

        // don't display the today before the time
        String today = "Today" + " ";
        if (text.startsWith(today)) {
            text = text.substring(today.length());
        }

        return text;
    }

     /** Checks network connection
     * */
    public static boolean isNetworkConnected() {
        ConnectivityManager connectivity =
                (ConnectivityManager) BaseApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static RoomItemType getRoomItemType(Room room){
        // check if the room exists
        // the user conference rooms are not displayed.
        if (room != null && !room.isConferenceUserRoom() && room.isInvited()) {
            if (room.isDirectChatInvitation()) {
                return RoomItemType.INVITE_DIRECT;
            } else {
                return RoomItemType.INVITE_ROOM;
            }
        }
      return RoomItemType.DEFAULT;
    }

    /**
     * Provide the user online status from his user Id.
     * if refreshCallback is set, try to refresh the user presence if it is not known
     *
     * @param context         the context.
     * @param session         the session.
     * @param userId          the userId.
     * @param refreshCallback the presence callback.
     * @return the online status description.
     */
    public static String getUserOnlineStatus(final Context context,
                                             final MXSession session,
                                             final String userId,
                                             final ApiCallback<Void> refreshCallback) {
        // sanity checks
        if ((null == session) || (null == userId)) {
            return null;
        }

        final User user = session.getDataHandler().getStore().getUser(userId);

        // refresh the presence with this conditions
        boolean triggerRefresh = (null == user) || user.isPresenceObsolete();

        if ((null != refreshCallback) && triggerRefresh) {
            Log.d(LOG_TAG, "Get the user presence : " + userId);

            final String fPresence = (null != user) ? user.presence : null;

            session.refreshUserPresence(userId, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void info) {
                    boolean isUpdated = false;
                    User updatedUser = session.getDataHandler().getStore().getUser(userId);

                    // don't find any info for the user
                    if ((null == user) && (null == updatedUser)) {
                        Log.d(LOG_TAG, "Don't find any presence info of " + userId);
                    } else if ((null == user) && (null != updatedUser)) {
                        Log.d(LOG_TAG, "Got the user presence : " + userId);
                        isUpdated = true;
                    } else if (!TextUtils.equals(fPresence, updatedUser.presence)) {
                        isUpdated = true;
                        Log.d(LOG_TAG, "Got some new user presence info : " + userId);
                        Log.d(LOG_TAG, "currently_active : " + updatedUser.currently_active);
                        Log.d(LOG_TAG, "lastActiveAgo : " + updatedUser.lastActiveAgo);
                    }

                    if (isUpdated && (null != refreshCallback)) {
                        try {
                            refreshCallback.onSuccess(null);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "getUserOnlineStatus refreshCallback failed", e);
                        }
                    }
                }

                @Override
                public void onNetworkError(Exception e) {
                    Log.e(LOG_TAG, "getUserOnlineStatus onNetworkError " + e.getLocalizedMessage(), e);
                }

                @Override
                public void onMatrixError(MatrixError e) {
                    Log.e(LOG_TAG, "getUserOnlineStatus onMatrixError " + e.getLocalizedMessage());
                }

                @Override
                public void onUnexpectedError(Exception e) {
                    Log.e(LOG_TAG, "getUserOnlineStatus onUnexpectedError " + e.getLocalizedMessage(), e);
                }
            });
        }

        // unknown user
        if (null == user) {
            return null;
        }

        String presenceText = null;
        if (TextUtils.equals(user.presence, User.PRESENCE_ONLINE)) {
//            presenceText = context.getString(R.string.room_participants_online);
        } else if (TextUtils.equals(user.presence, User.PRESENCE_UNAVAILABLE)) {
//            presenceText = context.getString(R.string.room_participants_idle);
        } else if (TextUtils.equals(user.presence, User.PRESENCE_OFFLINE) || (null == user.presence)) {
//            presenceText = context.getString(R.string.room_participants_offline);
        }

        if (presenceText != null) {
            if ((null != user.currently_active) && user.currently_active) {
//                presenceText = context.getString(R.string.room_participants_now, presenceText);
            } else if ((null != user.lastActiveAgo) && (user.lastActiveAgo > 0)) {
//                presenceText = context.getString(R.string.room_participants_ago, presenceText,
//                        formatSecondsIntervalFloored(context,
//                                user.getAbsoluteLastActiveAgo() / 1000L));
            }
        }

        return presenceText;
    }
}
