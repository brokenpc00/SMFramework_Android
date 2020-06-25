package com.interpark.smframework.util;

public class Vec4 implements Cloneable {
    public static final float MATH_FLOAT_SMALL = 1.0e-37f;
    public static final float MATH_TOLERANCE = 2e-37f;
    public static final float MATH_PIOVER2 = 1.57079632679489661923f;
    public static final float MATH_EPSILON = 0.000001f;

    public float x = 0;
    public float y = 0;
    public float z = 0;
    public float w = 0;

//    public Vec4 v = new Vec4(0, 0, 0, 0);

    public Vec4() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    public Vec4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4(float[] arr) {
        set(arr);
    }

    public Vec4(final Vec4 p1, final Vec4 p2) {
        set(p1, p2);
    }

    public Vec4(final Vec4 copy) {
        set(copy);
    }

    public static Vec4 fromColor(int color) {
        float[] components = new float[4];
        int componentIndex = 0;
        for (int i=3; i>=0; --i) {
            int component = (color>>i*8) & 0x000000ff;

            components[componentIndex++] = (float)component/255.0f;
        }

        Vec4 value = new Vec4(components);
        return value;
    }

    public boolean isZero() {
        return x == 0.0f && y == 0.0f && z == 0.0f && w == 0.0f;
    }

    public boolean isOne() {
        return x == 1.0f && y == 1.0f && z == 1.0f && w == 1.0f;
    }

    public static float angle(final Vec4 v1, final Vec4 v2) {
        float dx = v1.w * v2.x - v1.x * v2.w - v1.y * v2.z + v1.z * v2.y;
        float dy = v1.w * v2.y - v1.y * v2.w - v1.z * v2.x + v1.x * v2.z;
        float dz = v1.w * v2.z - v1.z * v2.w - v1.x * v2.y + v1.y * v2.x;

        return (float) Math.atan2(Math.sqrt(dx * dx + dy * dy + dz * dz) + MATH_FLOAT_SMALL, dot(v1, v2));

    }

//    public void add(final Vec4 v) {
//
//    }

    public static void add(final Vec4 v1, final Vec4 v2, Vec4 dst) {
        assert (dst!=null);

        dst.x = v1.x + v2.x;
        dst.y = v1.y + v2.y;
        dst.z = v1.z + v2.z;
        dst.w = v1.w + v2.w;
    }

    public void clamp(final  Vec4 min, final Vec4 max) {
        assert (!(min.x > max.x || min.y > max.y || min.z > max.z || min.w > max.w));

        // Clamp the x value.
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

        // Clamp the z value.
        if (w < min.w)
            w = min.w;
        if (w > max.w)
            w = max.w;
    }

    public static void clamp(final Vec4 v, final Vec4 min, final Vec4 max, Vec4 dst) {
        assert (dst!=null);
        assert (!(min.x > max.x || min.y > max.y || min.z > max.z || min.w > max.w));

        // Clamp the x value.
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

        // Clamp the w value.
        dst.w = v.w;
        if (dst.w < min.w)
            dst.w = min.w;
        if (dst.w > max.w)
            dst.w = max.w;

    }

    public float distance(final Vec4 v) {
        float dx = v.x - x;
        float dy = v.y - y;
        float dz = v.z - z;
        float dw = v.w - w;

        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz + dw * dw);

    }

    public float distanceSquared(final Vec4 v) {
        float dx = v.x - x;
        float dy = v.y - y;
        float dz = v.z - z;
        float dw = v.w - w;

        return (dx * dx + dy * dy + dz * dz + dw * dw);
    }

    public float dot(final Vec4 v) {
        return (x * v.x + y * v.y + z * v.z + w * v.w);
    }

    public static float dot(final Vec4 v1, final Vec4 v2) {
        return (v1.x * v2.x + v1.y * v2.y + v1.z * v2.z + v1.w * v2.w);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public float lengthSquared() {
        return (x * x + y * y + z * z + w * w);
    }

    public void negate() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
    }

    public void normalize() {
        float n = x * x + y * y + z * z + w * w;
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
        w *= n;
    }

    public Vec4 getNormalized() {
        Vec4 v = new Vec4(this);
        v.normalize();
        return v;
    }

    public void scale(float s) {
        x *= s;
        y *= s;
        z *= s;
        w *= s;
    }

    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void set(float[] arr) {
        assert (arr!=null && arr.length>=4);

        x = arr[0];
        y = arr[1];
        z = arr[2];
        w = arr[3];

    }
    public void set(final Vec4 p1, final Vec4 p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
        this.z = p2.z - p1.z;
        this.w = p2.w - p1.w;
    }
    public void set(final Vec4 copy) {
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
        this.w = copy.w;
    }

//    public void minus(final Vec4 v) {
//
//    }

    public static void minus(final Vec4 v1, final Vec4 v2, Vec4 dst) {
        assert (dst!=null);
        dst.x = v1.x - v2.x;
        dst.y = v1.y - v2.y;
        dst.z = v1.z - v2.z;
        dst.w = v1.w - v2.w;
    }

    public Vec4 add(final Vec4 v) {
        Vec4 ret = new Vec4(this);
        ret.x += v.x;
        ret.y += v.y;
        ret.z += v.z;
        ret.w += v.w;
        return ret;
    }

    public Vec4 minus(final Vec4 v) {
        Vec4 ret = new Vec4(this);
        ret.x -= v.x;
        ret.y -= v.y;
        ret.z -= v.z;
        ret.w -= v.w;
        return ret;
    }

    public Vec4 addLocal(final Vec4 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.w += v.w;
        return this;
    }

    public Vec4 minusLocal(final Vec4 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        this.w -= v.w;
        return this;
    }

    public Vec4 multiply(final float a) {
        Vec4 ret = new Vec4(this);
        ret.x *= a;
        ret.y *= a;
        ret.z *= a;
        ret.w *= a;
        return ret;
    }

    public Vec4 divide(final float a) {
        Vec4 ret = new Vec4(this);
        ret.x /= a;
        ret.y /= a;
        ret.z /= a;
        ret.w /= a;

        return ret;
    }

    public Vec4 multiplyLocal(final float a) {
        this.x *= a;
        this.y *= a;
        this.z *= a;
        this.w *= a;
        return this;
    }

    public Vec4 divideLocal(final float a) {
        this.x /= a;
        this.y /= a;
        this.z /= a;
        this.w /= a;
        return this;
    }

    public boolean greaterthan(final Vec4 v) {
        if (this.x==v.x) {
            if (this.y==v.y) {
                if (this.z==v.z) {
                    if (this.w>v.w) {
                        return this.w>v.w;
                    }
                }
                return this.z>v.z;
            }
            return this.y>v.y;
        }
        return this.x>v.x;
    }

    public boolean lessthan(final Vec4 v) {
        if (this.x==v.x) {
            if (this.y==v.y) {
                if (this.z==v.z) {
                    if (this.w<v.w) {
                        return this.w<v.w;
                    }
                }
                return this.y<v.z;
            }
            return this.y<v.y;
        }
        return this.x<v.x;
    }

    public boolean equals(final Vec4 v) {
        return (this.x==v.x && this.y==v.y && this.z==v.z && this.w==v.w);
    }

    public boolean noteuals(final Vec4 v) {
        return (this.x!=v.x || this.y!=v.y || this.z!=v.z || this.w==v.w);
    }

    public static final Vec4 ZERO = new Vec4(0, 0, 0, 0);
    public static final Vec4 ONE = new Vec4(1, 1, 1, 1);
    public static final Vec4 UNIT_X = new Vec4(1.0f, 0.0f, 0.0f, 0.0f);
    public static final Vec4 UNIT_Y = new Vec4(0.0f, 1.0f, 0.0f, 0.0f);
    public static final Vec4 UNIT_Z = new Vec4(0.0f, 0.0f, 1.0f, 0.0f);
    public static final Vec4 UNIT_W = new Vec4(0.0f, 0.0f, 0.0f, 1.0f);

}
