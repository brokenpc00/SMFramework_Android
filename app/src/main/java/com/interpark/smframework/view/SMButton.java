package com.interpark.smframework.view;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.UIContainerView;
import com.interpark.smframework.base.transition.StateTransitionAction;
import com.interpark.smframework.base.shape.ShapeConstant.LineType;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.FileTexture;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.ActionInstant;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class SMButton extends UIContainerView {
    public enum STYLE {
        DEFAULT,
        RECT,
        ROUNDRECT,
        CIRCLE,
        SOLID_RECT,
        SOLID_ROUNDRECT,
        SOLID_CIRCLE,
    }

    public enum ICONALIGN {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    private static long FLAG_CONTENT_SIZE = 1;
    private static long FLAG_BUTTON_COLOR = (1<<1);
    private static long FLAG_ICON_COLOR = (1<<2);
    private static long FLAG_TEXT_COLOR = (1<<3);
    private static long FLAG_OUTLINE_COLOR = (1<<4);
    private static long FLAG_TEXT_ICON_POSITION = (1<<5);
    private static long FLAG_SHAPE_STYLE = (1<<6);


    private long _smoothFlags = 0;

    private STYLE _style;
    private ICONALIGN _align;

    private SMLabel _textLabel = null;
    private Color4F[] _iconColor = null;
    private Color4F[] _textColor = null;
    private Color4F[] _buttonColor = null;
    private Color4F[] _outlineColor = null;

    private SMView[] _buttonView = null;
    private SMView[] _iconView = null;

//    private float _pushDownAlpha = 1.0f;

    private float _pushDownRotate = 0.0f;

    private Vec2 _pushDownOffset = new Vec2(0, 0);
    private SMSolidRectView _textUnderline = null;

    private float _pushDownScale = 1.0f;
    private float _iconScale = 1.0f;
    private float _textScale = 1.0f;
    private float _iconPadding = 0.0f;

    private Vec2 _iconOffset = new Vec2();
    private Vec2 _textOffset = new Vec2();

    private float _shapeRadius = 0.0f;
    private int _shapeQuardrant;
    private float _shapeLineWidth = 1.0f;
    private float _shapeAntiAliasWidth = 1.0f;
    private float _shapeOutlineWidth = 1.0f;

    private StateTransitionAction _buttonPressAction = null;
    private StateTransitionAction _buttonReleaseAction = null;

    private float _buttonPressAnimationTime;
    private float _buttonReleaseAnimationTime;
    private float _buttonStateValue;


    private int stateToInt(STATE state) {
        switch (state) {
            case NORMAL:
                return 0;
            case PRESSED:
                return 1;
            case MAX:
                return 2;
            default:
                return 0;
        }
    }

    private void setStateColor(Color4F[] target, STATE state, Color4F color) {
        if (target==null) {
            target = new Color4F[stateToInt(STATE.MAX)];
        }

        target[stateToInt(state)] = new Color4F(color);
    }

    private SMView[] getTargetView(boolean isButton) {
        if (isButton) {
            return _buttonView;
        } else {
            return _iconView;
        }
    }

    private void setTargetView(SMView[] target, boolean isButton) {
        if (isButton) {
            _buttonView = target;
        } else {
            _iconView = target;
        }
    }

    private Color4F[] getTargetColor(int colorType) {
        if (colorType==1) {
            return _buttonColor;
        } else if (colorType==2) {
            return _iconColor;
        } else {
            return _outlineColor;
        }
    }

    private void setTargetColor(Color4F[] colors, int colorType) {
        if (colorType==1) {
            _buttonColor = colors;
        } else if (colorType==2) {
            _iconColor = colors;
        } else {
            _outlineColor = colors;
        }
    }

    private void setStateView(SMView[] target, STATE state, SMView view, final int localZOrder, Color4F[] targetColor) {

        SMView currentStateView = target[stateToInt(state)];
        if (currentStateView!=view) {
            if (currentStateView!=null) {
                // remove previous state view
                _uiContainer.removeChild(currentStateView);
            }
            if (view!=null) {
                // change new state node
                _uiContainer.addChild(view, localZOrder);
            }
        }
        target[stateToInt(state)] = view;
    }

    private void setStateView(boolean isButton, STATE state, SMView view, final int localZOrder, int colorType) {
        // color type 1 : buttoncolor, 2 : iconcolor, 3 : outlinecolor

        SMView[] target = getTargetView(isButton);
        Color4F[] targetColor = getTargetColor(colorType);

        if (target==null && view!=null) {
            target = new SMView[stateToInt(STATE.MAX)];

            if (targetColor==null) {
                targetColor = new Color4F[stateToInt(STATE.MAX)];
            }
        }

        SMView currentStateView = target[stateToInt(state)];

        if (currentStateView!=view) {
            if (currentStateView!=null) {
                _uiContainer.removeChild(currentStateView);
            }
            if (view!=null) {
                _uiContainer.addChild(view, localZOrder);
            }
        }

        target[stateToInt(state)] = view;

        setTargetView(target, isButton);
        setTargetColor(targetColor, colorType);
    }

    private float _buttonPressActionTime = AppConst.Config.BUTTON_STATE_CHANGE_NORMAL_TO_PRESS_TIME;
    private float _buttonReleaseActionTime = AppConst.Config.BUTTON_STATE_CHANGE_PRESS_TO_NORMAL_TIME;


    @Override
    protected void onStateChangeNormalToPress(MotionEvent event) {
        if (_pushDownOffset.x!=0.0f || _pushDownOffset.y!=0.0f) {
            _uiContainer.setAnimOffset(_pushDownOffset);
        }

        if (_pushDownScale!=1.0f) {
            _uiContainer.setAnimScale(_pushDownScale);
        }

        if (_pushDownRotate!=0.0f) {
            _uiContainer.setAnimRotate(_pushDownRotate);
        }

//        if (_pushDownAlpha!=1.0f) {
//            _uiContainer.setAnimAlpha(_pushDownAlpha);
//        }

        if (_buttonPressAction==null) {
            _buttonPressAction = StateTransitionAction.create(getDirector(), STATE.PRESSED);
            _buttonPressAction.setTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS);
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_DELAY)!=null) {
            stopAction(getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_DELAY));
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS)==null) {
            _buttonPressAction.setDuration(_buttonPressActionTime);
            runAction(_buttonPressAction);
        }

    }

    @Override
    protected void onStateChangePressToNormal(MotionEvent event) {

        // anim offset 정상으로
        _uiContainer.setAnimOffset(Vec2.ZERO);
        _uiContainer.setAnimScale(1.0f);
        _uiContainer.setAnimRotate(0.0f);
//        _uiContainer.setAnimAlpha(1.0f);

        if (_buttonReleaseAction==null) {
            _buttonReleaseAction = StateTransitionAction.create(getDirector(), STATE.NORMAL);
            _buttonReleaseAction.setTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL);
        }

        StateTransitionAction action = (StateTransitionAction)getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS);
        if (action!=null) {
            float minTime = action.getDuration()*0.3f - action.getElapsed();
            if (minTime>0) {
                _buttonReleaseAction.setDuration(_buttonReleaseActionTime);

                // createDelay
                Sequence sequence = Sequence.createWithTwoActions(getDirector(), DelayTime.create(getDirector(), minTime), new _ReleaseActionStarter(getDirector()));
                sequence.setTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_DELAY);
                runAction(sequence);
                return;
            }
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL)==null) {
            _buttonReleaseAction.setDuration(_buttonReleaseActionTime);
            runAction(_buttonReleaseAction);
        }
    }

    protected class _ReleaseActionStarter extends ActionInstant {
        public _ReleaseActionStarter(IDirector director) {
            super(director);
        }

        @Override
        public void update(float t) {
            SMButton btn = (SMButton)_target;
            btn.runAction(btn._buttonReleaseAction);
        }
    }

    public void viewColorChange(SMView srcView, SMView dstView, Color4F srcColor, Color4F dstColor, float t) {
        float srcAlpha = 1.0f - t;
        float dstAlpha = t;

        if (dstView!=null) {
            if (srcColor!=null) {
                srcAlpha *= srcColor.a;
                if (srcView!=null) {
                    Color4F tintColor = new Color4F(srcColor);
                    tintColor.a = srcAlpha;
                    srcView.setColor(tintColor);
                }
            }
            if (dstColor!=null) {
                dstAlpha *= dstColor.a;
                Color4F tintColor = new Color4F(dstColor);
                tintColor.a = dstAlpha;
                dstView.setColor(tintColor);
            }
            if (srcView!=null) {
                srcView.setVisible(srcAlpha>0.0f);
            }
            if (dstView!=null) {
                dstView.setVisible(dstAlpha>0.0f);
            }
        } else if (srcView!=null) {
            Color4F sc = new Color4F(srcColor!=null?srcColor:Color4F.TRANSPARENT);
            Color4F dc = new Color4F(dstColor!=null?dstColor:_style==STYLE.DEFAULT?Color4F.WHITE:sc);
            Color4F rc = new Color4F(sc.multiply(srcAlpha).add(dc.multiply(dstAlpha)));
            srcView.setColor(rc);
        }
    }

    public void onUpdateStateTransition(STATE state, float t) {
        _buttonStateValue = t;
        if (_buttonView!=null) {
            if (_style==STYLE.DEFAULT) {
                // no button color and no outline color
                viewColorChange(_buttonView[0], _buttonView[1], _buttonColor[0], _buttonColor[1], t);
            } else {
                if (_buttonColor!=null) {
                    viewColorChange(_buttonView[0], null, _buttonColor[0], _buttonColor[1], t);
                }
                if (_outlineColor!=null) {
                    viewColorChange(_buttonView[1], null, _outlineColor[0], _outlineColor[1], t);
                }
            }
        }

        if (_iconView!=null) {
            viewColorChange(_iconView[0], _iconView[1], _iconColor[0], _iconColor[1], t);
        }

        if (_textLabel!=null) {
            viewColorChange(_textLabel, null, _textColor[0], _textColor[1], t);
            if (_textUnderline!=null) {
                viewColorChange(_textUnderline, null, _textColor[0], _textColor[1], t);
            }
        }
    }

    public SMButton(IDirector director) {
        super(director);
        setPosition(0, 0);
        setAnchorPoint(0, 0);
        setContentSize(new Size(0, 0));
        _style = STYLE.DEFAULT;
        setTag(0);
    }

    public static SMButton create(IDirector director) {
        return create(director, 0);
    }
    public static SMButton create(IDirector director, int tag) {
        return create(director, tag, STYLE.DEFAULT);
    }
    public static SMButton create(IDirector director, int tag, STYLE style) {
        return create(director, tag, style, 0, 0);
    }
    public static SMButton create(IDirector director, int tag, STYLE style, float x, float y) {
        return create(director, tag, style, x, y, 0, 0);
    }
    public static SMButton create(IDirector director, int tag, STYLE style, float x, float y, float width, float height) {
        return create(director, tag, style, x, y, width, height, 0, 0);
    }
    public static SMButton create(IDirector director, int tag, STYLE style, float x, float y, float width, float height, float anchorX, float anchorY) {
        SMButton btn = new SMButton(director, tag, style, x, y, width, height, anchorX, anchorY);
        return btn;
    }
    public SMButton(IDirector director, int tag, STYLE style, float x, float y, float width, float height, float anchorX, float anchorY) {
        this(director);
        setPosition(x, y);
        setAnchorPoint(anchorX, anchorY);
        setContentSize(new Size(width, height));
        setTag(tag);

        setClickable(true);
        initWithStyle(style);
    }

    protected boolean initWithStyle(STYLE style) {
        _style = style;

        SMView buttonView = null;

        switch (_style) {
            case RECT:
            {
                buttonView = new SMRectView(getDirector());
            }
            break;
            case SOLID_RECT:
            {
                buttonView = new SMSolidRectView(getDirector());
            }
            break;
            case ROUNDRECT:
            {
                buttonView = new SMRoundRectView(getDirector(), 1.0f, LineType.Solid);
            }
            break;
            case SOLID_ROUNDRECT:
            {
                buttonView = new SMSolidRoundRectView(getDirector());
            }
            break;
            case CIRCLE:
            {
                buttonView = new SMCircleView(getDirector());
            }
            break;
            case SOLID_CIRCLE:
            {
                buttonView = new SMSolidCircleView(getDirector());
            }
            break;
            default:
            case DEFAULT:
            {
                buttonView = null;
            }
            break;
        }

        if (buttonView!=null) {
            // not STYLE.DEFAULT
            _shapeRadius = 0.0f;
            _shapeLineWidth = AppConst.DEFAULT_VALUE.LINE_WIDTH;
            _shapeAntiAliasWidth = 1.0f;
            _shapeOutlineWidth = AppConst.DEFAULT_VALUE.LINE_WIDTH;

//            buttonView.setBackgroundColor(0, 0, 0, 0);
            buttonView.setBackgroundColor(Color4F.TRANSPARENT);
            buttonView.setAnchorPoint(Vec2.MIDDLE);
//            buttonView.setPivot(getContentSize().width/2, getContentSize().height/2);
            buttonView.setPosition(getContentSize().width/2, getContentSize().height/2);
            buttonView.setContentSize(getContentSize());

            setButton(STATE.NORMAL, buttonView);
            setButtonColor(STATE.NORMAL, Color4F.WHITE);
        }

        return true;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

//    @Override
//    public void setBounds(float x, float y, float width, float height) {
//        super.setBounds(x, y, width, height);
//        registerUpdate(FLAG_CONTENT_SIZE);
//    }

    @Override
    public void setContentSize(final Size size) {
        super.setContentSize(size);
        registerUpdate(FLAG_CONTENT_SIZE);
    }

    @Override
    public void setContentSize(float width, float height) {
        this.setContentSize(new Size(width, height));
    }

    public void setButtonColor(final STATE state, final Color4B color) {
        setButtonColor(state, new Color4F(color));
    }

    public void setButtonColor(final STATE state, final Color4F color) {
        if (_buttonColor==null) {
            _buttonColor = new Color4F[stateToInt(STATE.MAX)];
        }
        _buttonColor[stateToInt(state)] = new Color4F(color);

        registerUpdate(FLAG_BUTTON_COLOR);
    }

    public void setIconColor(final STATE state, final Color4B color) {
        setIconColor(state, new Color4F(color));
    }
    public void setIconColor(final STATE state, final Color4F color) {
        if (_iconColor==null) {
            _iconColor = new Color4F[stateToInt(STATE.MAX)];
        }
        _iconColor[stateToInt(state)] = new Color4F(color);
        registerUpdate(FLAG_ICON_COLOR);
    }

    public void setTextColor(final STATE state, final Color4B color) {
        setTextColor(state, new Color4F(color));
    }
    public void setTextColor(final STATE state, final Color4F color) {
        if (_textColor==null) {
            _textColor = new Color4F[stateToInt(STATE.MAX)];
        }
        _textColor[stateToInt(state)] = new Color4F(color);
        registerUpdate(FLAG_TEXT_COLOR);
    }

    public void setOutlineColor(final STATE state, final Color4B color) {
        setOutlineColor(state, new Color4F(color));
    }
    public void setOutlineColor(final STATE state, final Color4F color) {
        if (_outlineColor==null) {
            _outlineColor = new Color4F[stateToInt(STATE.MAX)];
        }
        _outlineColor[stateToInt(state)] = new Color4F(color);
        registerUpdate(FLAG_OUTLINE_COLOR);
    }

    public void setButton(final STATE state, SMView view) {

        if (_buttonView==null && view!=null) {
            _buttonView = new SMView[stateToInt(STATE.MAX)];
            _buttonView[0] = null;
            _buttonView[1] = null;

            if (_buttonColor==null) {
                _buttonColor = new Color4F[stateToInt(STATE.MAX)];
                _buttonColor[0] = null;
                _buttonColor[1] = null;
            }
        }

        view.setAnchorPoint(Vec2.MIDDLE);
        setStateView(_buttonView, state, view, state==STATE.NORMAL?AppConst.ZOrder.BUTTON_NORMAL:AppConst.ZOrder.BUTTON_PRESSED, _buttonColor);

        if (view!=null) {
            registerUpdate(FLAG_CONTENT_SIZE);
            registerUpdate(FLAG_BUTTON_COLOR);
        }
    }

    public void setButton(final STATE state, final String imageFileName) {
        assert (_style==STYLE.DEFAULT);

        if (imageFileName.isEmpty()) {
            return;
        }

        BitmapSprite sprite = BitmapSprite.createFromFile(getDirector(), imageFileName, false, null, 0);
        if (sprite==null) {
            return;
        }

        SMImageView imageView = new SMImageView(getDirector(), sprite);
        setButton(state, imageView);
    }

    public void setOutlineWidth(final float lineWidth) {
        if (_shapeOutlineWidth==lineWidth) {
            return;
        }
        if (lineWidth>0) {
            SMShapeView outlineView = null;
            switch (_style) {
                case RECT:
                case SOLID_RECT:
                {
                    outlineView = new SMRectView(getDirector());
                }
                break;
                case CIRCLE:
                case SOLID_CIRCLE:
                {
                    outlineView = new SMCircleView(getDirector());
                }
                break;
                case ROUNDRECT:
                case SOLID_ROUNDRECT:
                {
                    outlineView = new SMRoundRectView(getDirector(), 1.0f, LineType.Solid);
                }
                break;
                default:
                break;
            }
            if (outlineView!=null) {

                outlineView.setAnchorPoint(Vec2.MIDDLE);
                outlineView.setPosition(getContentSize().width/2, getContentSize().height/2);


                if (_buttonView==null) {
                    _buttonView = new SMView[stateToInt(STATE.MAX)];
                    _buttonView[0] = null;
                    _buttonView[1] = null;

                    if (_outlineColor==null) {
                        _outlineColor = new Color4F[stateToInt(STATE.MAX)];
                        _outlineColor[0] = null;
                        _outlineColor[1] = null;
                    }
                }

                setStateView(_buttonView, STATE.PRESSED, outlineView, AppConst.ZOrder.BUTTON_PRESSED, _outlineColor);

                registerUpdate(FLAG_CONTENT_SIZE);

                if (_outlineColor==null || _outlineColor[0]==null) {
                    setOutlineColor(STATE.NORMAL, new Color4F(0, 0,0, 1));
                }
            }

        } else {
            setButton(STATE.PRESSED, (SMView) null);
        }
        _shapeOutlineWidth = lineWidth;
        registerUpdate(FLAG_SHAPE_STYLE);
    }

    public void setIconAlign(ICONALIGN align) {
        _align = align;
        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setIcon(STATE state, SMView view) {

        if (_iconView==null && view!=null) {
            _iconView = new SMView[stateToInt(STATE.MAX)];
            _iconView[0] = null;
            _iconView[1] = null;

            if (_iconColor==null) {
                _iconColor = new Color4F[stateToInt(STATE.MAX)];
                _iconColor[0] = null;
                _iconColor[1] = null;
            }
        }
        if (_iconView!=null) {
            setStateView(_iconView, state, view, state==STATE.NORMAL?AppConst.ZOrder.BUTTON_ICON_NORMAL:AppConst.ZOrder.BUTTON_ICON_PRESSED, _iconColor);

            registerUpdate(FLAG_TEXT_ICON_POSITION);
            registerUpdate(FLAG_ICON_COLOR);
        }
    }

    public void setIcon(STATE state, final String imageFileName) {

        if (imageFileName.isEmpty()) {
            return;
        }

        SMImageView imageView = SMImageView.create(getDirector(), imageFileName);
        imageView.setAnchorPoint(Vec2.MIDDLE);
        imageView.setPosition(getContentSize().width/2, getContentSize().height/2);
        setIcon(state, imageView);
    }

    public void setText(final String text) {
        if (_textLabel==null) {
            setText(text, AppConst.DEFAULT_VALUE.FONT_SIZE);
        } else {
            if (!_textLabel.getText().equals(text)) {
                _textLabel.setText(text);
            }
        }

        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setText(final String text, final float fontSize) {
        if (_textLabel==null) {
            _textLabel = SMLabel.create(getDirector(), text, fontSize, Color4F.BLACK);
            _textLabel.setAnchorPoint(Vec2.MIDDLE);
            _uiContainer.addChild(_textLabel, AppConst.ZOrder.BUTTON_TEXT);
            if (_textColor==null || _textColor[0]==null) {
                setTextColor(STATE.NORMAL, Color4F.BLACK);
            }
        } else {
            // 중간에 바꾸는건 text만 가능하다
            _textLabel.setText(text);
        }

        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setUnderline() {
        _textUnderline = new SMSolidRectView(getDirector());
        this.addChild(_textUnderline);
    }

    public void setIconPadding(float padding) {
        _iconPadding = padding;
        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setIconScale(float scale) {
        _iconScale = scale;
        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setTextScale(float scale) {
        _textScale = scale;
        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setIconOffset(final Vec2 offset) {
        _iconOffset = offset;
        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setTextOffset(final Vec2 offset) {
        _textOffset = offset;
        registerUpdate(FLAG_TEXT_ICON_POSITION);
    }

    public void setShapeCornerRadius(float radius) {
        if (_style==STYLE.DEFAULT || _style==STYLE.RECT || _style==STYLE.SOLID_RECT) {
            return;
        }

        _shapeRadius = radius;
        registerUpdate(FLAG_SHAPE_STYLE);
    }

    public void setShapeCornerQuadrant(int quadrant) {
        if (_style==STYLE.DEFAULT) {
            return;
        }

        _shapeQuardrant = quadrant;
        registerUpdate(FLAG_SHAPE_STYLE);
    }

    public void setShapeAntiAliasWidth(float width) {
        if (_style==STYLE.DEFAULT) {
            return;
        }

        _shapeAntiAliasWidth = width;
        registerUpdate(FLAG_SHAPE_STYLE);
    }

    public Color4F getButtonColor(final STATE state) {
        return _buttonColor[stateToInt(state)];
    }

    public Color4F getIconColor(final STATE state) {
        return _iconColor[stateToInt(state)];
    }

    public Color4F getTextColor(final STATE state) {
        return _textColor[stateToInt(state)];
    }

    public Color4F getOutlineColor(final STATE state) {
        return _outlineColor[stateToInt(state)];
    }

    public SMView getButtonView(final STATE state) {
        if (_buttonView==null) {
            return null;
        }
        return _buttonView[stateToInt(state)];
    }

    public SMView getIconView(final STATE state) {
        if (_iconView==null) {
            return null;
        }
        return _iconView[stateToInt(state)];
    }

    public SMLabel getTextLabel() {return _textLabel;}

//    public void setPushDownAlpha(final float alpha) {_pushDownAlpha = alpha;}

    public void setPushDownRotate(final float rotate) {_pushDownRotate = rotate;}

    public void setPushDownOffset(final Vec2 offset) {
        _pushDownOffset = offset;
    }

    public void setPushDownScale(final float scale) {
        _pushDownScale = scale;
    }

    @Override
    protected void onUpdateOnVisit() {

        if (_updateFlags==0) {
            return;
        }

        if (getAlpha()==0.0f) {
            return;
        }

        if (isUpdate(FLAG_CONTENT_SIZE)) {
            registerUpdate(FLAG_TEXT_ICON_POSITION);

            if (_buttonView!=null) {
                Size size = _uiContainer.getContentSize();
                Vec2 center = new Vec2(size.width/2, size.height/2);

                for (int i=0; i<2; i++) {
                    SMView view = _buttonView[i];
                    if (view!=null) {
                        view.setPosition(center);
                        view.setContentSize(new Size(size.width, size.height));
                    }
                }
            }

            unregisterUpdate(FLAG_CONTENT_SIZE);
        }

        if (isUpdate(FLAG_TEXT_ICON_POSITION)) {
            boolean isContainedText = false;
            Size textSize = new Size();
            if (_textLabel!=null) {
                textSize.setWidth(_textLabel.getContentSize().width()*_textScale);
                textSize.setHeight(_textLabel.getContentSize().height()*_textScale);
                isContainedText = true;
            }

            boolean isContainedIcon = false;
            Size iconSize = new Size();
            if (_iconView!=null) {
                for (int i=0; i<2; i++) {
                    if (_iconView[i]!=null) {
                        // 둘중에 큰거
                        Size s = new Size(_iconView[i].getContentSize().width()*_iconScale, _iconView[i].getContentSize().height()*_iconScale);
                        iconSize.set(Math.max(iconSize.width(), s.width()), Math.max(iconSize.height(), s.height()));
                        isContainedIcon = true;
                    }
                }
            }

            Size size = _uiContainer.getContentSize();
            Vec2 center = new Vec2(size.width()/2, size.height()/2);
            Vec2 textPosition = center;
            Vec2 iconPosition = center;
            float width=0, height=0;

            if (isContainedText && isContainedIcon) {
                switch (_align) {
                    case LEFT:
                    {
                        width = textSize.width() + _iconPadding + iconSize.width();
                        iconPosition.x = (size.width()-width+iconSize.width())/2;
                        textPosition.x = (size.width()+width-textSize.width())/2;
                    }
                    break;
                    case RIGHT:
                    {
                        width = textSize.width() + _iconPadding + iconSize.width();
                        iconPosition.x = (size.width()+width-iconSize.width())/2;
                        textPosition.x = (size.width()-width+textSize.width())/2;
                    }
                    break;
                    case TOP:
                    {
                        height = textSize.height() + _iconPadding + iconSize.height();
                        iconPosition.y = (size.height()+height-iconSize.height())/2;
                        textPosition.y = (size.height()-height+textSize.height())/2;
                    }
                    break;
                    case BOTTOM:
                    {
                        height = textSize.height() + _iconPadding + iconSize.height();
                        iconPosition.y = (size.height()-height+iconSize.height())/2;
                        textPosition.y = (size.height()+height-textSize.height())/2;
                    }
                    break;
                    default:
                    {
                        // center?? nothing to do.
                    }
                    break;
                }
            }

            if (isContainedText) {
                _textLabel.setPosition(textPosition.x+_textOffset.x, textPosition.y+_textOffset.y);
                _textLabel.setScale(_textScale);
                if (_textUnderline!=null) {
                    Size extend = new Size(0, textSize.height()/2);
//                    _textUnderline.setBounds(textPosition.x+_textOffset.x, textPosition.y+_textOffset.y-extend.height(), textSize.width(), AppConst.DEFAULT_VALUE.LINE_WIDTH);
                    _textUnderline.setPosition(textPosition.x+_textOffset.x, textPosition.y+_textOffset.y-extend.height());
                    _textUnderline.setContentSize(new Size(textSize.width(), AppConst.DEFAULT_VALUE.LINE_WIDTH));
                }
            }

            if (isContainedIcon) {
                for (int i=0; i<2; i++) {
                    if (_iconView[i]!=null) {
                        _iconView[i].setPosition(iconPosition.x+_iconOffset.x, iconPosition.y+_iconOffset.y);
                        _iconView[i].setScale(_iconScale);
                    }
                }
            }

            unregisterUpdate(FLAG_TEXT_ICON_POSITION);
        }

        if (isUpdate(FLAG_BUTTON_COLOR | FLAG_ICON_COLOR | FLAG_TEXT_COLOR | FLAG_OUTLINE_COLOR)) {
            // 할일이 있을까 했는데... 아직은 없음.. 나중에 추가하자...
            unregisterUpdate(FLAG_BUTTON_COLOR | FLAG_ICON_COLOR | FLAG_TEXT_COLOR | FLAG_OUTLINE_COLOR);
        }

        if (isUpdate(FLAG_SHAPE_STYLE)) {
            if (_style!=STYLE.DEFAULT && _buttonView!=null) {
                for (int i=0; i<2; i++) {
                    SMView view = _buttonView[i];
                    if (view!=null) {
                        if (view instanceof SMShapeView) {
                            SMShapeView shape = (SMShapeView)view;
                            shape.setCornerRadius(_shapeRadius);
                            shape.setAntiAliasWidth(_shapeAntiAliasWidth);
                            if (i==0) {
                                shape.setLineWidth(_shapeLineWidth);
                            } else {
                                shape.setLineWidth(_shapeOutlineWidth);
                            }
                            shape.setCornerQuadrant(_shapeQuardrant);
                        }
                    }
                }
            }

            unregisterUpdate(FLAG_SHAPE_STYLE);
        }

        onUpdateStateTransition(STATE.NORMAL, _buttonStateValue);
    };

}
