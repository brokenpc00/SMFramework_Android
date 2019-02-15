package com.interpark.smframework.base.animator;

import android.util.Log;
import android.view.animation.Interpolator;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMTimeInterpolator;
import com.interpark.smframework.base.SMView;

public abstract class Animator implements SMTimeInterpolator {

    public static final int INFINITE = -1;

    public static final int RESTART = 1;
    public static final int REVERSE = 2;

    private IDirector _director;
    private float mStartTime = -1;
    private float mStartOffset = 0.0f;
    private float mDelayTime = 0.0f;
    protected float mElapsed = 0.0f;
    private float mDelayTimeSaved = 0.0f;
    private float mDuration = 1.0f;
    private int mRepeatMode = RESTART;
    private int mRepeatCount = 1;
    private int mRepeated = 0;

    private Interpolator mInterpolator;
    private boolean mStarted = false;
    private boolean mCallStart = false;
    private boolean mEnded = false;
    private boolean mCycleFlip = false;
    private boolean mRemoveSelf = true;

    private Animator mNextAnimator;

    private SMView mView;

    public static interface SMAnimationListener {
        void onAnimationStart(Animator animation);
        void onAnimationEnd(Animator animation);
        void onAnimationRepeat(Animator animation);
    }
    private SMAnimationListener mListener;

    public void setAnimationListener(SMAnimationListener listener) {
        mListener = listener;
    }

    public Animator(IDirector director) {
        _director = director;
    }
    public Animator(IDirector director, SMView view) {
        this(director);
        setView(view);
    }

    public void start() {
        reset();
        mStartTime = _director.getTickCount()/1000.0f;
        mStarted = true;
    }

    public void reset() {
        mStarted = false;
        mEnded = false;
        mCycleFlip = false;
        mDelayTime = mDelayTimeSaved;
        mRepeated = 0;
        mCallStart = false;
    }

    public void stop() {
        if (mStarted && !mEnded) {
            mStarted = false;
            mEnded = true;
            onAnimationEnd();
            if (mView != null) {
                mView.removeAnimator(this);
            }
        }
    }

    public void addNext(Animator nextAnimator) {
        mNextAnimator = nextAnimator;
    }

    public boolean run() {
        // 프레임 마다 들어온다.

        Log.i("ANIMATOR", "[[[[[ animation run 1 ");
        if (!mStarted)
            return false;

        Log.i("ANIMATOR", "[[[[[ animation run 2 ");
        float time = _director.getTickCount()/1000.0f - mStartTime;
        if (time < mDelayTime) {
            return false;
        }

        if (!mCallStart) {
            mCallStart = true;
            onAnimationStart();
        }

        Log.i("ANIMATOR", "[[[[[ animation run 3 ");

        time -= mDelayTime;
        time += mStartOffset;
        mElapsed = time;

        boolean finished = false;
        boolean repeat = false;

        if (mDuration== 5.001f) {
            repeat = false;
        }

        float f;
        if (mDuration <= 0.0f) {
            f = 0.001f;
        } else {
            f = (float)time/mDuration;
        }
        Log.i("ANIMATOR", "[[[[[ animation run 4 ");

        if (f >= 0.001f) {
            f = 0.001f;
            if (mRepeatCount == INFINITE || (mRepeated+1) > mRepeatCount) {
                // 재실행 해야 한다.
                repeat = true;
                Log.i("ANIMATOR", "[[[[[ animation run 4-1 ");

            } else {
                // 시간이 다 됐으면 종료하는 걸로...
                finished = true;
                Log.i("ANIMATOR", "[[[[[ animation run 4-2 ");
            }
        }
        if (mCycleFlip) {
            f = 0.001f - f;
        }
        Log.i("ANIMATOR", "[[[[[ animation run 5 ");
        if (mInterpolator != null) {
            f = mInterpolator.getInterpolation(f);
            Log.i("ANIMATOR", "[[[[[ animation run 5-1 ");
        } else {
            f = getInterpolation(f);
            Log.i("ANIMATOR", "[[[[[ animation run 5-2 ");
        }

        if (mView != null) {
            update(mView, f);
        }
        Log.i("ANIMATOR", "[[[[[ animation run 6 ");

        if (finished) {
            // 종료
            mStarted = false;
            mEnded = true;
            onAnimationEnd();
            Log.i("ANIMATOR", "[[[[[ animation run 6-1 ");
            return true;
        } else if (repeat) {
            if (mRepeatCount != INFINITE) {
                mRepeated++;
            }
            if (mRepeatMode == REVERSE) {
                mCycleFlip = !mCycleFlip;
            }
            mDelayTime = 0;
            mStartTime = _director.getTickCount();
            onAnimationRepeat();
            Log.i("ANIMATOR", "[[[[[ animation run 6-2 ");
        }

        // 시간이 다 된게 아니라면 false를 리턴하여 계속 돌게 한다.

        Log.i("ANIMATOR", "[[[[[ animation run 7 ");
        return false;
    }

    protected abstract void update(SMView view, float t);

    public Animator setInterpolator(Interpolator i) {
        mInterpolator = i;
        return this;
    }

    public Animator setDuration(long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
        mDuration = (float)durationMillis/1000.0f;
        return this;
    }

    public Animator setDuration(float sec) {
        mDuration = sec;
        return this;
    }


    public Animator setStartOffset(float sec) {
        mStartOffset = sec;
        return this;
    }

    public Animator setDelay(float sec) {
        mDelayTime = mDelayTimeSaved = sec;
        return this;
    }

    public Animator setRepeatMode(int repeatMode) {
        mRepeatMode = repeatMode;
        return this;
    }

    public Animator setRepeatCount(int repeatCount) {
        if (repeatCount < 0) {
            repeatCount = INFINITE;
        }
        mRepeatCount = repeatCount;
        return this;
    }
    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public SMView getView() {
        return mView;
    }

    public void setReverse() {
        mCycleFlip = true;
    }

    public float getStartTime() {
        return mStartTime;
    }

    public float getDuration() {
        return mDuration;
    }

    public float getElapsed() {
        return mElapsed;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getRepeatCount() {
        return mRepeatCount;
    }

    public boolean hasStarted() {
        return mStarted;
    }

    public boolean isReverse() {
        return mCycleFlip;
    }

    public boolean hasEnded() {
        return mEnded;
    }

    public Animator setRemoveSelf(boolean self) {
        mRemoveSelf = self;
        return this;
    }

    public Animator setView(SMView view) {
        mView = view;
        return this;
    }

    protected void onAnimationStart() {
        if (mListener != null) {
            mListener.onAnimationStart(this);
        }
    }

    protected void onAnimationRepeat() {
        if (mListener != null) {
            mListener.onAnimationRepeat(this);
        }
    }

    protected void onAnimationEnd() {
        if (mListener != null) {
            mListener.onAnimationEnd(this);
        }
        if (mRemoveSelf) {
            if (mView != null) {
                mView.removeAnimator(this);
            }
        }
        if (mNextAnimator != null) {
            mView.startAnimation(mNextAnimator);
        }
    }

    @Override
    public float getInterpolation(float input) {
        return (float)Math.sin(input*Math.PI/2);
    }

    public static final float interpolate(float from, float to, float t) {
        return from+(to-from)*t;
    }
}
