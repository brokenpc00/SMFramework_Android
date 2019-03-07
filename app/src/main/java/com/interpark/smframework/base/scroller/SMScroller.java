package com.interpark.smframework.base.scroller;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.AppConst;

public class SMScroller {
    public enum STATE {
        STOP,
        SCROLL,
        FLING
    }

    public enum ScrollMode {
        BASIC,
        PAGER,
        ALIGNED,
    }

    public SMScroller(IDirector director) {
        _director = director;
        _scrollSpeed = 0;
        _cellSize = 0;
        _scrollMode = ScrollMode.BASIC;
        onAlignCallback = null;
        reset();
    }


    public void reset() {
        _state = STATE.STOP;
        _position = _newPosition = 0;
        _minPosition = 0;
        _maxPosition = 0;
        _hangSize = 0;

    }

    public boolean update() {
        boolean updated = false;

        updated |= runScroll();
        updated |= runFling();
        SMView.InterpolateRet ret = SMView.smoothInterpolate(_position, _newPosition-_hangSize, AppConst.Config.TOLERANCE_POSITION);
        updated |= ret.retB;
        _position = ret.retF;

        return updated;
    }

    protected STATE _state = STATE.STOP;

    public STATE getState() {return _state;}

    protected float _windowSize = 0.0f;
    protected float _maxPosition = 0.0f;
    protected float _minPosition = 0.0f;

    public void setWindowSize(final float windowSize) {
        _windowSize = windowSize;
        _maxPosition = windowSize;

        if (_windowSize <= 0) {
            _windowSize = 1;
        }
    }

    protected float _timeStart = 0.0f;
    protected float _timeDuration = 0.0f;


    public float getWindowSize() {return _windowSize;}

    public void setScrollSize(final float scrollSize) {
        if (scrollSize>_windowSize) {
            _maxPosition = _minPosition + scrollSize - _windowSize + _hangSize;
        } else {
            _maxPosition = _minPosition;
        }
    }

    public float getScrollSize() {return _maxPosition - _minPosition;}

    public void setScrollPosition(final float position) {
        setScrollPosition(position, true);
    }

    public void setScrollPosition(final float position, boolean immediate) {
        if (immediate) {
            _newPosition = _position = position;
        } else {
            _newPosition = position;
        }
        _state = STATE.STOP;
    }

    protected float _position = 0.0f;
    public float getScrollPosition() {return _position - _minPosition;}

    protected float _newPosition = 0.0f;
    public float getNewScrollPosition() {return _newPosition-_minPosition;}

    protected float _lastPosition = 0.0f;

    protected float _startPos = 0.0f;
    public void setStartPosition(final float startPos) {_startPos += startPos;}
    protected float _stopPos = 0.0f;

    public float getStartPosition() {return _startPos;}

    public static float SCROLL_TIME = 0.2f;
    public static float GRAVITY = 9.8f*1000;
    public static int _SIGNUM(float x) {
        return (((x) > 0) ? 1 : (((x) < 0) ? -1 : 0));
    }
    public static float SPEED_LOW = 100.0f;

    public boolean isTouchable() {
        return (_state == STATE.STOP) || (_scrollSpeed < SPEED_LOW);
    }

    public static float decPrecesion(float value) {
        return decPrecesion(value, false);
    }
    public static float decPrecesion(float value, boolean isNew) {
        if (isNew) {
            return ((int)(Math.round(value * 100)))/100.0f;
        } else {
            return value;
        }
    }

    protected float _scrollSpeed = 0.0f;
    public float getScrollSpeed() {return _scrollSpeed;}

    public void justAtLast() {}

    public void onTouchDown() {
        onTouchDown(0);
    }
    public void onTouchDown(final int param) {}

    public void onTouchUp() {
        onTouchUp(0);
    }
    public void onTouchUp(final int param) {}

    public void onTouchScroll(final float delta) {
        onTouchScroll(delta, 0);
    }
    public void onTouchScroll(final float delta, final int param) {}

    protected float _velocity = 0.0f;
    protected float  _accelate = 0.0f;
    protected float  _touchDistance = 0.0f;


    public void onTouchFling(final float velocity) {
        onTouchFling(velocity, 0);
    }
    public void onTouchFling(final float velocity, final int param) {}

    protected float _hangSize = 0.0f;
    public void setHangSize(final float size) {_hangSize = size;}

    public void scrollBy(float distance) {}

    protected ScrollMode _scrollMode = ScrollMode.BASIC;
    public void setScrollMode(ScrollMode mode) {_scrollMode = mode;}

    protected float _cellSize = 0.0f;
    public void setCellSize(float cellSize) {_cellSize = cellSize;}
    public float getCellSize() {return _cellSize;}

    public interface ALIGN_CALLBACK {
        public void onAlignCallback(boolean aligned);
    }
    public ALIGN_CALLBACK onAlignCallback = null;

    public int getMaxPageNo() {
        int maxPageNo;

        maxPageNo = (int)Math.ceil(getScrollSize()/_cellSize);

        return maxPageNo;
    }

    protected boolean runFling() {return false;}
    protected boolean runScroll() {return false;}


    public void clone(SMScroller scroller) {
        this._state = scroller._state;
        this._position =  scroller._position;
        this._newPosition = scroller._newPosition;

        this._windowSize = scroller._windowSize;
        this._minPosition = scroller._minPosition;
        this._maxPosition = scroller._maxPosition;

        this._timeStart = scroller._timeStart;
        this._timeDuration = scroller._timeDuration;

        this._startPos = scroller._startPos;
        this._velocity = scroller._velocity;
        this._accelate = scroller._accelate;
        this._touchDistance = scroller._touchDistance;

        this._hangSize = scroller._hangSize;
    }


    protected IDirector _director;

}
