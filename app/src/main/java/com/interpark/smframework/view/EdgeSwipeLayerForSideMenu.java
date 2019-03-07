package com.interpark.smframework.view;

import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.scroller.PageScroller;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class EdgeSwipeLayerForSideMenu extends EdgeSwipeLayer {
    public EdgeSwipeLayerForSideMenu(IDirector director) {
        super(director);
    }

    public static EdgeSwipeLayerForSideMenu create(IDirector director, int tag, float x, float y, float width, float height) {
        return create(director, tag, x, y, width, height, 0, 0);
    }

    public static EdgeSwipeLayerForSideMenu create(IDirector director, int tag, float x, float y, float width, float height, float anchorX, float anchorY) {
        EdgeSwipeLayerForSideMenu layer = new EdgeSwipeLayerForSideMenu(director);
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

        if (_scroller!=null) {
            _scroller.setBounceBackEnable(false);
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


    @Override
    public void setSwipeWidth(final float width) {
        super.setSwipeWidth(width);

        _scroller.setScrollPosition(_swipeSize);
        _openState = 0;
    }

    public void open() { open(true); }
    public void open(boolean immediate) {
        if (immediate) {
            _scroller.setScrollPosition(0);
            _openState = 0;
        } else {
            scheduleUpdate();
            // ToDo. 좌측메뉴... 우측일경우 1
            _fakeFlingDirection = -1;
//            scheduleUpdate();
        }

        if (_velocityTracker!=null) {
            _velocityTracker.clear();;
        }
    }

    public void close() { close(true); }
    public void close(boolean immediate) {
        if (immediate) {
            _scroller.setScrollPosition(_swipeSize);
            _openState = 1;
        } else {
            scheduleUpdate();
            _fakeFlingDirection = +1;
        }

        if (_velocityTracker!=null) {
            _velocityTracker.clear();
        }
    }

    @Override
    public void updateScrollPosition(float dt) {
        if (_fakeFlingDirection!=0) {
            float velocity = _fakeFlingDirection>0?-5000:+5000;
            _scroller.onTouchFling(velocity, _openState);
            _fakeFlingDirection = 0;
        }

        _scroller.update();
        float position = _scroller.getScrollPosition();
        if (position!=_position) {
            _position = position;
            _director.setSideMenuOpenPosition(_swipeSize-_position);
        }
    }

    @Override
    public boolean isScrollArea(final Vec2 worldPoint) {
        if (_openState==0) {
            return true;
        }

        return super.isScrollArea(worldPoint);
    }

    public void closeComplete() {
        unscheduleUpdate();
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

                IDirector.SIDE_MENU_STATE state = _director.getSideMenuState();
                if (state==IDirector.SIDE_MENU_STATE.CLOSE) {
                    if (x<_edgeSize) {
                        _scrollEventTargeted = true;
                    }
                } else {
                    if (x>_swipeSize-_position) {
                        _scrollEventTargeted = true;
                    }
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

                        float vx, vy;
                        vx = _velocityTracker.getXVelocity(0);

                        if (Math.abs(vx) > AppConst.Config.MIN_VELOCITY) {
                            _scroller.onTouchFling(vx, _openState);
                        } else {
                            _scroller.onTouchUp();
                        }
                        scheduleUpdate();
                    } else {
                        IDirector.SIDE_MENU_STATE state = _director.getSideMenuState();
                        if (state!=IDirector.SIDE_MENU_STATE.CLOSE) {
                            _scroller.onTouchUp();
                        }
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
                    float deltaX;

                    if (!_inScrollEvent) {
                        deltaX = x - _lastMotionX;
                    } else {
                        deltaX = point.x - _touchPrevPosition.x;
                    }

                    if (!_inScrollEvent) {
                        float ax = Math.abs(x - _lastMotionX);

                        if (ax > AppConst.Config.SCROLL_TOLERANCE) {
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
