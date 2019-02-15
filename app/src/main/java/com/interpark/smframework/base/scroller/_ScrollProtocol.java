package com.interpark.smframework.base.scroller;

import android.view.VelocityTracker;

import com.interpark.smframework.util.Rect;

public interface _ScrollProtocol {
    public SMScroller getScroller();
    public boolean updateScrollInParentVisit(float deltaScroll);
    public void setScrollParent(_ScrollProtocol parent);
    public void notifyScrollUpdate();
    public void setInnerScrollMargin(final float margin);
    public void setMinScrollSize(final float minScrollSize);
    public void setTableRect(Rect tableRect);
    public void setScrollRect(Rect scrollRect);
    public void setBaseScrollPosition(float position);
    public float getBaseScrollPosition();
    public SMScroller _scroller = null;
    public VelocityTracker _velocityTracker = null;
    public _ScrollProtocol _scrollParent = null;
    public float _innerScrollMargin = 0;
    public float _minScrollSize = 0;
    public float _baseScrollPosition = 0;
    public boolean _inScrollEvent = false;
    public Rect _tableRect = null;
    public Rect _scrollRect = null;

}
