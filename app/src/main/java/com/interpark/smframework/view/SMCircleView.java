package com.interpark.smframework.view;

import android.graphics.Color;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveCircle;
import com.interpark.smframework.base.types.Color4F;

public class SMCircleView extends SMShapeView {
    public SMCircleView(IDirector director) {
        super(director);
        bgShape = new PrimitiveCircle(director);
    }

    public SMCircleView(IDirector director, final float linewidth) {
        this(director);
        _lineWidth = linewidth;
    }

    public SMCircleView(IDirector director, final float linewidth, final Color4F color) {
        this(director, linewidth);
        lineColor = color;
    }

    @Override
    public void setLineWidth(final float linewidth) {
        _lineWidth = linewidth*2.0f;
    }

    public void setLineColor(final Color4F color) {
        setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        lineColor = color;
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        lineColor = new Color4F(r, g, b, a);
    }


    protected PrimitiveCircle bgShape = null;
    private Color4F lineColor = new Color4F(0, 0, 0, 1);

    @Override
    protected void render(float a) {
        getDirector().setColor(lineColor.r, lineColor.g, lineColor.b, lineColor.a);
        float x = _contentSize.width/2;
        float y = _contentSize.height/2;
        float radius = _contentSize.width/2;
        if (_contentSize.width>_contentSize.height) {
            radius = _contentSize.height/2;
        }
        bgShape.drawRing(x, y, radius, _lineWidth, 1.5f);
    }
}
