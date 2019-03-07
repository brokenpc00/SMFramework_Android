package com.interpark.app.scene;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;

public class ViewDisplayScene extends SMMenuTransitionScene {
    public ViewDisplayScene _mainScene = null;

    public ViewDisplayScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private SMView _contentView = null;

    public static ViewDisplayScene create(IDirector director, MenuBar menuBar) {
        return create(director, menuBar, null);
    }
    public static ViewDisplayScene create(IDirector director, MenuBar menuBar, SceneParams params) {
        ViewDisplayScene scene = new ViewDisplayScene(director);

        scene.initWithParams(menuBar, params);

        return scene;
    }

    protected boolean initWithParams(MenuBar menuBar, SceneParams params) {
        super.initWithMenuBar(menuBar, SwipeType.DISMISS);
        _sceneParam = params;
        _mainScene = this;
        getRootView().setBackgroundColor(Color4F.XEEEFF1);
        setMenuBarTitle(_sceneParam.getString("MENU_NAME"));

        Size s = getDirector().getWinSize();
        _contentView = SMView.create(getDirector(), 0, 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
        _contentView.setBackgroundColor(Color4F.WHITE);
        addChild(_contentView);

        makeView();
        return true;
    }

    private int _viewType = 0;
    public void makeView() {
        _viewType = _sceneParam.getInt("VIEW_TYPE");

        switch (_viewType) {
            case 0:
            {
                // Image View
            }
            break;
            case 1:
            {
                // Zoom View
            }
            break;
            case 2:
            {
                // Table View
            }
            break;
            case 3:
            {
                // Page View
            }
            break;
            case 4:
            {
                // Circular View
            }
            break;
            case 5:
            {
                // Kenburn
            }
            break;
            case 6:
            {
                // Wave & Pulse
            }
            break;
            case 7:
            {
                // Stencil View
            }
            break;
            case 8:
            {
                // Sticker View
            }
            break;
            case 9:
            default:
            {
                // Swipe View
            }
            break;
        }
    }

}
