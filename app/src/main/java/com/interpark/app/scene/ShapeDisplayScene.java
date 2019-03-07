package com.interpark.app.scene;

import android.util.Log;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.view.SMSlider;
import com.interpark.smframework.view.SMSolidCircleView;
import com.interpark.smframework.view.SMSolidRectView;

import java.util.ArrayList;

public class ShapeDisplayScene extends SMMenuTransitionScene implements SMSlider.OnSliderListener {
    protected ShapeDisplayScene _mainScene = null;

    public ShapeDisplayScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private SMView _contentView = null;
    private ArrayList<String> _menuNames = new ArrayList<>();
    private SMSlider _slider = null;
    private SMShapeView _shape = null;




    public static ShapeDisplayScene create(IDirector director, MenuBar menuBar) {
        return create(director, menuBar, null);
    }
    public static ShapeDisplayScene create(IDirector director, MenuBar menuBar, SceneParams params) {
        ShapeDisplayScene scene = new ShapeDisplayScene(director);

        scene.initWithParams(menuBar, params);

        return scene;
    }

    protected boolean initWithParams(MenuBar menuBar, SceneParams params) {
        super.initWithMenuBar(menuBar, SwipeType.DISMISS);
        _mainScene = this;
//        super.initWithMenuBar(menuBar);

        getRootView().setBackgroundColor(Color4F.XEEEFF1);

        setMenuBarTitle("Display Shape");

        Size s = getDirector().getWinSize();

        _contentView = SMView.create(getDirector(), 0, 0, 0, s.width, s.height);
        _contentView.setBackgroundColor(Color4F.WHITE);
        addChild(_contentView);

        SMSolidCircleView dot = SMSolidCircleView.create(getDirector());
        dot.setAnchorPoint(Vec2.MIDDLE);
        dot.setPosition(_contentView.getContentSize().width/2, _contentView.getContentSize().height/2);
        dot.setContentSize(new Size(10, 10));
        dot.setBackgroundColor(new Color4F(0, 0, 0, 1));
        _contentView.addChild(dot);
        _shape = dot;

//        SMView sliderBg = SMView.create(getDirector(), 0, _contentView.getContentSize().height-AppConst.)

        _slider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _slider.setContentSize(new Size(s.width-80, AppConst.SIZE.MENUBAR_HEIGHT));
        _slider.setPosition(40, _contentView.getContentSize().height-AppConst.SIZE.MENUBAR_HEIGHT);
        _slider.setOnSliderListener(this);
        _contentView.addChild(_slider);
        return true;
    }

    @Override
    public void onSliderValueChanged(SMSlider slider, float value) {
        float scale = value * 20.0f;
        if (scale<1) scale=1;
        Log.i("Scene", "[[[[[ scale : " + scale);
        _shape.setScale(scale, false);
    }

    @Override
    public void onSliderValueChanged(SMSlider slider, float minValue, float maxValue) {

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


