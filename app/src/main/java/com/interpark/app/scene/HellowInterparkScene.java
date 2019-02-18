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
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMCircleView;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRectView;
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
//    public HellowInterparkScene(IDirector director, SceneParams params) {
//        super(director, params);
//
//        // anchor point
////        setPivot(0, getHeight()/2);
//    }

    protected HellowInterparkScene _mainScene = null;

    private SMTableView _tableView;

    public class TestClass {
        public TestClass() {
            a = 0;
            b = 0;
            c = 0;
        }
        public float a;
        public float b;
        public float c;
    }

    public void testAdd(TestClass tc) {
        tc.a++;
        tc.b++;
        tc.c = tc.a + tc.b;
    }

    public void testCom(TestClass[] a, int b) {

        if (a==null) {
            a = new TestClass[3];
            for (int i=0; i<4; i++) {
                a[i] = new TestClass();
                a[i].a = 0;
                a[i].b = 0;
                a[i].c = 0;
            }
        }

        TestClass c = a[b];

        c.a = 99;
        c.b = 1;
        c.c = c.a + c.b;
    }

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

    private boolean _fromMain = true;

    static boolean isBack = false;

    private TopMenu _topMenu = null;
    private SMView _contentView = null;


    @Override
    protected boolean init() {
        super.init();

        _mainScene = this;

        Size s = getContentSize();

        _contentView = SMView.create(getDirector(), 0, 0, s.width, s.height);
        _contentView.setBackgroundColor(AppConst.COLOR._WHITE);
        addChild(_contentView);

        

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

