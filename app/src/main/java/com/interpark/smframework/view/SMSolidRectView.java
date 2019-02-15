package com.interpark.smframework.view;

import android.opengl.GLES20;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveRect;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;

public class SMSolidRectView extends SMShapeView {
    public SMSolidRectView(IDirector director) {
        super(director);

        bgShape = new PrimitiveRect(director, 1, 1, 0.0f, 0.0f, true);
    }

    protected PrimitiveRect bgShape = null;

    private float lineWidth = 1.0f;
    private Color4F solidColor = new Color4F(0, 0, 0, 1);

    @Override
    public void setBackgroundColor(final float r, final float g, final float b, final float a) {
        solidColor = new Color4F(r, g, b, a);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        solidColor = color;
    }

    public SMSolidRectView(IDirector director, Color4F solidcolor) {
        this(director);
        solidColor = solidcolor;
    }

    @Override
    protected void render(float a) {
        getDirector().setColor(solidColor.r, solidColor.g, solidColor.b, solidColor.a);
        bgShape.drawScaleXY(0, 0, _contentSize.width, _contentSize.height);
    }
}
