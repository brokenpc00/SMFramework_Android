package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveCircle;
import com.interpark.smframework.base.types.Color4F;

public class SMSolidCircleView extends SMShapeView {
    public SMSolidCircleView(IDirector director) {
        super(director);
        bgShape = new PrimitiveCircle(director);
    }

    public SMSolidCircleView(IDirector director, Color4F color) {
        this(director);
        bgColor = color;
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        bgColor = color;
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        bgColor = new Color4F(r, g, b, a);
    }


    protected PrimitiveCircle bgShape = null;
    private Color4F bgColor = new Color4F(0, 0, 0, 1);

    @Override
    protected void render(float a) {
        getDirector().setColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        float x = _contentSize.width/2;
        float y = _contentSize.height/2;
        float radius = _contentSize.width/2;
        if (_contentSize.width>_contentSize.height) {
            radius = _contentSize.height/2;
        }
        bgShape.drawCircle(x, y, radius, 1.5f);
    }


}
