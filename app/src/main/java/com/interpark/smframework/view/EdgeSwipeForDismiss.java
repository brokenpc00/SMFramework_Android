package com.interpark.smframework.view;

import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.scroller.PageScroller;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class EdgeSwipeForDismiss extends EdgeSwipeLayer {
    public EdgeSwipeForDismiss(IDirector director) {
        super(director);
    }

    public static EdgeSwipeForDismiss create(IDirector director, int tag, float x, float y, float width, float height) {
        return create(director, tag, x, y, width, height, 0, 0);
    }

    public static EdgeSwipeForDismiss create(IDirector director, int tag, float x, float y, float width, float height, float anchorX, float anchorY) {
        EdgeSwipeForDismiss layer = new EdgeSwipeForDismiss(director);
        if (layer!=null) {
            layer.setAnchorPoint(new Vec2(anchorX, anchorY));
            layer.setContentSize(new Size(width, height));
            layer.setPosition(new Vec2(x, y));
            layer.init();
        }

        return layer;
    }

    @Override
    protected boolean init() {
        super.init();

        _scroller = new PageScroller(getDirector());
        _scrollEventTargeted = false;

        if (_scroller!=null) {
            _scroller.pageChangedCallback = new PageScroller.PAGE_CALLBACK() {
                @Override
                public void pageChangedCallback(int page) {
                    openStateChanged(page);
                }
            };


            return true;
        }
        return false;
    }


    public void back() { back(true); }
    public void back(boolean immediate) {
        if (immediate) {
            _scroller.setScrollPosition(_swipeSize);
        } else {
            scheduleUpdate();
            _fakeFlingDirection = 1;
        }

        if (_velocityTracker!=null) {
            _velocityTracker.clear();
        }
    }

    public void reset() {
        _scroller.setCurrentPage(1, true);
        _openState = 0;
        _position = 0;

        _inScrollEvent = false;
        _scrollEventTargeted = false;

        unscheduleUpdate();
    }

    public interface SWIPTE_DISMISS_UPDATE_CALLBCK {
        public void onSwipeUpdate(int a, float b);
    }
    public SWIPTE_DISMISS_UPDATE_CALLBCK _swipeUpdateCallback=null;


    @Override
    public void updateScrollPosition(float dt) {
        if (_fakeFlingDirection!=0) {
            float velocity = _fakeFlingDirection>0?-5000:+5000;
            _scroller.onTouchFling(velocity, _openState);
            _fakeFlingDirection = 0;
        }

        _scroller.update();
        _position = _scroller.getScrollPosition() - _swipeSize;

        if (_swipeUpdateCallback!=null) {
            _swipeUpdateCallback.onSwipeUpdate(_openState, -_position);
        }

    }

    @Override
    public int dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();

        if (_velocityTracker==null) {
            _velocityTracker = VelocityTracker.obtain();
        }

        int action = ev.getAction();
        Vec2 point = new Vec2(x, y);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            {
                _scrollEventTargeted = false;
                _inScrollEvent = false;
                _lastMotionX = x;
                _lastMotionY = y;
                _firstMotionTime = getDirector().getGlobalTime();

                if (_position < 1 && y < _edgeSize ) {
                    _scrollEventTargeted = true;
                }

                if (_scrollEventTargeted) {
                    _scroller.onTouchDown();
                    _velocityTracker.addMovement(ev);
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            {
                if (_scrollEventTargeted) {
                    if (_inScrollEvent) {
                        _inScrollEvent = false;

                        float vx = _velocityTracker.getXVelocity(0);
                        float vy = _velocityTracker.getYVelocity(0);


                        if (vx==0 && vy==0) {
                            float dt = getDirector().getGlobalTime() - _firstMotionTime;
                            if (dt>0) {
                                Vec2 p1 = _touchStartPosition;
                                Vec2 p2 = _touchCurrentPosition;
                                vx = (p2.x - p1.x) / dt;
                                vy = (p2.y - p1.y) / dt;
                            }
                        }

                        // 위아래로 내리므로 y로 비교
                        if (Math.abs(vy)>1000) {
                            _scroller.onTouchFling(vy, 1-_openState);
//                            _scroller.onTouchFling(-vy, 1 - _openState);
                        } else {
                            _scroller.onTouchUp();
                        }
                        scheduleUpdate();
                    } else {
                        _scroller.onTouchUp();
                        scheduleUpdate();
                    }
                }

                _scrollEventTargeted = false;
                _inScrollEvent = false;
                _velocityTracker.clear();;
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {
                if (_scrollEventTargeted) {
                    _velocityTracker.addMovement(ev);

                    float deltaY;

                    if (!_inScrollEvent) {
                        deltaY = y - _lastMotionY;
                    } else {
                        deltaY = point.y - _touchPrevPosition.y;
                    }

                    if (!_inScrollEvent) {
                        float ay = Math.abs(y - _lastMotionY);

                        // 첫번째 스크롤 이벤트에서만 체
                        if (ay > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true;
                        }

                        if (_inScrollEvent) {
                            if (_touchMotionTarget!=null) {
                                cancelTouchEvent(_touchMotionTarget, ev);
                                _touchMotionTarget = null;
                            }
                        }
                    }


                    if (_inScrollEvent) {
                        _scroller.onTouchScroll(+deltaY);
                        _lastMotionX = x;
                        _lastMotionY = y;
                        scheduleUpdate();
                    }
                }
            }
            break;
        }

        if (_inScrollEvent) {
            return TOUCH_INTERCEPT;
        }

        if (action==MotionEvent.ACTION_UP) {
            return TOUCH_FALSE;
        }


        return _scrollEventTargeted?TOUCH_TRUE:TOUCH_FALSE;
    }
}
