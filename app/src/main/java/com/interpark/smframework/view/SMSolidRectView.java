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

    public static SMSolidRectView create(IDirector director) {
        SMSolidRectView view = new SMSolidRectView(director);
        view.init();
        return view;
    }


    protected PrimitiveRect bgShape = null;

    private float lineWidth = 1.0f;
//    private Color4F solidColor = new Color4F(0, 0, 0, 1);

//    @Override
//    public void updateTintColor() {
//        solidColor.set(new Color4F(_tintColor));
//    }

    @Override
    public void setBackgroundColor(final float r, final float g, final float b, final float a) {
        setTintColor(r, g, b, a);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setTintColor(color);
    }

    public SMSolidRectView(IDirector director, Color4F solidcolor) {
        this(director);
//        solidColor = solidcolor;
        setTintColor(solidcolor);
    }

    @Override
    protected void render(float a) {
        getDirector().setColor(_shapeColor.r*a, _shapeColor.g*a, _shapeColor.b*a, _shapeColor.a*a);
//        getDirector().setColor(solidColor.r, solidColor.g, solidColor.b, solidColor.a);
        bgShape.drawScaleXY(0, 0, _contentSize.width, _contentSize.height);
    }
}
