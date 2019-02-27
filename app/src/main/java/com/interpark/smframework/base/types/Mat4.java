package com.interpark.smframework.base.types;

import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class Mat4 {

    public static final float MATH_DEG_TO_RAD(float x) {
        return (x * 0.0174532925f);
    }
    public static final float MATH_RAD_TO_DEG(float x) {
        return (x * 57.29577951f);
    }
    public static final float MATH_FLOAT_SMALL = 1.0e-37f;
    public static final float MATH_TOLERANCE = 2e-37f;
    public static final float MATH_PIOVER2 = 1.57079632679489661923f;
    public static final float MATH_EPSILON = 0.000001f;

    public Mat4() {
        set(IDENTITY);
    }

    public Mat4(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24,
                float m31, float m32, float m33, float m34, float m41, float m42, float m43, float m44) {
        set(m11, m12, m13, m14, m21, m22, m23, m24, m31, m32, m33, m34, m41, m42, m43, m44);
    }

    public Mat4(float[] mat) {
        set(mat);
    }

    public Mat4(Mat4 copy) {
        set(copy);
    }

    public static void createLookAt(final Vec3 eyePosition, final Vec3 targetPosition, final Vec3 up, Mat4 dst) {
        createLookAt(eyePosition.x, eyePosition.y, eyePosition.z, targetPosition.x, targetPosition.y, targetPosition.z, up.x, up.y, up.z, dst);
    }

    public static void createLookAt(float eyePositionX, float eyePositionY, float eyePositionZ,
                                    float targetCenterX, float targetCenterY, float targetCenterZ,
                                    float upX, float upY, float upZ, Mat4 dst) {
        Vec3 eye = new Vec3(eyePositionX, eyePositionY, eyePositionZ);
        Vec3 target = new Vec3(targetCenterX, targetCenterY, targetCenterZ);
        Vec3 up = new Vec3(upX, upY, upZ);
        up.normalize();

        Vec3 zaxis = new Vec3(Vec3.ZERO);
        Vec3.minus(eye, target, zaxis);
        zaxis.normalize();

        Vec3 xaxis = new Vec3(Vec3.ZERO);
        Vec3.minus(eye, target, xaxis);
        xaxis.normalize();

        Vec3 yaxis = new Vec3(Vec3.ZERO);
        Vec3.minus(eye, target, yaxis);
        yaxis.normalize();

        dst.m[0] = xaxis.x;
        dst.m[1] = yaxis.x;
        dst.m[2] = zaxis.x;
        dst.m[3] = 0;

        dst.m[4] = xaxis.y;
        dst.m[5] = yaxis.y;
        dst.m[6] = zaxis.y;
        dst.m[7] = 0;

        dst.m[8] = xaxis.z;
        dst.m[9] = yaxis.z;
        dst.m[10] = zaxis.z;
        dst.m[11] = 0;

        dst.m[12] = -Vec3.dot(xaxis, eye);
        dst.m[13] = -Vec3.dot(yaxis, eye);
        dst.m[14] = -Vec3.dot(zaxis, eye);
        dst.m[15] = 1;

    }

    public static void createPerspective(float fieldOfView, float aspectRatio, float zNearPlane, float zFarPlane, Mat4 dst) {
        assert (dst!=null);
        assert (zFarPlane!=zNearPlane);

        float f_n = 1.0f / (zFarPlane - zNearPlane);
        float theta = MATH_DEG_TO_RAD(fieldOfView) * 0.5f;
        if (Math.abs(theta % MATH_PIOVER2) < MATH_EPSILON) {
            return;
        }

        float divisor = (float) Math.tan(theta);
        assert (divisor!=0);
        float factor = 1.0f / divisor;
        dst.set(ZERO);

        assert (aspectRatio!=0);

        dst.m[0] = (1.0f / aspectRatio) * factor;
        dst.m[5] = factor;
        dst.m[10] = (-(zFarPlane + zNearPlane)) * f_n;
        dst.m[11] = -1.0f;
        dst.m[14] = -2.0f * zFarPlane * zNearPlane * f_n;
    }

    public static void createOrthographic(float width, float height, float zNearPlane, float zFarPlane, Mat4 dst) {
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        createOrthographicOffCenter(-halfWidth, halfWidth, -halfHeight, halfHeight, zNearPlane, zFarPlane, dst);
    }

    public static void createOrthographicOffCenter(float left, float right, float bottom, float top, float zNearPlane, float zFarPlane, Mat4 dst) {
        assert(dst!=null);
        assert(right != left);
        assert(top != bottom);
        assert(zFarPlane != zNearPlane);

        dst.set(ZERO);

        dst.m[0] = 2 / (right - left);
        dst.m[5] = 2 / (top - bottom);
        dst.m[10] = 2 / (zNearPlane - zFarPlane);

        dst.m[12] = (left + right) / (left - right);
        dst.m[13] = (top + bottom) / (bottom - top);
        dst.m[14] = (zNearPlane + zFarPlane) / (zNearPlane - zFarPlane);
        dst.m[15] = 1;
    }

    public static void createBillboard(final Vec3 objectPosition, Vec3 cameraPosition, Vec3 cameraUpVector, Mat4 dst) {
        createBillboardHelper(objectPosition, cameraPosition, cameraUpVector, null, dst);
    }

    public static void createBillboard(final Vec3 objectPosition, final Vec3 cameraPosition, final Vec3 cameraUpVector, final Vec3 cameraForwardVector, Mat4 dst) {
        createBillboardHelper(objectPosition, cameraPosition, cameraUpVector, cameraForwardVector, dst);
    }

    public static void createBillboardHelper(final Vec3 objectPosition, final Vec3 cameraPosition,
                                             final Vec3 cameraUpVector, final Vec3 cameraForwardVector,
                                            Mat4 dst)
    {
        Vec3 delta = new Vec3(objectPosition, cameraPosition);
        boolean isSufficientDelta = delta.lengthSquared() > MATH_EPSILON;

        dst.setIdentity();
        dst.m[3] = objectPosition.x;
        dst.m[7] = objectPosition.y;
        dst.m[11] = objectPosition.z;

        // As per the contracts for the 2 variants of createBillboard, we need
        // either a safe default or a sufficient distance between object and camera.
        if (cameraForwardVector!=null || isSufficientDelta)
        {
            Vec3 target = new Vec3(isSufficientDelta ? cameraPosition : (objectPosition.minus(cameraForwardVector)));

            // A billboard is the inverse of a lookAt rotation
            Mat4 lookAt = new Mat4(ZERO);
            createLookAt(objectPosition, target, cameraUpVector, lookAt);
            dst.m[0] = lookAt.m[0];
            dst.m[1] = lookAt.m[4];
            dst.m[2] = lookAt.m[8];
            dst.m[4] = lookAt.m[1];
            dst.m[5] = lookAt.m[5];
            dst.m[6] = lookAt.m[9];
            dst.m[8] = lookAt.m[2];
            dst.m[9] = lookAt.m[6];
            dst.m[10] = lookAt.m[10];
        }
    }

    public static void createScale(final Vec3 scale, Mat4 dst) {
        assert (dst!=null);

        dst.set(IDENTITY);

        dst.m[0] = scale.x;
        dst.m[5] = scale.y;
        dst.m[10] = scale.z;
    }

    public static void createScale(float xScale, float yScale, float zScale, Mat4 dst) {
        assert (dst!=null);

        dst.set(IDENTITY);

        dst.m[0] = xScale;
        dst.m[5] = yScale;
        dst.m[10] = zScale;
    }

    // Do Not use.
    public static void createRotation(final Quaternion q, Mat4 dst) {
        assert (dst!=null);

        float x2 = q.x + q.x;
        float y2 = q.y + q.y;
        float z2 = q.z + q.z;

        float xx2 = q.x * x2;
        float yy2 = q.y * y2;
        float zz2 = q.z * z2;
        float xy2 = q.x * y2;
        float xz2 = q.x * z2;
        float yz2 = q.y * z2;
        float wx2 = q.w * x2;
        float wy2 = q.w * y2;
        float wz2 = q.w * z2;

        dst.m[0] = 1.0f - yy2 - zz2;
        dst.m[1] = xy2 + wz2;
        dst.m[2] = xz2 - wy2;
        dst.m[3] = 0.0f;

        dst.m[4] = xy2 - wz2;
        dst.m[5] = 1.0f - xx2 - zz2;
        dst.m[6] = yz2 + wx2;
        dst.m[7] = 0.0f;

        dst.m[8] = xz2 + wy2;
        dst.m[9] = yz2 - wx2;
        dst.m[10] = 1.0f - xx2 - yy2;
        dst.m[11] = 0.0f;

        dst.m[12] = 0.0f;
        dst.m[13] = 0.0f;
        dst.m[14] = 0.0f;
        dst.m[15] = 1.0f;
    }

    public static void createRotation(final Vec3 axis, float angle, Mat4 dst) {
        assert (dst!=null);

        float x = axis.x;
        float y = axis.y;
        float z = axis.z;

        // Make sure the input axis is normalized.
        float n = x*x + y*y + z*z;
        if (n != 1.0f)
        {
            // Not normalized.
            n = (float)Math.sqrt(n);
            // Prevent divide too close to zero.
            if (n > 0.000001f)
            {
                n = 1.0f / n;
                x *= n;
                y *= n;
                z *= n;
            }
        }

        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        float t = 1.0f - c;
        float tx = t * x;
        float ty = t * y;
        float tz = t * z;
        float txy = tx * y;
        float txz = tx * z;
        float tyz = ty * z;
        float sx = s * x;
        float sy = s * y;
        float sz = s * z;

        dst.m[0] = c + tx*x;
        dst.m[1] = txy + sz;
        dst.m[2] = txz - sy;
        dst.m[3] = 0.0f;

        dst.m[4] = txy - sz;
        dst.m[5] = c + ty*y;
        dst.m[6] = tyz + sx;
        dst.m[7] = 0.0f;

        dst.m[8] = txz + sy;
        dst.m[9] = tyz - sx;
        dst.m[10] = c + tz*z;
        dst.m[11] = 0.0f;

        dst.m[12] = 0.0f;
        dst.m[13] = 0.0f;
        dst.m[14] = 0.0f;
        dst.m[15] = 1.0f;
    }

    public static void createRotationX(float angle, Mat4 dst) {
        assert (dst!=null);
        dst.set(IDENTITY);

        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        dst.m[5]  = c;
        dst.m[6]  = s;
        dst.m[9]  = -s;
        dst.m[10] = c;

    }

    public static void createRotationY(float angle, Mat4 dst) {
        assert (dst!=null);
        dst.set(IDENTITY);

        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        dst.m[0]  = c;
        dst.m[2]  = -s;
        dst.m[8]  = s;
        dst.m[10] = c;

    }

    public static void createRotationZ(float angle, Mat4 dst) {
        assert (dst!=null);
        dst.set(IDENTITY);

        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        dst.m[0] = c;
        dst.m[1] = s;
        dst.m[4] = -s;
        dst.m[5] = c;
    }

    public static void createTranslation(final Vec3 translation, Mat4 dst) {
        assert (dst!=null);

        dst.set(IDENTITY);

        dst.m[12] = translation.x;
        dst.m[13] = translation.y;
        dst.m[14] = translation.z;

    }

    public static void createTranslation(float xTranslation, float yTranslation, float zTranslation, Mat4 dst) {
        assert (dst!=null);
        dst.set(IDENTITY);

        dst.m[12] = xTranslation;
        dst.m[13] = yTranslation;
        dst.m[14] = zTranslation;
    }

    public Mat4 add(float scalar) {
        Mat4 ret = new Mat4(ZERO);
        add(scalar, ret);
        return ret;
    }

    public Mat4 addLocal(float scalar) {
        add(scalar, this);
        return this;
    }

    public void add(float scalar, Mat4 dst) {
        dst.m[0]  = this.m[0]  + scalar;
        dst.m[1]  = this.m[1]  + scalar;
        dst.m[2]  = this.m[2]  + scalar;
        dst.m[3]  = this.m[3]  + scalar;
        dst.m[4]  = this.m[4]  + scalar;
        dst.m[5]  = this.m[5]  + scalar;
        dst.m[6]  = this.m[6]  + scalar;
        dst.m[7]  = this.m[7]  + scalar;
        dst.m[8]  = this.m[8]  + scalar;
        dst.m[9]  = this.m[9]  + scalar;
        dst.m[10] = this.m[10] + scalar;
        dst.m[11] = this.m[11] + scalar;
        dst.m[12] = this.m[12] + scalar;
        dst.m[13] = this.m[13] + scalar;
        dst.m[14] = this.m[14] + scalar;
        dst.m[15] = this.m[15] + scalar;
    }

    public Mat4 add(final Mat4 mat) {
        return ZERO;
    }

    public Mat4 addLocal(final Mat4 mat) {
        return ZERO;
    }

    public static Mat4 add(final Mat4 m1, final Mat4 m2, Mat4 dst) {
        return ZERO;
    }

    public float determinant() {
        return 0;
    }

    public float getScale(Vec3 scale) {
        return 0;
    }

    public void getTranslation(Vec3 translation) {

    }

    public void getUpVector(Vec3 dst) {

    }

    public void getLeftVector(Vec3 dst) {

    }

    public void getForwardVector(Vec3 dst) {

    }

    public void getBackVector(Vec3 dst) {

    }

    public boolean inverse() {
        return true;
    }

    public Mat4 getInversed() {
        return ZERO;
    }

    public boolean isIdentity() {
        return true;
    }

    public void multiply(float scalar) {

    }

    public void multiply(float scalar, Mat4 dst) {

    }

    public static void multiply(final Mat4 mat, float scalar, Mat4 dst) {

    }

    public void multiply(Mat4 mat) {

    }

    public static void multiply(final Mat4 m1, final Mat4 m2, Mat4 dst) {

    }

    public void negate() {

    }

    public Mat4 getNegate() {
        return ZERO;
    }

//    public void rotate(Quaternion q) {
//
//    }

    public void rotate(final Vec3 axis, float angle) {

    }

    public void rotate(final Vec3 axis, float angle, Mat4 dst) {

    }

    public void rotateX(float angle) {

    }

    public void rotateX(float angle, Mat4 dst) {

    }

    public void rotateY(float angle) {

    }

    public void rotateY(float angle, Mat4 dst) {

    }

    public void rotateZ(float angle) {

    }

    public void rotateZ(float angle, Mat4 dst) {

    }

    public void scale(float value) {

    }

    public void scale(float value, Mat4 dst) {

    }

    public void scale(float xSclae, float yScale, float zScale) {

    }

    public void scale(Vec3 s) {

    }

    public void scale(Vec3 s, Mat4 dst) {

    }

    public void normalize() {

    }

    public void set(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24,
                    float m31, float m32, float m33, float m34, float m41, float m42, float m43, float m44) {

    }

    public void set(float[] mat) {

    }

    public void set(final Mat4 mat) {

    }

    public void setIdentity() {

    }

    public void setZero() {

    }

    public void minus(final Mat4 mat) {

    }

    public static void minus(final Mat4 m1, final Mat4 m2, Mat4 dst) {

    }

    public static final Mat4 ZERO = new Mat4(0, 0, 0, 0,
                                             0, 0, 0, 0,
                                             0, 0, 0, 0,
                                             0, 0, 0, 0);
    public static final Mat4 IDENTITY = new Mat4(1, 0, 0, 0,
                                                 0, 1, 0, 0,
                                                 0, 0, 1, 0,
                                                 0, 0, 0, 1);

    public float[] m = new float[16];


}
