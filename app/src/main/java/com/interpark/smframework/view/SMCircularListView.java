package com.interpark.smframework.view;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.ICircularCell;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.scroller.FinityScroller;
import com.interpark.smframework.base.scroller.InfinityScroller;
import com.interpark.smframework.base.scroller.PageScroller;
import com.interpark.smframework.base.scroller.SMScroller;
import com.interpark.smframework.base.scroller.SMScroller.ScrollMode;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;

import org.apache.http.cookie.SM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;

public class SMCircularListView extends SMView implements SMScroller.ALIGN_CALLBACK {

    private static int ACTION_TAG_CONSUME = 1000;
    private static int ACTION_TAG_POSITION = 1001;


    public SMCircularListView(IDirector director) {
        super(director);
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    public static class Config {
        public Orientation orient;
        public boolean circular;
        public float cellSize;
        public float windowSize;
        public float anchorPosition;
        public float preloadPadding;
        public float maxVelocity;
        public float minVelocity;
        public ScrollMode scrollMode;

        public Config() {
            orient = Orientation.HORIZONTAL;
            circular = true;
            anchorPosition = 0;
            preloadPadding = 0;
            maxVelocity = 5000;
            minVelocity = 0;
            scrollMode = ScrollMode.BASIC;
        }
    }

    public static SMCircularListView create(IDirector director, final Config config) {
        SMCircularListView listView = new SMCircularListView(director);
        listView.initWithConfig(config);
        return listView;
    }

    public void setCircularConfig(Config config) {
        _config = config;
    }

    public SMView dequeueReusableCellWithIdentifier(final String identifier) {
        _reuseScrapper._internalReuseIdentifier = identifier;
        _reuseScrapper._internalReuseNode = _reuseScrapper.back(identifier);

        return _reuseScrapper._internalReuseNode;
    }

    public interface CellForRowsAtIndex {
        public SMView cellForRowsAtIndex(final int index);
    }
    public CellForRowsAtIndex cellForRowsAtIndex = null;

    public int getIndexForCell(SMView cell) {
        if (numberOfRows==null) return -1;

        ArrayList<SMView> children = getChildren();
        for (SMView child : children) {
            if (cell==child) {
                ICircularCell iCell = (ICircularCell)child;
                if (iCell!=null) {
                    return convertToIndex(iCell.getCellIndex(), numberOfRows.numberOfRows());
                }
            }
        }

        return -1;
    }

    public interface PositionCell {
        public void positionCell(SMView cell, float position, boolean created);
    }
    public PositionCell positionCell = null;

    public interface InitFillWithCells {
        public void initFillWithCells();
    }
    public InitFillWithCells initFillWithCells = null;

    public interface PageScrollCallback {
        public void pageScrollCallback(float pagePosition);
    }
    public PageScrollCallback pageScrollCallback = null;

    public boolean deleteCell(SMView target, float deleteDt, float deleteDelay, float positionDt, float positionDelay) {
        return deleteCell(getIndexForCell(target), deleteDt, deleteDelay, positionDt, positionDelay);
    }
    public boolean deleteCell(int targetIndex, float deleteDt, float deleteDelay, float positionDt, float positionDelay) {
        int numRows = numberOfRows.numberOfRows();

        if (numRows<=1 || targetIndex<0 || targetIndex>=numRows) return false;

        _deleteIndex = convertToIndex(targetIndex, numRows);

        ArrayList<SMView> children = getChildren();

        assert (children.size()>0);

        if (children.size()==0) return false;

        ArrayList<ICircularCell> cells = new ArrayList<>();

        for (SMView child : children) {
            ICircularCell iCell = (ICircularCell)child;
            if (iCell!=null) {
                cells.add(iCell);
            }
        }

        sortFunc(cells);

        int realIndex = cells.get(0).getCellIndex();
        int index = convertToIndex(realIndex, numRows);
        int diff = realIndex - index;

        int deleteCount = 0;
        for (ICircularCell iCell : cells) {
            int idx = convertToIndex(iCell.getCellIndex(), numRows);
            if (idx==targetIndex) {
                iCell.markDelete();
                deleteCount++;
            }

            iCell.setCellIndex(iCell.getCellIndex()-diff-deleteCount);
        }

        float scrollPosition = _scroller.getScrollPosition();
        float maxScrollSize = _config.cellSize*numRows + _config.preloadPadding*2;
        float adjPosition = scrollPosition - _config.anchorPosition - _config.preloadPadding;
        float norPosition;

        if (adjPosition>0) {
            norPosition = adjPosition % maxScrollSize;
        } else {
            norPosition = (maxScrollSize-Math.abs(adjPosition % maxScrollSize)) % maxScrollSize;
        }

        _scroller.setScrollPosition(norPosition);

        int count = deleteCount;
        ICircularCell lastCell = cells.get(cells.size()-1);
        realIndex = lastCell.getCellIndex()+2;
        float position = lastCell.getCellPosition() + _config.cellSize;

        for (; count>0;) {
            index = convertToIndex(realIndex, numRows);

            if (index!=targetIndex) {
                _reuseScrapper._internalReuseIdentifier = "";
                _reuseScrapper._internalReuseNode = null;

                SMView cell = cellForRowsAtIndex.cellForRowsAtIndex(index);

                if (cell!=null) {
                    ICircularCell iCell = (ICircularCell)cell;
                    iCell.setCellIndex(realIndex);
                    iCell.setReuseIdentifier(_reuseScrapper._internalReuseIdentifier);
                    addChild(cell);

                    if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==cell) {
                        _reuseScrapper.popBack(_reuseScrapper._internalReuseIdentifier);
                    }

                    iCell.setCellPosition(position);
                    if (positionCell!=null) {
                        positionCell.positionCell(cell, position, true);
                    }

                    position += _config.cellSize;
                }

                _reuseScrapper._internalReuseIdentifier = "";
                _reuseScrapper._internalReuseNode = null;

                if (cell==null) break;;

                count--;
            }

            realIndex++;
        }

        children = getChildren();
        cells.clear();

        for (SMView child : children) {
            ICircularCell iCell = (ICircularCell)child;
            if (iCell!=null) {
                cells.add(iCell);
            }
        }

        sortFunc(cells);

        position = cells.get(0).getCellPosition();

        ArrayList<SMView> deleteCells = new ArrayList<>();
        ArrayList<SMView> remainCells = new ArrayList<>();


        for (ICircularCell iCell : cells) {
            index = convertToIndex(iCell.getCellIndex(), numRows);
            SMView child = (SMView)iCell;

            if (iCell.isDeleted()) {
                iCell.setAniSrc(iCell.getCellPosition());
                iCell.setAndDst(position);
                iCell.setAniIndex(iCell.getCellIndex());
                position += _config.cellSize;

                remainCells.add(child);
            } else {
                deleteCells.add(child);
            }
        }

        CellsAction deleteAction = CellsActionCreate(getDirector());
        deleteAction.setTag(ACTION_TAG_CONSUME);
        deleteAction.setTargetCells(deleteCells);
        deleteAction.setTimeValue(deleteDt, deleteDelay);
        runAction(deleteAction);

        CellsAction positionAction = CellsActionCreate(getDirector());
        positionAction.setTag(ACTION_TAG_POSITION);
        positionAction.setTargetCells(remainCells);
        positionAction.setTimeValue(positionDt, positionDelay);
        runAction(positionAction);

        _actionLock = true;
        return true;
    }

    public interface NumberOfRows {
        public int numberOfRows();
    }
    public NumberOfRows numberOfRows=null;

    public interface ScrollAlignedCallback {
        public void scrollAlignedCallback(boolean aligned, int index, boolean force);
    }
    public ScrollAlignedCallback scrollAlignedCallback=null;

    public interface CellDeleteUpdate {
        public void cellDeleteUpdate(SMView cell, float dt);
    }
    public CellDeleteUpdate cellDeleteUpdate=null;

    public ArrayList<SMView> getVisibleCells() {
        return getChildren();
    }

    public void stop() {
        stop(true);
    }
    public void stop(boolean align) {
        unscheduleScrollUpdate();

        _scroller.onTouchDown();
        if (align) {
            _scroller.onTouchUp();
        }
    }

    public void setScrollPosition(float position) {
        _scroller.setScrollPosition(position);
        if (numberOfRows!=null) {
            positionChildren(position, numberOfRows.numberOfRows());
        }
    }

    public void scrollByWithDuration(float distance, float dt) {
        if (Math.abs(distance)>0) {
            ((InfinityScroller)_scroller).scrollByWithDuration(distance, dt);
            scheduleScrollUpdate();
        }
    }

    public void runFling(float velocity) {
        _scroller.onTouchFling(velocity, 0);
        scheduleScrollUpdate();
    }

    public int getAlignedIndex() {
        float scrollPosition = ((int)(_scroller.getNewScrollPosition()*10))/10.0f;

        int index;
        if (scrollPosition>=0) {
            index = (int)(scrollPosition/_config.cellSize);
        } else {
            index = (int) Math.floor(scrollPosition/_config.cellSize);
        }

        return index;
    }

    public void updateData() {
        scheduleScrollUpdate();
    }

    private float _firstMotionTime = 0;

    @Override
    public int dispatchTouchEvent(MotionEvent event) {
        if (_actionLock) return TOUCH_TRUE;


        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        Vec2 point = new Vec2(x, y);

        if (!_inScrollEvent && _scroller.isTouchable()) {
            if (action==MotionEvent.ACTION_DOWN && _scroller.getState()!=SMScroller.STATE.STOP) {
                scheduleScrollUpdate();
            }
            int ret = super.dispatchTouchEvent(event);
            if (ret==TOUCH_INTERCEPT) {
                return ret;
            }
        }

        if (_velocityTracker==null) {
            _velocityTracker = VelocityTracker.obtain();
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            {
                _inScrollEvent = false;
                _touchFocused = true;

                _lastMotionX = x;
                _lastMotionY = y;
                _firstMotionTime = _director.getGlobalTime();

                _scroller.onTouchDown();

                _velocityTracker.addMovement(event);
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            {
                _touchFocused = false;

                if (_inScrollEvent) {
                    _inScrollEvent = false;

                    float vx = _velocityTracker.getXVelocity(0);
                    float vy = _velocityTracker.getYVelocity(0);

//                    // Velocity tracker에서 계산되지 않았을때 보정..
//                    if (vx==0 && vy==0) {
//                        float dt = _director.getGlobalTime() - _firstMotionTime;
//                        if (dt > 0) {
//                            Vec2 p1 = _touchStartPosition;
//                            Vec2 p2 = point;
//                            vx = -(p2.x - p1.x) / dt;
//                            vy = -(p2.y - p1.y) / dt;
//                        }
//                    }

//                    // Accelate scroll
//                    float maxVelocity = _maxVelocicy;
//                    if (_accelScrollEnable) {
//                        float dt = _director.getGlobalTime() - _firstMotionTime;
//                        if (dt < 0.15 && _accelCount > 3) {
//                            maxVelocity *= (_accelCount-2);
//                        }
//                    }

                    if (isVertical()) {
                        if (Math.abs(vy)>200) {
                            if (Math.abs(vy)>_config.maxVelocity) {
                                vy = SMView.signum(vy) * _config.maxVelocity;
                            } else if (_config.minVelocity>0 && Math.abs(vy)<_config.minVelocity) {
                                vy = SMView.signum(vy) * _config.minVelocity;
                            }
                            _scroller.onTouchFling(-vy, _currentPage);
                        } else {
                            _scroller.onTouchUp();
                        }
                    } else {
                        if (Math.abs(vx)>200) {
                            if (Math.abs(vx) > _config.maxVelocity) {
                                vx = SMView.signum(vx) * _config.maxVelocity;
                            } else if (_config.minVelocity>0 && Math.abs(vx)<_config.minVelocity) {
                                vx = SMView.signum(vx) * _config.minVelocity;
                            }
                            _scroller.onTouchFling(-vx, _currentPage);
                        } else {
                            _scroller.onTouchUp();
                        }
                    }
                    scheduleScrollUpdate();
                } else {
                    _scroller.onTouchUp();
                    scheduleScrollUpdate();
                }

                _velocityTracker.clear();
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {
                _velocityTracker.addMovement(event);
                float deltaX;
                float deltaY;

                if (!_inScrollEvent) {
                    deltaX = x - _lastMotionX;
                    deltaY = y - _lastMotionY;
                } else {
                    deltaX = point.x - _touchPrevPosition.x;
                    deltaY = point.y - _touchPrevPosition.y;
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
                            cancelTouchEvent(_touchMotionTarget, event);
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

    protected boolean initWithConfig(Config config) {
        _config = config;

        _reuseScrapper = new ReuseScrapper();

        if (_config.circular) {
            _scroller = new InfinityScroller(getDirector());
        } else {
            _scroller = new FinityScroller(getDirector());
        }

        _scroller.setCellSize(_config.cellSize);
        _scroller.setWindowSize(_config.windowSize);
        _scroller.setScrollMode(_config.scrollMode);
        _scroller.onAlignCallback = this;

        _lastScrollPosition = _scroller.getScrollPosition();
//        _velocityTracker = VelocityTracker.obtain();
//        _velocityTracker.clear();;

        scheduleScrollUpdate();

        return true;
    }

    protected void scheduleScrollUpdate() {
        registerUpdate(VIEWFLAG_POSITION);
    }

    protected void unscheduleScrollUpdate() {
        unregisterUpdate(VIEWFLAG_POSITION);
    }

    @Override
    protected void onUpdateOnVisit() {
        if (_contentSize.width<=0 || _contentSize.height<=0 || _actionLock) {
            return;
        }

        int numRows = numberOfRows.numberOfRows();

        _scroller.setScrollSize(_config.cellSize * numRows);

        boolean updated = _scroller.update();

        float scrollPosition = _scroller.getScrollPosition();

        positionChildren(scrollPosition, numRows);

        if (!updated) {
            unscheduleScrollUpdate();
        }

        _deltaScroll = _lastScrollPosition - scrollPosition;
        _lastScrollPosition = scrollPosition;

        if (!_fillWithCellsFirstTime) {
            _fillWithCellsFirstTime = true;
            if (initFillWithCells!=null) {
                initFillWithCells.initFillWithCells();
            }
        }
    }

    protected boolean isVertical() {return _config.orient==Orientation.VERTICAL;}
    protected boolean isHorizontal() {return _config.orient==Orientation.HORIZONTAL;}

    protected int convertToIndex(int realIndex, int numRows) {
        int index;
        if (realIndex>=0) {
            index = realIndex % numRows;
        } else {
            index = (numRows - Math.abs(realIndex%numRows)) % numRows;
        }

        return index;
    }

    protected void onCellAction(int tag, ArrayList<SMView> cells, float dt, boolean complete) {
        if (tag==ACTION_TAG_CONSUME) {
            if (complete) {
                for (SMView cell : cells) {
                    removeChild(cell);
                }
            } else {
                if (cellDeleteUpdate!=null) {
                    for (SMView child : cells) {
                        cellDeleteUpdate.cellDeleteUpdate(child, dt);
                    }
                }
            }
        } else if (tag==ACTION_TAG_POSITION) {
            if (complete) {
                for (SMView cell : cells) {
                    ICircularCell iCell = (ICircularCell)cell;
                    if (iCell!=null) {
                        iCell.setCellIndex(iCell.getAniIndex());
                    }
                }

                float scrollPosition = _config.cellSize*convertToIndex(_deleteIndex, numberOfRows.numberOfRows());
                _scroller.setScrollPosition(scrollPosition);

                if (scrollAlignedCallback!=null) {
                    scrollAlignedCallback.scrollAlignedCallback(true, _deleteIndex, true);
                }

                _actionLock = false;
            } else {
                dt = tweenfunc.cubicEaseOut(dt);
                for (SMView cell : cells) {
                    ICircularCell iCell = (ICircularCell)cell;
                    float x = SMView.interpolation(iCell.getAniSrc(), iCell.getAniDst(), dt);
                    iCell.setCellPosition(x);
                    if (positionCell!=null) {
                        positionCell.positionCell(cell, x, false);
                    }
                }
            }
        }
    }

    @Override
    public void onAlignCallback(boolean aligned) {
        if (scrollAlignedCallback!=null) {
            if (aligned) {
                _currentPage = getAlignedIndex();

                if (!_config.circular) {
                    if (_currentPage<0) {
                        _currentPage = 0;
                    } else if (numberOfRows!=null && _currentPage>=numberOfRows.numberOfRows()) {
                        _currentPage = numberOfRows.numberOfRows() -1;
                    }
                }

                scrollAlignedCallback.scrollAlignedCallback(aligned, _currentPage, false);
            } else {
                scrollAlignedCallback.scrollAlignedCallback(aligned, 0, false);
            }
        }
    }

    protected float getListAnchorX() {return _config.anchorPosition;}

    private void positionChildren(float scrollPosition, int numRows) {
        scrollPosition = ((int)(scrollPosition*10))/10.0f;

        float maxScrollSize = _config.cellSize * numRows + _config.preloadPadding*2;
        float adjPosition = scrollPosition - _config.anchorPosition - _config.preloadPadding;
        float norPosition;

        if (adjPosition>=0) {
            norPosition = adjPosition % maxScrollSize;
        } else {
            norPosition = (maxScrollSize - Math.abs(adjPosition % maxScrollSize)) % maxScrollSize;
        }

        float xx = -(norPosition % _config.cellSize);

        int start, end;
        if (adjPosition>=0) {
            start = (int)(adjPosition / _config.cellSize);
        } else {
            start = (int)Math.floor(adjPosition/_config.cellSize);
        }

        end = start + (int)Math.ceil(_config.windowSize/_config.cellSize);
        if (xx + _config.cellSize * (end-start) < _config.windowSize) {
            end++;
        }

        int first = end;
        int last = start;

        ArrayList<SMView> children = getChildren();
        ArrayList<SMView> tmpList = new ArrayList<>(children);

        for (SMView child : tmpList) {
            ICircularCell iCell = (ICircularCell)child;

            if (iCell!=null && !iCell.isDeleted()) {
                int realIndex = iCell.getCellIndex();
                if (realIndex>=start && realIndex<end) {
                    float x = xx + (realIndex - start) * _config.cellSize;
                    iCell.setCellPosition(x);

                    if (positionCell!=null) {
                        positionCell.positionCell(child, x, false);
                    }

                    first = Math.min(first, realIndex);
                    last = Math.max(last, realIndex);
                } else {
                    String reuseIdentifier = iCell.getCellIdentifier();
                    if (reuseIdentifier.isEmpty()) {
                        removeChild(child, true);
                    } else {
                        _reuseScrapper.scrap(reuseIdentifier, this, child, true);
                    }
                }
            }
        }
        tmpList.clear();
        tmpList = null;

        // fill front
        while (first>start) {
            int realIndex = first-1;

            if (!_config.circular && (realIndex<0 || realIndex>=numRows)) {
                first--;
                last = Math.max(last, realIndex+1);
                continue;
            }

            int index = convertToIndex(realIndex, numRows);

            _reuseScrapper._internalReuseIdentifier = "";
            _reuseScrapper._internalReuseNode = null;

            SMView cell = cellForRowsAtIndex.cellForRowsAtIndex(index);
            ICircularCell iCell = (ICircularCell)cell;

            // cicular list view cell is must implements ICircularCell...!!!!
            assert (iCell!=null);

            if (iCell!=null) {
                iCell.setCellIndex(realIndex);
                iCell.setReuseIdentifier(_reuseScrapper._internalReuseIdentifier);
                addChild(cell);

                if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==cell) {
                    _reuseScrapper.popBack(_reuseScrapper._internalReuseIdentifier);
                }

                float x = xx + (realIndex-start) * _config.cellSize;
                iCell.setCellPosition(x);

                if (positionCell!=null) {
                    positionCell.positionCell(cell, x, true);
                }
            }

            _reuseScrapper._internalReuseIdentifier = "";
            _reuseScrapper._internalReuseNode = null;

            if (cell==null) {
                break;
            }

            first--;
            last = Math.max(last, realIndex+1);
        }

        // fill back
        while (last+1<end) {
            int realIndex = last+1;

            if (!_config.circular && (realIndex<0 || realIndex>=numRows)) {
                last++;
                continue;
            }

            int index = convertToIndex(realIndex, numRows);

            _reuseScrapper._internalReuseIdentifier = "";
            _reuseScrapper._internalReuseNode = null;

            SMView cell = cellForRowsAtIndex.cellForRowsAtIndex(index);
            ICircularCell iCell = (ICircularCell)cell;

            assert (iCell!=null);

            if (iCell!=null) {
                iCell.setCellIndex(realIndex);
                iCell.setReuseIdentifier(_reuseScrapper._internalReuseIdentifier);
                addChild(cell);

                if (_reuseScrapper._internalReuseNode!=null && _reuseScrapper._internalReuseNode==cell) {
                    _reuseScrapper.popBack(_reuseScrapper._internalReuseIdentifier);
                }

                float x = xx + (realIndex-start) * _config.cellSize;
                iCell.setCellPosition(x);

                if (positionCell!=null) {
                    positionCell.positionCell(cell, x, true);
                }
            }

            _reuseScrapper._internalReuseIdentifier = "";
            _reuseScrapper._internalReuseNode = null;

            if (cell==null) {
                break;
            }

            last++;
        }

        if (pageScrollCallback!=null) {
            float position = (((_config.anchorPosition+norPosition)/_config.cellSize) % numRows);
            pageScrollCallback.pageScrollCallback(position);
        }

    }

    private static void sortFunc(ArrayList<ICircularCell> cells) {
        Collections.sort(cells, new Comparator<ICircularCell>() {
            @Override
            public int compare(ICircularCell l, ICircularCell r) {
                return l.getCellPosition() < r.getCellPosition() ? -1 : l.getCellPosition() > r.getCellPosition() ? 1 : 0;
            }
        });
    }

    private CellsAction CellsActionCreate(IDirector director) {
        CellsAction action = new CellsAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class CellsAction extends TransformAction {
        public CellsAction(IDirector director) {
            super(director);
        }

        @Override
        public void onUpdate(float t) {
            super.onUpdate(t);
            ((SMCircularListView)_target).onCellAction(getTag(), _cells, t, false);
        }

        @Override
        public void onEnd() {
            ((SMCircularListView)_target).onCellAction(getTag(), _cells, 1, true);
        }

        public void setTargetCells(ArrayList<SMView> cells) {
            _cells = cells;
        }

        public ArrayList<SMView> _cells = null;

    }

    private CellPositionAction CellPositionActionCreate(IDirector director) {
        CellPositionAction action = new CellPositionAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class CellPositionAction extends TransformAction {
        public CellPositionAction(IDirector director) {
            super(director);
        }

        @Override
        public void onUpdate(float t) {
            super.onUpdate(t);

            SMCircularListView listView = (SMCircularListView)_target;

            for (SMView cell : _cells) {
                if (listView.cellDeleteUpdate!=null) {
                    listView.cellDeleteUpdate.cellDeleteUpdate(cell, t);
                }
            }
        }

        @Override
        public void onEnd() {
            super.onEnd();

            SMCircularListView listView = (SMCircularListView)_target;
            for (SMView cell : _cells) {
                listView.removeChild(cell);
            }
        }

        public ArrayList<SMView> _cells = null;
        public void setTargetCells(ArrayList<SMView> cells) {
            _cells = cells;
        }
    }

    public class ReuseScrapper {
        private int _numberOfTypes = 0;

        private HashMap<String, Integer> _key = new HashMap<>();
        private ArrayList<ArrayList<SMView>> _data = new ArrayList<>();

        public SMView _internalReuseNode = null;
        public String _internalReuseIdentifier;

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

        public void scrap(final String reuseIdentifier, final SMView parent, final SMView child) {
            scrap(reuseIdentifier, parent, child, true);
        }
        public void scrap(final String reuseIdentifier, final SMView parent, final SMView child, final boolean cleanup) {
            int reuseType = getReuseType(reuseIdentifier);

            ArrayList<SMView> queue = _data.get(reuseType);
            if (parent!=null) {
                parent.removeChild(child, cleanup);
            }

            queue.add(child);
        }

        public void popBack(final String reuseIdentifier) {
            int reuseType = getReuseType(reuseIdentifier);
            int lastIndex = _data.get(reuseType).size()-1;
            _data.get(reuseType).remove(lastIndex);

        }


        public SMView back(final String reuseIdentifier) {
            int reuseType = getReuseType(reuseIdentifier);
            if (_data.get(reuseType).size()>0) {
                int lastIndex = _data.get(reuseType).size()-1;
                return _data.get(reuseType).get(lastIndex);
            }

            return null;

        }
    }

    private ReuseScrapper _reuseScrapper = null;
    private VelocityTracker _velocityTracker = null;
    private SMScroller _scroller = null;
    private Config _config = new Config();

    private float _lastScrollPosition;
    private float _lastMotionX;
    private float _lastMotionY;
    private float _deltaScroll;
    private int _deleteIndex;
    private boolean _inScrollEvent;
    private boolean _touchFocused;
    private boolean _needUpdate;
    private boolean _actionLock = false;
    private boolean _fillWithCellsFirstTime = false;
    private int _currentPage;
}
