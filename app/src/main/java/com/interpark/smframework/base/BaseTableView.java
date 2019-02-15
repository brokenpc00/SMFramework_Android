package com.interpark.smframework.base;

import android.telephony.cdma.CdmaCellLocation;
import android.util.Log;
import android.util.SparseArray;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.scroller.SMScroller;
import com.interpark.smframework.base.scroller._ScrollProtocol;
import com.interpark.smframework.base.types.Ref;
import com.interpark.smframework.util.Rect;

import org.apache.http.cookie.SM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BaseTableView extends SMView implements _ScrollProtocol {
    // _ScrollProtocol interface
    public SMScroller getScroller() {
        return _scroller;
    }

    public boolean updateScrollInParentVisit(float deltaScroll) {return false;}

    public void setScrollParent(_ScrollProtocol parent) {
        _scrollParent = parent;
    }

    public void notifyScrollUpdate() {}

    public void setInnerScrollMargin(final float margin) {_innerScrollMargin = margin;}

    public void setMinScrollSize(final float minScrollSize) {_minScrollSize = minScrollSize;}

    public void setTableRect(Rect tableRect) {
        if (tableRect!=null) {
            if (_tableRect!=null) {
                _tableRect.setRect(tableRect);
            } else {
                _tableRect = new Rect(tableRect);
            }
        } else {
            _tableRect = null;
        }
    }

    public void setScrollRect(Rect scrollRect) {
        if (scrollRect!=null) {
            if (_scrollRect!=null) {
                _scrollRect.setRect(scrollRect);
            } else {
                _scrollRect = new Rect(scrollRect);
            }
        } else {
            _scrollRect = null;
        }
    }

    public void setBaseScrollPosition(float position) {_baseScrollPosition = position;}

    public float getBaseScrollPosition() {return _baseScrollPosition;}

    public SMScroller _scroller = null;
    public VelocityTracker _velocityTracker = null;
    public _ScrollProtocol _scrollParent = null;
    public float _innerScrollMargin = 0;
    public float _minScrollSize = 0;
    public float _baseScrollPosition = 0;
    public boolean _inScrollEvent = false;
    public Rect _tableRect = null;
    public Rect _scrollRect = null;



    // BaseTableView

    public BaseTableView(IDirector director) {
        super(director);
    }

    @Override
    public void removeAllChildrenWithCleanup(boolean cleanup) {
        // contents 들만 남기고 다 지움

        // contents 떼어냄...
        if (_contentView!=null) {
            for (int i=0; i<_numContainer; i++) {
                super.removeChild(_contentView[i]);
            }
        }

        // 나머지 다 날리고
        super.removeAllChildrenWithCleanup(cleanup);

        // contents들을 다시 붙임
        if (_contentView!=null) {
            for (int i=0; i<_numContainer; i++) {
                super.addChild(_contentView[i]);
            }
        }
    }

    public long getContainerCount() {return _numContainer;}

    public void setHeaderView(SMView headerView) {
        if (_headerView!=null && _headerView!=headerView) {
            // exist header view
            // remove and release
            super.removeChild(_headerView, true);
            _headerView = null;
        }

        _headerView = headerView;
        _isHeaderInList = false;
    }

    public void setFooterView(SMView footerView) {
        if (_footerView!=null && _footerView!=footerView) {
            // exist footer view
            // remove and release
            super.removeChild(_footerView, true);
            _footerView = null;
        }

        _footerView = footerView;
        _isFooterInList = false;
    }

    public void stop() {
        if (_scroller!=null) {
            // last click;
            _scroller.onTouchDown();
            _scroller.onTouchUp();
        }
    }

    public ArrayList<SMView> getColumnChildren(final int column) {
        assert (column>=0 && column<_numContainer);

        return _contentView[column].getChildren();
    }

    public boolean isHeaderInList() { return _isHeaderInList; }
    public boolean isFooterInList() { return _isFooterInList; }
    public void setScrollLock(boolean bLock) {_lockScroll = bLock;};
    public SMView getHeaderView(){return _headerView;};


    protected boolean initWithContainer(final int numContainer) {
        assert (numContainer>0);

        _numContainer = numContainer;

        _contentView = new SMView[(int)_numContainer];
        for (int i=0; i<_numContainer; i++) {
            _contentView[i] = SMView.create(getDirector());
            super.addChild(_contentView[i]);
        }

        if (_headerView != null && _isHeaderInList) {
            super.removeChild(_headerView, true);
            _isHeaderInList = false;
        }

        if (_footerView != null && _isFooterInList) {
            super.removeChild(_footerView, true);
        }

        removeAllChildrenWithCleanup(true);

        return true;
    }

    protected void removeChildAndHold(final long columnNum, final int tag, SMView child) {
        removeChildAndHold(columnNum, tag, child, true);
    }
    protected void removeChildAndHold(final long columnNum, final int tag, SMView child, final boolean cleanup) {
        getHolder().insert(child.hashCode(), _contentView[(int)columnNum], child);
    }

    protected SMView findFromHolder(final int hashCode) {
        if (_holder!=null) {
            return getHolder().find(hashCode);
        }

        return null;
    }

    protected void eraseFromHolder(final int hashCode) {
        if (_holder!=null) {
            getHolder().erase(hashCode);
        }
    }

    protected void clearInstantHolder() {
        if (_holder!=null) {
            _holder.clear();
        }
    }

    protected void addChild(final long columnNum, SMView child) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].addChild(child);
    }
    protected void addChild(final long columnNum, SMView child, int localZOrder) {
        assert (columnNum>=0);

        _contentView[(int)columnNum].addChild(child, localZOrder);
    }
    protected void addChild(final long columnNum, SMView child, int localZOrder, int tag) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].addChild(child, localZOrder, tag);
    }
    protected void addChild(final long columnNum, SMView child, int localZOrder, final String name) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].addChild(child, localZOrder, name);
    }

    protected void removeChild(final long columnNum, SMView child) {
        removeChild(columnNum, child, true);
    }
    protected void removeChild(final long columnNum, SMView child, boolean cleanup) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].removeChild(child, cleanup);
    }

    protected void removeChildByTag(final long columnNum, int tag) {
        removeChildByTag(columnNum, tag, true);
    }
    protected void removeChildByTag(final long columnNum, int tag, boolean cleanup) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].removeChildByTag(tag, cleanup);
    }

    protected void removeChildByName(final long columnNum, final String name) {
        removeChildByName(columnNum, name, true);
    }
    protected void removeChildByName(final long columnNum, final String name, boolean cleanup) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].removeChildByName(name, cleanup);
    }

    protected void sortAllChildren(final long columnNum) {
        assert (columnNum>=0 && columnNum<_numContainer);

        _contentView[(int)columnNum].sortAllChildren();
    }

    protected int getChildrenCount(final long columnNum) {
        assert (columnNum>=0 && columnNum<_numContainer);

        return _contentView[(int)columnNum].getChildrenCount();
    }

    protected SMView getChildAt(final long columnNum, long index) {
        assert (columnNum>=0 && columnNum<_numContainer);

        return _contentView[(int)columnNum].getChildren().get((int)index);
    }

    // cell contentView
    protected SMView[] _contentView = null;

    protected int _numContainer;

    // header
    protected SMView _headerView = null;
    protected boolean _isHeaderInList = false;

    // footer
    protected SMView _footerView = null;
    protected boolean _isFooterInList = false;

    protected boolean _lockScroll = false;
    protected boolean _reloadFlag = false;


    private class InstantHolder extends Ref {
        public InstantHolder(IDirector director) {
            super(director);
        }
        public boolean insert(final int hashCode, SMView parent, SMView child) {
            return insert(hashCode, parent, child, true);
        }
        public boolean insert(final int hashCode, SMView parent, SMView child, boolean cleanup) {

            SMView view = _data.get(hashCode);

            if (view!=null) {
                return false;
            }

            parent.removeChild(child, cleanup);

            _data.append(hashCode, child);

            return true;
        }

        public SMView find(final int hashCode) {
            return _data.get(hashCode);
        }

        public void erase(final int hashCode) {
            _data.remove(hashCode);
        }

        public void clear() {
            _data.clear();
        }

        private SparseArray<SMView> _data = new SparseArray<>();
    };

    // instant view holder
    private InstantHolder _holder = null;

    private InstantHolder getHolder() {
        if (_holder==null) {
            _holder = new InstantHolder(getDirector());
        }

        return _holder;
    }
}


/*

 */