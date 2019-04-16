package com.interpark.smframework.view;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.R;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveRoundRectLine;
import com.interpark.smframework.base.shape.ShapeConstant.LineType;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Color4F;

public class SMRoundRectView extends SMShapeView {
    public SMRoundRectView(IDirector director) {
        super(director);
        mType = LineType.Solid;
        _lineWidth = 10.0f;
        lineTexture = director.getTextureManager().createTextureFromResource(R.raw.dash_line_2);
        mRound = 0.0f;
    }
    public SMRoundRectView (IDirector director, float tickness, LineType type) {
        this(director);
        mType = type;
        _lineWidth = tickness*10.0f;
        bgShape = new PrimitiveRoundRectLine(director, lineTexture, _lineWidth, type);
    }

    public SMRoundRectView (IDirector director, float tickness, LineType type, float round) {
        this (director, tickness, type);
        mRound = round;
    }

    public SMRoundRectView (IDirector director, float tickness, LineType type, float round, Color4F color) {
        this (director, tickness, type, round);
        setColor(color);
    }

    public static SMRoundRectView create(IDirector director) {
        SMRoundRectView view = new SMRoundRectView(director);
        view.init();
        return view;
    }

    public static SMRoundRectView create(IDirector director, float tickness, LineType type, float round) {
        SMRoundRectView view = new SMRoundRectView(director, tickness, type, round);
        view.init();
        return view;
    }


    public void setLineWidth(float width) {
        _lineWidth = width*10.0f;
        if (bgShape!=null) {
            bgShape.releaseResources();
            bgShape = null;
        }
        bgShape = new PrimitiveRoundRectLine(getDirector(), lineTexture, _lineWidth, mType);
    }

    public void setCornerRadius(float round) {
        mRound = round;
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setColor(color);
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        setColor(r, g, b, a);
    }

    public void setLineColor(Color4F color) {
        setColor(color);
    }

    protected PrimitiveRoundRectLine bgShape = null;
    protected Texture lineTexture = null;
    private float mRound;
    private LineType mType = LineType.Solid;

    @Override
    protected void draw(float a) {
        super.draw(a);

        bgShape.setSize(_contentSize.width, _contentSize.height, mRound);
        bgShape.drawRotate(_contentSize.width/2, _contentSize.height/2, 0);
    }

}
