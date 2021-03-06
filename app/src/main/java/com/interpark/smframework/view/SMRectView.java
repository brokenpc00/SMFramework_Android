package com.interpark.smframework.view;

import android.graphics.Color;
import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveRect;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;

public class SMRectView extends SMShapeView {
    public SMRectView(IDirector director) {
        super(director);

        bgShape = new PrimitiveRect(director, 1, 1, 0.0f, 0.0f, false);
    }
    public static SMRectView create(IDirector director) {
        SMRectView view = new SMRectView(director);
        view.init();
        return view;
    }


    protected PrimitiveRect bgShape = null;

    public SMRectView(IDirector director, Color4F outlinecolor) {
        this(director);
        setColor(outlinecolor);
    }

    public SMRectView(IDirector director, Color4F outlinecolor, float linewidth) {
        this(director);
        setColor(outlinecolor);
        _lineWidth = linewidth;
    }

    @Override
    public void setBackgroundColor(final float r, final float g, final float b, final float a) {
        setColor(r, g, b, a);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setColor(color);
    }


    @Override
    public void setLineWidth(final float lineWidth) {
        _lineWidth = lineWidth;
    }

    @Override
    protected void draw(final Mat4 m, int flags) {
        super.draw(m, flags);
        GLES20.glLineWidth(_lineWidth);
        bgShape.drawScaleXY(0, 0, _contentSize.width, _contentSize.height);
    }
}
