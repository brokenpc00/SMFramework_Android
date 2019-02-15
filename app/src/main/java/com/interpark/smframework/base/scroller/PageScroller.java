package com.interpark.smframework.base.scroller;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import static com.interpark.smframework.util.tweenfunc.M_PI_2;

public class PageScroller extends FlexibleScroller {
    public PageScroller(IDirector director) {
        super(director);
    }

    @Override
    public void reset() {
        super.reset();
        _controller.reset();
        _position = 0;
        _newPosition = 0;
    }

    @Override
    public boolean update() {
        boolean updagted = _controller.update();
        updagted |= runScroll();

        _newPosition = decPrecesion(_controller.getPanY(), true);

        if (!_bounceBackEnabled) {
            if (_newPosition<0) {
                _newPosition = 0;
                _controller.setPanY(0);
            } else if (_newPosition>getScrollSize()) {
                _newPosition = getScrollSize();
                _controller.setPanY(getScrollSize());
            }
        }

        SMView.InterpolateRet ret = SMView.smoothInterpolate(_position, _newPosition, 0.1f);
        updagted |= ret.retB;
        _position = ret.retF;

        _scrollSpeed = Math.abs(_lastPosition - _position) * 60;

        _lastPosition = _position;

        return updagted;
    }

    public void onTouchUp(final int unused) {
        float page = _newPosition / _cellSize;

        int maxPageNo = getMaxPageNo();
        if (page<0) {
            page = 0;
        } else if (page>maxPageNo) {
            page = maxPageNo;
        } else {
            int ipage = (int)Math.floor(page);
            float offset = page - ipage;

            if (offset<=0.5f) {
                page = ipage;
            } else {
                page = ipage + 1;
            }
        }

        _startPos = _newPosition;
        _stopPos = page * _cellSize;

        if ((_startPos <= 0 && page == 0) || (_startPos >= _cellSize * maxPageNo && page == maxPageNo) || _startPos == _stopPos) {
            // 첫페이지 또는 마지막 페이지 초기화
            _controller.startFling(0);

            if (pageChangedCallback!=null) {
                pageChangedCallback.onFunc((int)page);
            }

            return;
        }

        // 페이지 스크롤
        _state = STATE.SCROLL;

        _timeStart = _director.getGlobalTime();
        float distance = Math.abs(_startPos - _stopPos);
        _timeDuration = 0.05f +  0.15f * (1.0f - distance / _cellSize);
    }

    @Override
    public void onTouchFling(final float velocity, final int currentPage) {
        float v = velocity;
        final float maxVelocity = 15000;

        if (Math.abs(velocity) > maxVelocity) {
            v = _SIGNUM(v) * maxVelocity;
        }

        float position = _newPosition;
        if ((int)position < (int)_minPosition || (int)position > (int)_maxPosition) {
            onTouchUp();
            return;
        }

        int maxPageNo = getMaxPageNo();

        int page;
        if (v < 0) {
            page = currentPage + 1;
        } else {
            page = currentPage - 1;
        }

        if (page < 0) {
            page = 0;
        } else if (page > maxPageNo) {
            page = maxPageNo;
        }

        _startPos = _newPosition;
        _stopPos = page * _cellSize;


        _state = STATE.SCROLL;

        _timeStart = _director.getGlobalTime();

        _timeDuration = 0.05f +  0.15f * (1.0f + Math.abs(v) / maxVelocity);
    }

    @Override
    public boolean runScroll() {
        if (_state != STATE.SCROLL) {
            return false;
        }

        float dt = _director.getGlobalTime()-_timeStart;
        float rt = (float)dt/_timeDuration;

        if (rt < 1) {
            float f = 1 - (float)Math.sin(rt * M_PI_2);
            float newPosition = decPrecesion(_stopPos + f * (_startPos - _stopPos), true);
            _controller.setPanY(newPosition);
        } else {
            _state = STATE.STOP;
            _controller.setPanY(_stopPos);

            if (pageChangedCallback!=null) {
                float value = (float)Math.floor(_startPos/_cellSize);
                pageChangedCallback.onFunc((int)value);
            }
        }

        return true;
    }

    public interface PAGE_CALLBACK {
        public void onFunc(final int page);
    }
    public PAGE_CALLBACK pageChangedCallback = null;

    public void setCurrentPage(final int page) {
        setCurrentPage(page, true);
    }
    public void setCurrentPage(final int page, boolean immediate) {
        _newPosition = _cellSize * page;
        _controller.setPanY(_newPosition);

        if (immediate) {
            _position = _newPosition;
        }
    }

    public void setBounceBackEnable(boolean enable) {
        _bounceBackEnabled = enable;
    }
    private boolean _bounceBackEnabled = true;
}
