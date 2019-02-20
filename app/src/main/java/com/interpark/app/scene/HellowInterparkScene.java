package com.interpark.app.scene;

import android.transition.Scene;
import android.util.Log;
import android.view.MotionEvent;

import com.interpark.app.menu.TopMenu;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.NativeImageProcess.ImageProcessing;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMTableView.NumberOfRowsInSection;
import com.interpark.smframework.base.SMTableView.CellForRowAtIndexPath;
import com.interpark.smframework.base.SMZoomView;
import com.interpark.smframework.base._UIContainerView;
import com.interpark.smframework.base.shape.ShapeConstant.LineType;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.TextSprite;
import com.interpark.smframework.base.texture.BitmapTexture;
import com.interpark.smframework.base.texture.FileTexture;
import com.interpark.smframework.base.texture.ResourceTexture;
import com.interpark.smframework.base.transition.SlideInToLeft;
import com.interpark.smframework.base.transition.SlideInToTop;
import com.interpark.smframework.base.transition.SlideOutToBottom;
import com.interpark.smframework.base.transition.SlideOutToRight;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.BGColorTo;
import com.interpark.smframework.base.types.Bounce;
import com.interpark.smframework.base.types.CallFunc;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.EaseCubicActionOut;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.ScaleSine;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMCircleView;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRectView;
import com.interpark.smframework.view.SMRoundLine;
import com.interpark.smframework.view.SMRoundRectView;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.view.SMSolidCircleView;
import com.interpark.smframework.view.SMSolidRectView;
import com.interpark.smframework.view.SMSolidRoundRectView;
import com.interpark.app.menu.TopMenu.TopMenuComponentType;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

public class HellowInterparkScene extends SMScene {

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

    private TopMenu _topMenu = null;
    private SMView _contentView = null;
    private ArrayList<String> _menuNames = new ArrayList<>();


    @Override
    protected boolean init() {
        super.init();

        _mainScene = this;

        Size s = getContentSize();

        _contentView = SMView.create(getDirector(), 0, 0, s.width, s.height);
        _contentView.setBackgroundColor(AppConst.COLOR._WHITE);
        addChild(_contentView);

        _menuNames.add("Shapes");
        _menuNames.add("View");
        _menuNames.add("Controls");


        _tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0, AppConst.SIZE.TOP_MENU_HEIGHT, s.width, s.height-AppConst.SIZE.TOP_MENU_HEIGHT);
        _tableView.cellForRowAtIndexPath = new CellForRowAtIndexPath() {
            @Override
            public SMView onFunc(IndexPath indexPath) {
                int index = indexPath.getIndex();
                String cellID = "CELL" + index;
                Size s = _tableView.getContentSize();
                SMView cell = _tableView.dequeueReusableCellWithIdentifier(cellID);
                if (cell==null) {
                    cell = SMView.create(getDirector(), 0, 0, 0, s.width, 300);
                    cell.setBackgroundColor(Color4F.WHITE);

                    String str = _menuNames.get(index);
                    SMLabel title = SMLabel.create(getDirector(), str, 55, MakeColor4F(0x222222, 1.0f));
                    title.setAnchorPoint(Vec2.MIDDLE);
                    title.setPosition(new Vec2(s.width/2, cell.getContentSize().height/2));
                    cell.addChild(title);

                    SMRoundLine line = SMRoundLine.create(getDirector());
                    line.setBackgroundColor(MakeColor4F(0xdbdcdf, 1.0f));
                    line.setLineWidth(2);
                    line.line(20, 298, s.width-20, 298);
                    line.setLengthScale(1);
                    cell.addChild(line);

                    cell.setTag(index);
                    cell.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(SMView view) {
                            int index = view.getTag();
                            ShapeScene scene = null;
                            switch (index) {
                                case 0:
                                {
                                    // shapes.
                                    scene = ShapeScene.create(getDirector(), new SceneParams(), SwipeType.BACK);
                                }
                                break;
                                case 1:
                                {
                                    // view
                                }
                                break;
                                case 2:
                                {
                                    // controls
                                }
                                break;
                            }

                            if (scene!=null) {
                                SlideInToLeft left = SlideInToLeft.create(getDirector(), 0.3f, scene);
                                getDirector().pushScene(left);
                            }
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
                                BGColorTo color = BGColorTo.create(getDirector(), 0.15f, MakeColor4F(0xeeeff1, 1.0f));
                                color.setTag(0xfffffe);
                                view.runAction(color);
                            } else {
//                                view.setBackgroundColor(Color4F.WHITE);
                                BGColorTo color = BGColorTo.create(getDirector(), 0.15f, Color4F.WHITE);
                                color.setTag(0xfffffe);
                                view.runAction(color);
                            }
                        }
                    });
                }
                return cell;
            }
        };

        _tableView.numberOfRowsInSection = new NumberOfRowsInSection() {
            @Override
            public int onFunc(int section) {
                return _menuNames.size();
            }
        };
//        _tableView.setScissorEnable(true);
//        _tableView.setScissorRect(new Rect(50, 50, s.width-100, _tableView.getContentSize().height-100));
//
//        _tableView.setAnchorPoint(Vec2.MIDDLE);
//        _tableView.setPosition(new Vec2(s.width/2, s.height/2+AppConst.SIZE.TOP_MENU_HEIGHT/2));
        _contentView.addChild(_tableView);

        return true;
    }

    @Override
    public void onEnter() {
        super.onEnter();
        if (_topMenu==null) {
            _topMenu = TopMenu.create(getDirector());
            _contentView.addChild(_topMenu);

            _topMenu.addMenuType(TopMenuComponentType.MENU);
            SceneParams titleParam = new SceneParams();
            titleParam.putString("MENU_TITLE", "SMFrameWork Lib.");
            _topMenu.addMenuType(TopMenuComponentType.TITLE, titleParam);
            _topMenu.generateMenu();


            _topMenu._listener = new TopMenu.TopMenuClickListener() {
                @Override
                public void onTopMenuClick(TopMenuComponentType type) {
                    switch (type) {
                        case MENU:
                        {
                            SideMenu.OpenMenu(_mainScene);
                        }
                        break;
                        case BACK:
                        {

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

    public void openMenu() {
        SideMenu.OpenMenu(this);
    }

}

