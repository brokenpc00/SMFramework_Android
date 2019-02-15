package com.interpark.smframework.base;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.BackPressable;

public class Popup extends SMView implements BackPressable {
    public static final long POPUP_SHOW_TIMEMILLIS = 250;
    public static final long POPUP_HIDE_TIMEMILLIS = 150;
    public static final float POPUP_DEFAULT_FADEVALUE = .6f;

    public interface OnDismissListener {
        public void onDismiss(Popup popup);
    }
    private OnDismissListener mDismissListener;

    public void setOnDismissListener(final OnDismissListener listener) {
        mDismissListener = listener;
    }


    private boolean mCancelable = true;

    public Popup(IDirector director) {
        super(director);
        setCancelable(mCancelable);
    }

    public Popup(IDirector director, int id) {
        super(director, id);
    }

//    public Popup(IDirector director, float x, float y, float width, float height, float cx, float cy) {
//        super(director, x, y, width, height, cx, cy);
//    }

    public Popup(IDirector director, float x, float y, float width, float height) {
        super(director, x, y, width, height);
    }

    public void dismiss() {
        getDirector().closePopupView(this);
        if (mDismissListener != null) {
            mDismissListener.onDismiss(this);
        }
    }

    @Override
    public void cancel() {
        dismiss();
    }

    @Override
    public void hideComplete() {
        super.hideComplete();
        if (getParent() != null) {
            getParent().removeChild(this);
            onDestoryView();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mCancelable) {
            cancel();
        }
        return true;
    }

    public Popup setCancelable(boolean flag) {
        mCancelable = flag;
        setCancelIfTouchOutside(flag);
        return this;
    }
}
