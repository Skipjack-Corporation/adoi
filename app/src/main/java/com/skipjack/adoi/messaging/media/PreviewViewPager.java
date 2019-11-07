package com.skipjack.adoi.messaging.media;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class PreviewViewPager extends ViewPager {
    private static final String LOG_TAG = PreviewViewPager.class.getSimpleName();

    public PreviewViewPager(Context context) {
        super(context);
    }

    public PreviewViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (getAdapter() == null || getAdapter().getCount() == 0) {
            return false;
        }

        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException exception) {
            Log.e(LOG_TAG, "Exception: " + exception.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getAdapter() == null || getAdapter().getCount() == 0) {
            return false;
        }

        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException exception) {
            Log.e(LOG_TAG, "Exception: " + exception.getLocalizedMessage());
        }
        return false;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        try {
            return super.getChildDrawingOrder(childCount, i);
        } catch (Exception e) {
            Log.e(LOG_TAG, "## getChildDrawingOrder() failed " + e.getMessage(), e);
        }

        return 0;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            Log.e(LOG_TAG, "## dispatchDraw() failed " + e.getMessage(), e);
        }
    }
}
