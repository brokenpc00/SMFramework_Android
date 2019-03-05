package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.UIContainerView;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

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
                _leftLine.setAnchorPoint(Vec2.MIDDLE_LEFT);
                _leftLine.setColor(initColor.bgLine);
                addChild(_leftLine);

                _rightLine = SMSolidRectView.create(getDirector());
                _rightLine.setAnchorPoint(Vec2.MIDDLE_RIGHT);
                _rightLine.setColor(initColor.bgLine);
                addChild(_rightLine);

                _circle = SMCircleView.create(getDirector());
                _circle.setContentSize(new Size(CENTER_RADIUS*2, CENTER_RADIUS*2));
                _circle.setLineWidth(4);
                _circle.setAnchorPoint(Vec2.MIDDLE);
                _circle.setColor(initColor.fgLine);
                addChild(_circle);
            }
            break;
            case ZERO_TO_ONE:
            {
                _rightLine = SMSolidRectView.create(getDirector());
                _rightLine.setAnchorPoint(Vec2.MIDDLE_RIGHT);
                _rightLine.setColor(initColor.bgLine);
                addChild(_rightLine);

                _circle = SMCircleView.create(getDirector());
                _circle.setContentSize(new Size(CENTER_RADIUS*2, CENTER_RADIUS*2));
                _circle.setLineWidth(4);
                _circle.setAnchorPoint(Vec2.MIDDLE);
                _circle.setColor(initColor.fgLine);
                addChild(_circle);
            }
            break;
            case MIN_TO_MAX:
            {
                _rightLine = SMSolidRectView.create(getDirector());
                _rightLine.setAnchorPoint(Vec2.MIDDLE_RIGHT);
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
            _minButton.setAnchorPoint(Vec2.MIDDLE);
            _minButton.setPadding(29);
            _minButton.setButtonColor(STATE.NORMAL, initColor.knobNormal);
            _minButton.setButtonColor(STATE.PRESSED, initColor.knobPress);

            SMSolidCircleView minShadow = SMSolidCircleView.create(getDirector());
//            _minButton.setBack

        } else {

        }

        return true;
    }

    public void setSliderValue(final float sliderValue) {
        setSliderValue(sliderValue, true);
    }
    public void setSliderValue(final float sliderValue, final boolean immediate) {

    }

    public void setSliderValue(final float minValue, final float maxValue) {
        setSliderValue(minValue, maxValue, true);
    }
    public void setSliderValue(final float minValue, final float maxValue, final boolean immediate) {

    }

    public float getSliderValue(){return _sliderValue;}


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

    private Vec2 _knobPoint = null;
    private Vec2 _minPoint = null;
    private Vec2 _maxPoint = null;

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

    }


    public interface OnSliderListener {
        // onSliderValueChanged(SMSlider slider, float value);
        public void func(SMSlider slider, float value);
        // onSliderValueChanged(SMSlider slider, float minValue, float maxValue);
        public void func(SMSlider slider, float minValue, float maxValue);
    }
    private OnSliderListener _listener;
}
