package com.interpark.smframework.view;

import android.util.Log;
import android.view.MotionEvent;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.UIContainerView;
import com.interpark.smframework.base.shape.PrimitiveTriangle;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.shader.ShaderNode;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import org.apache.http.cookie.SM;

public class SMSlider extends UIContainerView {
    public SMSlider(IDirector director) {
        super(director);
    }

    public static final InnerColor LIGHT = new InnerColor(MakeColor4F(0xdbdcdf, 1), MakeColor4F(0x222222, 1), MakeColor4F(0xffffff, 1), MakeColor4F(0xeeeff1, 1));
    public static final InnerColor DARK = new InnerColor(MakeColor4F(0x5e5e5e, 1), MakeColor4F(0xffffff, 1), MakeColor4F(0xffffff, 1), MakeColor4F(0xeeeff1, 1));

    public static class InnerColor {
        public InnerColor(final Color4F bgLine, final Color4F fgLine, final Color4F knobNormal, Color4F knobPress) {
            this.bgLine = new Color4F(bgLine);
            this.fgLine = new Color4F(fgLine);
            this.knobNormal = new Color4F(knobNormal);
            this.knobPress = new Color4F(knobPress);
        }

        public Color4F bgLine = null;
        public Color4F fgLine = null;
        public Color4F knobNormal = null;
        public Color4F knobPress = null;
    }

    public enum Type {
        MINUS_ONE_TO_ONE,
        ZERO_TO_ONE,
        MIN_TO_MAX
    }

    public static SMSlider create(IDirector director) {
        return create(director, Type.ZERO_TO_ONE);
    }
    public static SMSlider create(IDirector director, final Type type) {
        return create(director, type, LIGHT);
    }
    public static SMSlider create(IDirector director, final Type type, final InnerColor initColor) {
        SMSlider slider = new SMSlider(director);
        slider.initWithType(type, initColor);
        return slider;
    }

    private static final float CENTER_RADIUS = 10;
    private static final long FLAG_SLIDE_VALUE = 1;
    private static final long FLAG_CONTENT_SIZE = 1<<1;

    protected boolean initWithType(final Type type, final InnerColor initColor) {
        super.init();

        _type = type;

        switch (_type) {
            case MINUS_ONE_TO_ONE:
            {
                _leftLine = SMSolidRectView.create(getDirector());
                _leftLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_LEFT));
                _leftLine.setColor(initColor.bgLine);
                addChild(_leftLine);

                _rightLine = SMSolidRectView.create(getDirector());
                _rightLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_RIGHT));
                _rightLine.setColor(initColor.bgLine);
                addChild(_rightLine);

                _circle = SMCircleView.create(getDirector());
                _circle.setContentSize(new Size(CENTER_RADIUS*2, CENTER_RADIUS*2));
                _circle.setLineWidth(4);
                _circle.setAnchorPoint(new Vec2(Vec2.MIDDLE));
                _circle.setColor(initColor.fgLine);
                addChild(_circle);
            }
            break;
            case ZERO_TO_ONE:
            {
                _rightLine = SMSolidRectView.create(getDirector());
                _rightLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_RIGHT));
                _rightLine.setColor(initColor.bgLine);
                addChild(_rightLine);

                _circle = SMCircleView.create(getDirector());
                _circle.setContentSize(new Size(CENTER_RADIUS*2, CENTER_RADIUS*2));
                _circle.setLineWidth(4);
                _circle.setAnchorPoint(new Vec2(Vec2.MIDDLE));
                _circle.setColor(initColor.fgLine);
                addChild(_circle);
            }
            break;
            case MIN_TO_MAX:
            {
                _rightLine = SMSolidRectView.create(getDirector());
                _rightLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_RIGHT));
                _rightLine.setColor(initColor.fgLine);
                addChild(_rightLine);
            }
            break;
        }

        _bgLine = SMSolidRectView.create(getDirector());
        _bgLine.setColor(initColor.fgLine);
        addChild(_bgLine);

        if (_type==Type.MIN_TO_MAX) {
            _minButton = KnobButtonCreate(getDirector());
            _minButton.setContentSize(new Size(120, 120));
            _minButton.setAnchorPoint(new Vec2(Vec2.MIDDLE));
            _minButton.setPadding(29);
            _minButton.setButtonColor(STATE.NORMAL, initColor.knobNormal);
            _minButton.setButtonColor(STATE.PRESSED, initColor.knobPress);

            SMSolidCircleView minShadow = SMSolidCircleView.create(getDirector());
            _minButton.setBackgroundView(minShadow);
            minShadow.setContentSize(new Size(75, 75));
            minShadow.setAnchorPoint(new Vec2(Vec2.MIDDLE));
            // Todo... see. this value
            minShadow.setAntiAliasWidth(20);
            minShadow.setPosition(60, 60-2);
            _minButton.setBackgroundColor(new Color4F(0, 0, 0, 0.2f));
            _minButton.setOnTouchListener(new OnTouchListener() {
                @Override
                public int onTouch(SMView view, MotionEvent event) {
                    return buttonTouch(view, event);
                }
            });
            addChild(_minButton);


            _maxButton = KnobButtonCreate(getDirector());
            _maxButton.setContentSize(new Size(120, 120));
            _maxButton.setAnchorPoint(new Vec2(Vec2.MIDDLE));
            _maxButton.setPadding(29);
            _maxButton.setButtonColor(STATE.NORMAL, initColor.knobNormal);
            _maxButton.setButtonColor(STATE.PRESSED, initColor.knobPress);


            SMSolidCircleView maxShadow = SMSolidCircleView.create(getDirector());
            _maxButton.setBackgroundView(maxShadow);
            maxShadow.setContentSize(new Size(75, 75));
            maxShadow.setAnchorPoint(new Vec2(Vec2.MIDDLE));
            // Todo... see. this value
            maxShadow.setAntiAliasWidth(20);
            maxShadow.setPosition(60, 60-2);
            _maxButton.setBackgroundColor(new Color4F(0, 0, 0, 0.2f));
            _maxButton.setOnTouchListener(new OnTouchListener() {
                @Override
                public int onTouch(SMView view, MotionEvent event) {
                    return buttonTouch(view, event);
                }
            });
            addChild(_maxButton);

            setSliderValue(_minValue, _maxValue);
        } else {
            _knobButton = KnobButtonCreate(getDirector());
            _knobButton.setContentSize(new Size(120, 120));
            _knobButton.setAnchorPoint(new Vec2(Vec2.MIDDLE));
            _knobButton.setPadding(29);
            _knobButton.setButtonColor(STATE.NORMAL, initColor.knobNormal);
            _knobButton.setButtonColor(STATE.PRESSED, initColor.knobPress);

            SMSolidCircleView shadow = SMSolidCircleView.create(getDirector());
            _knobButton.setBackgroundView(shadow);
            shadow.setContentSize(new Size(75, 75));
            shadow.setAnchorPoint(new Vec2(Vec2.MIDDLE));
            // Todo... see. this value
            shadow.setAntiAliasWidth(20);
            shadow.setPosition(60, 60-2);
            _knobButton.setBackgroundColor(new Color4F(0, 0, 0, 0.2f));
            _knobButton.setOnTouchListener(new OnTouchListener() {
                @Override
                public int onTouch(SMView view, MotionEvent event) {
                    return buttonTouch(view, event);
                }
            });
            addChild(_knobButton);
            setSliderValue(0);
        }

        return true;
    }

    public int buttonTouch(SMView view, MotionEvent event) {
        int action = event.getAction();
        Vec2 point = new Vec2(event.getX(), event.getY());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            {
                if (getActionByTag(AppConst.TAG.USER+2)==null) {
                    if (_type==Type.MIN_TO_MAX) {
                        if (view==_minButton) {
                            _minPoint.set(point);
                            _minFocused = true;
                        } else {
                            _maxPoint.set(point);
                            _maxFocused = true;
                        }
                    } else {
                        _knobPoint.set(point);
                        _knobFocused = true;
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {
                if (view==_knobButton) {
                    if (_knobFocused) {
                        float value;
                        if (_type==Type.MINUS_ONE_TO_ONE) {
                            Vec2 pt = new Vec2(view.getPosition().minus(new Vec2(_contentSize.width/2, _contentSize.height/2)).add(point).minus(_knobPoint));
                            float dist = pt.x;

                            if (dist>_sliderWidth/2) {
                                dist = _sliderWidth/2;
                            } else if (dist<-_sliderWidth/2) {
                                dist = -_sliderValue/2;
                            }

                            value = dist / (_sliderWidth/2);
                        } else {
                            Vec2 pt = new Vec2(view.getPosition().add(point).minus(_knobPoint));
                            float dist = pt.x - 50;

                            if (dist<0) {
                                dist = 0;
                            } else if (dist>_sliderWidth) {
                                dist = _sliderWidth;
                            }

                            value = dist / _sliderWidth;
                        }

                        if (Math.abs(value) < 0.015f) {
                            value = 0;
                        }

                        setSliderValue(value, false);
                    }

                    return TOUCH_INTERCEPT;
                } else if (view==_minButton) {
                    if (_minFocused) {
                        float value;
                        Vec2 pt = new Vec2(view.getPosition().add(point).minus(_maxPoint));
                        float dist = pt.x - 50;
                        if (dist<0) {
                            dist = 0;
                        } else if (dist>_maxButton.getPositionX()-110) {
                            dist = _maxButton.getPositionX()-110;
                        }

                        value = dist / _sliderWidth;

                        if (Math.abs(value) < 0.015f) {
                            value = 0;
                        }

                        setSliderValue(value, _maxValue, false);
                    }

                    return TOUCH_INTERCEPT;
                } else if (view==_maxButton) {
                    if (_maxFocused) {
                        float value;
                        Vec2 pt = new Vec2(view.getPosition().add(point).minus(_maxPoint));
                        float dist = pt.x - 50;
                        if (dist<_minButton.getPositionX()+10) {
                            dist = _minButton.getPositionX()+10;
                        } else if (dist>_sliderWidth) {
                            dist = _sliderWidth;
                        }

                        value = dist / _sliderWidth;

                        setSliderValue(_minValue, value, false);
                    }

                    return TOUCH_INTERCEPT;
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            {
                _minFocused = false;
                _maxFocused = false;
                _knobFocused = false;
            }
            break;
        }

        return TOUCH_FALSE;
    }

    @Override
    public int dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return TOUCH_TRUE;
    }

    @Override
    public void onUpdateOnVisit() {
        if (isUpdate(FLAG_CONTENT_SIZE)) {
            unregisterUpdate(FLAG_CONTENT_SIZE);
            updateLayout();
        }

        if (isUpdate(FLAG_SLIDE_VALUE)) {
            unregisterUpdate(FLAG_SLIDE_VALUE);

            switch (_type) {
                case MINUS_ONE_TO_ONE:
                {
                    float x = _knobButton.getPositionX() - _contentSize.width/2;
                    if (x>0) {
                        float len = x - (CENTER_RADIUS-ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f);
                        if (len<0) {
                            len = 0;
                        }

                        _bgLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_LEFT));
                        _bgLine.setContentSize(new Size(len, ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2));
                        _bgLine.setPosition(_contentSize.width/2 + (CENTER_RADIUS-1.5f), _contentSize.height/2);
                    } else {
                        float len = -x - (CENTER_RADIUS-1.5f);
                        if (len<0) {
                            len = 0;
                        }

                        _bgLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_RIGHT));
                        _bgLine.setContentSize(new Size(len, ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2));
                        _bgLine.setPosition(_contentSize.width/2 - (CENTER_RADIUS-ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f), _contentSize.height/2);
                    }
                }
                break;
                case ZERO_TO_ONE:
                {
                    float x = _knobButton.getPositionX() - 50;

                    float len = x - (CENTER_RADIUS-ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f);
                    if (len<0) {
                        len = 0;
                    }

                    _bgLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_LEFT));
                    _bgLine.setContentSize(new Size(len, ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2));
                    _bgLine.setPosition(50 + (CENTER_RADIUS-ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f), _contentSize.height/2);
                }
                break;
                case MIN_TO_MAX:
                {
                    float x = _minButton.getPositionX() - 50;
                    float len = _maxButton.getPositionX() - x - 50 - (CENTER_RADIUS-1.5f)*ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2;

                    if (len<0) {
                        len = 0;
                    }

                    _bgLine.setAnchorPoint(new Vec2(Vec2.MIDDLE_LEFT));
                    _bgLine.setContentSize(new Size(len, ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2));
                    _bgLine.setPosition(_minButton.getPositionX(), _contentSize.height/2);
                }
                break;
            }
        }
    }

    public void updateLayout() {
        _sliderWidth = _contentSize.width - 100;

        switch (_type) {
            case MINUS_ONE_TO_ONE:
            {
                _leftLine.setContentSize(new Size(_sliderWidth/2 - (CENTER_RADIUS-ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f), ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2));
                _leftLine.setPosition(new Vec2(50, _contentSize.height/2));

                Size newSize = new Size(_sliderWidth/2 - (CENTER_RADIUS-ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*.15f), ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2);
                _rightLine.setContentSize(newSize);
                _rightLine.setPosition(_contentSize.width-50, _contentSize.height/2);

                _circle.setPosition(_contentSize.width/2, _contentSize.height/2);
            }
            break;
            case ZERO_TO_ONE:
            {
                Size newSize = new Size(_sliderWidth-(CENTER_RADIUS-1.5f), ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2);
                _rightLine.setContentSize(newSize);
                _rightLine.setPosition(_contentSize.width-50, _contentSize.height/2);

                _circle.setPosition(50, _contentSize.height/2);
            }
            break;
            case MIN_TO_MAX:
            {
                Size newSize = new Size(_sliderWidth, ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2);
                _rightLine.setContentSize(newSize);
                _rightLine.setPosition(_contentSize.width-50, _contentSize.height/2);
            }
            break;
        }

        if (_type==Type.MIN_TO_MAX) {
            setKnobPosition(_minValue, _maxValue, true);
        } else {
            setKnobPosition(_sliderValue, true);
        }
    }

    @Override
    public void setContentSize(final Size size) {
        super.setContentSize(size);
        registerUpdate(FLAG_CONTENT_SIZE);

    }

    @Override
    public void setContentSize(final float width, final float height) {
        super.setContentSize(width, height);
        registerUpdate(FLAG_CONTENT_SIZE);
    }


    public void setSliderValue(final float sliderValue) {
        setSliderValue(sliderValue, true);
    }
    public void setSliderValue(final float sliderValue, final boolean immediate) {
        assert (_type!=Type.MIN_TO_MAX);

        if (_type==Type.MINUS_ONE_TO_ONE) {
            _sliderValue = Math.min(Math.max(sliderValue, -1.0f), 1.0f);
        } else {
            _sliderValue = Math.min(Math.max(sliderValue, 0.0f), 1.0f);
        }

        if (_listener!=null) {
            _listener.onSliderValueChanged(this, _sliderValue);
        }

        setKnobPosition(_sliderValue, immediate);
    }

    public void setSliderValue(final float minValue, final float maxValue) {
        setSliderValue(minValue, maxValue, true);
    }
    public void setSliderValue(final float minValue, final float maxValue, final boolean immediate) {
        assert (_type==Type.MIN_TO_MAX);
        _minValue = Math.min(Math.max(minValue, 0.0f), 1.0f);
        _maxValue = Math.min(Math.max(maxValue, 0.0f), 1.0f);

        if (_listener!=null) {
            _listener.onSliderValueChanged(this, _minValue, _maxValue);
        }

        setKnobPosition(_minValue, _maxValue, immediate);
    }


    private void setKnobPosition(final float sliderValue) {
        setKnobPosition(sliderValue, true);
    }
    private void setKnobPosition(final float sliderValue, final boolean immediate) {
        assert (_type!=Type.MIN_TO_MAX);

        float x;
        if (_type==Type.MINUS_ONE_TO_ONE) {
            x = sliderValue * _sliderWidth/2;
            _knobButton.setPosition(_contentSize.width/2 + x, _contentSize.height/2, sliderValue==0?true:immediate);
        } else {
            x = sliderValue * _sliderWidth;
            _knobButton.setPosition(50+x, _contentSize.height/2, sliderValue==0?true:immediate);
        }

        registerUpdate(FLAG_SLIDE_VALUE);
    }

    private void setKnobPosition(final float minValue, final float maxValue) {
        setKnobPosition(minValue, maxValue, true);
    }
    private void setKnobPosition(final float minValue, final float maxValue, final boolean immediate) {
        assert (_type==Type.MIN_TO_MAX);

        float minX = minValue * _sliderWidth;
        float maxX = maxValue * _sliderWidth;

        _minButton.setPosition(50+minX, _contentSize.height/2, minValue==0?true:immediate);
        _maxButton.setPosition(50+maxX, _contentSize.height/2, maxValue==0?true:immediate);

        registerUpdate(FLAG_SLIDE_VALUE);
    }


    public float getSliderValue(){return _sliderValue;}
    public float getMinValue() {return _minValue;}
    public float getMaxValue() {return _maxValue;}



    private KnobButton KnobButtonCreate(IDirector director) {
        KnobButton knob = new KnobButton(director);
        knob.initWithStyle(SMButton.STYLE.SOLID_CIRCLE);
        return knob;
    }

    private class KnobButton extends SMButton {
        public KnobButton(IDirector director) {
            super(director);
        }

        @Override
        protected void onSmoothUpdate(final long flags, float dt) {
            if ((flags & VIEWFLAG_POSITION)>0) {
                ((SMSlider)getParent()).updateKnob();
            }
        }
    }

    private KnobButton _knobButton = null;
    private KnobButton _minButton = null;
    private KnobButton _maxButton = null;

    private Vec2 _knobPoint = new Vec2(Vec2.ZERO);
    private Vec2 _minPoint = new Vec2(Vec2.ZERO);
    private Vec2 _maxPoint = new Vec2(Vec2.ZERO);

    private float _sliderValue = 0;
    private float _minValue = 0;
    private float _maxValue = 1;

    private float _sliderWidth = 1;
    private boolean _knobFocused = false;
    private boolean _minFocused = false;
    private boolean _maxFocused = false;

    private Type _type = Type.ZERO_TO_ONE;

    private SMSolidRectView _bgLine = null;
    private SMSolidRectView _leftLine = null;
    private SMSolidRectView _rightLine = null;
    private SMCircleView _circle = null;

    public void updateKnob() {
        registerUpdate(FLAG_SLIDE_VALUE);
    }


    public interface OnSliderListener {
        public void onSliderValueChanged(SMSlider slider, float value);
        public void onSliderValueChanged(SMSlider slider, float minValue, float maxValue);
    }
    private OnSliderListener _listener = null;
    public void setOnSliderListener(OnSliderListener l) {_listener = l;}

}
