package com.interpark.smframework.util;

import android.graphics.Color;

import com.interpark.smframework.base.types.Color4F;

public class AppConst {

    public static class SceneTransitionTime {
        public static final float NORMAL = 0.3f;
        public static final float FAST = 0.2f;
        public static final float SLOW = 0.5f;
        public static final float DEFAULT_DELAY_TIME = 0.1f;
    }

    public static class ZOrder {

        public static final int USER = 0;
        public static final int BG = Integer.MIN_VALUE+1;

        public static final int BUTTON_NORMAL = Integer.MIN_VALUE+100;
        public static final int BUTTON_PRESSED = BUTTON_NORMAL+1;
        public static final int BUTTON_TEXT = BUTTON_PRESSED+1;
        public static final int BUTTON_ICON_NORMAL = BUTTON_TEXT+1;
        public static final int BUTTON_ICON_PRESSED = BUTTON_ICON_NORMAL+1;
    }

    public static class DEFAULT_VALUE {
        public static final float FONT_SIZE = 12.0f;
        public static final float LINE_WIDTH = 2.0f;
    }

    public static class COLOR {
        public static final Color4F _BLACK = new Color4F(0, 0, 0, 1);
        public static final Color4F _WHITE = new Color4F(1, 1, 1, 1);
        public static final Color4F _EEEFF1 = new Color4F(0xee, 0xef, 0xf1);
        public static final Color4F _DBDCDF = new Color4F(0xdb, 0xdc, 0xdf);
        public static final Color4F _ADAFB3 = new Color4F(0xad, 0xaf, 0xb3);
        public static final Color4F _00A1E4 = new Color4F(0x00, 0xa1, 0xe4);
    }

    public static class SIZE {
        public static final float EDGE_SWIPE_MENU = 80.0f;
        public static final float EDGE_SWIPE_TOP = 100.0f;
        public static final float LEFT_SIDE_MENU_WIDTH = 550.0f;
        public static final float TOP_MENU_HEIGHT = 130.0f;
        public static final float TOP_MENU_BUTTON_HEIGHT = 120.0f;
        public static final float MENUBAR_HEIGHT = 130.0f;
        public static final float DOT_DIAMETER = 20.0f;
        public static final float LINE_DIAMETER = 5.0f;
        public static final float TOP_MENU_BUTTONE_SIZE = 120.0f;
    }

    public static class TAG {
        public static final int USER =  0x10000;
        public static final int SYSTEM = 0x10000;
        public static final int ACTION_VIEW_SHOW = (SYSTEM + 1);
        public static final int ACTION_VIEW_HIDE = (SYSTEM + 2);
        public static final int ACTION_BG_COLOR = (SYSTEM + 3);
        public static final int ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL = (SYSTEM + 4);
        public static final int ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS = (SYSTEM + 5);
        public static final int ACTION_VIEW_STATE_CHANGE_DELAY = (SYSTEM + 6);
        public static final int ACTION_ZOOM = (SYSTEM + 7);
        public static final int ACTION_STICKER_REMOVE = (SYSTEM + 10);
        public static final int ACTION_LIST_ITEM_DEFAULT = (SYSTEM + 100);
        public static final int ACTION_LIST_HIDE_REFRESH = (SYSTEM + 101);
        public static final int ACTION_LIST_JUMP = (SYSTEM + 102);
        public static final int ACTION_PROGRESS1 = (SYSTEM + 103);
        public static final int ACTION_PROGRESS2 = (SYSTEM + 104);
        public static final int ACTION_PROGRESS3 = (SYSTEM + 105);
    }

    public static class Config {
        public static final float DEFAULT_FONT_SIZE = 12;

        public static final float TAP_TIMEOUT = 0.5f;
        public static final float DOUBLE_TAP_TIMEOUT = 0.3f;
        public static final float LONG_PRESS_TIMEOUT = 0.5f;

        public static final float SCALED_TOUCH_SLOPE = 100.0f;
        public static final float SCALED_DOUBLE_TAB_SLOPE = 100.0f;

        public static final float SMOOTH_DIVIDER = 3.0f;
        public static final float TOLERANCE_POSITION = 0.01f;
        public static final float TOLERANCE_ROTATE = 0.01f;
        public static final float TOLERANCE_SCALE = 0.005f;
        public static final float TOLERANCE_COLOR = 0.0005f;
        public static final float TOLERANCE_ALPHA = 0.0005f;

        public static final float MIN_VELOCITY = 1000.0f;
        public static final float MAX_VELOCITY = 30000.0f;

        public static final float SCROLL_TOLERANCE = 10.0f;
        public static final float SCROLL_HORIZONTAL_TOLERANCE = 20.0f;

        public static final float BUTTON_PUSHDOWN_PIXELS = -10.0f;
        public static final float BUTTON_PUSHDOWN_SCALE = 0.9f;
        public static final float BUTTON_STATE_CHANGE_PRESS_TO_NORMAL_TIME = 0.25f;
        public static final float BUTTON_STATE_CHANGE_NORMAL_TO_PRESS_TIME = 0.15f;

        public static final float ZOOM_SHORT_TIME = 0.1f;
        public static final float ZOOM_NORMAL_TIME = 0.30f;
        public static final float LIST_HIDE_REFRESH_TIME = 0.1f;

        public static final float TEXT_TRANS_DELAY = 0.05f;
        public static final float TEXT_TRANS_DURATION = 0.17f;
        public static final float TEXT_TRANS_MOVE_DURATION = 0.6f;
    }
}
