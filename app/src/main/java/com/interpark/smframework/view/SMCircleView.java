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
    public static SMCircleView create(IDirector director) {
        SMCircleView view = new SMCircleView(director);
        view.init();
        return view;
    }


    public SMCircleView(IDirector director, final float linewidth) {
        this(director);
        _lineWidth = linewidth;
    }

    public SMCircleView(IDirector director, final float linewidth, final Color4F color) {
        this(director, linewidth);
        setTintColor(color);
    }

    @Override
    public void setLineWidth(final float linewidth) {
        _lineWidth = linewidth*2.0f;
    }

    public void setLineColor(final Color4F color) {
        setTintColor(color);
    }

    protected PrimitiveCircle bgShape = null;

    @Override
    protected void draw(float a) {
        super.draw(a);
        float x = _contentSize.width/2;
        float y = _contentSize.height/2;
        float radius = _contentSize.width/2;
        if (_contentSize.width>_contentSize.height) {
            radius = _contentSize.height/2;
        }
        bgShape.drawRing(x, y, radius, _lineWidth, 1.5f);
    }
}
