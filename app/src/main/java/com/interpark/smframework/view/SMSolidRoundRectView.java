package com.interpark.smframework.view;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveAARect;
import com.interpark.smframework.base.types.Color4F;

public class SMSolidRoundRectView extends SMShapeView {
    public SMSolidRoundRectView (IDirector director) {
        super(director);
        bgShape = new PrimitiveAARect(director);
        _cornerRadius = 0.0f;
    }

    public static SMSolidRoundRectView create(IDirector director) {
        SMSolidRoundRectView view = new SMSolidRoundRectView(director);
        view.init();
        return view;
    }

    public SMSolidRoundRectView (IDirector director, float radius) {
        this (director);
        _cornerRadius = radius;
    }

    public SMSolidRoundRectView (IDirector director, float round, Color4F color) {
        this (director, round);
//        roundColor = color;
        setTintColor(color);
    }

    public void setCornerRadius(float radius) {
        _cornerRadius = radius;
    }

//    @Override
//    public void updateTintColor() {
//        roundColor.set(new Color4F(_tintColor));
//    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setTintColor(color);
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        setTintColor(r, g, b, a);
    }

    protected PrimitiveAARect bgShape = null;

    protected float _cornerRadius;
//    protected Color4F roundColor = new Color4F(0, 0, 0, 1);

    @Override
    protected void draw(float a) {
        super.draw(a);
        bgShape.drawRect(_contentSize.width/2, _contentSize.height/2, _contentSize.width, _contentSize.height, _cornerRadius, 1);
    }
}
