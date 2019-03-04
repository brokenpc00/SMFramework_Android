package com.interpark.smframework;

import android.util.Log;

import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.EdgeSwipeLayerForSideMenu;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.IDirector.SIDE_MENU_STATE;

import org.apache.http.cookie.SM;

public class SideMenu extends SMView {
    private SideMenu(IDirector director) {
        super(director);

        setVisible(false);

        setPosition(new Vec2(-AppConst.SIZE.LEFT_SIDE_MENU_WIDTH, 0));
        setContentSize(new Size(AppConst.SIZE.LEFT_SIDE_MENU_WIDTH, getDirector().getHeight()));
        setAnchorPoint(new Vec2(0, 0));

        Size s = director.getWinSize();
        _contentView = SMView.create(director, 0, 0, getContentSize().width, getContentSize().height);
        _contentView.setBackgroundColor(new Color4F(new Color4B(0xf4, 0xf5, 0xf6, 0xff)));
        addChild(_contentView);


        _sideMenuTableView = SMTableView.createMultiColumn(director, SMTableView.Orientation.VERTICAL, 1, 0, 0, getContentSize().width, getContentSize().height);
        _sideMenuTableView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
            @Override
            public int onFunc(int section) {
                return 5;
            }
        };
        _sideMenuTableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView onFunc(IndexPath indexPath) {
                return cellForRowsAtIndexPath(indexPath);
            }
        };
        _contentView.addChild(_sideMenuTableView);
    }

    public interface SIDE_MENU_LISTENER {
        public void onSideMenuSelect(int tag);
        public void onSideMenuVisible(boolean visible);
    }

    public interface MENU_OPEN_CLOSE {
        public void onFunc();
    };
    public MENU_OPEN_CLOSE _callback = null;

    private static SideMenu _instance = null;

    public static SideMenu GetSideMenu() {
        if (_instance==null) {
            _instance = new SideMenu(SMDirector.getDirector());
        }
        return _instance;
    }

    public void clearMenu() {
        _instance.removeFromParent();
        _instance = null;
    }

    public static void OpenMenu(SMScene mainScene) {
        OpenMenu(mainScene, null);
    }
    public static void OpenMenu(SMScene mainScene, final MENU_OPEN_CLOSE callback) {
        SideMenu.GetSideMenu()._callback = callback;
        if (SideMenu.GetSideMenu()._swipeLayer!=null) {
        SideMenu.GetSideMenu()._swipeLayer.open(false);
    }
    }

    public static void CloseMenu() {
        CloseMenu(null);
    }
    public static void CloseMenu(final MENU_OPEN_CLOSE callback) {
        SideMenu.GetSideMenu()._callback = callback;
        if (SideMenu.GetSideMenu()._swipeLayer!=null) {
        SideMenu.GetSideMenu()._swipeLayer.close(false);
    }
    }

    protected void menuClick(SMView view) {

    }

    public void setSwipeLayer(EdgeSwipeLayerForSideMenu swipeLayer) {
        _swipeLayer = swipeLayer;
    }

    public void setSideMenuListener(SIDE_MENU_LISTENER listener) {
        _listener = listener;
    }

    public void setOpenPosition(final float position) {
        float f = 0;
        if (position >= _contentSize.width) {
            // 완전 열림
            if (_state!=SIDE_MENU_STATE.OPEN) {
                _state = SIDE_MENU_STATE.OPEN;
                if (_callback!=null) {
                    _callback.onFunc();;
                }

                if (!isVisible()) {
                    setVisible(true);
                }
            }
            f = 1.0f;
        } else if (position <= 0) {
            // 완전 닫힘
            if (_state!=SIDE_MENU_STATE.CLOSE) {
                _state = SIDE_MENU_STATE.CLOSE;
                if (_swipeLayer!=null) {
                    _swipeLayer.closeComplete();
                }
                if (_callback!=null) {
                    _callback.onFunc();
                }
                if (isVisible()) {
                    setVisible(false);
                }
            }
            f = 0.0f;
        } else {
            // 이동중
            if (_state!=SIDE_MENU_STATE.MOVING) {
                _state = SIDE_MENU_STATE.MOVING;

                if (!isVisible()) {
                    setVisible(true);
                }
            }

            f = position / _contentSize.width;
            if (f<0) f = 0;
            else if (f>1) f = 1;
        }

        float x = -0.3f * (1.0f - f) * _contentSize.width;
        setPositionX(x);

        if (_sideMenuUpdateCallback!=null) {
            _sideMenuUpdateCallback.Func(_state, position);
        }

        _lastPosition = position;
    }

    public float getOpenPosition() {return _lastPosition;}

    public SIDE_MENU_STATE getState() {return _state;}

    public interface SIDE_MENU_UPDATE_CALLBACK {
        public void Func(SIDE_MENU_STATE state, float position);
    }
    public SIDE_MENU_UPDATE_CALLBACK _sideMenuUpdateCallback = null;

    private SMView cellForRowsAtIndexPath (final IndexPath indexPath) {
        int index = indexPath.getIndex();
        String cellID = "SIDE MENU : " + index;
        SMView convertView = _sideMenuTableView.dequeueReusableCellWithIdentifier(cellID);

        SideMenuCell cell = null;

        Size s = getContentSize();

        if (convertView!=null) {
            cell = (SideMenuCell)convertView;
        } else {
            cell = new SideMenuCell(getDirector());
            cell.setContentSize(new Size(s.width, 150));
            cell.setPosition(new Vec2(0, 0));
            cell.setAnchorPoint(new Vec2(0, 0));

            cell._contentView = SMView.create(getDirector(), 0, 0, s.width, 150);
            cell.addChild(cell._contentView);

            //(IDirector director, String text, float fontSize, float textColorR, float textColorG, float textColorB, float textColorA) {
            cell._title = SMLabel.create(getDirector(), cellID, 45, new Color4F(0, 0, 1, 1));
            cell._title.setAnchorPoint(Vec2.MIDDLE);
            cell._title.setPosition(new Vec2(s.width/2, 75));
            cell._contentView.addChild(cell._title);
        }

        return cell;
    }

    private class SideMenuCell extends SMView {
        public SideMenuCell(IDirector director) {
            super(director);
        }

        public SideMenu _parent;
        public SMView _contentView;
        public SMLabel _title;
    }


    private SMView _contentView = null;
    private SMTableView _sideMenuTableView = null;
    private EdgeSwipeLayerForSideMenu _swipeLayer = null;
    private SIDE_MENU_STATE _state = SIDE_MENU_STATE.CLOSE;
    private SIDE_MENU_LISTENER _listener = null;
    private float _lastPosition = 0.0f;
}
