package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.shape.PrimitiveCircle;
import com.interpark.smframework.base.shape.PrimitiveTriangle;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMTriangleView extends SMShapeView {
    private SMTriangleView(IDirector director) {
        super(director);

    }

    public static SMTriangleView create(IDirector director) {
        SMTriangleView view = new SMTriangleView(director);
        view.initWithValue(0, 0,0, Vec2.ZERO, Vec2.ZERO, Vec2.ZERO);
        return view;
    }

    public static SMTriangleView create(IDirector director, Size size) {
        return create(director, size.width, size.height);
    }
    public static SMTriangleView create(IDirector director, float width, float height) {
        SMTriangleView view = new SMTriangleView(director);
        view.initWithValue(width, height, 0.015f, Vec2.ZERO, Vec2.ZERO, Vec2.ZERO);
        return view;
    }
    public static SMTriangleView create(IDirector director, float width, float height, float aaWidth, Vec2 p0, Vec2 p1, Vec2 p2) {
        SMTriangleView view = new SMTriangleView(director);
        view.initWithValue(width, height, aaWidth, p0, p1, p2);
        return view;
    }

    // anticlockwise
    public void setTriangle(Vec2 p0, Vec2 p1, Vec2 p2) {
        setTriangle(_aaWidth, p0, p1, p2);
    }

    // anticlockwise
    public void setTriangle(float aaWidth, Vec2 p0, Vec2 p1, Vec2 p2) {
        _aaWidth = aaWidth;
        _p0.set(p0);
        _p1.set(p1);
        _p2.set(p2);
    }

    // anticlockwise
    protected boolean initWithValue(float width, float height, float lineWidth, Vec2 p0, Vec2 p1, Vec2 p2) {

        bgShape = new PrimitiveTriangle(getDirector(), width, height);

        setTriangle(lineWidth, p0, p1, p2);

        return true;
    }

    protected PrimitiveTriangle bgShape = null;

    @Override
    protected void draw(final Mat4 m, int flags) {
        super.draw(m, flags);
        bgShape.drawTrinalge(_p0, _p1, _p2, _aaWidth);
    }


    private Vec2 _p0 = new Vec2(Vec2.ZERO);
    private Vec2 _p1 = new Vec2(Vec2.ZERO);
    private Vec2 _p2 = new Vec2(Vec2.ZERO);

}
