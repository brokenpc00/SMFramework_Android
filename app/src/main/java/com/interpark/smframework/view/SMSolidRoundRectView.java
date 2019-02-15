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

    public SMSolidRoundRectView (IDirector director, float round) {
        this (director);
        mRound = round;
    }

    public SMSolidRoundRectView (IDirector director, float round, Color4F color) {
        this (director, round);
        roundColor = color;
    }

    public void setCornerRadius(float round) {
        mRound = round;
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
//        super.setBackgroundColor(1, 1, 1, 0);
        roundColor = new Color4F(r, g, b, a);
    }

    protected PrimitiveAARect bgShape = null;

    private float mRound;
    private Color4F roundColor = new Color4F(0, 0, 0, 1);

    @Override
    protected void render(float a) {
        getDirector().setColor(roundColor.r, roundColor.g, roundColor.b, roundColor.a);
        bgShape.drawRect(_contentSize.width/2, _contentSize.height/2, _contentSize.width, _contentSize.height, mRound, 1);
    }
}
