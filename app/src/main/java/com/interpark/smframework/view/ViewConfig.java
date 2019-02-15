package com.interpark.smframework.view;

import android.view.ViewConfiguration;

import com.interpark.smframework.IDirector;

public class ViewConfig {
    public static final float SCROLL_TOLERANCE = 10;
    public static final float MIN_VELOCITY = 100;
    private static float MAX_VELOCITY = 10000;

    public static final long TAP_TIMEOUT;
    public static final long DOUBLE_TAP_TIMEOUT;
    public static final long LONG_PRESS_TIMEOUT;

    static {
        // 안드로이드 기본은 100인데 너무 짧다... 300정도로 하자
        TAP_TIMEOUT = 300;//ViewConfiguration.getTapTimeout();

        // 나머지는 안드로이드 기본에 맞춘다.
        DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();   // 300
        LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();   // 500
    }

    static public float getScaledMaximumFlingVelocity(IDirector director) {
        return MAX_VELOCITY;
    }

    static public float getScaledTouchSlop(IDirector director) {
        return ViewConfiguration.get(director.getContext()).getScaledTouchSlop();
    }

    static public float getScaledDoubleTouchSlop(IDirector director) {
        return ViewConfiguration.get(director.getContext()).getScaledDoubleTapSlop();
    }

    public static final int ACTIONBAR_HEIGHT = 100;
    public static final int ACTIONBAR_TAB_HEIGHT = 96;
    public static final int SIDEMENU_WIDTH = 614;
    public static final int SIDEMENU_GRAB_AREA = 15;
}
