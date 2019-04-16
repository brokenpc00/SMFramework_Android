package com.interpark.smframework.base.types;

import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class Quaternion {
    public Quaternion() {

    }

    public float x;
    public float y;
    public float z;
    public float w;

    public Quaternion(float xx, float yy, float zz, float ww) {
        x = xx;
        y = yy;
        z = zz;
        w = ww;
    }

    public Quaternion(float[] array) {
        set(array);
    }

    public Quaternion(final Mat4 m) {
        set(m);
    }

    public Quaternion(final Vec3 axis, float angle) {
        set(axis, angle);
    }

    public Quaternion(final Quaternion copy) {
        set(copy);
    }

    private static Quaternion identityValue = null;
    public static final Quaternion identity() {

        if (identityValue==null) {
            identityValue = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
        }

        return identityValue;
    }

    private static Quaternion zeorValue = null;
    public static final Quaternion zero() {
        if (zeorValue==null) {
            zeorValue = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
        }

        return zeorValue;
    }

    public boolean isIdentity() {
        return x == 0.0f && y == 0.0f && z == 0.0f && w == 1.0f;
    }

    public boolean isZero() {
        return x == 0.0f && y == 0.0f && z == 0.0f && w == 0.0f;
    }

    public static void createFromRotationMatrix(final Mat4 m, Quaternion dst) {
        m.getRotation(dst);
    }

    public static void createFromAxisAngle(final Vec3 axis, float angle, Quaternion dst) {
        float halfAngle = angle * 0.5f;
        float sinHalfAngle = (float) Math.sin(halfAngle);

        Vec3 normal = new Vec3(axis);
        normal.normalize();
        dst.x = normal.x * sinHalfAngle;
        dst.y = normal.y * sinHalfAngle;
        dst.z = normal.z * sinHalfAngle;
        dst.w = (float) Math.cos(halfAngle);
    }

    public void conjugate() {
        x = -x;
        y = -y;
        z = -z;
    }

    public Quaternion getConjugated() {
        Quaternion q = new Quaternion(this);
        q.conjugate();
        return q;
    }

    public boolean inverse() {
        float n = x * x + y * y + z * z + w * w;
        if (n == 1.0f)
        {
            x = -x;
            y = -y;
            z = -z;
            //w = w;

            return true;
        }

        // Too close to zero.
        if (n < 0.000001f)
            return false;

        n = 1.0f / n;
        x = -x * n;
        y = -y * n;
        z = -z * n;
        w = w * n;

        return true;
    }

    public Quaternion getInversed() {
        Quaternion q = new Quaternion(this);
        q.inverse();
        return q;
    }

    public static void multiply(final Quaternion q1, final Quaternion q2, Quaternion dst) {
        float x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;
        float y = q1.w * q2.y - q1.x * q2.z + q1.y * q2.w + q1.z * q2.x;
        float z = q1.w * q2.z + q1.x * q2.y - q1.y * q2.x + q1.z * q2.w;
        float w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;

        dst.x = x;
        dst.y = y;
        dst.z = z;
        dst.w = w;
    }

    public void normalize() {
        float n = x * x + y * y + z * z + w * w;

        // Already normalized.
        if (n == 1.0f)
            return;

        n = (float) Math.sqrt(n);
        // Too close to zero.
        if (n < 0.000001f)
            return;

        n = 1.0f / n;
        x *= n;
        y *= n;
        z *= n;
        w *= n;
    }

    public Quaternion getNormalized() {
        Quaternion q = new Quaternion(this);
        q.normalize();
        return q;
    }

    public void set(float xx, float yy, float zz, float ww) {
        this.x = xx;
        this.y = yy;
        this.z = zz;
        this.w = ww;
    }

    public void set(float[] array) {
        x = array[0];
        y = array[1];
        z = array[2];
        w = array[3];
    }

    public void set(final Mat4 m) {
        Quaternion.createFromRotationMatrix(m, this);
    }

    public void set(final Vec3 axis, float angle) {
        Quaternion.createFromAxisAngle(axis, angle, this);
    }

    public void set(final Quaternion q) {
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
        this.w = q.w;
    }

    public void setIdentity() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
        w = 1.0f;
    }

    public float toAxisAngle(Vec3 axis) {
        Quaternion q = new Quaternion(x, y, z, w);
        q.normalize();
        axis.x = q.x;
        axis.y = q.y;
        axis.z = q.z;
        axis.normalize();

        return (2.0f * (float) Math.acos(q.w));

    }

    public static void lerp(final Quaternion q1, final Quaternion q2, float t, Quaternion dst) {
        if (t == 0.0f) {
            dst.set(q1);
            return;
        } else if (t == 1.0f) {
            dst.set(q2);
            return;
        }

        float t1 = 1.0f - t;

        dst.x = t1 * q1.x + t * q2.x;
        dst.y = t1 * q1.y + t * q2.y;
        dst.z = t1 * q1.z + t * q2.z;
        dst.w = t1 * q1.w + t * q2.w;
    }

    public static void slerp(final Quaternion q1, final Quaternion q2, float t, Quaternion dst) {


        float[] x = new float[1];
        float[] y = new float[1];
        float[] z = new float[1];
        float[] w = new float[1];

        x[0] = dst.x;
        y[0] = dst.y;
        z[0] = dst.z;
        w[0] = dst.w;

        slerp(q1.x, q1.y, q1.z, q1.w, q2.x, q2.y, q2.z, q2.w, t, x, y, z, w);

        dst.x = x[0];
        dst.y = y[0];
        dst.z = z[0];
        dst.w = w[0];
    }

    public static void squad(final Quaternion q1, final Quaternion q2, final Quaternion s1, final Quaternion s2, float t, Quaternion dst) {
        Quaternion dstQ = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
        Quaternion dstS = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

        slerpForSquad(q1, q2, t, dstQ);
        slerpForSquad(s1, s2, t, dstS);
        slerpForSquad(dstQ, dstS, 2.0f * t * (1.0f - t), dst);
    }

    public Quaternion multiply(final Quaternion q) {
        Quaternion result = new Quaternion(this);
        multiply(this, q, this);
        return result;
    }

    public Vec3 multiply(final Vec3 v) {
        Vec3 uv = new Vec3();
        Vec3 uuv = new Vec3();

        Vec3 qvec = new Vec3(this.x, this.y, this.z);
        Vec3.cross(qvec, v, uv);
        Vec3.cross(qvec, uv, uuv);

        uv.multiplyLocal(2.0f*w);
        uuv.multiplyLocal(2.0f);

        return v.add(uv).add(uuv);
    }

    public Quaternion multiplyLocal(final Quaternion q) {
        multiply(q);
        return this;
    }

    public static final Quaternion ZERO = new Quaternion();

    private static void slerp(float q1x, float q1y, float q1z, float q1w, float q2x, float q2y, float q2z, float q2w, float t, float[] dstx, float[] dsty, float[] dstz, float[] dstw) {
        if (t == 0.0f) {
            dstx[0] = q1x;
            dsty[0] = q1y;
            dstz[0] = q1z;
            dstw[0] = q1w;
            return;
        } else if (t == 1.0f) {
            dstx[0] = q2x;
            dsty[0] = q2y;
            dstz[0] = q2z;
            dstw[0] = q2w;
            return;
        }

        if (q1x == q2x && q1y == q2y && q1z == q2z && q1w == q2w) {
            dstx[0] = q1x;
            dsty[0] = q1y;
            dstz[0] = q1z;
            dstw[0] = q1w;
            return;
        }

        float halfY, alpha, beta;
        float u, f1, f2a, f2b;
        float ratio1, ratio2;
        float halfSecHalfTheta, versHalfTheta;
        float sqNotU, sqU;

        float cosTheta = q1w * q2w + q1x * q2x + q1y * q2y + q1z * q2z;

        // As usual in all slerp implementations, we fold theta.
        alpha = cosTheta >= 0 ? 1.0f : -1.0f;
        halfY = 1.0f + alpha * cosTheta;

        // Here we bisect the interval, so we need to fold t as well.
        f2b = t - 0.5f;
        u = f2b >= 0 ? f2b : -f2b;
        f2a = u - f2b;
        f2b += u;
        u += u;
        f1 = 1.0f - u;

        // One iteration of Newton to get 1-cos(theta / 2) to good accuracy.
        halfSecHalfTheta = 1.09f - (0.476537f - 0.0903321f * halfY) * halfY;
        halfSecHalfTheta *= 1.5f - halfY * halfSecHalfTheta * halfSecHalfTheta;
        versHalfTheta = 1.0f - halfY * halfSecHalfTheta;

        // Evaluate series expansions of the coefficients.
        sqNotU = f1 * f1;
        ratio2 = 0.0000440917108f * versHalfTheta;
        ratio1 = -0.00158730159f + (sqNotU - 16.0f) * ratio2;
        ratio1 = 0.0333333333f + ratio1 * (sqNotU - 9.0f) * versHalfTheta;
        ratio1 = -0.333333333f + ratio1 * (sqNotU - 4.0f) * versHalfTheta;
        ratio1 = 1.0f + ratio1 * (sqNotU - 1.0f) * versHalfTheta;

        sqU = u * u;
        ratio2 = -0.00158730159f + (sqU - 16.0f) * ratio2;
        ratio2 = 0.0333333333f + ratio2 * (sqU - 9.0f) * versHalfTheta;
        ratio2 = -0.333333333f + ratio2 * (sqU - 4.0f) * versHalfTheta;
        ratio2 = 1.0f + ratio2 * (sqU - 1.0f) * versHalfTheta;

        // Perform the bisection and resolve the folding done earlier.
        f1 *= ratio1 * halfSecHalfTheta;
        f2a *= ratio2;
        f2b *= ratio2;
        alpha *= f1 + f2a;
        beta = f1 + f2b;

        // Apply final coefficients to a and b as usual.
        float w = alpha * q1w + beta * q2w;
        float x = alpha * q1x + beta * q2x;
        float y = alpha * q1y + beta * q2y;
        float z = alpha * q1z + beta * q2z;

        // This final adjustment to the quaternion's length corrects for
        // any small constraint error in the inputs q1 and q2 But as you
        // can see, it comes at the cost of 9 additional multiplication
        // operations. If this error-correcting feature is not required,
        // the following code may be removed.
        f1 = 1.5f - 0.5f * (w * w + x * x + y * y + z * z);
        dstw[0] = w * f1;
        dstx[0] = x * f1;
        dsty[0] = y * f1;
        dstz[0] = z * f1;
    }

    private static void slerpForSquad(final Quaternion q1, final Quaternion q2, float t, Quaternion dst) {
        float c = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;

        if (Math.abs(c) >= 1.0f)
        {
            dst.x = q1.x;
            dst.y = q1.y;
            dst.z = q1.z;
            dst.w = q1.w;
            return;
        }

        float omega = (float) Math.acos(c);
        float s = (float) Math.sqrt(1.0f - c * c);
        if ((float) Math.abs(s) <= 0.00001f)
        {
            dst.x = q1.x;
            dst.y = q1.y;
            dst.z = q1.z;
            dst.w = q1.w;
            return;
        }

        float r1 = (float) Math.sin((1 - t) * omega) / s;
        float r2 = (float) Math.sin(t * omega) / s;
        dst.x = (q1.x * r1 + q2.x * r2);
        dst.y = (q1.y * r1 + q2.y * r2);
        dst.z = (q1.z * r1 + q2.z * r2);
        dst.w = (q1.w * r1 + q2.w * r2);
    }
}
