package com.interpark.smframework.view;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.scroller.PageScroller;
import com.interpark.smframework.base.scroller.SMScroller;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.Size;

public class SMPageView extends SMTableView implements PageScroller.PAGE_CALLBACK {
    public SMPageView(IDirector director) {
        super(director);
    }

    public static SMPageView create(IDirector director, Orientation orientation, float x, float y, float width, float height) {
        return create(director, orientation, x, y, width, height, 0, 0);
    }
    public static SMPageView create(IDirector director, Orientation orientation, float x, float y, float width, float height, float anchorX, float anchorY) {
        SMPageView view = new SMPageView(director);
        view.initWithOrientAndSize(orientation, orientation==Orientation.HORIZONTAL?width:height);
        view.setContentSize(new Size(width, height));
        view.setPosition(x, y);
        view.setAnchorPoint(anchorX, anchorY);

        return view;
    }

    public void initFixedPages(final int numOfPages, final float pageSize) {
        initFixedPages(numOfPages, pageSize, 0);
    }
    public void initFixedPages(final int numOfPages, final float pageSize, final int initPage) {
        initFixedColumnInfo(numOfPages, pageSize, initPage);
    }

    public interface OnPageScrollCallback {
        public void onPageScrollCallback(SMPageView view, float postion, float distance);
    }
    private OnPageScrollCallback _onPageScrollCallback = null;
    public void setOnPageScrollCallback(OnPageScrollCallback callback) {_onPageScrollCallback = callback;}

    public interface OnPageChangedCallback {
        public void onPageChangedCallback(SMPageView view,int page);
    }
    private OnPageChangedCallback _onPageChangedCallback = null;
    public void setOnPageChangedCallback(OnPageChangedCallback callback) {_onPageChangedCallback = callback;}

    @Override
    public void scrollFling(final float velocity) {
        int movePage = (int)(_scroller.getScrollPosition()/_scroller.getWindowSize());

        if (velocity>0) {
            if (movePage==_currentPage) {
                _scroller.onTouchFling(velocity, _currentPage+1);
                return;
            }
        } else {
            if (movePage==_currentPage-1) {
                _scroller.onTouchFling(velocity, _currentPage-1);
                return;
            }
        }

        _scroller.onTouchFling(velocity, _currentPage);
    }

    public void goPage(int page) {
        goPage(page, false);
    }
    public void goPage(int page, boolean immediate) {
        assert (page>=0 && page<=_pageScroller.getMaxPageNo());

        PageScroller scroller = (PageScroller)getScroller();
        if (immediate) {
            fakeSetCurrentPage(page);
        } else {
            jumpPage(page, scroller.getCellSize());
        }
    }

    public int getCurrentPage() {
        return _currentPage;
    }

    public void fakeSetCurrentPage(int page) {
        setScrollPosition(page*_scroller.getCellSize());
        _currentPage = page;
    }

    protected boolean initWithOrientAndSize(Orientation orient, float pageSize) {
        if (super.initWithOrientAndColumns(orient, 1)) {
            super.hintFixedCellSize(pageSize);

            _pageScroller.setCellSize(pageSize);
            _pageScroller.pageChangedCallback = this;

            return true;
        }

        return false;
    }

    @Override
    public void pageChangedCallback(final int page) {
        _currentPage = page;
        if (_onPageChangedCallback!=null) {
            _onPageChangedCallback.onPageChangedCallback(this, _currentPage);
        }
    }

    @Override
    protected SMScroller initScroller() {
        _scroller = _pageScroller = new PageScroller(getDirector());
        return _scroller;
    }

    @Override
    protected void onScrollChanged(float position, float distance) {
        if (_onPageScrollCallback!=null) {
            _onPageScrollCallback.onPageScrollCallback(this, position/_scroller.getCellSize(), distance);
        }
    }

    private PageScroller _pageScroller = null;

    @Override
    public void scrollTo(float position) {}

    @Override
    public void scrollBy(float ofsset) {}

    @Override
    public boolean resizeRowForCell(SMView cell, float newSize, float duration, float delay) {return false;}

    @Override
    public boolean resizeRowForIndexPath(IndexPath indexPath, float newSize, float duration, float delay) {return false;}

    @Override
    public void setScrollMarginSize(final float topMargin, final float bottomMargin) {}

    @Override
    public void hintFixedCellSize(final float cellSize) {}

}
