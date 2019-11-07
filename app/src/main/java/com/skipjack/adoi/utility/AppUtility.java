package com.skipjack.adoi.utility;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.skipjack.adoi.base.BaseApplication;

import org.matrix.androidsdk.core.Log;

import java.util.Calendar;
import java.util.Date;

public class AppUtility {

    /**
     * Checks if any field is empty
     * */
    public static boolean checkIfEmpty(EditText... editTexts){
        for (EditText editText: editTexts){
            if (editText.getText().toString().replace(" ","").isEmpty()){
                return true;
            }
        }
        return false;
    }
    /**
     * Toast implementation
     * @param message - the message
     * */
    public static void toast(Context context,String message){
        Toast toast  = Toast.makeText(context,message,Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    /**
     * Checks network connection
     * */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    /**
     * Check if email is valid
     * */
    public static boolean isEmailValid(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public static boolean isEmailValid(EditText... editTexts) {
        for (EditText editText: editTexts){
            if (!isEmailValid(editText.getText().toString())){
                return false;
            }
        }
        return true;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int getColorByAttribute(int attr){
        TypedValue typedValue = new TypedValue();
        BaseApplication.getContext().getTheme().resolveAttribute(attr, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = BaseApplication.getContext().getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("MX", "Not found color resource by id: " + colorRes);
        }


        return color;
    }

    public static boolean isSameDay(long time1, long time2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date(time1));
        cal2.setTime(new Date(time2));
        boolean isSameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return isSameDay;
    }

    public static int[] getScreenSize(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return new int[]{width,height};
    }
}
