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
        set(v);
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
        set(val);
    }

    public Vec3 fromColor(int color) {
        float[] components = new float[3];
        int componentIndex = 0;
        for (int i=2; i>=0; --i) {
            int component = (color >> i*8) & 0x0000ff;

            components[componentIndex++] = (float)(component/255.0f);
        }

        return new Vec3(components);
    }

    public static float angle(final Vec3 v1, final Vec3 v2) {
        float dx = v1.y * v2.z - v1.z * v2.y;
        float dy = v1.z * v2.x - v1.x * v2.z;
        float dz = v1.x * v2.y - v1.y * v2.x;

        return (float) Math.atan2((float)Math.sqrt(dx * dx + dy * dy + dz * dz) + MATH_FLOAT_SMALL, dot(v1, v2));
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

    public static void add(final Vec3 v1, final Vec3 v2, Vec3 dst) {
        dst.x = v1.x + v2.x;
        dst.y = v1.y + v2.y;
        dst.z = v1.z + v2.z;
    }

    public void clamp(final Vec3 min, final Vec3 max) {
        if (x < min.x)
            x = min.x;
        if (x > max.x)
            x = max.x;

        // Clamp the y value.
        if (y < min.y)
            y = min.y;
        if (y > max.y)
            y = max.y;

        // Clamp the z value.
        if (z < min.z)
            z = min.z;
        if (z > max.z)
            z = max.z;
    }

    public static void clamp(final Vec3 v, final Vec3 min, final Vec3 max, Vec3 dst) {
        dst.x = v.x;
        if (dst.x < min.x)
            dst.x = min.x;
        if (dst.x > max.x)
            dst.x = max.x;

        // Clamp the y value.
        dst.y = v.y;
        if (dst.y < min.y)
            dst.y = min.y;
        if (dst.y > max.y)
            dst.y = max.y;

        // Clamp the z value.
        dst.z = v.z;
        if (dst.z < min.z)
            dst.z = min.z;
        if (dst.z > max.z)
            dst.z = max.z;
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

    public void cross(final Vec3 v) {
        cross(this, v, this);
    }

    public static void cross(final Vec3 v1, final Vec3 v2, Vec3 dst) {
        float[] v1x = new float[1];
        float[] v2x = new float[1];
        float[] dstx = new float[1];
        v1x[0] = v1.x;
        v2x[0] = v2.x;
        dstx[0] = dst.x;
        MathUtilC.crossVec3(v1x, v2x, dstx);
        v1.x = v1x[0];
        v2.x = v2x[0];
        dst.x = dstx[0];
    }

    public float distance(final Vec3 v) {
        float dx = v.x - x;
        float dy = v.y - y;
        float dz = v.z - z;

        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distanceSquared(final Vec3 v) {
        float dx = v.x - x;
        float dy = v.y - y;
        float dz = v.z - z;

        return (dx * dx + dy * dy + dz * dz);
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
        this.z *= v;
    }

    public Vec3 minus(Vec3 r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x-r.x());
        ret.setY(this.y-r.y());
        ret.setZ(this.z-r.z());
        return ret;
    }

    public Vec3 minusLocal(Vec3 r) {
        this.x -= r.x();
        this.y -= r.y();
        this.z -= r.z();

        return this;
    }

    public Vec3 subtractLocal(Vec3 r) {
        return minusLocal(r);
    }

    public void smooth(final Vec3 target, float elapsedTime, float responseTime) {
        if (elapsedTime > 0) {
            this.addLocal(target.minus(this).multiply(elapsedTime/(elapsedTime-responseTime)));
        }
    }

    public static void minus(final Vec3 v1, final Vec3 v2, Vec3 dst) {
        assert (dst!=null);

        dst.x = v1.x-v2.x;
        dst.y = v1.y-v2.y;
        dst.z = v1.z-v2.z;
    }

    public static void subtract(final Vec3 v1, final Vec3 v2, Vec3 dst) {
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
