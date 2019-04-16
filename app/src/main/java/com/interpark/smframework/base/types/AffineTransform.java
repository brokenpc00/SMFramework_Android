package com.interpark.smframework.base.types;

import android.renderscript.Matrix4f;

import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class AffineTransform {
    public AffineTransform() {

    }

    public AffineTransform(AffineTransform transform) {
        set(transform);
    }

    public void set(AffineTransform t) {
        this.a = t.a;
        this.b = t.b;
        this.c = t.c;
        this.d = t.d;
        this.tx = t.tx;
        this.ty = t.ty;
    }

    public static void CGAffineToGL(final AffineTransform t, float[] m) {
        m[2] = m[3] = m[6] = m[7] = m[8] = m[9] = m[11] = m[14] = 0.0f;
        m[10] = m[15] = 1.0f;
        m[0] = t.a; m[4] = t.c; m[12] = t.tx;
        m[1] = t.b; m[5] = t.d; m[13] = t.ty;
    }

    public static void GLToCGAffine(final float[] m, AffineTransform t) {
        t.a = m[0]; t.c = m[4]; t.tx = m[12];
        t.b = m[1]; t.d = m[5]; t.ty = m[13];
    }

    public float a, b, c, d;
    public float tx, ty;

    public static final AffineTransform IDENTIFY = AffineTransformMakeIdentity();
    public static final AffineTransform AffineTransformIdentity = AffineTransformMakeIdentity();

    public static AffineTransform AffineTransformMakeIdentity() {
        return __CCAffineTransformMake(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
    }

    public static Size __CCSizeApplyAffineTransform(final Size size, final AffineTransform t) {
        Size s = new Size();
        s.width = (float)((double)t.a * size.width + (double)t.c * size.height);
        s.height = (float)((double)t.b * size.width + (double)t.d * size.height);
        return s;
    }

    public static Rect RectApplyAffineTransform(final Rect rect, final AffineTransform anAffineTransform) {
        float top    = rect.getMinY();
        float left   = rect.getMinX();
        float right  = rect.getMaxX();
        float bottom = rect.getMaxY();

        Vec2 topLeft = __CCPointApplyAffineTransform(new Vec2(left, top), anAffineTransform);
        Vec2 topRight = __CCPointApplyAffineTransform(new Vec2(right, top), anAffineTransform);
        Vec2 bottomLeft = __CCPointApplyAffineTransform(new Vec2(left, bottom), anAffineTransform);
        Vec2 bottomRight = __CCPointApplyAffineTransform(new Vec2(right, bottom), anAffineTransform);

        float minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        float maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        float minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        float maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));

        return new Rect(minX, minY, (maxX - minX), (maxY - minY));
    }

    public static Rect RectApplyTransform(final Rect rect, final Mat4 transform) {
        float top    = rect.getMinY();
        float left   = rect.getMinX();
        float right  = rect.getMaxX();
        float bottom = rect.getMaxY();

        Vec3 topLeft = new Vec3(left, top, 0);
        Vec3 topRight = new Vec3(right, top, 0);
        Vec3 bottomLeft = new Vec3(left, bottom, 0);
        Vec3 bottomRight = new Vec3(right, bottom, 0);
        transform.transformPoint(topLeft);
        transform.transformPoint(topRight);
        transform.transformPoint(bottomLeft);
        transform.transformPoint(bottomRight);

        float minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        float maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        float minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        float maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));

        return new Rect(minX, minY, (maxX - minX), (maxY - minY));
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

    public static Vec2 PointApplyTransform(final Vec2 point, final Mat4 transform) {
        Vec3 vec = new Vec3(point.x, point.y, 0);
        transform.transformPoint(vec);
        return new Vec2(vec.x, vec.y);
    }

    public static AffineTransform AffineTransformTranslate(final AffineTransform t, float tx, float ty) {
        return __CCAffineTransformMake(t.a, t.b, t.c, t.d, t.tx + t.a * tx + t.c * ty, t.ty + t.b * tx + t.d * ty);
    }

    public static AffineTransform AffineTransformRotate(final AffineTransform t, float anAngle) {
        float sine = (float) Math.sin(anAngle);
        float cosine = (float) Math.cos(anAngle);

        return __CCAffineTransformMake(    t.a * cosine + t.c * sine,
                t.b * cosine + t.d * sine,
                t.c * cosine - t.a * sine,
                t.d * cosine - t.b * sine,
                t.tx,
                t.ty);
    }

    public static AffineTransform AffineTransformScale(final AffineTransform t, float sx, float sy) {
        return __CCAffineTransformMake(t.a * sx, t.b * sx, t.c * sy, t.d * sy, t.tx, t.ty);
    }

    public static AffineTransform AffineTransformConcat(final AffineTransform t1, final AffineTransform t2) {
        return __CCAffineTransformMake(    t1.a * t2.a + t1.b * t2.c, t1.a * t2.b + t1.b * t2.d, //a,b
                t1.c * t2.a + t1.d * t2.c, t1.c * t2.b + t1.d * t2.d, //c,d
                t1.tx * t2.a + t1.ty * t2.c + t2.tx,                  //tx
                t1.tx * t2.b + t1.ty * t2.d + t2.ty);
    }

    public static boolean AffineTransformEqualToTransform(final AffineTransform t1, final AffineTransform t2) {
        return (t1.a == t2.a && t1.b == t2.b && t1.c == t2.c && t1.d == t2.d && t1.tx == t2.tx && t1.ty == t2.ty);
    }

    public static AffineTransform AffineTransformInvert(final AffineTransform t) {
        float determinant = 1 / (t.a * t.d - t.b * t.c);

        return __CCAffineTransformMake(determinant * t.d, -determinant * t.b, -determinant * t.c, determinant * t.a,
                determinant * (t.c * t.ty - t.d * t.tx), determinant * (t.b * t.tx - t.a * t.ty) );
    }

    public static Mat4 TransformConcat(final Mat4 t1, final Mat4 t2) {
        return t1.multiplyRet(t2);
    }


}
