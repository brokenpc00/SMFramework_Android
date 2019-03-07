package com.interpark.app.scene;

import android.util.Log;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMTimeInterpolator;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMCircleView;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRectView;
import com.interpark.smframework.view.SMRoundLine;
import com.interpark.smframework.view.SMRoundRectView;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.view.SMSlider;
import com.interpark.smframework.view.SMSolidCircleView;
import com.interpark.smframework.view.SMSolidRectView;
import com.interpark.smframework.view.SMSolidRoundRectView;
import com.interpark.smframework.view.SMTriangleView;

import org.apache.http.cookie.SM;

import java.util.ArrayList;

public class ShapeDisplayScene extends SMMenuTransitionScene implements SMSlider.OnSliderListener {
    protected ShapeDisplayScene _mainScene = null;

    public ShapeDisplayScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private SMView _contentView = null;
    private SMView _shapeBG = null;
    private SMView _sliderBG = null;
    private ArrayList<String> _menuNames = new ArrayList<>();
    private SMSlider _scaleSlider = null;
    private SMSlider _rotateSlider = null;
    private SMSlider _moveXSlider = null;
    private SMSlider _moveYSlider = null;
    private SMSlider _colorRSlider = null;
    private SMSlider _colorGSlider = null;
    private SMSlider _colorBSlider = null;
    private SMSlider _colorASlider = null;

    private float _shapeScale = 1.0f;
    private float _shapeRotate = 0.0f;
    private float _shapePosX = 0.0f;
    private float _shapePosY = 0.0f;
    private Color4F _shapeColor = new Color4F(Color4F.BLACK);

    private SMShapeView _shape = null;
    private SMTriangleView _triangle = null;
    private int _shapeType = 0;




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
        _sceneParam = params;
        _mainScene = this;

//        super.initWithMenuBar(menuBar);

        getRootView().setBackgroundColor(Color4F.XEEEFF1);

        setMenuBarTitle("Display Shape");

        Size s = getDirector().getWinSize();

        _contentView = SMView.create(getDirector(), 0, 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
        _contentView.setBackgroundColor(Color4F.WHITE);
        addChild(_contentView);

        makeShape();

        return true;
    }

    private void makeShape() {
        _shapeType = getSceneParams().getInt("SHAPE_TYPE");
        Size s = _contentView.getContentSize();

        float bgViewHeight = AppConst.SIZE.MENUBAR_HEIGHT*4;
        _shapeBG = SMView.create(getDirector(), 0, 0, s.width, s.height-bgViewHeight);
        _shapeBG.setBackgroundColor(Color4F.WHITE);
        _contentView.addChild(_shapeBG);

        _sliderBG = SMView.create(getDirector(), 0, s.height - bgViewHeight, s.width, bgViewHeight);
        _sliderBG.setBackgroundColor(Color4F.XEEEFF1);
        _contentView.addChild(_sliderBG);

        _shapeColor.r = getRandomColorF();
        _shapeColor.g = getRandomColorF();
        _shapeColor.b = getRandomColorF();
        _shapeColor.a = 1;

        SMLabel scaleLabel = SMLabel.create(getDirector(), "Scale", 25);
        SMLabel rotateLabel = SMLabel.create(getDirector(), "Rotate", 25);
        SMLabel moveXLabel = SMLabel.create(getDirector(), "Move H", 25);
        SMLabel moveYLabel = SMLabel.create(getDirector(), "Move V", 25);
        SMLabel colorRLable = SMLabel.create(getDirector(), "Red", 25);
        SMLabel colorGLable = SMLabel.create(getDirector(), "Green", 25);
        SMLabel colorBLable = SMLabel.create(getDirector(), "Blue", 25);
        SMLabel colorALable = SMLabel.create(getDirector(), "Alpha", 25);

        scaleLabel.setAnchorPoint(Vec2.LEFT_TOP);
        rotateLabel.setAnchorPoint(Vec2.LEFT_TOP);
        moveXLabel.setAnchorPoint(Vec2.LEFT_TOP);
        moveYLabel.setAnchorPoint(Vec2.LEFT_TOP);
        colorRLable.setAnchorPoint(Vec2.LEFT_TOP);
        colorGLable.setAnchorPoint(Vec2.LEFT_TOP);
        colorBLable.setAnchorPoint(Vec2.LEFT_TOP);
        colorALable.setAnchorPoint(Vec2.LEFT_TOP);

        float posY = 0;
        float posX = 0;
        scaleLabel.setPosition(posX, posY);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;
        rotateLabel.setPosition(posX, posY);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;
        moveXLabel.setPosition(posX, posY);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;
        moveYLabel.setPosition(posX, posY);

        posY = 0;
        posX = s.width/2;
        colorRLable.setPosition(posX, posY);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;
        colorGLable.setPosition(posX, posY);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;
        colorBLable.setPosition(posX, posY);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;
        colorALable.setPosition(posX, posY);

        _sliderBG.addChild(scaleLabel);
        _sliderBG.addChild(rotateLabel);
        _sliderBG.addChild(moveXLabel);
        _sliderBG.addChild(moveYLabel);
        _sliderBG.addChild(colorRLable);
        _sliderBG.addChild(colorGLable);
        _sliderBG.addChild(colorBLable);
        _sliderBG.addChild(colorALable);


        posY = 20;
        posX = 10;

        _scaleSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _scaleSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _scaleSlider.setPosition(posX, posY);
        _scaleSlider.setOnSliderListener(this);
        _sliderBG.addChild(_scaleSlider);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;

        _rotateSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _rotateSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _rotateSlider.setPosition(posX, posY);
        _rotateSlider.setOnSliderListener(this);
        _sliderBG.addChild(_rotateSlider);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;

        _moveXSlider = SMSlider.create(getDirector(), SMSlider.Type.MINUS_ONE_TO_ONE, SMSlider.LIGHT);
        _moveXSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _moveXSlider.setPosition(posX, posY);
        _moveXSlider.setOnSliderListener(this);
        _sliderBG.addChild(_moveXSlider);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;

        _moveYSlider = SMSlider.create(getDirector(), SMSlider.Type.MINUS_ONE_TO_ONE, SMSlider.LIGHT);
        _moveYSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _moveYSlider.setPosition(posX, posY);
        _moveYSlider.setOnSliderListener(this);
        _sliderBG.addChild(_moveYSlider);

        posY = 20;
        posX = s.width/2 + 10;

        _colorRSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _colorRSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _colorRSlider.setPosition(posX, posY);
        _colorRSlider.setOnSliderListener(this);
        _colorRSlider.setSliderValue(_shapeColor.r);
        _sliderBG.addChild(_colorRSlider);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;

        _colorGSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _colorGSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _colorGSlider.setPosition(posX, posY);
        _colorGSlider.setOnSliderListener(this);
        _colorGSlider.setSliderValue(_shapeColor.g);
        _sliderBG.addChild(_colorGSlider);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;

        _colorBSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _colorBSlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _colorBSlider.setPosition(posX, posY);
        _colorBSlider.setOnSliderListener(this);
        _colorBSlider.setSliderValue(_shapeColor.b);
        _sliderBG.addChild(_colorBSlider);
        posY += AppConst.SIZE.MENUBAR_HEIGHT;

        _colorASlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT);
        _colorASlider.setContentSize(new Size(s.width/2-20, AppConst.SIZE.MENUBAR_HEIGHT-20));
        _colorASlider.setPosition(posX, posY);
        _colorASlider.setOnSliderListener(this);
        _colorASlider.setSliderValue(_shapeColor.a);
        _sliderBG.addChild(_colorASlider);

        switch (_shapeType) {
            case 1:
            {
                // LINE
                SMRoundLine line = SMRoundLine.create(getDirector());
                line.line(10, s.height/2, s.width-20, s.height/2);
                line.setLineWidth(1.0f);
                line.setColor(_shapeColor);
                _shape = line;

            }
            break;
            case 2:
            {
                // RECT
                SMRectView rect = SMRectView.create(getDirector());
                rect.setContentSize(new Size(50, 50));
                rect.setLineWidth(4.0f);
                rect.setColor(_shapeColor);
                _shape = rect;
            }
            break;
            case 3:
            {
                // ROUNDEDRECT
                SMRoundRectView rect = SMRoundRectView.create(getDirector());
                rect.setContentSize(new Size(50, 50));
                rect.setLineWidth(4.0f);
                rect.setCornerRadius(5);
                rect.setColor(_shapeColor);
                _shape = rect;
            }
            break;
            case 4:
            {
                // CIRCLE
                SMCircleView circle = SMCircleView.create(getDirector());
                circle.setContentSize(new Size(50, 50));
                circle.setLineWidth(4.0f);
                circle.setColor(_shapeColor);
                _shape = circle;

            }
            break;
            case 5:
            {
                // SOLID-RECT
                SMSolidRectView rect = SMSolidRectView.create(getDirector());
                rect.setContentSize(new Size(50, 50));
                rect.setColor(_shapeColor);
                _shape = rect;
            }
            break;
            case 6:
            {
                // SOLID-ROUNDEDRECT
                SMSolidRoundRectView rect = SMSolidRoundRectView.create(getDirector());
                rect.setContentSize(new Size(50, 50));
                rect.setCornerRadius(5);
                rect.setColor(_shapeColor);
                _shape = rect;
            }
            break;
            case 7:
            {
                // SOLID-CIRCLE
                SMSolidCircleView circle = SMSolidCircleView.create(getDirector());
                circle.setContentSize(new Size(50, 50));
                circle.setColor(_shapeColor);
                _shape = circle;
            }
            break;
            case 8:
            {
                // SOLID-TRIANGLE
                SMShapeView triangleBg = new SMShapeView(getDirector());
                triangleBg.setContentSize(new Size(50, 50));

                _triangle = SMTriangleView.create(getDirector(), 50, 50);
                _triangle.setColor(_shapeColor);
                _triangle.setTriangle(new Vec2(25,0), new Vec2(0,50), new Vec2(50,50));
                triangleBg.addChild(_triangle);
                _shape = triangleBg;
            }
            break;
            default:
            case 0:
            {
                // DOT
        SMSolidCircleView dot = SMSolidCircleView.create(getDirector());
        dot.setContentSize(new Size(10, 10));
                dot.setColor(_shapeColor);
        _shape = dot;

            }
            break;

        }

        if (_shape!=null) {
            _shape.setAnchorPoint(Vec2.MIDDLE);
            _shape.setPosition(_shapeBG.getContentSize().width/2, _shapeBG.getContentSize().height/2);
            _shapeBG.addChild(_shape);
        }
    }

    private void layoutShape() {
        if (_shape!=null) {
            if (_shapeType==8) {
                _triangle.setColor(_shapeColor, false);
            } else {
                _shape.setColor(_shapeColor, false);
            }
            _shape.setScale(_shapeScale, false);
            _shape.setPosition(_shapeBG.getContentSize().width/2+_shapePosX, _shapeBG.getContentSize().height/2+_shapePosY, false);
            _shape.setRotation(_shapeRotate, false);
        }
    }

    @Override
    public void onSliderValueChanged(SMSlider slider, float value) {
        if (slider==_scaleSlider) {
        float scale = value * 20.0f;
        if (scale<1) scale=1;
            _shapeScale = scale;
        } else if (slider==_rotateSlider) {
            _shapeRotate = 360.0f * value;
        } else if (slider==_colorRSlider) {
            _shapeColor.r = value;
        } else if (slider==_colorGSlider) {
            _shapeColor.g = value;
        } else if (slider==_colorBSlider) {
            _shapeColor.b = value;
        } else if (slider==_colorASlider) {
            _shapeColor.a = value;
        } else if (slider==_moveXSlider) {
            _shapePosX = _shapeBG.getContentSize().width/2 * value;
        } else if (slider==_moveYSlider) {
            _shapePosY = _shapeBG.getContentSize().height/2 * value;
        }

        layoutShape();
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


