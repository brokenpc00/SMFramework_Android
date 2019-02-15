package com.interpark.smframework.base.scroller;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import static com.interpark.smframework.util.tweenfunc.cubicEaseOut;

public class InfinityScroller extends SMScroller {
    public InfinityScroller(IDirector director) {
        super(director);
    }

    @Override
    public void onTouchDown(final int unused) {
        _state = STATE.STOP;
        _startPos = _newPosition;
        _touchDistance = 0;
    }

    @Override
    public void onTouchUp(final int unused) {
        _state = STATE.STOP;

        if (_scrollMode != ScrollMode.BASIC) {
            // 멈출때 가장 근접한 position으로
            float r = _newPosition % _cellSize;
            float distance;

            if (Math.abs(r) <= _cellSize/2) {
                distance = -r;
            } else {
                if (r >= 0) distance = _cellSize - r;
                else distance = -(_cellSize + r);
            }
            if (distance == 0) {
                if (onAlignCallback!=null) {
                    onAlignCallback.onFunc(true);
                }
                return;
            }

            _state = STATE.FLING;
            float dir = _SIGNUM(distance);
            _accelate = +dir*GRAVITY;
            _startPos = _newPosition;
            _timeDuration = Math.abs(distance) * 0.25f / 1000;

            _startPos = _newPosition;
            _velocity = distance / _timeDuration - 0.5f * _accelate * _timeDuration;
            _stopPos = _startPos + distance;
        }
    }

    @Override
    public void onTouchScroll(final float delta, final int unused) {
        _touchDistance -= delta;
        _newPosition = decPrecesion(_startPos + _touchDistance);

        if (_scrollMode != ScrollMode.BASIC) {
            if (onAlignCallback!=null) {
                onAlignCallback.onFunc(false);
            }
        }

    }

    @Override
    public void onTouchFling(final float velocity, final int unused) {
        float dir = _SIGNUM(velocity);
        float v0 = Math.abs(velocity);

        final float maxVelocity = 25000;
        v0 = Math.min(maxVelocity, v0);

        // 멈추는 예정 시간
        _timeDuration = v0 / GRAVITY;

        _state = STATE.FLING;
        _startPos = _newPosition;

        _velocity = -dir*v0;
        _accelate = +dir*GRAVITY;

        _timeStart = _director.getGlobalTime();
        float distance = _velocity * _timeDuration + 0.5f * _accelate * _timeDuration * _timeDuration;

        if (_scrollMode == ScrollMode.PAGER) {
            if (Math.abs(distance) > _cellSize) {
                distance = (distance >= 0 ? 1 : -1) * _cellSize;
            }
        }

        _stopPos = _startPos + distance;

        if (_scrollMode != ScrollMode.BASIC) {
            // 멈출때 가까운 position
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

    public void scrollByWithDuration(float distance, float duration) {
        _state = STATE.SCROLL;
        _stopPos = _startPos + distance;

        _timeStart = _director.getGlobalTime();
        _timeDuration = duration;

        if (_scrollMode != ScrollMode.BASIC) {
            if (onAlignCallback!=null) {
                onAlignCallback.onFunc(false);
            }
        }
    }

    @Override
    protected boolean runFling() {
        if (_state != STATE.FLING) {
            return false;
        }

        float globalTime = _director.getGlobalTime();
        float nowTime = globalTime - _timeStart;
        if(nowTime > _timeDuration) {
            _newPosition = _stopPos;
            _state = STATE.STOP;
            nowTime = _timeDuration;
            // STOP
            if (_scrollMode != ScrollMode.BASIC) {
                if (onAlignCallback!=null) {
                    onAlignCallback.onFunc(true);
                }
            }
            return false;
        }

        float distance = _velocity * nowTime + 0.5f * _accelate * nowTime * nowTime;
        _newPosition = decPrecesion(_startPos + distance);

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
        } else {
            _state = STATE.STOP;
            _newPosition = _stopPos;

            if (_scrollMode != ScrollMode.BASIC) {
                if (onAlignCallback!=null) {
                    onAlignCallback.onFunc(true);
                }
            }
            return false;
        }

        return true;
    }


}
