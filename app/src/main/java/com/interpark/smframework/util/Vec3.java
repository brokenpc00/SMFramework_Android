package com.interpark.smframework.util;

public final class Vec3 implements Cloneable  {
    public static final float MATH_FLOAT_SMALL = 1.0e-37f;
    public static final float MATH_TOLERANCE = 2e-37f;
    public static final float MATH_PIOVER2 = 1.57079632679489661923f;
    public static final float MATH_EPSILON = 0.000001f;

    public float x;
    public float y;
    public float z;

    public Vec3(Vec3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vec3(Vec3 p1, Vec3 p2) {
        set(p1, p2);
    }

    public Vec3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(float[] val) {
        this.x = val[0];
        this.y = val[1];
        this.z = val[2];
    }

    public float x() {return this.x;}
    public float y() {return this.y;}
    public float z() {return this.z;}

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void set(Vec3 r) {
        this.x = r.x;
        this.y = r.y;
        this.z = r.z;
    }

    public void set(Vec3 p1, Vec3 p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
        this.z = p2.z - p1.z;
    }

    public void set(float[] val) {
        this.x = val[0];
        this.y = val[1];
        this.z = val[2];
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float lengthSquared() {
        return (x * x + y * y + z * z);
    }

    public float magnitude() {
        return (float)Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public Vec3 getUintVector() {
        float m = magnitude();
        return new Vec3(this.x/m, this.y/m , this.z/m);
    }

    public float dot(Vec3 r) {
        return (this.x*r.x + this.y*r.y + this.z*r.z);
    }

    public static float dot(Vec3 v1, Vec3 v2) {
        return (v1.x*v2.x + v1.y*v2.y + v1.z*v2.z);
    }

    public void normalize() {
        float n = x * x + y * y + z * z;
        // Already normalized.
        if (n == 1.0f)
            return;

        n = (float) Math.sqrt(n);
        // Too close to zero.
        if (n < MATH_TOLERANCE)
            return;

        n = 1.0f / n;
        x *= n;
        y *= n;
        z *= n;
    }

    public Vec3 getNormalize() {
        Vec3 v = new Vec3(this);
        v.normalize();
        return v;
    }

    public Vec3 add(Vec3 r) {
        Vec3 ret = new Vec3();
        ret.x = this.x + r.x();
        ret.y = this.y + r.y();
        ret.z = this.z = r.z();
        return ret;
    }

    public void addLocal(Vec3 r) {
        this.x += r.x();
        this.y += r.y();
        this.z += r.z();
    }

    public Vec3 scale(float v) {
        Vec3 ret = new Vec3();
        ret.setX(this.x * v);
        ret.setY(this.y * v);
        ret.setZ(this.z * v);
        return ret;
    }

    public Vec3 lerp(final Vec3 target, float alpha) {
        return this.multiply(1.0f-alpha).add(target.multiply(alpha));
    }

    public void scaleLocal(float v) {
        this.x *= v;
        this.y *= v;
        this.z *= z;
    }

    public Vec3 minus(Vec3 r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x-r.x());
        ret.setY(this.y-r.y());
        ret.setZ(this.z-r.z());
        return ret;
    }

    public void minusLocal(Vec3 r) {
        this.x -= r.x();
        this.y -= r.y();
        this.z -= r.z();
    }

    public static void minus(final Vec3 v1, final Vec3 v2, Vec3 dst) {
        assert (dst!=null);

        dst.x = v1.x-v2.x;
        dst.y = v1.y-v2.y;
        dst.z = v1.z-v2.z;
    }

    public Vec3 multiply(float r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x*r);
        ret.setY(this.y*r);
        ret.setZ(this.z*r);
        return ret;
    }

    public void multiplyLocal(float r) {
        this.x *= r;
        this.y *= r;
        this.z *= r;
    }

    public Vec3 divide(float r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x/r);
        ret.setY(this.y/r);
        ret.setZ(this.z/r);
        return ret;
    }

    public void divideLocal(float r) {
        this.x /= r;
        this.y /= r;
        this.z /= r;
    }

    public boolean equals(Vec3 v) {
        if (this.x==v.x && this.y==v.y && this.z==v.z) {
            return true;
        }
        return false;
    }

    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    public float[] toFloats() {
        return new float[] {this.x, this.y, this.z};
    }
}
