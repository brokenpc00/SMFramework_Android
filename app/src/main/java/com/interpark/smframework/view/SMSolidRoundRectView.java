package com.interpark.smframework.view;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveSolidRect;
import com.interpark.smframework.base.types.Color4F;

public class SMSolidRoundRectView extends SMShapeView {
    public SMSolidRoundRectView (IDirector director) {
        super(director);
        bgShape = new PrimitiveSolidRect(director);
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
        setColor(color);
    }

    public void setCornerRadius(float radius) {
        _cornerRadius = radius;
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setColor(color);
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        setColor(r, g, b, a);
    }

    protected PrimitiveSolidRect bgShape = null;

    protected float _cornerRadius;

    @Override
    protected void draw(final Mat4 m, int flags) {
        super.draw(m, flags);
        bgShape.drawRect(_contentSize.width/2, _contentSize.height/2, _contentSize.width, _contentSize.height, _cornerRadius, 1);
    }
}
