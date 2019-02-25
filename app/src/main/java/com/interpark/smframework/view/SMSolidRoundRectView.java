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
        mRound = 0.0f;
    }

    public static SMSolidRoundRectView create(IDirector director) {
        SMSolidRoundRectView view = new SMSolidRoundRectView(director);
        view.init();
        return view;
    }

    public SMSolidRoundRectView (IDirector director, float round) {
        this (director);
        mRound = round;
    }

    public SMSolidRoundRectView (IDirector director, float round, Color4F color) {
        this (director, round);
//        roundColor = color;
        setTintColor(color);
    }

    public void setCornerRadius(float round) {
        mRound = round;
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

    protected float mRound;
//    protected Color4F roundColor = new Color4F(0, 0, 0, 1);

    @Override
    protected void render(float a) {
//        getDirector().setColor(roundColor.r, roundColor.g, roundColor.b, roundColor.a);
//        setRenderColor(a);
        getDirector().setColor(_shapeColor.r*a, _shapeColor.g*a, _shapeColor.b*a, _shapeColor.a*a);
        bgShape.drawRect(_contentSize.width/2, _contentSize.height/2, _contentSize.width, _contentSize.height, mRound, 1);
    }
}
