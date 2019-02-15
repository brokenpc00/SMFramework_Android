package com.interpark.smframework.base.scroller;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import static com.interpark.smframework.util.tweenfunc.cubicEaseOut;

public class FinityScroller extends FlexibleScroller {
    public FinityScroller(IDirector director) {
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
        boolean updated = _controller.update();
        updated |= runScroll();
        updated |= runFling();

        _newPosition = decPrecesion(_controller.getPanY(), true);

        SMView.InterpolateRet ret = SMView.smoothInterpolate(_position, _newPosition, 0.1f);
        updated |= ret.retB;
        _position = ret.retF;

        _scrollSpeed = Math.abs(_lastPosition - _position) * 60;

        _lastPosition = _position;

        return updated;
    }

    @Override
    public void onTouchUp(int unused) {
        float page = _newPosition / _cellSize;
        int maxPageNo = getMaxPageNo();

        if (page < 0) {
            page = 0;
        } else if (page > maxPageNo) {
            page = maxPageNo;
        } else {
            int iPage = (int)Math.floor(page);
            float offset = page - iPage;

            if (offset <= 0.5f) {
                page = iPage;
            } else {
                page = iPage + 1;
            }
        }

        _startPos = _newPosition;
        _stopPos = page * _cellSize;

        if ((_startPos <= 0 && page == 0) || (_startPos >= _cellSize * maxPageNo && page == maxPageNo) || _startPos == _stopPos) {
            _controller.startFling(0);

            if (onAlignCallback!=null) {
                onAlignCallback.onFunc(true);
            }

            return;
        }

        _state = STATE.SCROLL;

        _timeStart = _director.getGlobalTime();
        float distance = Math.abs(_startPos - _stopPos);
        _timeDuration = 0.05f +  0.35f * (1.0f - distance / _cellSize);
    }

    @Override
    public void onTouchFling(float velocity, int unused) {
        float dir = _SIGNUM(velocity);
        float v0 = Math.abs(velocity);

        final float maxVelocity = 25000;
        v0 = Math.min(maxVelocity, v0);

        _startPos = _newPosition;

        // 멈추는 예정 시간
        _timeDuration = v0 / GRAVITY;
        _velocity = -dir*v0;
        _accelate = +dir*GRAVITY;
        _timeStart = _director.getGlobalTime();

        float distance = _velocity * _timeDuration + 0.5f * _accelate * _timeDuration * _timeDuration;

        if (_startPos + distance < _minPosition) {
            if (_startPos <= 0) {
                _state = STATE.STOP;
                _controller.startFling(0);
                if (onAlignCallback!=null) {
                    onAlignCallback.onFunc(true);
                }
                return;
            }
            distance = _minPosition - _startPos;

            _state = STATE.SCROLL;
            _stopPos = _minPosition;
            _timeStart = _director.getGlobalTime();
            _timeDuration = 0.25f;
            return;
        } else if (_startPos + distance > _maxPosition) {
            if (_startPos >= _maxPosition) {
                _state = STATE.STOP;
                _controller.startFling(0);
                if (onAlignCallback!=null) {
                    onAlignCallback.onFunc(true);
                }
                return;
            }
            distance = _maxPosition - _startPos;

            _state = STATE.SCROLL;
            _stopPos = _maxPosition;
            _timeStart = _director.getGlobalTime();
            _timeDuration = 0.25f;
            return;
        }

        _state = STATE.FLING;
        _stopPos = _startPos + distance;

        if (_scrollMode != ScrollMode.BASIC) {
            // 멈출때 가까운 Position
            float r = _stopPos % _cellSize;
            if (Math.abs(r) <= _cellSize/2) {
                if (r >= 0) distance -= r;
                else distance += - r;
            } else {
                if (r >= 0) distance += _cellSize - r;
                else distance -= _cellSize + r;
            }

            _velocity = distance / _timeDuration - 0.5f * _accelate * _timeDuration;
            _stopPos = _startPos + distance;
        }
    }

    @Override
    public void setScrollSize(float scrollSize) {
        _maxPosition = _minPosition + scrollSize - _cellSize;
        _controller.setScrollSize(scrollSize);
        _controller.setViewSize(_cellSize);
    }

    @Override
    public void setWindowSize(float windowSize) {
        super.setWindowSize(windowSize);
        _controller.setViewSize(_cellSize);
    }

    @Override
    protected boolean runFling() {
        if (_state != STATE.FLING) {
            return false;
        }

        float globalTime = _director.getGlobalTime();
        float nowTime = globalTime - _timeStart;
        if(nowTime > _timeDuration) {
            _state = STATE.STOP;

            _newPosition = _stopPos;
            _controller.setPanY(_stopPos);
            // STOP
            if (onAlignCallback!=null) {
                onAlignCallback.onFunc(true);
            }
            return false;
        }

        float distance = _velocity * nowTime + 0.5f * _accelate * nowTime * nowTime;
        _newPosition = decPrecesion(_startPos + distance);
        _controller.setPanY(_newPosition);
        return true;
    }

    @Override
    protected boolean runScroll() {
        if (_state != STATE.SCROLL) {
            return false;
        }

        float dt = _director.getGlobalTime()-_timeStart;
        float t = (float)dt/_timeDuration;

        if (t < 1) {
            t = cubicEaseOut(t);
            float interpolate = _startPos + (_stopPos - _startPos) * t;
            _newPosition = decPrecesion(interpolate);
            _controller.setPanY(_newPosition);

        } else {
            _state = STATE.STOP;
            _newPosition = _stopPos;
            _controller.setPanY(_stopPos);

            if (onAlignCallback!=null) {
                onAlignCallback.onFunc(true);
            }
            return false;
        }

        return true;
    }
}
