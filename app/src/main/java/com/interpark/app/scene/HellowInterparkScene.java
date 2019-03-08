package com.interpark.app.scene;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.view.SMTableView;
import com.interpark.smframework.base.transition.SlideInToLeft;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;

import java.util.ArrayList;

public class HellowInterparkScene extends SMScene implements SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection, SMView.OnClickListener {

    protected HellowInterparkScene _mainScene = null;

    private SMTableView _tableView = null;

    public HellowInterparkScene(IDirector director) {
        super(director);
    }

    public static HellowInterparkScene create(IDirector director, SceneParams params, SwipeType type) {
        HellowInterparkScene scene = new HellowInterparkScene(director);
        if (scene!=null) {
            scene.initWithSceneParams(params, type);
        }

        return scene;
    }

    public static SMImageView tmpView = null;

    static boolean isBack = false;

    private MenuBar _menuBar = null;
//    private TopMenu _topMenu = null;
    private SMView _contentView = null;
    private ArrayList<String> _menuNames = new ArrayList<>();

    public static String _sceneTitle = "SMFrame Lib.";

    public SMView _uiLayer = null;

    @Override
    protected boolean init() {
        super.init();

        setBackgroundColor(1, 1, 1, 1);

        _mainScene = this;

        _menuBar = MenuBar.create(getDirector());
        _menuBar.setMenuButtonType(MenuBar.MenuType.MENU, true);
        _menuBar.setText(_sceneTitle, true);
        _menuBar.setColorSet(MenuBar.ColorSet.WHITE_TRANSULANT, true);
        _menuBar.setLocalZOrder(10);
        _menuBar.setMenuBarListener(_menuBarListener);
        addChild(_menuBar);

        Size s = getContentSize();
        _contentView = SMView.create(getDirector(), 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
        _contentView.setBackgroundColor(Color4F.WHITE);
        addChild(_contentView);


        _menuNames.add("Shapes.");
        _menuNames.add("Views.");
        _menuNames.add("Controls.");
        _menuNames.add("Etcetera.");

        _tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0, 0, s.width, _contentView.getContentSize().height);
        _tableView.cellForRowAtIndexPath = this;
        _tableView.numberOfRowsInSection = this;

        _tableView.setScissorEnable(true);
//        _tableView.setScissorRect(new Rect(200, 200, s.width-400, _tableView.getContentSize().height-400));
//
//        _tableView.setAnchorPoint(Vec2.MIDDLE);
//        _tableView.setPosition(new Vec2(s.width/2, s.height/2+AppConst.SIZE.MENUBAR_HEIGHT/2));
        _contentView.addChild(_tableView);
        _contentView.setLocalZOrder(-10);
//        _contentView.setLocalZOrder(990);

        return true;
    }

    @Override
    public int numberOfRowsInSection(int section) {
        return _menuNames.size();
    }


            @Override
            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                int index = indexPath.getIndex();
                String cellID = "CELL" + index;
                Size s = _tableView.getContentSize();
                SMView cell = _tableView.dequeueReusableCellWithIdentifier(cellID);
                if (cell==null) {
            cell = SMView.create(getDirector(), 0, 0, 0, s.width, 250);
                    cell.setBackgroundColor(Color4F.WHITE);

                    String str = _menuNames.get(index);
                    SMLabel title = SMLabel.create(getDirector(), str, 55, MakeColor4F(0x222222, 1.0f));
                    title.setAnchorPoint(Vec2.MIDDLE);
                    title.setPosition(new Vec2(s.width/2, cell.getContentSize().height/2));
                    cell.addChild(title);

                    SMRoundLine line = SMRoundLine.create(getDirector());
                    line.setBackgroundColor(MakeColor4F(0xdbdcdf, 1.0f));
                    line.setLineWidth(2);
            line.line(20, 248, s.width-20, 248);
                    line.setLengthScale(1);
                    cell.addChild(line);

                    cell.setTag(index);
            cell.setOnClickListener(this);

            cell.setOnStateChangeListener(new OnStateChangeListener() {
                @Override
                public void onStateChange(SMView view, STATE state) {
                    if (state==STATE.PRESSED) {
                        view.setBackgroundColor(Color4F.XEEEFF1, 0.15f);
                    } else {
                        view.setBackgroundColor(Color4F.WHITE, 0.15f);
                    }
                }
            });
        }
        return cell;
    }

                        @Override
                        public void onClick(SMView view) {
                            int index = view.getTag();

        SMScene scene = null;
        SceneParams params = new SceneParams();
        params.putInt("SCENE_TYPE", index);
        params.putString("MENU_NAME", _menuNames.get(index));
        scene = ListScene.create(getDirector(), _menuBar, params);
                                SlideInToLeft left = SlideInToLeft.create(getDirector(), 0.3f, scene);
                                getDirector().pushScene(left);
                            }

    protected SMView arrowView = null;

    public boolean onMenuClick(SMView view) {
        MenuBar.MenuType type = MenuBar.intToMenuType(view.getTag());
        switch (type) {
            case MENU:
            {
                // side menu open
                SideMenu.OpenMenu(this);
                return true;
            }
        }
        return false;
    }

    public void onMenuTouchg() {

    }

    public void openMenu() {
        SideMenu.OpenMenu(this);
    }

    @Override
    public void onTransitionStart(final Transition type, final int tag) {

        if (type==Transition.IN) {
            if (getSwipeType()==SwipeType.MENU) {
                _menuBar.setMenuButtonType(MenuBar.MenuType.MENU, false);
            } else {
                _menuBar.setMenuButtonType(MenuBar.MenuType.BACK, false);
            }
            _menuBar.setColorSet(MenuBar.ColorSet.WHITE_TRANSULANT, false);
            _menuBar.setText(_sceneTitle, false);
        }
    }

    protected MenuBar.MenuBarListener _menuBarListener = new MenuBar.MenuBarListener() {
        @Override
        public boolean func1(SMView view) {
            return onMenuClick(view);
        }

        @Override
        public void func2() {
            onMenuTouchg();
        }
    };



    @Override
    public void onTransitionComplete(final Transition type, final int tag) {
        if (type == Transition.RESUME) {
            bringMenuBarFromLayer();
        } else if (type == Transition.OUT) {

        }
    }

    protected void bringMenuBarFromLayer() {
        SMView layer = _director.getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI);
        if (layer==null) return;

        ArrayList<SMView> children = layer.getChildren();
        for (SMView child : children) {
            if (child==_menuBar) {
                _menuBar.changeParent(this);
                break;
            }
        }

        _menuBar.setMenuBarListener(_menuBarListener);
    }

}

