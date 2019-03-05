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
import com.interpark.smframework.base.transition.SlideInToLeft;
import com.interpark.smframework.base.transition.SlideInToTop;
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
    private ArrayList<String> _menuNames = new ArrayList<>();

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

        Size s = getDirector().getWinSize();

        _contentView = SMView.create(getDirector(), 0, 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
        addChild(_contentView);

        _menuNames.add("DOT");
        _menuNames.add("LINE");
        _menuNames.add("RECT");
        _menuNames.add("ROUNDEDRECT");
        _menuNames.add("CIRCLE");
        _menuNames.add("SOLID-RECT");
        _menuNames.add("SOLID-ROUNDEDRECT");
        _menuNames.add("SOLID_CIRCLE");
        _menuNames.add("SOLID_TRIANGLE");


        _tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0, 0, s.width, _contentView.getContentSize().height);
        _tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView onFunc(IndexPath indexPath) {
                int index = indexPath.getIndex();
                String cellID = "CELL" + index;
                Size s = _tableView.getContentSize();
                SMView cell = _tableView.dequeueReusableCellWithIdentifier(cellID);
                if (cell==null) {
                    cell = SMView.create(getDirector(), 0, 0, 0, s.width, 200);
                    cell.setBackgroundColor(Color4F.WHITE);

                    String str = _menuNames.get(index);
                    SMLabel title = SMLabel.create(getDirector(), str, 55, MakeColor4F(0x222222, 1.0f));
                    title.setAnchorPoint(Vec2.MIDDLE);
                    title.setPosition(new Vec2(s.width/2, cell.getContentSize().height/2));
                    title.setAlpha(0.6f);
                    cell.addChild(title);

                    SMRoundLine line = SMRoundLine.create(getDirector());
                    line.setBackgroundColor(MakeColor4F(0xdbdcdf, 1.0f));
                    line.setLineWidth(2);
                    line.line(20, 198, s.width-20, 198);
                    line.setLengthScale(1);
                    cell.addChild(line);

                    cell.setTag(index);
                    cell.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(SMView view) {
                            onShapeClick(view);
                        }
                    });

                    cell.setOnStateChangeListener(new OnStateChangeListener() {
                        @Override
                        public void onStateChange(SMView view, STATE state) {
                            if (state==STATE.PRESSED) {
                                view.setBackgroundColor(MakeColor4F(0xeeeff1, 1), 0.15f);
                            } else {
                                view.setBackgroundColor(MakeColor4F(0xffffff, 1), 0.15f);
                            }
                        }
                    });
                }
                return cell;
            }
        };

        _tableView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
            @Override
            public int onFunc(int section) {
                return _menuNames.size();
            }
        };
        _tableView.setScissorEnable(true);

        _contentView.addChild(_tableView);
        _contentView.setLocalZOrder(-10);
        return true;
    }

    protected void onShapeClick(SMView view) {
        int tag = view.getTag();
        switch (tag) {
            case 0:
            {
                // DOT
            }
            break;
            case 1:
            {
                // LINE
            }
            break;
            case 2:
            {
                // RECT
            }
            break;
            case 3:
            {
                // ROUNDEDRECT
            }
            break;
            case 4:
            {
                // CIRCLE
            }
            break;
            case 5:
            {
                // SOLID-RECT
            }
            break;
            case 6:
            {
                // SOLID-ROUNDEDRECT
            }
            break;
            case 7:
            {
                // SOLID-CIRCLE
            }
            break;
            case 8:
            {
                // SOLID-TRIANGLE
            }
            break;
        }

        ShapeDisplayScene scene = ShapeDisplayScene.create(getDirector(), _menuBar);
        if (scene!=null) {
            SlideInToTop top = SlideInToTop.create(getDirector(), 0.3f, scene);
            getDirector().pushScene(top);
//            SlideInToLeft left = SlideInToLeft.create(getDirector(), 0.3f, scene);
//            getDirector().pushScene(left);
        }
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
