package com.interpark.smframework.view;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.BaseTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.scroller.FlexibleScroller;
import com.interpark.smframework.base.scroller.SMScroller;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class SMTableView extends BaseTableView {

    public static final float JUMP_ACTION_DURATION = 0.25f;
    public static final boolean CLEANUP_FLAG = true;
    public static final long FLAG_SCROLL_UPDATE = 1;


    public SMTableView(IDirector director) {
        super(director);
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL,
    }

    public enum RefreshState {
        NONE,
        ENTER,
        READY,
        REFRESHING,
        EXIT,
    }

    public static SMTableView create(IDirector director, Orientation orient) {
        return createMultiColumn(director, orient, 1);
    }

    public static SMTableView create(IDirector director, Orientation orient, float x, float y, float width, float height) {
        return createMultiColumn(director, orient, 1, x, y, width, height, 0, 0);
    }

    public static SMTableView create(IDirector director, Orientation orient, float x, float y, float width, float height, float anchorX, float anchorY) {
        return createMultiColumn(director, orient, 1, x, y, width, height, anchorX, anchorY);
    }

    public static SMTableView createMultiColumn(IDirector director, Orientation orient, int numOfColumn) {
        SMTableView view = new SMTableView(director);
        if (view.initWithOrientAndColumns(orient, numOfColumn)) {
            return view;
        }
        return null;
    }

    public static SMTableView createMultiColumn(IDirector director, Orientation orient, int numOfColumn, float x, float y, float width, float height) {
        return createMultiColumn(director, orient, numOfColumn, x, y, width, height, 0, 0);
    }

    public static SMTableView createMultiColumn(IDirector director, Orientation orient, int numOfColumn, float x, float y, float width, float height, float anchorX, float anchorY) {
        SMTableView view = new SMTableView(director);
        if (view.initWithOrientAndColumns(orient, numOfColumn)) {
            view.setContentSize(new Size(width, height));
            view.setPosition(x, y);
            view.setAnchorPoint(anchorX, anchorY);
            return view;
        }
        return null;
    }


    public SMView dequeueReusableCellWithIdentifier(final String cellID) {
        _reuseScrapper._internalReuseType = _reuseScrapper.getReuseType(cellID);
        _reuseScrapper._internalReuseNode = _reuseScrapper.back(_reuseScrapper._internalReuseType);
        return _reuseScrapper._internalReuseNode;
    }



    // callback & listener
    // section 당 row 개수 delegate
    public interface NumberOfRowsInSection {
        public int  numberOfRowsInSection(final int section);
    }
    public NumberOfRowsInSection numberOfRowsInSection = null;

    // IndexPath로 cell을 하나 얻어오는 delegate
    public interface CellForRowAtIndexPath {
        public SMView cellForRowAtIndexPath(final IndexPath indexPath);
    }
    public CellForRowAtIndexPath cellForRowAtIndexPath = null;

    public interface CellResizeCallback {
        public void onCellResizeCallback(SMView cell, float newSize);
    }
    public CellResizeCallback onCellResizeCallback = null;

    public interface CellResizeCompletionCallback {
        public void onCellResizeCompletionCallback(SMView cell);
    }
    public CellResizeCompletionCallback onCellResizeCompletionCallback = null;

    public interface CellInsertCallback {
        public void onCellInsertCallback(SMView cell, float progress);
    }
    public CellInsertCallback onCellInsertCallback = null;

    public interface CellDeleteCallback {
        public void onCellDeleteCallback(SMView cell, float progress);
    }
    public CellDeleteCallback onCellDeleteCallback = null;

    public interface CellDeleteCompletionCallback {
        public void onCellDeleteCompletionCallback();
    }
    public CellDeleteCompletionCallback onCellDeleteCompletionCallback = null;

    public interface ScrollCallback {
        public void onScrollCallback(float position, float distance);
    }
    public ScrollCallback onScrollCallback = null;




    public interface RefreshDataCallback {
        public void onRefreshDataCallback(SMView cell, RefreshState state, float size);
    }
    public RefreshDataCallback onRefreshDataCallback = null;

    public interface CanRefreshData {
        public boolean canRefreshData();
    }
    public CanRefreshData canRefreshData = null;


    // load more callback... callback이 세팅 되었을때 footer가 나타나면 호출된다. 여기서 통신등 페이지 더보기를 호출 하면 된다. 끝나면 endLoadData()를 호출 할 것.
    // 다시 호출 될일 이 없다면 callback을 nullptr로 세팅하거나 footer 자체를 nullptr로 세팅하면 된다.
    public interface LoadDataCallback {
        public boolean onLoadDataCallback(SMView cell);
    }
    public LoadDataCallback onLoadDataCallback = null;
    private LoadDataCallback onLoadDataCallbackTemp = null;



    // cell이 처음 나타날때 애니메이션을 위한 callback (willDisplayCell...같은 역할)
    public interface InitFillWithCells {
        public void onInitFillWithCells(SMTableView tableView);
    }
    public InitFillWithCells onInitFillWithCells = null;






    @Override
    public boolean isTouchEnable() {return true;}

    public int getColumnCount() {return (int)getContainerCount();}

//    public enum Direction {
//        UP,
//        LEFT,
//        DOWN,
//        RIGHT
//    }
//
//    public static Direction getDirection(float dx, float dy) {
//        final int VERTICAL_WIDE = 100;
//        final int HORIZONTAL_WIDE = (180-VERTICAL_WIDE);
//        double radians = Math.atan2(dy, dx);
//        int degrees = (int)Math.toDegrees(radians);
//        degrees = (degrees % 360) + (degrees < 0 ? 360 : 0); // normalize
//
//        int a = HORIZONTAL_WIDE/2;
//        if (degrees > a && degrees < a + VERTICAL_WIDE) {
//            return Direction.UP;
//        }
//
//        a += VERTICAL_WIDE;
//
//        if (degrees > a && degrees < a + HORIZONTAL_WIDE) {
//            return Direction.LEFT;
//        }
//
//        a += HORIZONTAL_WIDE;
//
//        if (degrees > a && degrees < a + VERTICAL_WIDE) {
//            return Direction.DOWN;
//        }
//
//        return Direction.RIGHT;
//    }




    // page view에서
    public boolean jumpPage(final int pageNo, final float pageSize) {
        assert (cellForRowAtIndexPath!=null);
        if (_forceJumpPage) {
            return false;
        }

        int currentPage = (int)(_scroller.getNewScrollPosition() / pageSize);
        ColumnInfo info = _column[0];

        if (pageNo == currentPage) {
            return false;
        } else if (pageNo == currentPage+1) {
            // 다음 페이지
            _scroller.onTouchFling(-10000, currentPage);
            scheduleScrollUpdate();
        } else if (pageNo == currentPage - 1) {
            _scroller.onTouchFling(10000, currentPage);
            scheduleScrollUpdate();
        } else {
            int numChild = getChildrenCount(0);
            Cursor cursor = new Cursor(info.getViewLastCursor());
//            Cursor cursor = info.getViewLastCursor();
            for (int i=numChild-1; i>=0; i--) {
                cursor.dec(true);
                if (cursor.getIndexPath().getIndex()!=currentPage) {
                    SMView child = getChildAt(0, i);
                    Item item = cursor.getItem();

                    removeChildAndReuseScrap(0, item._reuseType, child, true);
                }
            }

            // 적절한 위치에 추가.
            float position;
            int direction;

            if (pageNo > currentPage) {
                // 뒤쪽 페이지
                position = pageSize;
                direction = +1;
            } else {
                position = -pageSize;
                direction = -1;
            }

            cursor.set(info.getFirstCursor());
            for (int i=0; i<pageNo; i++) {
                // target cursor
                cursor.inc(false);
            }

            _reuseScrapper._internalReuseType = -1;
            _reuseScrapper._internalReuseNode = null;

            SMView child = cellForRowAtIndexPath.cellForRowAtIndexPath(cursor.getIndexPath());

            if (child==null) {
                assert (false);
            }

            if (child.getParent()!=null) {
                _scroller.onTouchUp();
                _forceJumpPage = false;
                return true;
            }

            // order
            child.setLocalZOrder(cursor.getPosition());

            Item item = cursor.getItem();
            if (_reuseScrapper._internalReuseType>=0) {
                item._reuseType = _reuseScrapper._internalReuseType;
            }

            child.setPositionX(position);
            addChild(0, child);
            sortAllChildren(0);

            if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType);
                _reuseScrapper._internalReuseType = -1;
                _reuseScrapper._internalReuseNode = null;
            }

            Action remainAction = getActionByTag(AppConst.TAG.ACTION_LIST_JUMP);
            if (remainAction!=null) {
                _PageJumpAction action = (_PageJumpAction)remainAction;
                action.complete();
                stopAction(action);
            }

            _forceJumpPage = true;

            _PageJumpAction action = new _PageJumpAction(getDirector(), this, cursor, pageSize, currentPage, pageNo, direction);
            action.setTag(AppConst.TAG.ACTION_LIST_JUMP);
            action.setDuration(JUMP_ACTION_DURATION);
            runAction(action);
        }

        return true;
    }




    protected boolean initWithOrientAndColumns(Orientation orient, int numOfColumn) {
        if (initWithContainer(numOfColumn)) {
            _orient = orient;

            _column = new ColumnInfo[numOfColumn];
            for (int col=0; col<numOfColumn; col++) {
                _column[col] = new ColumnInfo();
                _column[col].init(this, col);
            }

            _reuseScrapper = new ReuseScrapper();

            initScroller();
            _lastScrollPosition = _scroller.getScrollPosition();

            if (_velocityTracker==null) {
                _velocityTracker = VelocityTracker.obtain();
            }
            _velocityTracker.clear();

            scheduleScrollUpdate();

            return true;
        }

        return false;
    }

    protected SMScroller initScroller() {
        _scroller = new FlexibleScroller(getDirector());
        return _scroller;
    }

    @Override
    public void setContentSize(Size size) {
        super.setContentSize(size);

        if (isVertical()) {
            _scroller.setWindowSize(size.height);
            for (int col=0; col<_numContainer; col++) {
                _contentView[col].setContentSize(new Size(size.width/_numContainer, size.height));
                _contentView[col].setPositionX(col*size.width/_numContainer);
            }
        } else {
            _scroller.setWindowSize(size.width);
            for (int col=0; col<_numContainer; col++) {
                _contentView[col].setContentSize(new Size(size.width, size.height/_numContainer));
//                _contentView[col].setPositionY(size.height - (col+1)*size.height/_numContainer);
                _contentView[col].setPositionY(col*size.height/_numContainer);
            }
        }

        scheduleScrollUpdate();
    }

    public void hintFixedCellSize(final float cellSize) {
        _hintIsFixedSize = true;
        _hintFixedChildSize = cellSize;
    }

    //View가 미리 생성되는 경계선 바깥쪽 padding
    //@param paddingPixels Scroll padding pixels
    public void setPreloadPaddingSize(final float paddingSize) {
        if (paddingSize >= 0) {
            _preloadPadding = paddingSize;
        }

        scheduleScrollUpdate();
    }

    public void setScrollMarginSize(final float firstMargin, final float lastMargin) {
        _firstMargin = firstMargin;
        _lastMargin = lastMargin;
        scheduleScrollUpdate();
    }

    // position childe view
    protected void positionChildren(final float scrollPosition, final float containerSize, final float headerSize, final float footerSize) {

        assert (cellForRowAtIndexPath!=null);

        // scroll 위치에 따라 children 좌표 세팅.
        float startLocation = headerSize + _firstMargin + _innerScrollMargin - scrollPosition;
        float lastLocation = 0;

        for (int col=0;col<_numContainer; col++) {
            int numChild = getChildrenCount(col);
            if (numChild>0) {
                ColumnInfo info = _column[col];


                Cursor cursor = new Cursor(info.getViewFirstCursor());
//                Cursor cursor = info.getViewFirstCursor();


                for (int i=0; i<numChild; i++, cursor.inc(false)) {
                    SMView child = getChildAt(col, i);
                    Item item = cursor.getItem();
                    if (item._reload) {
                        // cell reload
                        item._reload= false;

                        // child를 없애고 다시 만든다.
                        removeChild(col, child);

                        _reuseScrapper._internalReuseType = -1;
                        _reuseScrapper._internalReuseNode = null;
                        child = cellForRowAtIndexPath.cellForRowAtIndexPath(item._indexPath);
                        if (child==null) {
                            assert (false);
                        }

                        addChild(col, child);
                        child.setLocalZOrder(cursor.getPosition());

                        if (_reuseScrapper._internalReuseType>=0) {
                            item._reuseType = _reuseScrapper._internalReuseType;
                        }

                        if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==child) {
                            _reuseScrapper.popBack(_reuseScrapper._internalReuseType);
                            _reuseScrapper._internalReuseType = -1;
                            _reuseScrapper._internalReuseNode = null;
                        }

                        if (_hintIsFixedSize) {
                            item._newSize = _hintFixedChildSize;
                        } else {
                            if (isVertical()) {
                                item._newSize = child.getContentSize().height;
                            } else {
                                item._newSize = child.getContentSize().width;
                            }
                        }

                        info.resizeCursor(cursor);
                        sortAllChildren(col);
                    }

                    // Resize 처리
                    if (item._size != item._newSize) {
                        info.resizeCursor(cursor);

                        if (onCellResizeCallback!=null) {
                            onCellResizeCallback.onCellResizeCallback(child, item._newSize);
                        }
                    }

                    float location = startLocation + cursor.getLocation();

//                    if (isVertical()) {
//                        float childSize;
//                        if (_hintIsFixedSize) {
//                            childSize = _hintFixedChildSize;
//                        } else {
//                            childSize = child.getContentSize().height;
//                        }
////                        onPositionCell(child, containerSize - (location+childSize), false);
//                        onPositionCell(child, location, false);
//                    } else {
//                        onPositionCell(child, location, false);
//                    }

                        onPositionCell(child, location, false);

                    child.setLocalZOrder(cursor.getPosition());
                }

                lastLocation = Math.max(lastLocation, info.getLastCursor().getLocation());

            }


        }

        lastLocation += startLocation;

        if (_headerView!=null && _isHeaderInList) {
                onPositionHeader(_headerView, startLocation - headerSize, false);
            }

        if (_footerView!=null && _isFooterInList) {
                onPositionFooter(_footerView, lastLocation, false);
            }

        if (_refreshView!=null && _refreshState!=RefreshState.NONE) {
            if (isVertical()) {
                _refreshView.setPositionY(startLocation - _refreshView.getContentSize().height);
            } else {
                _refreshView.setPositionX(startLocation - _refreshView.getContentSize().width);
            }
        }

    }

    public void onPositionCell(SMView cell, final float position, final boolean isAdded) {
        if (isVertical()) {
            cell.setPositionY(position);
        } else {
            cell.setPositionX(position);
        }
    }

    public void onPositionHeader(SMView headerView, final float position, final boolean isAdded) {
        if (isVertical()) {
            headerView.setPositionY(position);
        } else {
            headerView.setPositionX(position);
        }
    }

    public void onPositionFooter(SMView footerView, final float position, final boolean isAdded) {
        if (isVertical()) {
            footerView.setPositionY(position);
        } else {
            footerView.setPositionX(position);
        }
    }

    // view를 벗어난 cell를 화면에서 제거
    protected void clippingChildren(final float scrollPosition, final float containerSize, final float headerSize, final float footerSize) {
        // 스크롤 위치에 따라 화면에 보이지 않는 child 제거

        float startLocation = headerSize + _firstMargin + _innerScrollMargin - scrollPosition;
        float lastLocation = 0;

        for (int col=0; col<_numContainer; col++) {
            int numChild = getChildrenCount(col);
            ColumnInfo info = _column[col];

            // 상단 제거
            if (numChild>0) {
                for (int i = 0; i < numChild; i++) {
                    Cursor cursor = new Cursor(info.getViewFirstCursor());
//                    Cursor cursor = info.getViewFirstCursor();

                    SMView child = getChildAt(col, 0);

                    if (child!=null && startLocation + cursor.getLastLocation() <= -_preloadPadding) {
                        Item item = cursor.getItem();

                        if (item.isDeleted()) {
                            //Delete중인 상단 child는 hold (Animation이 진행되어야 하기 때문에)
                            removeChildAndHold(col, item._tag, child, false);
                        } else {
                            removeChildAndReuseScrap(col, item._reuseType, child, CLEANUP_FLAG);
                        }

                        info.retreatViewFirst();
                    } else {
                        break;
                    }
                }
            }

            // 하단 제거
            numChild = getChildrenCount(col);
            if (numChild>0) {
                for (int i=numChild-1; i>=0; i--) {
                    Cursor cursor = new Cursor(info.getViewLastCursor(-1));
//                    Cursor cursor = info.getViewLastCursor(-1);

                    SMView child = getChildAt(col, i);
                    if (child!=null && startLocation + cursor.getLocation()>=containerSize+_preloadPadding) {
                        Item item = cursor.getItem();

                        if (item.isDeleted()) {
                            // 삭제중인 하단 child는 즉시 삭제
                            stopAndCompleteChildAction(item._tag);
                        } else {
                            if (item._tag!=0) {
                                // Animation중인 하단 child는 즉시 적용
                                stopAndCompleteChildAction(item._tag);
                                item._tag = 0;
                            }
                            removeChildAndReuseScrap(col, item._reuseType, child, CLEANUP_FLAG);

                            info.retreatViewLast();
                        }
                    } else {
                        break;
                    }
                }
            }

            lastLocation = Math.max(lastLocation, info.getLastCursor().getLocation());
        }

        lastLocation += startLocation;

        // 헤더 제거
        if (_headerView!=null && _isHeaderInList) {
            if (startLocation < -_preloadPadding) {
                super.removeChild(_headerView, CLEANUP_FLAG);
                _isHeaderInList = false;
            }
        }

        // 푸터 제거
        if (_footerView!=null && _isFooterInList) {
            if (lastLocation > containerSize + _preloadPadding) {
                super.removeChild(_footerView, CLEANUP_FLAG);
                _isFooterInList = false;
            }
        }
    }

    // fill backward
    protected boolean fillListBack(final int adapterItemCount, final float scrollPosition, final float containerSize, final float headerSize, float footerSize) {

        assert (cellForRowAtIndexPath!=null);


        float scrollLocation = _firstMargin + _innerScrollMargin - scrollPosition;
        float limitLocation = containerSize + _preloadPadding - scrollLocation;

        if (_headerView != null) {
            limitLocation -= headerSize;
        }

        SMView child = null;
        ColumnInfo info = null;
        boolean added = false;

        int lastIndex = 0;
        for (int col=0; col<_numContainer; col++) {
            lastIndex += _column[col].getAliveItemCount();
        }

        while (adapterItemCount > 0) {
            float lastLocation = Float.MAX_VALUE;
            boolean isAtLast = (lastIndex == adapterItemCount);

            int column = -1;
            for (int col=0; col<_numContainer; col++) {
                info = _column[col];

                if (isAtLast) {
                    if (info.getViewLastCursor().getLocation() < lastLocation && !info.isAtLast()) {
                        column = col;
                        lastLocation = info.getViewLastCursor().getLocation();
                    }
                } else {
                    if (info.getViewLastCursor().getLocation() < lastLocation) {
                        column = col;
                        lastLocation = info.getViewLastCursor().getLocation();
                    }
                }
            }

            if (lastLocation >= limitLocation || column < 0) {

                break;
            }


            // 다음 추가할 아이템을 찾는다.
            info = _column[column];
            IndexPath indexPath = null;

            if (info.isAtLast()) {
                // 이전에 생성된 아이템 없음 => 추가
                indexPath = new IndexPath(0, column, lastIndex);
                lastIndex++;
            } else {
                // 이전에 생성된 아이템 있음.
                indexPath = new IndexPath(info.getViewLastCursor().getIndexPath());
            }

            _reuseScrapper._internalReuseType = -1;
            _reuseScrapper._internalReuseNode = null;

            child = cellForRowAtIndexPath.cellForRowAtIndexPath(indexPath);

            if (child==null) {
                assert (false);
            }

            if (child.getParent()!=null) {
                // 이미 attach 되어 있다???
                break;
            }


            float childSize;
            boolean reload = false;
            if (!info.isAtLast()) {
                Item item = info.getViewLastCursor().getItem();

                if (item._reload) {
                    item._reload = false;
                    if (_hintIsFixedSize) {
                        childSize = _hintFixedChildSize;
                    } else {
                        childSize = isVertical()?child.getContentSize().height:child.getContentSize().width;
                    }
                    item._newSize = childSize;
                    reload = true;
                } else {
                    childSize = item._size;
                }
            } else {
                if (_hintIsFixedSize) {
                    childSize = _hintFixedChildSize;
                } else {
                    childSize = isVertical()?child.getContentSize().height:child.getContentSize().width;
                }
            }

            // cursor 진행
            Cursor cursor = new Cursor(info.advanceViewLast(new IndexPath(0, column, indexPath.getIndex()), _reuseScrapper._internalReuseType, childSize));
//            Cursor cursor = info.advanceViewLast(new IndexPath(0, column, indexPath.getIndex()), _reuseScrapper._internalReuseType, childSize);

            if (reload) {
                info.resizeCursor(cursor);
            }

            // order
            child.setLocalZOrder(cursor.getPosition());

            Item item = cursor.getItem();
            if (_reuseScrapper._internalReuseType>=0) {
                item._reuseType = _reuseScrapper._internalReuseType;
            }

            if ((item._flags&ITEM_FLAG_RESIZE)>0) {
                if (onCellResizeCallback!=null) {
                    onCellResizeCallback.onCellResizeCallback(child, item._size);
                }
            }

            if (item._tag==0) {
                if ((item._flags&ITEM_FLAG_INSERT)>0) {
                    if (onCellInsertCallback!=null) {
                        onCellInsertCallback.onCellInsertCallback(child, 1);
                    }
                }

                item._flags = 0;
            }

            childSize = item._size;

            // view 내 위치 참조
            float locationInView = headerSize + _firstMargin + _innerScrollMargin - scrollPosition;
//            if (isVertical()) {
////                onPositionCell(child, _contentSize.height - locationInView - cursor.getLastLocation(), true);
//
//                float cellPosY = locationInView + cursor.getLocation();
//                onPositionCell(child, cellPosY, true);
//            } else {
//                onPositionCell(child, locationInView + cursor.getLocation(), true);
//            }

                onPositionCell(child, locationInView + cursor.getLocation(), true);

            addChild(column, child);

            if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType);
                _reuseScrapper._internalReuseType = -1;
                _reuseScrapper._internalReuseNode = null;
            }

            added = true;
        }

        return added;
    }

    // fill forward
    protected boolean fillListFront(final int adapterItemCount, final float scrollPosition, final float containerSize, final float headerSize, float footerSize) {
        assert (cellForRowAtIndexPath!=null);

        float limitLocation = -_preloadPadding + scrollPosition - (headerSize + _firstMargin + _innerScrollMargin);

        SMView child = null;
        ColumnInfo info = null;
        boolean added = false;

        int nCount = 0;
        while (adapterItemCount>0) {
            float firstLocation = Float.MIN_VALUE;

            // 다음 child 추가할 컬럼 / 인덱스 찾기
            int column = -1;
            for (int col=0; col<_numContainer; col++) {
                info = _column[col];

                if (info.getViewFirstCursor().getLocation() > firstLocation && !info.isAtFirst()) {
                    column = col;
                    firstLocation = info.getViewFirstCursor().getLocation();
                }

            }

            if (firstLocation <= limitLocation || column<0) {
                break;
            }

            info = _column[column];

            Cursor cursor = new Cursor(info.advanceViewFirst());
//            Cursor cursor = info.advanceViewFirst();
            Item item = cursor.getItem();

            if (item.isDeleted()) {
                // 삭재중 아이템
                child = findFromHolder(item.hashCode());
                if (child==null) {
                    child = _DeleteNode.create(getDirector());

                    if (isVertical()) {
                        // 아이템의 높이
                        child.setContentSize(new Size(child.getContentSize().width, item._size));
                    } else {
                        // 아이템의 넓이
                        child.setContentSize(new Size(item._size, child.getContentSize().width));
                    }
                }
            } else {
                _reuseScrapper._internalReuseType = -1;
                _reuseScrapper._internalReuseNode = null;
                child = cellForRowAtIndexPath.cellForRowAtIndexPath(item._indexPath);
                if (_reuseScrapper._internalReuseType>=0) {
                    item._reuseType = _reuseScrapper._internalReuseType;
                }

                if (item._reload) {
                    item._reload = false;
                    if (_hintIsFixedSize) {
                        item._newSize = _hintFixedChildSize;
                    } else {
                        item._newSize = isVertical()?child.getContentSize().height:child.getContentSize().width;
                    }
                    info.resizeCursor(cursor);
                }
            }

            if (child==null) {
                assert (false);
            }

            if (child.getParent()!=null) {
                // 이미 attach 되어 있다???
                break;
            }

            // order
            child.setLocalZOrder(cursor.getPosition());

            if ((item._flags & ITEM_FLAG_RESIZE)>0) {
                if (onCellResizeCallback!=null && child.getClass()==_DeleteNode.class) {
                    onCellResizeCallback.onCellResizeCallback(child, item._size);
                }
            }

            if (item._tag==0) {
                if ((item._flags & ITEM_FLAG_INSERT)>0) {
                    if (onCellInsertCallback!=null) {
                        onCellInsertCallback.onCellInsertCallback(child, 1);
                    }
                }
                item._flags = 0;
            }

            // view 안의 위치
            float locationInView = headerSize + _firstMargin + _innerScrollMargin - scrollPosition;
//            if (isVertical()) {
////                onPositionCell(child, _contentSize.height - locationInView - cursor.getLastLocation(), true);
//                onPositionCell(child, locationInView + cursor.getLocation(), true);
//            } else {
//                onPositionCell(child, locationInView + cursor.getLocation(), true);
//            }
                onPositionCell(child, locationInView + cursor.getLocation(), true);

            addChild(column, child);

            if ((item._flags & ITEM_FLAG_DELETE)>0) {
                eraseFromHolder(item._tag);
            } else if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==child){
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType);
                _reuseScrapper._internalReuseType = -1;
                _reuseScrapper._internalReuseNode = null;
            }

            added = true;
        }

        if (added) {
            for (int col=0; col<_numContainer; col++) {
                sortAllChildren(col);
            }
        }

        return added;
    }

    // fill list
    protected boolean fillList(final int adapterItemCount, final float scrollPosition, final float containerSize, final float headerSize, float footerSize) {

        boolean backAdded = fillListBack(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize);

        boolean frontAdded = fillListFront(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize);


        if (_headerView!=null) {
            if (!_isHeaderInList) {
                if (scrollPosition < headerSize + _firstMargin + _innerScrollMargin) {
                    super.addChild(_headerView);

//                    if (isVertical()) {
////                        float position = scrollPosition + containerSize - (headerSize + _firstMargin + _innerScrollMargin);
//                        float position = -scrollPosition + _firstMargin + _innerScrollMargin;
//                        onPositionHeader(_headerView, position, true);
//                    } else {
//                        float position = -scrollPosition + _firstMargin + _innerScrollMargin;
//                        onPositionHeader(_headerView, position, true);
//                    }
                        float position = -scrollPosition + _firstMargin + _innerScrollMargin;
                        onPositionHeader(_headerView, position, true);
                    _isHeaderInList = true;
                    frontAdded |= true;
                }
            }
        }

        if (_footerView!=null) {
            while (!_isFooterInList) {
                float lastLocation = 0;
                int aliveItemCount = 0;

                for (int col = 0; col < _numContainer; col++) {
                    if (!_column[col].isAtLast()) {
                        aliveItemCount = -1;
                        break;
                    }
                    aliveItemCount += _column[col].getAliveItemCount();
                    lastLocation = Math.max(_column[col].getLastCursor().getLocation(), lastLocation);
                }

                if (aliveItemCount >= adapterItemCount && adapterItemCount > 0 && scrollPosition + containerSize  > headerSize + _firstMargin + _innerScrollMargin + lastLocation) {
                    if (onLoadDataCallback!=null) {
                        if (!_progressLoading) {
                            _progressLoading = true;

                            onLoadDataCallback.onLoadDataCallback(_footerView);
                        }
                    } else if (onLoadDataCallbackTemp!=null) {
                        break;
                    }

                    super.addChild(_footerView);

//                    if (isVertical()) {
////                        float position = scrollPosition + containerSize - (headerSize + _firstMargin + _innerScrollMargin + lastLocation) - _footerView.getContentSize().height;
//                        float position = (headerSize + _firstMargin + _innerScrollMargin + lastLocation) - scrollPosition;
//                        onPositionFooter(_footerView, position, true);
//                    } else {
//                        float position = (headerSize + _firstMargin + _innerScrollMargin + lastLocation) - scrollPosition;
//                        onPositionFooter(_footerView, position, true);
//                    }
                        float position = (headerSize + _firstMargin + _innerScrollMargin + lastLocation) - scrollPosition;
                        onPositionFooter(_footerView, position, true);

                    _isFooterInList = true;
                    backAdded = true;
                }
                break;
            }
        }

        return frontAdded | backAdded;
    }

    // scroll size 계산
    protected float measureScrollSize() {
        float headerSize = 0;
        if (_headerView!=null && _headerView.isVisible()) {
            headerSize = isVertical()?_headerView.getContentSize().height:_headerView.getContentSize().width;
        }

        float footerSize = 0;
        if (_footerView!=null && _footerView.isVisible()) {
            footerSize = isVertical()?_footerView.getContentSize().height:_footerView.getContentSize().width;
        }

        // 스크롤 사이즈 최종 계산
        float scrollSize = 0;
        _canExactScrollSize = false;
        if (_hintIsFixedSize) {
            scrollSize = (float)Math.ceil((float)_lastItemCount / (float)_numContainer) * _hintFixedChildSize;
            _canExactScrollSize = true;
        } else {
            int aliveItemCount = 0;
            for (int col=0; col<_numContainer; col++) {
                aliveItemCount += _column[col].getAliveItemCount();
            }

            if (aliveItemCount >= _lastItemCount) {
                // 마지막일때 정확한 계산
                for (int col=0; col<_numContainer; col++) {
                    scrollSize = Math.max(scrollSize, _column[col].getLastCursor().getLocation());
                }
                _canExactScrollSize = true;
            } else {
                // 마지막 아닐때 평균으로 계산(정확한 사이즈를 알수 없으므로...)
                if (aliveItemCount > 0) {
                    float containerSize = isVertical()?_contentSize.height:_contentSize.width;
                    float columnSizeTotal = 0;
                    for (int col=0; col<_numContainer; col++) {
                        columnSizeTotal += _column[col].getLastCursor().getLocation();
                    }

                    scrollSize = (_lastItemCount * columnSizeTotal / (float)aliveItemCount) / (float)_numContainer;
                    scrollSize += containerSize * 0.3f; // 30%여분 추가
                }

                _justAtLast = false;
            }
        }

        scrollSize += headerSize + footerSize + _firstMargin + _innerScrollMargin + _lastMargin;



        return scrollSize;
    }

    protected void scheduleScrollUpdate() {
        registerUpdate(FLAG_SCROLL_UPDATE);
        if (_scrollParent!=null) {
            _scrollParent.notifyScrollUpdate();
        }
    }

    protected void unscheduleScrollUpdate() {
        unregisterUpdate(FLAG_SCROLL_UPDATE);
    }

    @Override
    public boolean updateScrollInParentVisit(float deltaScroll) {
        _needUpdate = false;
        _deltaScroll = 0;

        if (isUpdate(FLAG_SCROLL_UPDATE)) {
            _skipUpdateOnVisit = false;
            onUpdateOnVisit();
            _skipUpdateOnVisit = true;
        }

        deltaScroll = _deltaScroll;

        return _needUpdate;
    }


    @Override
    public void onUpdateOnVisit() {

        if (_skipUpdateOnVisit) {
            _skipUpdateOnVisit = false;
            return;
        }

        if (_contentSize.width<=0 || _contentSize.height<=0) {
            return;
        }

        assert (cellForRowAtIndexPath!=null);
        assert (numberOfRowsInSection!=null);

        if (_reloadFlag) {
            _reloadFlag = false;

            _velocityTracker.clear();
            _scroller.reset();
            _scroller.setScrollPosition(getBaseScrollPosition());
            _lastScrollPosition = _scroller.getScrollPosition();

            _lastItemCount = 0;
            _inScrollEvent = false;
            _touchFocused = false;
            _justAtLast = false;
            _forceJumpPage = false;
            _fillWithCellsFirstTime = false;

            for (int col=0; col<_numContainer; col++) {
                ColumnInfo info = _column[col];

                // 수행중인 Animation 종료
                for (Cursor cursor = new Cursor(info.getFirstCursor()); cursor._position<info.getViewLastCursor()._position; cursor.inc(true)) {
//                for (Cursor cursor = info.getFirstCursor(); cursor._position<info.getViewLastCursor()._position; cursor.inc(true)) {
                    if (cursor.getItem()._tag>0) {
                        stopActionByTag(cursor.getItem()._tag);
                    }
                }

                // child 제거
                int numChild = getChildrenCount(col);
                if (numChild>0) {
                    Cursor cursor = new Cursor(info.getViewFirstCursor());
//                    Cursor cursor = info.getViewFirstCursor();
                    for (int i=0; i<numChild; i++, cursor.inc(true)) {
                        SMView child = getChildAt(col, 0);
                        // reload면 reuse하지 않는다.
                        removeChild(col, child, true);


                    }

                    // scrapper clear
                    _reuseScrapper.clear();

                    // holder clear
                    clearInstantHolder();
                }

                info.init(this, col);

                if (onLoadDataCallbackTemp!=null) {
                    onLoadDataCallback = onLoadDataCallbackTemp;
                    onLoadDataCallbackTemp = null;
                }
            }

            if (!_reloadExceptHeader) {
                if (_headerView!=null && _isHeaderInList) {
                    super.removeChild(_headerView, CLEANUP_FLAG);
                    _isHeaderInList = false;
                }
            }
//            _reloadExceptHeader = true;
            _reloadExceptHeader = false;

            if (_footerView!=null && _isFooterInList) {
                super.removeChild(_footerView, CLEANUP_FLAG);
                _isFooterInList = false;
            }
        }

        if (_forceJumpPage) {
            return;
        }

        boolean updated = false;

        final int adapterItemCount = numberOfRowsInSection.numberOfRowsInSection(0);
        if (_lastItemCount != adapterItemCount) {
            _lastItemCount = adapterItemCount;
            updated = true;
        }

        updated |= ((SMScroller)_scroller).update();
        float scrollPosition = _scroller.getScrollPosition();
        float containerSize = isVertical()?_contentSize.height:_contentSize.width;
        float headerSize = 0;
        float footerSize = 0;

        if (_refreshView!=null) {
            boolean updateRefresh = false;

            if (_refreshState!=_lastRefreshState) {
                if (_lastRefreshState == RefreshState.NONE) {
                    super.addChild(_refreshView, 1);
                }

                if (_refreshState==RefreshState.NONE) {
                    super.removeChild(_refreshView);
                } else if (_refreshState==RefreshState.REFRESHING) {
                    // 터치 release됨, Refresh 시작
                    _scroller.setHangSize(_refreshTriggerSize);
                    updated |= _scroller.update();
                    scrollPosition = _scroller.getScrollPosition();
                } else if (_lastRefreshState==RefreshState.REFRESHING && _refreshState==RefreshState.NONE) {
                    _scroller.setHangSize(0);
                }
                updateRefresh = true;
            }

            if (_refreshState!=RefreshState.NONE) {
                _refreshSize = -_scroller.getScrollPosition();
                if (_refreshState == RefreshState.REFRESHING) {
                    if (_refreshSize < _refreshMinSize) {
                        _refreshSize = Math.max(-_scroller.getScrollPosition(), _refreshMinSize);
                    }
                } else {
                    _refreshSize = Math.max(.0f, -_scroller.getScrollPosition());
                }

                if (_refreshSize != _lastRefreshSize) {
                    updateRefresh = true;
                }
            } else {
                _refreshSize = 0;
            }

            if (updateRefresh) {
                _lastRefreshState = _refreshState;
                _lastRefreshSize = _refreshSize;

                if (getActionByTag(AppConst.TAG.ACTION_LIST_HIDE_REFRESH)==null) {
                    if (onRefreshDataCallback!=null) {
                        onRefreshDataCallback.onRefreshDataCallback(_refreshView, _refreshState, _refreshSize);
                    }

                    // ToDo. 대충 기준을 10으로 잡자... 나중에 수정해야 함.
                    if (_refreshSize <= 10 && _refreshState == RefreshState.EXIT) {
                        _refreshState = RefreshState.NONE;
                    }
                }
                updated = true;
            }
        }

        if (_headerView!=null) {
            headerSize = isVertical()?_headerView.getContentSize().height:_headerView.getContentSize().width;
        }
        if (_footerView!=null) {
            footerSize = isVertical()?_footerView.getContentSize().height:_footerView.getContentSize().width;
        }

        if (_animationDirty) {
            _animationDirty = false;

            positionChildren(scrollPosition, containerSize, headerSize, footerSize);
            float newScrollSize = measureScrollSize();
            _scroller.setScrollSize(Math.max(_minScrollSize, newScrollSize));

            if (!_justAtLast && _canExactScrollSize) {
                // 최초로 마지막에 도달했을 때 잘못된 오버스크롤 방지
                _justAtLast = true;
                _scroller.justAtLast();
            }

            scrollPosition = _scroller.getScrollPosition();
        }
        positionChildren(scrollPosition, containerSize, headerSize, footerSize);
        clippingChildren(scrollPosition, containerSize, headerSize, footerSize);

//        boolean fillFlag = fillList(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize);
//        if (fillList(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize)) {
//        if (fillFlag) {
        if (fillList(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize)) {
            _scroller.setScrollSize(Math.max(_minScrollSize, measureScrollSize()));

            if (!_justAtLast && _canExactScrollSize) {
                // 최초로 마지막에 도달했을 때 잘못된 오버스크롤 방지
                _justAtLast = true;
                _scroller.justAtLast();
            }

            if (adapterItemCount > 0) {
                if (!_fillWithCellsFirstTime) {
                    _fillWithCellsFirstTime = true;
                    if (onInitFillWithCells!=null) {
                        onInitFillWithCells.onInitFillWithCells(this);
                    }

                    if (_initRefreshEnable && _refreshView != null) {
                        _initRefreshEnable = false;
                        if (_scroller.getNewScrollPosition() <= 0) {
                            _scroller.setScrollPosition(-_refreshTriggerSize);
                        }
                        _scroller.setHangSize(_refreshTriggerSize);
                        _refreshState = RefreshState.REFRESHING;
                        positionChildren(-_refreshTriggerSize, containerSize, headerSize, footerSize);

                        if (onRefreshDataCallback!=null) {
                            onRefreshDataCallback.onRefreshDataCallback(_refreshView, _refreshState, _refreshTriggerSize);
                        }

                        updated = true;
                    }
                }
            }
        }

        _needUpdate = true;
        if (_lastScrollPosition != scrollPosition) {
            float distance = (scrollPosition - _lastScrollPosition);
            onScrollChanged(scrollPosition, distance);
        } else if (!updated) {
            _needUpdate = false;
            unscheduleScrollUpdate();
        }
        _deltaScroll = _lastScrollPosition - scrollPosition;
        _lastScrollPosition = scrollPosition;
    }

    protected void onScrollChanged(float position, float distance) {
        if (onScrollCallback!=null) {
            onScrollCallback.onScrollCallback(position, distance);
        }
    }

    protected void scrollFling(final float velocity) {
        _scroller.onTouchFling(velocity);
    }

    @Override
    public int dispatchTouchEvent(MotionEvent ev) {
        if (_forceJumpPage) {
            return TOUCH_TRUE;
        }


        int action = ev.getAction();
        Vec2 point = new Vec2(ev.getX(), ev.getY());

        if (_tableRect!=null && action==MotionEvent.ACTION_DOWN && !_tableRect.containsPoint(point)) {
            if (_lockScroll) {
                return TOUCH_TRUE;
            }
        } else {
            if (_lockScroll) {
                return super.dispatchTouchEvent(ev);
            }

            if (!_inScrollEvent && _scroller.isTouchable()) {
                if (action==MotionEvent.ACTION_DOWN && _scroller.getState()!=SMScroller.STATE.STOP) {
                    stop();
                    scheduleScrollUpdate();
                }

                int ret = super.dispatchTouchEvent(ev);
                if (ret==TOUCH_INTERCEPT) {
                    return TOUCH_INTERCEPT;
                }
            }
        }

        if (_velocityTracker==null) {
            _velocityTracker = VelocityTracker.obtain();
        }


        float x = point.x;
        float y = point.y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            {

                _inScrollEvent = false;
                _lastMotionX = x;
                _lastMotionY = y;
                _firstMotionTime = _director.getGlobalTime();

                if (_accelScrollEnable) {
                    if (_scroller.getScrollSpeed() > 2000) {
                        _accelCount++;
                    } else {
                        _accelCount = 0;
                    }
                }

                _scroller.onTouchDown();

                if (_scrollRect != null && !_scrollRect.containsPoint(point)) {
                    _touchFocused = false;
                    return TOUCH_FALSE;
                }

                _touchFocused = true;
                _velocityTracker.addMovement(ev);
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                _touchFocused = false;

                if (_inScrollEvent) {
                    _inScrollEvent = false;
                    float vx, vy;
                    vx = _velocityTracker.getXVelocity(0);
                    vy = _velocityTracker.getYVelocity(0);

                    // Velocity tracker에서 계산되지 않았을때 보정..
                    if (vx==0 && vy==0) {
                        float dt = _director.getGlobalTime() - _firstMotionTime;
                        if (dt > 0) {
                            Vec2 p1 = _touchStartPosition;
                            Vec2 p2 = point;
                            vx = -(p2.x - p1.x) / dt;
                            vy = -(p2.y - p1.y) / dt;
                        }
                    }

                    // Accelate scroll
                    float maxVelocity = _maxVelocicy;
                    if (_accelScrollEnable) {
                        float dt = _director.getGlobalTime() - _firstMotionTime;
                        if (dt < 0.15 && _accelCount > 3) {
                            maxVelocity *= (_accelCount-2);
                        }
                    }

                    if (isVertical()) {
                        if (Math.abs(vy) > AppConst.Config.MIN_VELOCITY) {
                            if (Math.abs(vy) > maxVelocity) {
                                vy =  SMView.signum(vy) * maxVelocity;
                            }
                            scrollFling(-vy);
                        } else {
                            _scroller.onTouchUp();
                        }
                        _lastVelocityY = vy;
                        _lastVelocityX = 0;
                        _lastFlingTime = _director.getGlobalTime();
                    } else {
                        if (Math.abs(vx)>AppConst.Config.MIN_VELOCITY) {
                            if (Math.abs(vx) > maxVelocity) {
                                vx =  SMView.signum(vx) * maxVelocity;
                            }
                            scrollFling(-vx);
                        } else {
                            _scroller.onTouchUp();
                        }

                        _lastVelocityX = vx;
                        _lastVelocityY = 0;
                        _lastFlingTime = _director.getGlobalTime();
                    }

                    scheduleScrollUpdate();
                } else {
                    _scroller.onTouchUp();
                    scheduleScrollUpdate();
                    _lastVelocityX = 0;
                    _lastVelocityY = 0;
                }

                _velocityTracker.clear();
//                _velocityTracker.recycle();
//                _velocityTracker = null;

                // 터치로 놓을 때 refreshView 처리
                if (_refreshView!=null && _refreshState!=RefreshState.NONE) {
                    // float size = -_scroller->getScrollPosition();
                    switch (_refreshState) {
                        // 나중에 필요하면 부활
//                        case READY:
//                        {
//                            // 준비상태에서 터치 release
//                            if (size >= _refreshTriggerSize) {
//                                // 충분한 사이즈일태 BEGIN REFRESH
//                                _refreshState = RefreshState.REFRESHING;
//                            } else {
//                                // 충분한 사이즈 아니면 취소
//                                _refreshState = RefreshState.EXIT;
//                            }
//                            scheduleScrollUpdate();
//                        }
//                        break;
                        case ENTER:
                        {
                            _refreshState = RefreshState.EXIT;
                            scheduleScrollUpdate();
                        }
                        break;
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {
                _velocityTracker.addMovement(ev);
                float deltaX;
                float deltaY;

                if (!_inScrollEvent) {
                    deltaX = x - _lastMotionX;
                    deltaY = y - _lastMotionY;
                } else {
                    deltaX = point.x - _touchPrevPosition.x;
                    deltaY = point.y - _touchPrevPosition.y;
                }


                // 터치로 당길 때 refreshView 처리
                if (_refreshView!=null && getActionByTag(AppConst.TAG.ACTION_LIST_HIDE_REFRESH)==null) {
                    float size = _scroller.getScrollPosition();

                    switch (_refreshState) {
                        case NONE:
                        {
                            if (canRefreshData != null && !canRefreshData.canRefreshData()) {
                                break;
                            }

                            if (size>0) {
                                // 최상단에서 아래로 당길때 RefreshView 추가
                                _refreshState = RefreshState.ENTER;
                                scheduleScrollUpdate();
                            }
                        }
                        break;
                        case ENTER:
                        {
                            if (size<0) {
                                _refreshState = RefreshState.NONE;
                            } else if (size>=_refreshTriggerSize){
                                // 발동 사이즈 이상이면 준비 상태로 전환 (삭제)... 나중에 ready가 필요하면 다시 넣자
                                // _refreshState = RefreshState.READY;

                                // 충분히 당겨지면 바로 발동하는 것으로 변경함 (추가)
                                _refreshState = RefreshState.REFRESHING;
                            }
                            scheduleScrollUpdate();
                        }
                        break;
                        case READY: // 준비 상태
                        {
                            // 나중에 쓰는 걸로...
                            if (size < 0) {
                                // refresh 시작 전 사이즈 0 이하면 취소
                                _refreshState = RefreshState.NONE;
                            } else if (size < _refreshTriggerSize) {
                                // 발동 사이즈 이하로 내려가면 ENTER 상태로 전환
                                _refreshState = RefreshState.ENTER;
                            }
                            scheduleScrollUpdate();
                        }
                        break;
                        default:
                            break;
                    }
                }

                if (_touchFocused && !_inScrollEvent) {
                    float ax = x - _lastMotionX;
                    float ay = y - _lastMotionY;

                    // 첫번째 스크롤 이벤트에서만 체크한다
                    Direction dir = getDirection(ax, ay);
                    if (isVertical()) {
                        if ((dir==Direction.UP || dir==Direction.DOWN) && Math.abs(ay) > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true;
                        }
                    } else {
                        if ((dir==Direction.LEFT || dir==Direction.RIGHT) && Math.abs(ax) > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true;
                        }
                    }

                    if (_inScrollEvent) {
                        if (_touchMotionTarget!=null) {
                            cancelTouchEvent(_touchMotionTarget, ev);
                            _touchMotionTarget = null;
                        }
                    }
                }

                if (_inScrollEvent) {
                    if (isVertical()) {
                        _scroller.onTouchScroll(deltaY);
                    } else {
                        _scroller.onTouchScroll(deltaX);
                    }
                    _lastMotionX = x;
                    _lastMotionY = y;
                    scheduleScrollUpdate();
                }
            }
            break;
        }

        if (_inScrollEvent) {
            return TOUCH_INTERCEPT;
        }

        return TOUCH_TRUE;
    }

    private FindCursorRet findCursorForIndexPath(IndexPath indexPath) {

        assert (indexPath.getIndex()>=0 && indexPath.getIndex()==numberOfRowsInSection.numberOfRowsInSection(0));

        int index = indexPath.getIndex();

        for (int col = 0; col < _numContainer; col++) {
            ColumnInfo info = _column[col];
            Cursor begin;
            Cursor end;

            begin = new Cursor(info.getFirstCursor());
            end = new Cursor(info.getLastCursor());

            if (!info.getViewFirstCursor().isEnd()) {
                if (index < info.getViewFirstCursor().getIndexPath().getIndex()) {
                    end.set(info.getViewFirstCursor().inc(true));
                } else if (!info.getViewLastCursor().isEnd()) {
                    if (index < info.getViewLastCursor().getIndexPath().getIndex()) {
                        begin.set(info.getViewFirstCursor());
                        end.set(info.getViewLastCursor());
                    } else {
                        begin.set(info.getViewLastCursor());
                    }
                }
            }

            Cursor cursor = new Cursor(begin);
            int count = end._position - begin._position;

            for (; count>0; cursor.inc(true), --count) {
                if (!cursor.getItem().isDeleted()) {
                    if (cursor.getIndexPath().getIndex() == index) {
                        indexPath.set(new IndexPath(0, col, index));
                        return new FindCursorRet(cursor, true);
                    }
                }
            }
        }

        return new FindCursorRet(null, false);
    }
    private FindCursorRet findCursorForChild(SMView child) {
        assert (child!=null);

        Cursor retCursor = null;

        for (int col=0; col<_numContainer; col++) {
            int numChild = getChildrenCount(col);
            for (int i = 0; i < numChild; i++) {
                if (child == getChildAt(col, i)) {
                    ColumnInfo info = _column[col];
                    retCursor = new Cursor(info.getViewFirstCursor(i));
                    if ((retCursor.getItem()._flags & ITEM_FLAG_DELETE)<=0) {
                        return new FindCursorRet(retCursor, true);
                    }
                    break;
                }
            }
        }

        return new FindCursorRet(retCursor, false);
    }
    private FindCursorRet findChildForIndexPath(IndexPath indexPath) {
        Cursor retCursor = null;
        FindCursorRet ret = findCursorForIndexPath(indexPath);
        if (!ret.retBool) {
            return new FindCursorRet(null, false, null);
        }
        retCursor = ret.retCursor;

        ColumnInfo info = _column[indexPath.getColumn()];
        if (retCursor._position < info.getViewFirstCursor()._position || retCursor._position >= info.getViewLastCursor()._position) {
            return new FindCursorRet(null, false, null);
        }

        int offset = retCursor._position - info.getViewFirstCursor()._position;
        if (offset < 0 || offset >= getChildrenCount(indexPath.getColumn())) {
            return new FindCursorRet(null, false, null);
        }

        SMView view = getChildAt(indexPath.getColumn(), retCursor._position - info.getViewFirstCursor()._position);
        return new FindCursorRet(retCursor, true, view);
    }

    protected void stopAndCompleteChildAction(int tag) {
        if (tag<=0) {
            return;
        }

        Action action = getActionByTag(tag);
        if (action!=null) {
            _BaseAction a = null;
            if (action.getClass()==_DelaySequence.class) {
                _DelaySequence seq = (_DelaySequence)action;
                a = seq.getAction();
                if (a!=null) {
                    a.complete();
                    stopActionByTag(tag);
                }

            } else {
                if (action.getClass()==_BaseAction.class) {
                    a = (_BaseAction)action;

                    if (a!=null) {
                        a.complete();
                        stopActionByTag(tag);
                    }
                }
            }
        }
    }

    private void deleteCursor(final int column, Cursor cursor) {
        deleteCursor(column, cursor, true);
    }

    private void deleteCursor(final int column, Cursor cursor, boolean cleanup) {
        ColumnInfo info = _column[column];

        // 화면에 표시중이면 child 삭제
        if (cursor._position>=info.getViewFirstCursor()._position && cursor._position<info.getViewLastCursor()._position) {
            int position = cursor._position - info.getViewFirstCursor()._position;
            SMView child = getChildAt(column, position);
            Item item = cursor.getItem();

            if (onCellDeleteCallback!=null) {
                onCellDeleteCallback.onCellDeleteCallback(child, 1);
            }

            if (child.getClass()==_DeleteNode.class || item._dontReuse) {
                removeChild(column, child, cleanup);
            } else {
                removeChildAndReuseScrap(column, item._reuseType, child, CLEANUP_FLAG);
            }
        }

        // column에서 cursor 삭제
        _column[column].deleteCursor(cursor);

        // 삭제 후 child reorder
        if (_column[column]._data.size() > 0) {
            int numChild = getChildrenCount(column);

            if (numChild > 0) {
                Cursor c = new Cursor(_column[column].getViewFirstCursor());
                for (int i=0; i<numChild; i++) {
                    SMView child = getChildAt(column, i);
                    if (child!=null) {
                        child.setLocalZOrder(c.getPosition());
                    }
                    c.inc(false);
                }
                sortAllChildren(column);
            }
        }
    }

    private boolean performDelete(Cursor cursor, SMView child, float duration, float delay) {
        return performDelete(cursor, child, duration, delay, true);
    }
    private boolean performDelete(Cursor cursor, SMView child, float duration, float delay, boolean cleanup) {
        if (cursor.getItem().isDeleted()) {
            return false;
        }

        int column = cursor.getIndexPath().getColumn();

        for (int col=0; col<_numContainer; col++) {
            _column[col].markDeleteCursor(cursor);
        }

        Item item = cursor.getItem();
        if (item._tag>0) {
            Action a = getActionByTag(item._tag);
            if (a!=null) {
                stopAction(a);
            }
        }

        _column[column]._resizeReserveSize = (int)-item._size;

        if (duration>0 || delay>0) {
            // 애니메이션
            _BaseAction delteAction = new _DeleteAction(getDirector(), this, column, cursor);
            delteAction.setDuration(duration);
            Action action = null;
            if (delay>0) {
                action = new _DelaySequence(getDirector(), delay, delteAction);
            } else {
                action = delteAction;
            }

            item._flags |= ITEM_FLAG_RESIZE;
            item._tag = _internalActionTag++;
            action.setTag(item._tag);

            runAction(action);
        } else {
            item._newSize = 0;
            _column[column].resizeCursor(cursor);
            deleteCursor(column, cursor, cleanup);
        }

        scheduleScrollUpdate();

        return true;
    }

    private boolean performResize(Cursor cursor, SMView child, final float newSize, float duration, float delay) {
        Item item = cursor.getItem();

        if (item._tag>0) {
            stopAndCompleteChildAction(item._tag);
        }

        IndexPath indexPath = cursor.getIndexPath();
        _column[indexPath.getIndex()]._resizeReserveSize += newSize - item._size;

        if (duration>0 || delay>0) {
            // 애니메이션
            _BaseAction resizeAction = new _ResizeAction(getDirector(), this, indexPath.getColumn(), cursor, newSize);
            resizeAction.setDuration(duration);
            Action action = null;
            if (delay>0) {
                action = new _DelaySequence(getDirector(), delay, resizeAction);
            } else {
                action = resizeAction;
            }

            item._tag = _internalActionTag++;
            action.setTag(item._tag);

            runAction(action);
        } else {
            item._newSize = newSize;
            _column[indexPath.getColumn()].resizeCursor(cursor);
            _animationDirty = true;
        }

        scheduleScrollUpdate();
        return true;
    }

    @Override
    public void setMinScrollSize(final float minScrollSize) {
        super.setMinScrollSize(minScrollSize);
        _scroller.setScrollSize(Math.max(_minScrollSize, _scroller.getScrollSize()));
    }

    public void setScrollPosition(final float position) {
        _scroller.setScrollPosition(position);
    }

    public void reloadData() {
        reloadData(false);
    }
    public void reloadData(boolean bExceptHeader) {
        _reloadExceptHeader = bExceptHeader;
        _reloadFlag = true;
        scheduleScrollUpdate();
    }

    // update date.. 보통 아래 또는 위로 추가되었을경우
    public void updateData() {
        _animationDirty = true;
        scheduleScrollUpdate();
    }


    @Override
    public void notifyScrollUpdate() {
        scheduleScrollUpdate();
    }

    public void reloadRowsAtIndexPath(final IndexPath indexPath) {
        FindCursorRet ret = findCursorForIndexPath(indexPath);
        if (!ret.retBool) {
            return;
        }

        ret.retCursor.getItem()._reload = true;

        scheduleScrollUpdate();
    }

    public SMView getCellForIndexPath(final IndexPath indexPath) {
        FindCursorRet ret = findChildForIndexPath(indexPath);

        SMView retView = ret.retView;
        if (ret.retView==null || retView.getClass()==_DeleteNode.class) {
            // 해당 cell 찾지 못함 or 이미 지워짐
            return null;
        }

        return ret.retView;
    }

    public IndexPath getIndexPathForCell(SMView child) {
        FindCursorRet ret = findCursorForChild(child);
        if (!ret.retBool) {
            // 해당 item 찾지 못함 or 이미 지워짐
            return null;
        }

        return ret.retCursor.getIndexPath();
    }

    public boolean insertRowAtIndexPath(final IndexPath indexPath, float estimateSize) {
        return insertRowAtIndexPath(indexPath, estimateSize, 0);
    }
    public boolean insertRowAtIndexPath(final IndexPath indexPath, float estimateSize, float duration) {
        return insertRowAtIndexPath(indexPath, estimateSize, duration, 0);
    }
    public boolean insertRowAtIndexPath(final IndexPath indexPath, float estimateSize, float duration, float delay) {
        return insertRowAtIndexPath(indexPath, estimateSize, duration, delay, false);
    }
    public boolean insertRowAtIndexPath(final IndexPath indexPath, float estimateSize, float duration, float delay, boolean immediate) {
        assert (numberOfRowsInSection!=null);
        assert (cellForRowAtIndexPath!=null);
        assert (indexPath.getIndex()>=0 && indexPath.getIndex()<=numberOfRowsInSection.numberOfRowsInSection(0));

        ColumnInfo info = null;

        int column = -1;
        float lastLocation = Float.MAX_VALUE;
        int lastIndex = Integer.MIN_VALUE;
        for (int col=_numContainer-1; col>=0; col--) {
            info = _column[col];
            lastIndex = Math.max(lastIndex, info._lastIndexPath.getIndex());

            if (info._data.size()==0) {
                column = col;
                lastLocation = 0;
            } else {
                if (info.getLastCursor().getLocation() + info._resizeReserveSize < lastLocation) {
                    column = 0;
                    lastLocation = info.getLastCursor().getLocation() + info._resizeReserveSize;
                }
            }
        }

        assert (column!=-1);

        if (indexPath.getIndex() > lastIndex+1) {
            // 아직 붙지 않은 cell이면 나중에 하고 지금은 넘어간다.
            return true;
        }

        Cursor cursor = null;

        for (int col=0; col<_numContainer; col++) {
            if (col==column) {
                cursor = _column[col].insertItem(new IndexPath(0, column, indexPath.getIndex()), -1, estimateSize);
            } else {
                _column[col].markInsertItem(new IndexPath(0, column, indexPath.getIndex()));
            }
        }

        Item item = cursor.getItem();
        info = _column[column];

        boolean needchild = cursor._position>=info.getViewFirstCursor()._position && cursor._position < info.getViewLastCursor()._position;
        boolean addChildOnTop = false;

        if (immediate) {
            if (info.getViewFirstCursor()._position > info.getFirstCursor()._position && cursor._position==info.getViewFirstCursor(-1)._position) {
                addChildOnTop = true;
                needchild = true;
            } else if (cursor._position==info.getViewLastCursor(1)._position) {
                needchild = true;
            }
        }

        if (needchild && cursor._position == info.getViewLastCursor(-1)._position) {
            float headerSize = 0;
            if (_headerView!=null) {
                headerSize = isVertical() ? _headerView.getContentSize().height : _headerView.getContentSize().width;
            }

            // view 안의 위치
            float location = headerSize + _firstMargin + _scroller.getScrollPosition() + cursor.getLocation();
            float containerSize = isVertical() ? _contentSize.height : _contentSize.width;

            if (location > containerSize) {
                info.retreatViewLast();
                needchild = false;
            }
        }


        boolean needAnimation = (duration>0 || delay>0) && (needchild || cursor._position < info.getViewFirstCursor()._position);

        if (!needAnimation) {
            item._newSize = estimateSize;
            info.resizeCursor(cursor);
        }

        if (needchild) {
            // 화면에 즉시 보여야 한다.
            int childIndex = 0;

            int numChild = getChildrenCount(column);

            // 1) 현재 children reorder
            for (Cursor c = new Cursor(info.getViewFirstCursor()); childIndex<numChild && c._position<info.getViewLastCursor()._position; c.inc(true)) {
                if (c._position==cursor._position) {
                    continue;
                }

                SMView child = getChildAt(column, childIndex++);
                if (child!=null) {
                    child.setLocalZOrder(c.getPosition());
                }
            }

            if (addChildOnTop) {
                info.advanceViewFirst();
            }

            // 2) child 추가
            _reuseScrapper._internalReuseType = -1;
            _reuseScrapper._internalReuseNode = null;

            SMView child = cellForRowAtIndexPath.cellForRowAtIndexPath(item._indexPath);
            if (_reuseScrapper._internalReuseType>=0) {
                item._reuseType = _reuseScrapper._internalReuseType;
            }

            if (child==null) {
                // something is wrong!!!
                assert (false);
            }


            // order
            child.setLocalZOrder(cursor.getPosition());

            float headerSize = 0;
            if (_headerView != null) {
                headerSize = isVertical()?_headerView.getContentSize().height:_headerView.getContentSize().width;
            }

            // view 안의 위치
            float locationInView = headerSize + _firstMargin - _scroller.getScrollPosition();
            if (isVertical()) {
//                onPositionCell(child, _contentSize.height - (cursor.getLastLocation() + locationInView), true);
                onPositionCell(child, cursor.getLocation() + locationInView, true);
            } else {
                onPositionCell(child, cursor.getLocation() + locationInView, true);
            }

            addChild(column, child);
            if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType);
            }

            _reuseScrapper._internalReuseType = -1;
            _reuseScrapper._internalReuseNode = null;

            if (!needAnimation) {
                item._newSize = estimateSize;
                info.resizeCursor(cursor);
            }

            if (onCellResizeCallback!=null) {
                onCellResizeCallback.onCellResizeCallback(child, item._newSize);
            }

            if (!needAnimation && onCellInsertCallback!=null) {
                onCellInsertCallback.onCellInsertCallback(child, 1);
            }

            sortAllChildren(column);
        }

        if (needAnimation) {
            // animation
            _BaseAction insertAction = new _InsertAction(getDirector(), this, column, cursor, estimateSize);
            insertAction.setDuration(duration);
            Action action = null;
            if (delay>0) {
                action = new _DelaySequence(getDirector(), delay, insertAction);
            } else {
                action = insertAction;
            }

            item._flags = ITEM_FLAG_RESIZE | ITEM_FLAG_INSERT;
            item._tag = _internalActionTag++;
            action.setTag(item._tag);

            runAction(action);
        } else {
            info.resizeCursor(cursor);

            if (!needchild) {
                // child도 없고 animation도 없으면 flags에 표시만 해둔다.
                // => 추가되는 순간 onCellInsertCallback 호출됨
                cursor.getItem()._flags = ITEM_FLAG_INSERT;
            }
        }

        scheduleScrollUpdate();
        return false;
    }

    public boolean deleteRowForCell(SMView child) {
        return deleteRowForCell(child, 0);
    }
    public boolean deleteRowForCell(SMView child, float duration) {
        return deleteRowForCell(child, duration, 0);
    }
    public boolean deleteRowForCell(SMView child, float duration, float delay) {
        assert (child!=null);

        FindCursorRet ret = findCursorForChild(child);
        if (!ret.retBool) {
            // child가 없거나 delete중임
            return false;
        }

        if (ret.retCursor._position>=_column[ret.retCursor.getIndexPath().getColumn()].getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0;
            delay = 0;
        }

        return performDelete(ret.retCursor, child, duration, delay);
    }

    public SMView popCell(SMView child) {
        assert (child!=null);

        FindCursorRet ret = findCursorForChild(child);
        if (!ret.retBool) {
            return null;
        }

        if (ret.retCursor._position < _column[ret.retCursor.getIndexPath().getColumn()].getViewFirstCursor()._position || ret.retCursor._position >= _column[ret.retCursor.getIndexPath().getColumn()].getViewLastCursor()._position) {
            return null;
        }

        ret.retCursor.getItem()._dontReuse = true;

        if (performDelete(ret.retCursor, child, 0, 0, false)) {
            return child;
        }

        return null;
    }

    // 일반 method
    public boolean isDeleteCell(SMView child) {
        FindCursorRet ret = findCursorForChild(child);
        if (!ret.retBool) {
            // child가 없거나 delete중임
            return true;
        }

        return false;
    }


    public boolean deleteRowAtIndexPath(final IndexPath indexPath) {
        return deleteRowAtIndexPath(indexPath, 0);
    }
    public boolean deleteRowAtIndexPath(final IndexPath indexPath, float duration) {
        return deleteRowAtIndexPath(indexPath, 0, 0);
    }
    public boolean deleteRowAtIndexPath(final IndexPath indexPath, float duration, float delay) {
        // 일단 0 section만
        assert (indexPath.getIndex()>=0 && indexPath.getIndex()<=numberOfRowsInSection.numberOfRowsInSection(0));

        int lastIndex = Integer.MIN_VALUE;
        for (int col=0; col<_numContainer; col++) {
            lastIndex = Math.max(lastIndex, _column[col].getLastIndexPath().getIndex());
        }

        if (indexPath.getIndex() > lastIndex) {
            // 아직 생성되지 않은 Item이면 바로 지운다.
            return true;
        }

        FindCursorRet ret = findChildForIndexPath(indexPath);
        if (ret.retView==null) {
            // 해당 item 찾지 못함 or 이미 지워짐
            return false;
        }

        if (ret.retCursor._position>=_column[indexPath.getColumn()].getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0;
            delay = 0;
        }

        return performDelete(ret.retCursor, ret.retView, duration, delay);
    }

    public boolean resizeRowForCell(SMView child, float newSize) {
        return resizeRowForCell(child, newSize, 0);
    }
    public boolean resizeRowForCell(SMView child, float newSize, float duration) {
        return resizeRowForCell(child, newSize, 0, 0);
    }
    public boolean resizeRowForCell(SMView child, float newSize, float duration, float delay) {
        assert (child!=null);

        FindCursorRet ret = findCursorForChild(child);
        if (!ret.retBool) {
            // 해당 item 찾지 못함 or 이미 지워짐
            return false;
        }

        if (ret.retCursor._position >= _column[ret.retCursor.getIndexPath().getColumn()].getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0;
            delay = 0;
        }

        return performResize(ret.retCursor, child, newSize, duration, delay);
    }

    public boolean resizeRowForIndexPath(IndexPath indexPath, float newSize) {
        return resizeRowForIndexPath(indexPath, newSize, 0);
    }
    public boolean resizeRowForIndexPath(IndexPath indexPath, float newSize, float duration) {
        return resizeRowForIndexPath(indexPath, newSize, duration, 0);
    }
    public boolean resizeRowForIndexPath(IndexPath indexPath, float newSize, float duration, float delay) {
        FindCursorRet ret = findChildForIndexPath(indexPath);
        if (!ret.retBool) {
            return false;
        }

        if (ret.retCursor._position >= _column[ret.retCursor.getIndexPath().getColumn()].getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0;
            delay = 0;
        }

        return performResize(ret.retCursor, ret.retView, newSize, duration, delay);
    }

    public ArrayList<SMView> getVisibleCells() {
        return getVisibleCells(0);
    }
    public ArrayList<SMView> getVisibleCells(final int column) {
        assert (column>=0 && column<_numContainer);

        return _contentView[column].getChildren();
    }

    // current scroll position
    public float getScrollPosition() {
        return _scroller.getScrollPosition();
    }

    // scroll size... 이거는 끝까지 가봐야 안다... 정확하지 않음. (fixed인경우 계산에 의해 뱉어낼 수 있음)
    public float getScrollSize() {
        if (_scroller.getScrollSize() <= 0 && numberOfRowsInSection!=null) {
            measureScrollSize();
        }

        return _scroller.getScrollSize();
    }

    // for override
    // scroll to
    public void scrollTo(float position) {
        if (_scroller.getClass()==FlexibleScroller.class) {
            FlexibleScroller scroller = (FlexibleScroller)_scroller;
            if (position<0) {
                position = 0;
            } else {
                if (measureScrollSize()!=0) {
                    float scrollSize = scroller.getScrollSize();
                    if (position>scrollSize) {
                        position = scrollSize;
                    }
                }
            }

            scroller.scrollTo(position);
            scheduleScrollUpdate();
        }
    }

    public void scrollToWithDuration(float position) {
        scrollToWithDuration(position, 0);
    }
    public void scrollToWithDuration(float position, float duration) {
        if (_scroller.getClass()==FlexibleScroller.class) {
            FlexibleScroller scroller = (FlexibleScroller) _scroller;
            scroller.scrollToWithDuration(position, duration);
            scheduleScrollUpdate();
        }
    }

    // scroll by
    public void scrollBy(float offset) {
        scrollTo(_scroller.getScrollPosition() + offset);
    }

    public void scrollByWithDuration(float offset, float duration) {
        scrollToWithDuration(_scroller.getScrollPosition() + offset, duration);
    }

    // refresh data view (당겨서 새로고침 할때, 새로고침 뷰... 로딩뷰를 여기에 넣으면 됨)
    void setRefreshDataView(SMView cell, float triggerSize) {
        setRefreshDataView(cell, triggerSize, -1);
    }
    void setRefreshDataView(SMView cell, float triggerSize, float minSize) {
        if (_refreshView!=null && _refreshView!=cell) {
            super.removeChild(_refreshView, true);
            _refreshView = null;
        }

        _refreshView = cell;
        _refreshTriggerSize = triggerSize;
        if (minSize < 0) {
            _refreshMinSize = _refreshTriggerSize;
        } else {
            _refreshMinSize = minSize;
        }

        scheduleScrollUpdate();
    }

    // refresh 끝났음을 알려줘야한다. refresh data view가 들어가야하므로.
    public void endRefreshData() {
        if (_refreshView==null || _refreshState != RefreshState.REFRESHING)
            return;

        _refreshState = RefreshState.EXIT;

        _scroller.setHangSize(0);
        _scroller.onTouchUp();

        scheduleScrollUpdate();
    }

    // true이면 그만 부른다.
    public void endLoadData(boolean bNeedMore) {
        if (!bNeedMore) {
            // 더이상 로드할 데이터가 없음
            //        setFooterView(nullptr);
            if (onLoadDataCallback!=null) {
                onLoadDataCallbackTemp = onLoadDataCallback;
                onLoadDataCallback = null;
            }

            if (_footerView!=null && _isFooterInList) {
                super.removeChild(_footerView,  true);
            }
            _isFooterInList = false;
            _justAtLast = false;
        }
        _progressLoading = false;
        updateData();
    }

    // max scroll velocity
    public void setMaxScrollVelocity(final float maxVelocity) {
        float v = maxVelocity;
        if (v < AppConst.Config.MIN_VELOCITY) {
            v = AppConst.Config.MIN_VELOCITY+1;
        }
        _maxVelocicy = v;
    }

    public void enableAccelerateScroll(boolean enable) {
        _accelScrollEnable = enable;
    }

    public void enableInitRefresh(boolean enable) {
        _initRefreshEnable = true;
    }

    // virtual method from SMScroller
    @Override
    public void setHeaderView(SMView headerView) {
        super.setHeaderView(headerView);
        scheduleScrollUpdate();
    }

    @Override
    public void setFooterView(SMView footerView) {
        super.setFooterView(footerView);
        scheduleScrollUpdate();
    }

    public float getScrollSpeed() {
        return _scroller.getScrollSpeed();
    }

    public float getTotalHeightInSection(int section) {
        return _column[section].getLastCursor().getLocation();
    }

    // 페이지 끝까지 scroll 해야 전체 사이즈를 알 수 있기 때문에 fake로 끝까지 가본것 처럼 한다.
    public void fakeAdvanceLast(int index, float size) {
        _column[0].advanceViewLast(new IndexPath(index), 0, size);
        _column[0].retreatViewFirst();
    }

    public void fakeAdvanceLast2(int index, float size) {
        _column[0].advanceViewLast(new IndexPath(index), 0, size);
    }


    protected boolean isVertical() {return _orient==Orientation.VERTICAL;}
    protected boolean isHorizontal() {return _orient==Orientation.HORIZONTAL;}

    protected void initFixedColumnInfo(int numPages, float pageSize, int initPage) {
        for (int i=0; i<numPages; i++) {
            _column[0].advanceViewLast(new IndexPath(0, 0, i), -1, pageSize);
        }

        if (initPage==0) {
            _column[0].rewindViewLastCursor();
        } else {
            Cursor cursor = new Cursor(_column[0].getFirstCursor(initPage));
            _column[0].setViewCursor(cursor);
            _column[0].retreatViewLast();
        }
    }


    // member class

    private static final int ITEM_FLAG_DELETE = 1;
    private static final int ITEM_FLAG_RESIZE = 1<<1;
    private static final int ITEM_FLAG_INSERT = 1<<2;

    private class ReuseScrapper {
        private int _numberOfTypes = 0;
        private HashMap<String, Integer> _key = new HashMap<>();
        private ArrayList<ArrayList<SMView>> _data = new ArrayList<>();

        public int _internalReuseType;
        public SMView _internalReuseNode = null;

        public int getReuseType(final String reuseIdentifier) {
            int reuseType;
            Integer val = _key.get(reuseIdentifier);
            if (val==null) {
                // not exist?
                reuseType = _numberOfTypes;
                _key.put(reuseIdentifier, _numberOfTypes++);
                _data.add(new ArrayList<SMView>());
            } else {
                reuseType = val.intValue();
            }


            return reuseType;
        }

        public void scrap(final int reuseType, final SMView parent, final SMView child) {
            scrap(reuseType, parent, child, true);
        }
        public void scrap(final int reuseType, final SMView parent, final SMView child, final boolean cleanup) {
            ArrayList<SMView> queue = _data.get(reuseType);
            if (parent!=null) {
                parent.removeChild(child, cleanup);
            }

            if (child.getClass()!=_DeleteNode.class) {
                queue.add(child);
                _data.set(reuseType, queue);
            }
        }

        public void popBack(final int reuseType) {

            ArrayList<SMView> al = _data.get(reuseType);
            if (al.size()>0) {
                al.remove(al.size()-1);
            }
//
//            int lastIndex = _data.get(reuseType).size()-1;
//            _data.get(reuseType).remove(lastIndex);
        }

        public SMView back(final int reuseType) {

            ArrayList<SMView> al = _data.get(reuseType);
            if (al.size()>0) {
                return al.get(al.size()-1);
            }
            return null;

//
//            if (_data.get(reuseType).size()>0) {
//                int lastIndex = _data.get(reuseType).size()-1;
//                return _data.get(reuseType).get(lastIndex);
//            }
//
//            return null;
        }

        public void clear() {
            for (int i=0; i<_data.size(); i++) {
                _data.get(i).clear();
            }
        }
    }

    private class Item {
        public Item() {
            _indexPath = new IndexPath(0, 0);
            _reuseType = 0;
            _size = 0;
            _newSize = 0;
            _flags = 0;
            _state = null;
            _tag = 0;
            _reload = false;
            _dontReuse = false;
        }
        public Item(final IndexPath indexPath, final int type, final float size) {
            _indexPath.set(indexPath);
            _reuseType = type;
            _size = size;
            _newSize = size;
            _flags = 0;
            _state = null;
            _tag = 0;
            _reload = false;
            _dontReuse = false;
        }
        public Item(final Item item) {
            _indexPath.set(item._indexPath);
            _reuseType = item._reuseType;
            _size = item._size;
            _newSize = item._newSize;
            _flags = item._flags;
            _state = item._state;
            _tag = item._tag;
            _reload = item._reload;
            _dontReuse = item._dontReuse;
        }

        public Item get() {
            return this;
        }

        public IndexPath _indexPath = new IndexPath(0, 0);
        public int _reuseType;
        public int _tag;
        public float _size, _newSize;
        public int _flags;
        public boolean _reload;
        public boolean _dontReuse;

        public boolean isDeleted() {return (_flags & ITEM_FLAG_DELETE) != 0;}

        private SceneParams _state;
    }

    private class ItemIterator {
        public ItemIterator() {
            _data = null;
            _curIndex = -1;
        }
        public ItemIterator(ItemIterator iter) {
            setIterator(iter);
        }
        public ItemIterator(ArrayList<Item> data) {
            setIterator(data);
        }
        public ItemIterator(ArrayList<Item> data, int index) {
            setIterator(data, index);
        }

        public void setIterator(ItemIterator iter) {
            setIterator(iter._data, iter._curIndex);
        }
        public void setIterator(ArrayList<Item> data) {
            setIterator(data, 0);
        }
        public void setIterator(ArrayList<Item> data, int index) {
            _data = data;
            _curIndex = index;
        }

        public boolean isBegin() {
            return _curIndex==0;
        }

        public boolean isEnd() {
            boolean bEnd = _curIndex==_data.size();
            return bEnd;
        }

        public ItemIterator begin() {
            this._curIndex = 0;
            return this;
        }

        public ItemIterator end() {
            if (_data!=null) {
                this._curIndex = _data.size();
            } else {
                this._curIndex = 1;
            }
            return this;
        }

        public int getCurrentIndex() {return _curIndex;}

        public ItemIterator inc() {
            return inc(true);
        }
        public ItemIterator dec() {
            return dec(true);
        }
        public ItemIterator inc(boolean before) {
            if (before) {
                _curIndex++;
                return this;
            } else {
                ItemIterator tmp = new ItemIterator(this);
                this._curIndex++;
                return tmp;
            }
        }

        public ItemIterator dec(boolean before) {
            if (before) {
                _curIndex--;
                return this;
            } else {
                ItemIterator tmp = new ItemIterator(this);
                this._curIndex--;
                return tmp;
            }
        }

        public Item getItem() {
            if (_data!=null && _data.size()>0 && _curIndex>=0 && _curIndex<_data.size()) {
                return _data.get(_curIndex);
            }

            return null;
        }

        private int _curIndex = -1;
        private ArrayList<Item> _data = null;
    }

    private class Cursor {
        public Cursor() {
            _iter = new ItemIterator();
            _location = 0;
            _position = 0;
        }
        public Cursor(final Cursor cursor) {
            if (this==cursor) {
                return;
            }

            set(cursor);
        }

        public void set(final Cursor cursor) {
            if (this==cursor) {
                return;
            }
            this._iter = new ItemIterator(cursor.getIterator());
            this._location = cursor._location;
            this._position = cursor._position;
        }

        public boolean equal(final Cursor cursor) {
            return this._position==cursor._position;
        }

        public boolean notequal(final  Cursor cursor) {return this._position!=cursor._position;}

        public Cursor inc(boolean before) {
            if (before) {
                if (!isEnd()) {
                    ++_position;
                    _location += getItem()._size;
                    // iter를 다음으로 하나 늘린다.
                    _iter.inc(true);
                }

                return this;
            } else {
                Cursor tmp = new Cursor(this);
                if (!isEnd()) {
                    ++_position;
                    _location += getItem()._size;
                    _iter.inc(true);
                }
                return tmp;
            }
        }

        public Cursor dec(boolean before) {

            if (before) {
                if (!isBegin()) {
                    --_position;
                    _iter.dec(true);
                    _location -= getItem()._size;
                }

                return this;

            } else {
                Cursor tmp = new Cursor(this);
                if (!isBegin()) {
                    --_position;
                    _iter.dec(true);
                    _location -= getItem()._size;
                }

                return tmp;

            }
        }

        public void init(ArrayList<Item> data) {
            _iter = new ItemIterator(data).end();
            _position = 0;
            _location = 0;
        }

        public Cursor advance(int offset) {
            if (offset>0) {
                for (;offset>0; offset--) {
                    this.inc(true);
                }
            } else if (offset<0) {
                for (;offset<0; offset++) {
                    this.dec(true);
                }
            }

            return this;
        }

        public Item getItem() {
            return _iter.getItem();
        }


        public IndexPath getIndexPath() {
            return _iter.getItem()._indexPath;
        }
        public float getLocation() {return _location;}
        public float getLastLocation() {
            if (getItem()!=null) {
            return _location + getItem()._size;
        }
            return _location;
        }
        public void offsetLocation(final float offset) {_location += offset;}

        public int getPosition() {return _position;}
        public void offsetPosition(final int offset) {_position += offset;}
        public void setPosition(final int position) {
            _position = position;
        }

        public void offset(final int position, final float location) {
            _position += position; _location += location;
        }

        public ItemIterator getIterator() {return _iter;}
        public void setIterator(ItemIterator iter) {
            _iter.setIterator(iter._data, iter._curIndex);
        }
        public void incIterator() {
            _iter.inc(true);
        }
        public void decIterator() {
            _iter.dec(true);
        }
        public void incPosition() {++_position;}
        public void decPosition() {--_position;}
        public boolean isBegin() {
            return _iter.isBegin();
        }

        public boolean isEnd() {
            return _iter.isEnd();
        }

        public ItemIterator _iter = null;
        public int _position = 0;
        public float _location = 0.0f;
    }

    private class ColumnInfo {
        public ColumnInfo() {

        }

        public void init(SMTableView parent, int column) {
            _parent = parent;
            _column = column;

            _numAliveItem = 0;
            _lastIndexPath = new IndexPath(0, _column, 0);
            _resizeReserveSize = 0;

            _data.clear();

            _firstCursor.init(_data);
            _lastCursor.init(_data);
            _viewFirstCursor.init(_data);
            _viewLastCursor.init(_data);

            _buffer.clear();
        }

        public Cursor advanceViewFirst() {
            _viewFirstCursor.dec(true);
            return _viewFirstCursor;
        }

        public Cursor advanceViewLast(final IndexPath indexPath, final int type, final float size) {
            return advanceViewLast(indexPath, type, size, 0);
        }
        public Cursor advanceViewLast(final IndexPath indexPath, final int type, final float size, final int flags) {
            Cursor cursor = new Cursor();


            if (_data.size()==0) {
                // 첫번째 데이터
                _data.add(new Item(indexPath, type, size));

                _firstCursor.setIterator(new ItemIterator(_data));

                _lastCursor.set(_firstCursor);
                _lastCursor.inc(true);

                _viewFirstCursor.set(_firstCursor);
                _viewLastCursor.set(_lastCursor);
                _lastIndexPath.set(indexPath);
                _numAliveItem++;
                cursor.set(_viewFirstCursor);

            } else if (_viewLastCursor.isEnd()) {
                // 마지막이면 끝에 계속 Attach
                cursor.set(_viewLastCursor);

                int lastIndex = _viewLastCursor.getIterator().getCurrentIndex();
                _data.add(lastIndex, new Item(indexPath, type, size));

                cursor.setIterator(new ItemIterator(_data, lastIndex));

                _viewLastCursor.set(cursor);
                _lastCursor.set(_viewLastCursor.inc(true));

                _lastIndexPath.set(indexPath);
                _numAliveItem++;
            } else {
                // 처음도 아니고 마지막도 아니면 이미 생성되어 있는 어중간한 넘...
                cursor.set(_viewLastCursor.inc(false));
            }

            return cursor;
        }

        public Cursor retreatViewFirst() {
            _viewFirstCursor.inc(true);
            return _viewFirstCursor;
        }

        public Cursor retreatViewLast() {
            return _viewLastCursor.dec(true);
        }

        public Cursor getFirstCursor() {
            return _firstCursor;
        }

        public Cursor getLastCursor() {
            return _lastCursor;
        }

        public Cursor getViewFirstCursor() {
            return _viewFirstCursor;
        }

        public Cursor getViewLastCursor() {
            return _viewLastCursor;
        }

        public Cursor getFirstCursor(final int offset) {
            return _firstCursor.advance(offset);
        }

        public Cursor getLastCursor(final int offset) {
            return _lastCursor.advance(offset);
        }

        public Cursor getViewFirstCursor(final int offset) {
            return new Cursor(_viewFirstCursor).advance(offset);
        }

        public Cursor getViewLastCursor(final int offset) {
            return new Cursor(_viewLastCursor).advance(offset);
        }

        public void rewindViewLastCursor() {
            _viewLastCursor.set(_firstCursor);
        }

        public void setViewCursor(Cursor cursor) {
            _viewFirstCursor.set(cursor);
            _viewLastCursor.set(cursor);
            _viewLastCursor.inc(false);
        }

        public void resizeCursor(Cursor cursor) {
            if (cursor.getItem()._size!=cursor.getItem()._newSize) {
                Item item = cursor.getItem();

                float deltaSize = item._newSize - item._size;
                item._size = item._newSize;

                Cursor[] c = new Cursor[3];
                c[0] = new Cursor(_viewFirstCursor);
                c[1] = new Cursor(_viewLastCursor);
                c[2] = new Cursor(_lastCursor);
                for (int i=0; i<3; i++) {
                    if (c[i]._position > cursor._position) {
                        c[i].offsetLocation(deltaSize);
                    }
                }
                _viewFirstCursor.set(c[0]);
                _viewLastCursor.set(c[1]);
                _lastCursor.set(c[2]);

                resizeCursorBuffer(cursor, deltaSize);

                _resizeReserveSize -= deltaSize;
            }
        }

        public void markDeleteCursor(Cursor cursor) {

            assert (cursor.getItem()!=null);

            if (_data.size()==0) {
                return;
            }

            if (_column!=cursor.getIndexPath().getColumn()) {
                IndexPath indexPath = new IndexPath(cursor.getItem()._indexPath);

//                Cursor c;
                // 뒤에서 부터 찾아봄
                if (!_viewLastCursor.isEnd() && _viewLastCursor.getIndexPath().lessequal(indexPath)) {
//                    c = _viewLastCursor;
                    // 경계점까지 cursor 증가
                    for (;_viewLastCursor._position<_lastCursor._position && _viewLastCursor.getIndexPath().lessequal(indexPath);  _viewLastCursor.inc(true)) {

                    }


                    // 나머지 모든 index 감소시킴
                    while (!_viewLastCursor.getIterator().isEnd()) {
                        Item item = _viewLastCursor.getIterator().getItem();
                        item._indexPath.dec();
                        _viewLastCursor.getIterator().inc();
                    }

                } else if (!_viewFirstCursor.isEnd() && _viewFirstCursor.getIndexPath().lessequal(indexPath)) {
//                    c = _viewFirstCursor;
                    // 경계점까지 cursor 증가
                    for (;_viewFirstCursor._position<_lastCursor._position && _viewFirstCursor.getIndexPath().lessequal(indexPath); _viewFirstCursor.inc(true)) {

                    }


                    // 나머지 모든 index 감소시킴
                    while (!_viewFirstCursor.getIterator().isEnd()) {
                        Item item = _viewFirstCursor.getIterator().getItem();
                        item._indexPath.dec();
                        _viewFirstCursor.getIterator().inc();
                    }

                } else {
//                    c = _firstCursor;
                    // 경계점까지 cursor 증가
                    for (;_firstCursor._position<_lastCursor._position && _firstCursor.getIndexPath().lessequal(indexPath);  _firstCursor.inc(true)) {

                    }


                    // 나머지 모든 index 감소시킴
                    while (!_firstCursor.getIterator().isEnd()) {
                        Item item = _firstCursor.getIterator().getItem();
                        item._indexPath.dec();
                        _firstCursor.getIterator().inc();
                    }

                }

            } else {
                // 현재 컬럼에서 삭제
                _numAliveItem--;
                assert (_numAliveItem>=0);

                // Cursor 에 삭제된 아이템 표시
                cursor.getItem()._flags |= ITEM_FLAG_DELETE;

                Cursor c = new Cursor(cursor);
                c.inc(true);

                // 나머지 모든 index 감소시킴
                while (!c.getIterator().isEnd()) {
                    Item item = c.getIterator().getItem();
                    item._indexPath.dec();
                    c.getIterator().inc();
                }


                if (_numAliveItem==0) {
                    // 모든 아이템 삭제됨.
                    _lastIndexPath.set(0, 0, 0);
                }
            }

            // lastIndex 세팅
            if (_numAliveItem>0) {
                ListIterator<Item> iter = _data.listIterator(_data.size());

                while (iter.hasPrevious()) {
                    Item item = iter.previous();
                    if (!item.isDeleted()) {
                        _lastIndexPath.set(item._indexPath);
                        break;
                    }
                }
            }
        }

        public void deleteCursor(Cursor cursor) {
            deleteCursorBuffer(cursor);

            Cursor[] c = new Cursor[4];
            c[0] = _firstCursor;
            c[1] = _viewFirstCursor;
            c[2] = _viewLastCursor;
            c[3] = _lastCursor;

            for (int i=0; i<4; i++) {
                if (cursor._position < c[i]._position) {
                    c[i].offset(-1, -cursor.getItem()._size);
                } else if (cursor._position == c[i]._position) {
                    c[i].incIterator();
                }
            }
            _firstCursor.set(c[0]);
            _viewFirstCursor.set(c[1]);
            _viewLastCursor.set(c[2]);
            _lastCursor.set(c[3]);

            _data.remove(cursor.getItem());
        }

        public Cursor markInsertItem(final IndexPath indexPath) {
            Cursor cursor;

            if (_data.size() > 0) {
                // 다른 컬럼
                // index보다 큰거나 같은 item index + 1

//                Cursor c;
                // 뒤에서 부터 찾아봄
                if (!_viewLastCursor.isEnd() && _viewLastCursor.getIndexPath().lessequal(indexPath)) {
                    for (;_viewLastCursor._position<_lastCursor._position && _viewLastCursor.getIndexPath().lessthan(indexPath); _viewLastCursor.inc(true)) {

                    }

                    // 이 위치에 추가
                    cursor = new Cursor(_viewLastCursor);

                    // 나머지 모든 index 증시킴
                    while (!_viewLastCursor.getIterator().isEnd()) {
                        Item item = _viewLastCursor.getIterator().getItem();
                        item._indexPath.inc();
                        _viewLastCursor.getIterator().inc();
                    }

                } else if (!_viewFirstCursor.isEnd() && _viewFirstCursor.getIndexPath().lessequal(indexPath)) {
//                    c = _viewFirstCursor;
                    for (;_viewFirstCursor._position<_lastCursor._position && _viewFirstCursor.getIndexPath().lessthan(indexPath); _viewFirstCursor.inc(true)) {

                    }

                    // 이 위치에 추가
                    cursor = new Cursor(_viewFirstCursor);

                    // 나머지 모든 index 증가시킴
                    while (!_viewFirstCursor.getIterator().isEnd()) {
                        Item item = _viewFirstCursor.getIterator().getItem();
                        item._indexPath.inc();
                        _viewFirstCursor.getIterator().inc();
                    }

                } else {
//                    c = _firstCursor;
                    for (;_firstCursor._position<_lastCursor._position && _firstCursor.getIndexPath().lessthan(indexPath); _firstCursor.inc(true)) {

                    }

                    // 이 위치에 추가
                    cursor = new Cursor(_firstCursor);

                    // 나머지 모든 index 증가시킴
                    while (!_firstCursor.getIterator().isEnd()) {
                        Item item = _firstCursor.getIterator().getItem();
                        item._indexPath.inc();
                        _firstCursor.getIterator().inc();
                    }

                }

                for (int i=_data.size()-1; i>=0; --i) {
                    Item item = _data.get(i);
                    if (!item.isDeleted()) {
                        _lastIndexPath.set(item._indexPath);
                        break;
                    }
                }

            } else { // _data.size() == 0
                // 첫번째 새로운 데이터 (최초추가)
                cursor = new Cursor(_firstCursor);
            }

            return cursor;
        }

        public Cursor insertItem(final IndexPath indexPath, final int type, final float estimateSize) {
            Cursor cursor = new Cursor(markInsertItem(indexPath));

            if (indexPath.greaterthan(_lastIndexPath)) {
                _lastIndexPath.set(indexPath);
            }

            //cursor.setIterator(_data.insert(cursor.getIterator(), Item(indexPath, type, 0)));
            int lastIndex = cursor.getIterator().getCurrentIndex();
            _data.add(lastIndex, new Item(indexPath, type, 0));
            cursor.setIterator(new ItemIterator(_data, lastIndex));

            Cursor[] c = new Cursor[3];
            c[0].set(_viewFirstCursor);
            c[1].set(_viewLastCursor);
            c[2].set(_lastCursor);
            for (int i=0; i<3; i++) {
                if (c[i]._position >= cursor._position) {
                    c[i].offset(1, cursor.getItem()._size);
                }
            }
            _viewFirstCursor.set(c[0]);
            _viewLastCursor.set(c[1]);
            _lastCursor.set(c[2]);

            if (cursor.isBegin()) {
                _firstCursor.set(cursor);
            }

            _numAliveItem++;

            insertCursorBuffer(cursor);

            _resizeReserveSize += estimateSize;

            return cursor;
        }

        public boolean isAtFirst() {
            return _viewFirstCursor._position == _firstCursor._position;
        }

        public boolean isAtLast() { return _viewLastCursor._position == _lastCursor._position; }

        public int getAliveItemCount() { return _numAliveItem; }

        public IndexPath getLastIndexPath() { return _lastIndexPath; }

        public Cursor obtainCursorBuffer(Cursor cursor) {
            Cursor cloneCursor = new Cursor(cursor);
            _buffer.add(cloneCursor);
            return cloneCursor;
        }

        public void recycleCursorBuffer(Cursor cursor) {
            if (cursor!=null) {
                ListIterator<Cursor> iter = _buffer.listIterator();
                while (iter.hasNext()) {
                    Cursor c = iter.next();
                    if (c.equal(cursor)) {
                        _buffer.remove(c);
                        break;
                    }
                }
            }
        }

        public void resizeCursorBuffer(Cursor targetCursor, final float deltaSize) {
            ListIterator<Cursor> iter = _buffer.listIterator();
            while (iter.hasNext()) {
                Cursor c = iter.next();
                if (c._position>targetCursor._position) {
                    c.offsetLocation(deltaSize);
                }
            }
        }

        public void deleteCursorBuffer(Cursor targetCursor) {
            Item item = targetCursor.getItem();
            if (item!=null) {
                float size = item._size;

                ListIterator<Cursor> iter = _buffer.listIterator();
                while (iter.hasNext()) {
                    Cursor c = iter.next();

                    if (c._position > targetCursor._position) {
                        c.offset(-1, -size);
                    } else if (c._position==targetCursor._position) {
                        c.incIterator();
                    }
                }
            }
        }

        public void insertCursorBuffer(Cursor targetCursor) {
            Item item = targetCursor.getItem();
            if (item!=null) {
                float size = item._size;

                ListIterator<Cursor> iter = _buffer.listIterator();
                while (iter.hasNext()) {
                    Cursor c = iter.next();
                    if (c._position>=targetCursor._position) {
                        c.offset(+1, +size);
                    }
                }
            }
        }

        public ArrayList<Item> _data = new ArrayList<>();
        public Cursor _firstCursor = new Cursor();
        public Cursor _lastCursor = new Cursor();
        public Cursor _viewFirstCursor = new Cursor();
        public Cursor _viewLastCursor = new Cursor();
        public int _numAliveItem;
        public IndexPath _lastIndexPath = new IndexPath(0, 0);
        public int _column;
        public int _resizeReserveSize;
        private SMTableView _parent = null;
        private ArrayList<Cursor> _buffer = new ArrayList<>();

    } // ColumnInfo class


    private class  _DeleteNode extends SMView {
        public _DeleteNode(IDirector director) {
            super(director);
        }
    }


    // 각종 Animation Action
    private class _BaseAction extends ActionInterval {
        public _BaseAction(IDirector director) {
            super(director);
        }

        public SMView getChild() {
            ColumnInfo info = _parent._column[_col];
            int offset = _cursor.getPosition() - info.getViewFirstCursor().getPosition();
            if (_cursor._position < info.getViewFirstCursor()._position || _cursor._position >= info.getViewLastCursor()._position || _parent.getChildrenCount(_col) <= offset) {
                return null;
            }

            return _parent._contentView[_col].getChildren().get(offset);
        }

        @Override
        public void startWithTarget(SMView target) {
            super.startWithTarget(target);
            _startSize = _cursor.getItem()._size;
        }

        public void complete() {}

        protected int _col;
        protected float _startSize;
        protected Cursor _cursor = new Cursor();
        SMTableView _parent = null;
    }

    private class _DeleteAction extends _BaseAction {
        public _DeleteAction(IDirector director, SMTableView parent, int column, Cursor cursor) {
            super(director);
            _parent = parent;
            _col = column;
            _cursor = parent._column[column].obtainCursorBuffer(cursor);
        }

        @Override
        public void update(float t) {
            Item item = _cursor.getItem();
            item._newSize = _startSize * (1-t);
            SMView child = getChild();

            if (child!=null) {
                if (_parent.onCellResizeCallback!=null) {
                    _parent.onCellResizeCallback.onCellResizeCallback(child, item._newSize);
                }

                if (t<1) {
                    if (_parent.onCellDeleteCallback!=null) {
                        onCellDeleteCallback.onCellDeleteCallback(child, t);
                    }
                }
            } else {
                child = _parent.findFromHolder(item.hashCode());
                if (child!=null) {
                    if (_parent.onCellResizeCallback!=null) {
                        _parent.onCellResizeCallback.onCellResizeCallback(child, item._newSize);
                    }

                    if (_parent.onCellDeleteCallback!=null) {
                        _parent.onCellDeleteCallback.onCellDeleteCallback(child, t);
                    }
                }
            }

            if (t<1) {
                _parent._column[_col].resizeCursor(_cursor);
                _parent.scheduleScrollUpdate();
                _parent._animationDirty = true;
            } else {
                complete();
            }
        }

        @Override
        public void complete() {
            Item item = _cursor.getItem();

            SMView childInHolder = _parent.findFromHolder(item.hashCode());
            if (childInHolder!=null) {
                _parent._reuseScrapper.scrap(item._reuseType, null, childInHolder);
                _parent.eraseFromHolder(item.hashCode());
            }

            _parent._column[_col].resizeCursor(_cursor);
            _parent.deleteCursor(_col, _cursor);
            _parent._column[_col].recycleCursorBuffer(_cursor);

            item._tag = 0;
            item._flags = 0;

            _parent.scheduleScrollUpdate();
            _parent._animationDirty = true;

            if (_parent.onCellDeleteCompletionCallback!=null) {
                _parent.onCellDeleteCompletionCallback.onCellDeleteCompletionCallback();
            }
        }
    }

    private class _ResizeAction extends _BaseAction {
        public _ResizeAction(IDirector director, SMTableView parent, int column, Cursor cursor, float newSize) {
            super(director);
            _parent = parent;
            _col = column;
            _cursor = parent._column[column].obtainCursorBuffer(cursor);
            _newSize = newSize;
            _insert = false;
        }

        public SMView updateResize(float t) {
            Item item = _cursor.getItem();
            item._newSize = _startSize + (_newSize - _startSize) * t;
            _parent._column[_col].resizeCursor(_cursor);

            SMView child = getChild();

            if (child!=null) {
                if (_parent.onCellResizeCallback!=null) {
                    _parent.onCellResizeCallback.onCellResizeCallback(child, item._newSize);
                }

                if (_insert && _parent.onCellInsertCallback!=null) {
                    _parent.onCellInsertCallback.onCellInsertCallback(child, t);
                }
            }

            _parent.scheduleScrollUpdate();
            _parent._animationDirty = true;

            return child;
        }

        @Override
        public void update(float t) {
            if (t<1) {
                updateResize(t);
            } else {
                complete();
            }
        }

        @Override
        public void complete() {
            Item item = _cursor.getItem();

            SMView child = updateResize(1);

            if (child==null) {
                // 완료시 화면에 child가 없으면 다음번 add될때 최종 resize한다.
                item._flags = ITEM_FLAG_RESIZE;
                if (_insert) {
                    item._flags |= ITEM_FLAG_INSERT;
                }
            } else {
                item._flags = 0;
            }
            item._tag = 0;

            _parent._column[_col].recycleCursorBuffer(_cursor);
            _parent.scheduleScrollUpdate();
            _parent._animationDirty = true;

            if (_parent.onCellResizeCompletionCallback!=null) {
                _parent.onCellResizeCompletionCallback.onCellResizeCompletionCallback(child);
            }
        }

        protected float _newSize = 0;
        protected boolean _insert = false;
    }

    private class _InsertAction extends  _ResizeAction {
        public _InsertAction(IDirector director, SMTableView parent, int column, Cursor cursor, float newSize) {
            super(director, parent, column, cursor, newSize);

            _parent = parent;
            _col = column;
            _cursor = parent._column[column].obtainCursorBuffer(cursor);
            _newSize = newSize;
            _insert = true;
        }
    }

    private class _DelaySequence extends Sequence {
        public _DelaySequence(IDirector director, float delay, _BaseAction action) {
            super(director);
            initWithTwoActions(DelayTime.create(getDirector(), Math.max(0.0f, delay)), action);
            _action = action;
        }

        private _BaseAction _action;
        public _BaseAction getAction() {
            return _action;
        }
    }

    private class _PageJumpAction extends _BaseAction {
        public _PageJumpAction(IDirector director, SMTableView parent, Cursor cursor, float pageSize, int fromPage, int toPage, int direction) {
            super(director);
            _parent = parent;
            _cursor = parent._column[0].obtainCursorBuffer(cursor);
            _pageSize = pageSize;
            _fromPage = fromPage;
            _toPage = toPage;
            _direction = direction;

        }

        @Override
        public void update(float t) {
            float position;
            if (_direction<0) {
                position = -_pageSize + _pageSize*t;
                for (int i=0; i<2; i++) {
                    if (i<_parent._contentView[0].getChildrenCount()) {
                        _parent.getChildAt(0, i).setPositionX(position);
                    }
                    position += _pageSize;
                }
            } else {
                position = -_pageSize*t;
                for (int i=0; i<2; i++) {
                    if (i<_parent._contentView[0].getChildrenCount()) {
                        _parent.getChildAt(0, i).setPositionX(position);
                    }
                    position += _pageSize;
                }
            }

            float p1 = _fromPage*_pageSize;
            float p2 = _toPage*_pageSize;
            float scrollPosition = p1 + t * (p2-p1);
            _parent.onScrollChanged(scrollPosition, 0);

            if (t>=1.0) {
                complete();
            }
        }

        @Override
        public void complete() {
            SMView child = null;

            int index = _direction>0 ? 0 : 1;
            if (index<_parent.getChildrenCount(0)) {
                child = _parent.getChildAt(0, index);
            }

            if (child!=null) {
                Item item = _cursor.getItem();
                _parent.removeChildAndReuseScrap(0, item._reuseType, child, true);
            }

            _parent._column[0].setViewCursor(_cursor);
            _parent._column[0].recycleCursorBuffer(_cursor);

            _parent._forceJumpPage = false;
            _parent._currentPage = _toPage;
            _parent._scroller.setScrollPosition(_pageSize*_toPage);
            _parent.scheduleScrollUpdate();
        }

        private float _pageSize;
        private int _fromPage;
        private int _toPage;
        private int _direction;
    }

    private ColumnInfo[] _column = null;
    private int _columnIndex = 0;
    private Orientation _orient = Orientation.VERTICAL;
    private long _lastItemCount = 0;
    private float _firstMargin = 0;
    private float _lastMargin = 0;
    private float _preloadPadding = 0;
    private float _lastScrollPosition = 0;
    private float _hintFixedChildSize = 0;
    private boolean _hintIsFixedSize = false;
    private boolean _justAtLast = false;
    private boolean _canExactScrollSize = false;
    private int _internalActionTag = AppConst.TAG.ACTION_LIST_ITEM_DEFAULT;
    private ReuseScrapper _reuseScrapper = null;
    private SMView _refreshView = null;
    private float _refreshTriggerSize = 0;
    private float _refreshMinSize = 0;
    private float _refreshSize = 0;
    private float _lastRefreshSize = 0;
    private RefreshState _refreshState = RefreshState.NONE;
    private RefreshState _lastRefreshState = RefreshState.NONE;
    private boolean _progressLoading = false;
    private boolean _animationDirty = false;
    private float _firstMotionTime = 0;
    private float _lastMotionX = 0;
    private float _lastMotionY = 0;
    private float _deltaScroll = 0;
    private boolean _needUpdate = false;
    private boolean _skipUpdateOnVisit = false;
    private boolean _forceJumpPage = false;
    private boolean _touchFocused = false;
    private boolean _fillWithCellsFirstTime = false;
    private float _maxVelocicy = AppConst.Config.MAX_VELOCITY;
    private boolean _accelScrollEnable = false;
    private float _lastVelocityX = 0;
    private float _lastVelocityY = 0;
    private float _lastFlingTime = 0;
    private int _accelCount = 0;
    private boolean _initRefreshEnable = false;
    private boolean _reloadExceptHeader = false;

    protected int _currentPage;

    public class FindCursorRet {
        public FindCursorRet(Cursor cursor, boolean bool) {
            this(cursor, bool, null);
        }

        public FindCursorRet(Cursor cursor, boolean bool, SMView view) {
            retCursor = cursor;
            retBool = bool;
            retView = view;
        }

        public Cursor retCursor = null;
        public boolean retBool = false;
        public SMView retView = null;
    }


    private void removeChildAndReuseScrap(final int container, final int reuseType, SMView child, final boolean cleanup) {
        if (reuseType >= 0) {
            _reuseScrapper.scrap(reuseType, _contentView[container], child, cleanup);
        } else {
            removeChild(container, child, cleanup);
        }
    }


}
