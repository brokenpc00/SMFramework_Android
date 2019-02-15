package com.interpark.smframework.view;

import android.graphics.Color;
import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveRect;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;

public class SMRectView extends SMShapeView {
    public SMRectView(IDirector director) {
        super(director);

        bgShape = new PrimitiveRect(director, 1, 1, 0.0f, 0.0f, false);
    }

    protected PrimitiveRect bgShape = null;

//    private float xScale = 1.0f;
//    private float yScale = 1.0f;
    private Color4F outlineColor = new Color4F(0, 0, 0, 1);

    public SMRectView(IDirector director, Color4F outlinecolor) {
        this(director);
        outlineColor = outlinecolor;
    }

    public SMRectView(IDirector director, Color4F outlinecolor, float linewidth) {
        this(director);
        outlineColor = outlinecolor;
        _lineWidth = linewidth;
    }

    @Override
    public void setBackgroundColor(final float r, final float g, final float b, final float a) {
        outlineColor = new Color4F(r, g, b, a);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        outlineColor = color;
    }

    @Override
    public void setLineWidth(final float lineWidth) {
        _lineWidth = lineWidth;
    }

    @Override
    protected void render(float a) {
        getDirector().setColor(outlineColor.r, outlineColor.g, outlineColor.b, outlineColor.a);
        GLES20.glLineWidth(_lineWidth);
        bgShape.drawScaleXY(0, 0, _contentSize.width, _contentSize.height);
    }
}
