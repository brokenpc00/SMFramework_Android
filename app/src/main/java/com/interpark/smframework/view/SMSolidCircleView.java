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

    public static SMSolidCircleView create(IDirector director) {
        SMSolidCircleView view = new SMSolidCircleView(director);
        view.init();
        return view;
    }

    public SMSolidCircleView(IDirector director, Color4F color) {
        this(director);
        setTintColor(color);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setTintColor(color);
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        setTintColor(r, g, b, a);
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
        bgShape.drawCircle(x, y, radius, 1.5f);
    }


}
