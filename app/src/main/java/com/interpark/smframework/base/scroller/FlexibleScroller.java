package com.interpark.smframework.base.scroller;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class FlexibleScroller extends SMScroller {
    public FlexibleScroller(IDirector director) {
        super(director);
        if (_controller==null) {
            _controller = new ScrollController(director);
        }
        reset();
    }

    @Override
    public boolean update() {
        boolean updated = _controller.update();
        updated |= onAutoScroll();

        _newPosition = decPrecesion(_controller.getPanY());

        SMView.InterpolateRet ret = SMView.smoothInterpolate(_position, _newPosition, 0.1f);
        updated |= ret.retB;

        _position = ret.retF;

        _scrollSpeed = Math.abs(_lastPosition - _position) * 60;

        _lastPosition = _position;

        if (!updated) {
            _state = STATE.STOP;
        } else {
            _state = STATE.SCROLL;
        }

        return updated;
    }

    @Override
    public void setWindowSize(final float windowSize) {
        super.setWindowSize(windowSize);
        _controller.setViewSize(windowSize);
    }

    @Override
    public void setScrollSize(final float scrollSize) {
        super.setScrollSize(scrollSize);
        _controller.setScrollSize(scrollSize);
    }

    @Override
    public void setScrollPosition(final float position, boolean immediate) {
        super.setScrollPosition(position, immediate);
        _controller.setPanY(position);
    }

    @Override
    public float getScrollPosition() {
        return _position;
    }

    @Override
    public void reset() {
        super.reset();
        if (_controller==null) {
            _controller = new ScrollController(_director);
        }
        _controller.reset();
        _position = 0;
        _newPosition = 0;
        _scrollSpeed = 0;
        _autoScroll = false;
    }

    @Override
    public void justAtLast() {
        _controller.stopIfExceedLimit();
    }

    @Override
    public void onTouchDown(final int unused) {
        _autoScroll = false;
        _controller.stopFling();
    }

    @Override
    public void onTouchUp(final int unused) {
        _controller.startFling(0);
    }

    @Override
    public void onTouchScroll(final float delta, final int unused) {
        _controller.pan(-delta);
    }

    @Override
    public void onTouchFling(final float velocity, final int unused) {
        _controller.startFling(-velocity);
    }

    public void scrollTo(float position) {
        scrollToWithDuration(position, -1);
    }

    public void scrollToWithDuration(float position, float duration) {
        if (position < _minPosition) {
            position = _minPosition;
        }

        if (Math.abs(position - _newPosition) < 1)
        return;

        onTouchDown();

        final float PIXELS_PER_SEC = 20000.0f;

        float dist = position - _newPosition;

        float dir = _SIGNUM(dist);

        if (duration <= 0) {
            _timeDuration = Math.min(1.5f, Math.max(0.15f, Math.abs(dist) / PIXELS_PER_SEC));
        } else {
            _timeDuration = duration;
        }

        _accelate = -dir*GRAVITY;

        // decelerate
        // duration 동안 감속
        _velocity = -_accelate * _timeDuration;


        float dist0 =  _velocity * _timeDuration + 0.5f * _accelate * _timeDuration * _timeDuration;
        float scale = dist / dist0;

        _velocity *= scale;
        _accelate *= scale;

        _startPos = _newPosition;
        _timeStart = _director.getGlobalTime();
        _autoScroll = true;
    }

    public boolean onAutoScroll() {
        if (!_autoScroll) {
            return false;
        }

        float globalTime = _director.getGlobalTime();
        float nowTime = globalTime - _timeStart;
        if(nowTime > _timeDuration) {
            nowTime = _timeDuration;
            _autoScroll = false;
        }

        float oldPosition = _newPosition;
        float distance =  _velocity * nowTime + 0.5f * _accelate * nowTime * nowTime;
        float newPosition = decPrecesion(_startPos + distance);
        float dir = _SIGNUM(_velocity);

        float maxScrollWindowSize = _windowSize * 0.9f;
        if (Math.abs(newPosition - oldPosition) > maxScrollWindowSize) {

            // 한꺼번에 확확 넘어가는걸 방지
            // 한 프레임에 스크롤이 화면의 90%가 넘지 않도록 계산
            // 2차 방정식 근의 공식 Ax^2+Bx+C=0.
            //  x=-b+or-sqrt(b^2-4*a*c)/2*a

            // x = now
            // a = 0.5*_accelate
            // b = -_velocity
            // c = newDistance
            float newPosition2 = oldPosition + dir * maxScrollWindowSize;
            float newDistance = newPosition2 - _startPos;

            float a = 0.5f*_accelate;
            float b = -_velocity;
            float c = newDistance;

            double discriminant = b*b-4*a*c;
            float newNowTime;
            boolean bUserMath = true;
            if (bUserMath) {
                if (discriminant > 0)
                {
                    newNowTime=((-b)+(float)Math.sqrt(discriminant))/(2*a);
                } else  if (discriminant==0) {
                    newNowTime=(-b)/(2*a);
                } else {
                    newNowTime=((-b)-(float)Math.sqrt(discriminant))/(2*a);
                }
            } else {
                // ???
                newNowTime = (float)(-b - dir*(float)Math.sqrt(b*b - 4*a*c)) / (2*a);
            }

            _timeStart = globalTime - newNowTime;
            newPosition = newPosition2;
            newPosition = decPrecesion(newPosition);
        }

        if (newPosition > _maxPosition) {
            _controller.setPanY(_maxPosition);
        } else if (newPosition < _minPosition) {
            _controller.setPanY(_minPosition);
        } else {
            _controller.setPanY(newPosition, true);
        }

        return true;
    }

    @Override
    public void setHangSize(final float size) {
        _controller.setHangSize(size);
    }


    protected ScrollController _controller = null;
    protected boolean _autoScroll;
}
