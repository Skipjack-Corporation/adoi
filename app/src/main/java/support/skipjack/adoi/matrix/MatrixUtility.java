package support.skipjack.adoi.matrix;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateUtils;
import android.util.Log;

import com.skipjack.adoi.base.BaseApplication;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.RoomItemType;

public class MatrixUtility {
    public static final String LOG_TAG = "API_MATRIX_TAG";
    public static void LOG(String message){
        Log.e(LOG_TAG,"" +message);
    }
    public static void LOG(String title,String message){
        Log.e(title,"" +message);
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
}
