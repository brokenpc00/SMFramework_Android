package com.interpark.smframework.view;

import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.scroller.PageScroller;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class EdgeSwipeLayerForPushBack extends EdgeSwipeLayer {
    public EdgeSwipeLayerForPushBack(IDirector director) {
        super(director);
    }

    public static EdgeSwipeLayerForPushBack create(IDirector director, int tag, float x, float y, float width, float height) {
        return create(director, tag, x, y, width, height, 0, 0);
    }

    public static EdgeSwipeLayerForPushBack create(IDirector director, int tag, float x, float y, float width, float height, float anchorX, float anchorY) {
        EdgeSwipeLayerForPushBack layer = new EdgeSwipeLayerForPushBack(director);
        if (layer!=null) {
            layer.setAnchorPoint(anchorX, anchorY);
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
                public void onFunc(int page) {
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

    public interface SWIPTE_BACK_UPDATE_CALLBCK {
        public void Func(int a, float b);
    }
    public SWIPTE_BACK_UPDATE_CALLBCK _swipeUpdateCallback=null;


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
            _swipeUpdateCallback.Func(_openState, -_position);
        }
    }

//    public int SMViewDispatchTouchEvent (MotionEvent e) {
//        return super.dispatchTouchEvent(e);
//    }

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

                if (((int)Math.abs(_position)) <= 1 && x < _edgeSize) {
                    _scrollEventTargeted = true;
                } else {
                    _scrollEventTargeted = false;
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
                                Vec2 p1 = new Vec2(_touchStartPosition);
                                Vec2 p2 = new Vec2(_touchCurrentPosition);

                                vx = (p2.x-p1.x) / dt;
                                vy = (p2.y-p1.y) / dt;
                            }
                        }

                        if (Math.abs(vx)>1000) {
                            _scroller.onTouchFling(vx, 1-_openState);
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
                _velocityTracker.clear();
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {
                if (_scrollEventTargeted) {

                    _velocityTracker.addMovement(ev);
                    float deltaX;

                    if (!_inScrollEvent) {
                        deltaX = x - _lastMotionX;
                    } else {
                        deltaX = point.x - _touchPrevPosition.x;
                    }

                    if (!_inScrollEvent) {
                        float ax = Math.abs(x - _lastMotionX);

                        if (ax > AppConst.Config.SCROLL_TOLERANCE) { // 10보다움직였으면
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
                        _scroller.onTouchScroll(+deltaX);
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
