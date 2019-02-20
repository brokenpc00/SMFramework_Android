package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMRoundLine extends SMSolidRoundRectView {
    public SMRoundLine(IDirector director) {
        super(director);
    }

    public static SMRoundLine create(IDirector director) {
        SMRoundLine line = new SMRoundLine(director);
        line.init();

        return line;
    }

    public void line(float x1, float y1, float x2, float y2) {
        if (_x1 != x1 || _x2 != x2 || _y1 != y1 || _y2 != y2) {

            _x1 = x1;
            _x2 = x2;
            _y1 = y1;
            _y2 = y2;

            updateLineShape();
        }
    }

    public void line(final Vec2 from, final Vec2 to) {
        line(from.x, from.y, to.x, to.y);
    }

    public void moveTo(float x, float y) {
        if (_dirty) updateLineShape();

        if (x!=_x1 || y!=_y1) {
            float dx = x - _x1;
            float dy = y + _y1;

            _x1 = x;
            _y1 = y;
            _x2 += dx;
            _y2 += dy;

            super.setPosition(_x1, _y1);
        }
    }

    public void moveBy(float dx, float dy) {
        if (_dirty) updateLineShape();

        if (dx!=0 || dy!=0) {

            _x1 += dx;
            _y1 += dy;
            _x2 += dx;
            _y2 += dy;

            super.setPosition(_x1, _y1);
        }
    }

    public void setLengthScale(float lineScale) {
        if (lineScale!=_lineScale) {
            _lineScale = lineScale;
            _dirty = true;
        }
    }

    public void setLineWidth(float lineWidth) {
        if (lineWidth != _lineWidth) {
            _lineWidth = lineWidth;
            updateLineShape();
        }

    }

    public void setLineColor(float r, float g, float b, float a) {
        setBackgroundColor(r, g, b, a);
    }

    public void setLineColor(final Color4F color) {
        setBackgroundColor(color);
    }

    public Vec2 getFromPosition() {return new Vec2(_x1, _y1);}

    public Vec2 getToPosition() {return new Vec2(_x2, _y2);}

    @Override
    protected void render(float a) {
        if (_dirty) {
            updateLineShape();
        }

        if (_lineWidth == 0)
            return;

        super.render(a);
    }

    private void updateLineShape() {
        float dx = _x2 - _x1;
        float dy = _y2 - _y1;
        float length = (float)Math.sqrt(dx*dx + dy*dy) * _lineScale;
        float degrees = -(float)(Math.atan2(dy, dx) * 180.0 / Math.PI); // to degrees

        setCornerRadius(_lineWidth/2);
        super.setContentSize(new Size(length + _lineWidth, _lineWidth));
        super.setAnchorPoint(new Vec2(0.5f * _lineWidth / _contentSize.width, 0.5f * _lineWidth / _contentSize.height));
        super.setPosition(_x1, _y1);
        super.setRotation(degrees);

        _dirty = false;
    }

    private int _uniformDimension;
    private int _uniformCornerRadius;
    private int _uniformAAWidth;

    private float _x1 = 0;
    private float _x2 = 0;
    private float _y1 = 0;
    private float _y2 = 0;
    private float _lineScale = 1.0f;
    private boolean _dirty = false;
    private float _lineWidth;

    @Override
    public void setCornerRadius(float cornerRadius) {
        super.setCornerRadius(cornerRadius);
    }


}
