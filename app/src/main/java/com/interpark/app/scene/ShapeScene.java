package com.interpark.app.scene;

import com.interpark.app.menu.MenuBar;
import com.interpark.app.menu.TopMenu;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.transition.SlideOutToRight;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.BGColorTo;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;

import java.util.ArrayList;

public class ShapeScene extends SMMenuTransitionScene {
    protected ShapeScene _mainScene = null;
    public ShapeScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private SMView _contentView = null;

    public static ShapeScene create(IDirector director, MenuBar menuBar) {
        ShapeScene scene = new ShapeScene(director);

        scene.initWithMenuBar(menuBar);

        return scene;
    }

    @Override
    protected boolean initWithMenuBar(MenuBar menuBar) {
        super.initWithMenuBar(menuBar);

        getRootView().setBackgroundColor(Color4F.XEEEFF1);


        setMenuBarTitle("Shapes.");



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

                        }
        return false;
    }

}
