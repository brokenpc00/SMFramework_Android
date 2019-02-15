package com.interpark.smframework.base.types;

import android.renderscript.Matrix4f;

import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class AffineTransform {
    public AffineTransform() {

    }

    public float a, b, c, d;
    public float tx, ty;

//    static final AffineTransform IDENTIFY = ;

    public static AffineTransform AffineTransformMakeIdentity() {
        return __CCAffineTransformMake(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
    }

    public static AffineTransform __CCAffineTransformMake(float a, float b, float c, float d, float tx, float ty) {
        AffineTransform t = new AffineTransform();
        t.a = a; t.b = b; t.c = c; t.d = d; t.tx = tx; t.ty = ty;
        return t;
    }

    public static Vec2 __CCPointApplyAffineTransform(final Vec2 point, final AffineTransform t) {
        Vec2 p = new Vec2();
        p.x = (float)((double)t.a * point.x + (double)t.c * point.y + t.tx);
        p.y = (float)((double)t.b * point.x + (double)t.d * point.y + t.ty);
        return p;
    }

    public static Vec2 PointApplyTransform(final Vec2 point, final Matrix4f transform) {
        Vec3 vec = new Vec3(point.x, point.y, 0);
//        transform.translate();
//        transform.transformPoint(&vec);
        return new Vec2(vec.x, vec.y);
    }
}
