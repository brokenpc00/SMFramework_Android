package com.interpark.app.scene;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Color4F;

import java.util.ArrayList;

public class ShapeDisplayScene extends SMMenuTransitionScene {
    protected ShapeDisplayScene _mainScene = null;

    public ShapeDisplayScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private SMView _contentView = null;
    private ArrayList<String> _menuNames = new ArrayList<>();

    public static ShapeDisplayScene create(IDirector director, MenuBar menuBar) {
        ShapeDisplayScene scene = new ShapeDisplayScene(director);

        scene.initWithMenuBar(menuBar);

        return scene;
    }

    @Override
    protected boolean initWithMenuBar(MenuBar menuBar) {
        super.initWithMenuBar(menuBar, SwipeType.DISMISS);
//        super.initWithMenuBar(menuBar);

        getRootView().setBackgroundColor(Color4F.XEEEFF1);

        setMenuBarTitle("Display Shape");

        return true;
    }

    @Override
    protected boolean onMenuBarClick(SMView view) {
        MenuBar.MenuType type = MenuBar.intToMenuType(view.getTag());
        switch (type) {
            case BACK:
            {
                finishScene();
                return true;
            }
            case CLOSE:
            {
                finishScene();
                return true;
            }
        }
        return false;
    }
}


