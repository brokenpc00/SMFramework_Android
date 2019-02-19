package com.interpark.app.scene;

import com.interpark.app.menu.TopMenu;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
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

public class ShapeScene extends SMScene {
    protected ShapeScene _mainScene = null;
    public ShapeScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private TopMenu _topMenu = null;
    private SMView _contentView = null;
    private ArrayList<String> _menuNames = new ArrayList<>();

    public static ShapeScene create(IDirector director, SceneParams params, SwipeType type) {
        ShapeScene scene = new ShapeScene(director);
        if (scene != null) {
            scene.initWithSceneParams(params, type);
        }

        return scene;
    }

    @Override
    protected boolean init() {
        super.init();

        _mainScene = this;

        Size s = getContentSize();

        _contentView = SMView.create(getDirector(), 0, 0, s.width, s.height);
        _contentView.setBackgroundColor(AppConst.COLOR._WHITE);
        addChild(_contentView);

        _menuNames.add("Rect");
        _menuNames.add("Solid-Rect");
        _menuNames.add("RoundRect");
        _menuNames.add("Solid-RoundRect");
        _menuNames.add("Circle");
        _menuNames.add("Solid-Circle");


        _tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0, AppConst.SIZE.TOP_MENU_HEIGHT, s.width, s.height-AppConst.SIZE.TOP_MENU_HEIGHT);
        _tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView onFunc(IndexPath indexPath) {
                int index = indexPath.getIndex();
                String cellID = "CELL" + index;
                Size s = getDirector().getWinSize();
                SMView cell = _tableView.dequeueReusableCellWithIdentifier(cellID);
                if (cell==null) {
                    cell = SMView.create(getDirector(), 0, 0, 0, s.width, 200);
                    cell.setBackgroundColor(new Color4F(1, 1, 1, 1));

                    String str = _menuNames.get(index);
                    SMLabel title = SMLabel.create(getDirector(), str, 55, 0x22/255.0f, 0x22/255.0f, 0x22/255.0f, 1.0f);
                    title.setAnchorPoint(new Vec2(0.5f, 0.5f));
                    title.setPosition(new Vec2(s.width/2, cell.getContentSize().height/2));
                    cell.addChild(title);

                    SMRoundLine line = SMRoundLine.create(getDirector());
                    line.setBackgroundColor(new Color4F(new Color4B(0xdb, 0xdc, 0xdf, 0xff)));
                    line.setLineWidth(2);
                    line.line(20, 198, s.width-20, 198);
                    line.setLengthScale(1);
                    cell.addChild(line);

                    cell.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(SMView view) {

                        }
                    });

                    cell.setOnStateChangeListener(new OnStateChangeListener() {
                        @Override
                        public void onStateChange(SMView view, STATE state) {
                            Action action = view.getActionByTag(0xfffffe);
                            if (action!=null) {
                                action.stop();;
                            }
                            if (state==STATE.PRESSED) {
//                                view.setBackgroundColor(new Color4F(new Color4B(0xee, 0xef, 0xf1, 0xff)));
                                BGColorTo color = BGColorTo.create(getDirector(), 0.15f, new Color4F(new Color4B(0xee, 0xef, 0xf1, 0xff)));
                                color.setTag(0xfffffe);
                                view.runAction(color);
                            } else {
//                                view.setBackgroundColor(new Color4F(1, 1, 1, 1));
                                BGColorTo color = BGColorTo.create(getDirector(), 0.15f, new Color4F(1, 1, 1, 1));
                                color.setTag(0xfffffe);
                                view.runAction(color);
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

        _contentView.addChild(_tableView);

        return true;
    }

    @Override
    public void onEnter() {
        super.onEnter();
        if (_topMenu==null) {
            _topMenu = TopMenu.create(getDirector());
            _contentView.addChild(_topMenu);

            _topMenu.addMenuType(TopMenu.TopMenuComponentType.BACK);
            SceneParams titleParam = new SceneParams();
            titleParam.putString("MENU_TITLE", "Shapes...");
            _topMenu.addMenuType(TopMenu.TopMenuComponentType.TITLE, titleParam);
            _topMenu.generateMenu();


            _topMenu._listener = new TopMenu.TopMenuClickListener() {
                @Override
                public void onTopMenuClick(TopMenu.TopMenuComponentType type) {
                    switch (type) {
                        case MENU:
                        {

                        }
                        break;
                        case BACK:
                        {
                            SlideOutToRight scene = SlideOutToRight.create(getDirector(), 0.3f, getDirector().getPreviousScene());
                            getDirector().popSceneWithTransition(scene);
                        }
                        break;
                        case HOME:
                        {

                        }
                        break;
                        default:
                        {

                        }
                        break;
                    }
                }
            };
        }
    }
}
