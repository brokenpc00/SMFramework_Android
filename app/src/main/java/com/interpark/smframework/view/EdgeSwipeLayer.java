package com.interpark.smframework.view;

import android.util.Log;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.scroller.PageScroller;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class EdgeSwipeLayer extends SMView {
    public EdgeSwipeLayer(IDirector director) {
        super(director);
    }

//    public static EdgeSwipeLayer create(IDirector director, int tag, float x, float y, float width, float height) {
//        return create(director, tag, x, y, width, height, 0, 0);
//    }
//
//    public static EdgeSwipeLayer create(IDirector director, int tag, float x, float y, float width, float height, float anchorX, float anchorY) {
//        EdgeSwipeLayer layer = new EdgeSwipeLayer(director);
//        if (layer!=null) {
//            layer.setAnchorPoint(new Vec2(anchorX, anchorY));
//            layer.setContentSize(new Size(width, height));
//            layer.setPosition(new Vec2(x, y));
//            layer.init();
//        }
//
//        return layer;
//    }

    protected int _fakeFlingDirection;
    protected int _openState = 0;
    protected float _swipeSize;
    protected float _edgeSize;
    protected float _position;
    protected float _firstMotionTime;
    protected boolean _inScrollEvent;
    protected float _lastMotionX;
    protected float _lastMotionY;
    protected float _lastScrollPosition;
    protected boolean _scrollEventTargeted = false;
    protected PageScroller _scroller;
    protected VelocityTracker _velocityTracker = null;

    public void setSwipeWidth(final float width) {
        _swipeSize = width;
        _scroller.setCellSize(_swipeSize);
        _scroller.setWindowSize(_swipeSize);
        _scroller.setScrollSize(_swipeSize*2);
    }

    public void setEdgeWidth(final float width) {
        _edgeSize = width;
    }

    @Override
    public void update(float dt) {
        updateScrollPosition(dt);
    }

    public void updateScrollPosition(float dt) {}

    public boolean isOpen() {return _openState==1;}

    public boolean inScrollEvent() {return _inScrollEvent;}

    public boolean isScrollTargeted() {return _scrollEventTargeted;}

    public boolean isScrollArea(final Vec2 worldPoint) {
        if (worldPoint.x < _edgeSize) {
            return true;
        }

        return false;
    }

    public void openStateChanged(final int openState) {
        _openState = openState;
    }
}
