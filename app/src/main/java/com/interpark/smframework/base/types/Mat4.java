package com.interpark.smframework.base.types;

import com.interpark.smframework.NativeImageProcess.ImageProcessing;
import com.interpark.smframework.util.MathUtilC;
import com.interpark.smframework.util.OpenGlUtils;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;
import com.interpark.smframework.util.Vec4;

import java.util.Arrays;

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
        Vec3.subtract(eye, target, xaxis);
        xaxis.normalize();

        Vec3 yaxis = new Vec3(Vec3.ZERO);
        Vec3.subtract(eye, target, yaxis);
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

    public boolean decompose(Vec3 scale, Quaternion rotation, Vec3 translation) {
        if (translation!=null)
        {
            // Extract the translation.
            translation.x = m[12];
            translation.y = m[13];
            translation.z = m[14];
        }

        // Nothing left to do.
        if (scale == null && rotation == null)
            return true;

        // Extract the scale.
        // This is simply the length of each axis (row/column) in the matrix.
        Vec3 xaxis = new Vec3(m[0], m[1], m[2]);
        float scaleX = xaxis.length();

        Vec3 yaxis = new Vec3(m[4], m[5], m[6]);
        float scaleY = yaxis.length();

        Vec3 zaxis = new Vec3(m[8], m[9], m[10]);
        float scaleZ = zaxis.length();

        // Determine if we have a negative scale (true if determinant is less than zero).
        // In this case, we simply negate a single axis of the scale.
        float det = determinant();
        if (det < 0)
            scaleZ = -scaleZ;

        if (scale!=null)
        {
            scale.x = scaleX;
            scale.y = scaleY;
            scale.z = scaleZ;
        }

        // Nothing left to do.
        if (rotation == null)
            return true;

        // Scale too close to zero, can't decompose rotation.
        if (scaleX < MATH_TOLERANCE || scaleY < MATH_TOLERANCE || Math.abs(scaleZ) < MATH_TOLERANCE)
        return false;

        float rn;

        // Factor the scale out of the matrix axes.
        rn = 1.0f / scaleX;
        xaxis.x *= rn;
        xaxis.y *= rn;
        xaxis.z *= rn;

        rn = 1.0f / scaleY;
        yaxis.x *= rn;
        yaxis.y *= rn;
        yaxis.z *= rn;

        rn = 1.0f / scaleZ;
        zaxis.x *= rn;
        zaxis.y *= rn;
        zaxis.z *= rn;

        // Now calculate the rotation from the resulting matrix (axes).
        float trace = xaxis.x + yaxis.y + zaxis.z + 1.0f;

        if (trace > MATH_EPSILON)
        {
            float s = 0.5f / (float) Math.sqrt(trace);
            rotation.w = 0.25f / s;
            rotation.x = (yaxis.z - zaxis.y) * s;
            rotation.y = (zaxis.x - xaxis.z) * s;
            rotation.z = (xaxis.y - yaxis.x) * s;
        }
        else
        {
            // Note: since xaxis, yaxis, and zaxis are normalized,
            // we will never divide by zero in the code below.
            if (xaxis.x > yaxis.y && xaxis.x > zaxis.z)
            {
                float s = 0.5f / (float) Math.sqrt(1.0f + xaxis.x - yaxis.y - zaxis.z);
                rotation.w = (yaxis.z - zaxis.y) * s;
                rotation.x = 0.25f / s;
                rotation.y = (yaxis.x + xaxis.y) * s;
                rotation.z = (zaxis.x + xaxis.z) * s;
            }
            else if (yaxis.y > zaxis.z)
            {
                float s = 0.5f / (float) Math.sqrt(1.0f + yaxis.y - xaxis.x - zaxis.z);
                rotation.w = (zaxis.x - xaxis.z) * s;
                rotation.x = (yaxis.x + xaxis.y) * s;
                rotation.y = 0.25f / s;
                rotation.z = (zaxis.y + yaxis.z) * s;
            }
            else
            {
                float s = 0.5f / (float) Math.sqrt(1.0f + zaxis.z - xaxis.x - yaxis.y);
                rotation.w = (xaxis.y - yaxis.x ) * s;
                rotation.x = (zaxis.x + xaxis.z ) * s;
                rotation.y = (zaxis.y + yaxis.z ) * s;
                rotation.z = 0.25f / s;
            }
        }

        return true;
    }

    public float determinant() {
        float a0 = m[0] * m[5] - m[1] * m[4];
        float a1 = m[0] * m[6] - m[2] * m[4];
        float a2 = m[0] * m[7] - m[3] * m[4];
        float a3 = m[1] * m[6] - m[2] * m[5];
        float a4 = m[1] * m[7] - m[3] * m[5];
        float a5 = m[2] * m[7] - m[3] * m[6];
        float b0 = m[8] * m[13] - m[9] * m[12];
        float b1 = m[8] * m[14] - m[10] * m[12];
        float b2 = m[8] * m[15] - m[11] * m[12];
        float b3 = m[9] * m[14] - m[10] * m[13];
        float b4 = m[9] * m[15] - m[11] * m[13];
        float b5 = m[10] * m[15] - m[11] * m[14];

        // Calculate the determinant.
        return (a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0);
    }

    public void getScale(Vec3 scale) {
        decompose(scale, null, null);
    }

    public boolean getRotation(Quaternion rotation) {
        return decompose(null, rotation, null);
    }

    public void getTranslation(Vec3 translation) {
        decompose(null, null, translation);
    }

    public void getUpVector(Vec3 dst) {
        dst.x = m[4];
        dst.y = m[5];
        dst.z = m[6];
    }

    public void getDownVector(Vec3 dst) {
        dst.x = -m[4];
        dst.y = -m[5];
        dst.z = -m[6];
    }

    public void getLeftVector(Vec3 dst) {
        dst.x = -m[0];
        dst.y = -m[1];
        dst.z = -m[2];
    }

    public void getRightVector(Vec3 dst) {
        dst.x = m[0];
        dst.y = m[1];
        dst.z = m[2];
    }

    public void getForwardVector(Vec3 dst) {
        dst.x = -m[8];
        dst.y = -m[9];
        dst.z = -m[10];
    }

    public void getBackVector(Vec3 dst) {
        dst.x = m[8];
        dst.y = m[9];
        dst.z = m[10];
    }

    public boolean inverse() {
        float a0 = m[0] * m[5] - m[1] * m[4];
        float a1 = m[0] * m[6] - m[2] * m[4];
        float a2 = m[0] * m[7] - m[3] * m[4];
        float a3 = m[1] * m[6] - m[2] * m[5];
        float a4 = m[1] * m[7] - m[3] * m[5];
        float a5 = m[2] * m[7] - m[3] * m[6];
        float b0 = m[8] * m[13] - m[9] * m[12];
        float b1 = m[8] * m[14] - m[10] * m[12];
        float b2 = m[8] * m[15] - m[11] * m[12];
        float b3 = m[9] * m[14] - m[10] * m[13];
        float b4 = m[9] * m[15] - m[11] * m[13];
        float b5 = m[10] * m[15] - m[11] * m[14];

        // Calculate the determinant.
        float det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;

        // Close to zero, can't invert.
        if (Math.abs(det) <= MATH_TOLERANCE)
        return false;

        // Support the case where m == dst.
        Mat4 inverse = new Mat4();
        inverse.m[0]  = m[5] * b5 - m[6] * b4 + m[7] * b3;
        inverse.m[1]  = -m[1] * b5 + m[2] * b4 - m[3] * b3;
        inverse.m[2]  = m[13] * a5 - m[14] * a4 + m[15] * a3;
        inverse.m[3]  = -m[9] * a5 + m[10] * a4 - m[11] * a3;

        inverse.m[4]  = -m[4] * b5 + m[6] * b2 - m[7] * b1;
        inverse.m[5]  = m[0] * b5 - m[2] * b2 + m[3] * b1;
        inverse.m[6]  = -m[12] * a5 + m[14] * a2 - m[15] * a1;
        inverse.m[7]  = m[8] * a5 - m[10] * a2 + m[11] * a1;

        inverse.m[8]  = m[4] * b4 - m[5] * b2 + m[7] * b0;
        inverse.m[9]  = -m[0] * b4 + m[1] * b2 - m[3] * b0;
        inverse.m[10] = m[12] * a4 - m[13] * a2 + m[15] * a0;
        inverse.m[11] = -m[8] * a4 + m[9] * a2 - m[11] * a0;

        inverse.m[12] = -m[4] * b3 + m[5] * b1 - m[6] * b0;
        inverse.m[13] = m[0] * b3 - m[1] * b1 + m[2] * b0;
        inverse.m[14] = -m[12] * a3 + m[13] * a1 - m[14] * a0;
        inverse.m[15] = m[8] * a3 - m[9] * a1 + m[10] * a0;

        multiply(inverse, 1.0f / det, this);


        return true;
    }

    public Mat4 getInversed() {
        Mat4 mat = new Mat4(this);
        mat.inverse();
        return mat;
    }

    public boolean isIdentity() {
        return Arrays.equals(m, IDENTITY.m);
    }

    public void multiply(float scalar) {
        multiply(scalar, this);
    }

    public void multiply(float scalar, Mat4 dst) {
        multiply(this, scalar, dst);
    }

    public static void multiply(final Mat4 mat, float scalar, Mat4 dst) {
        MathUtilC.multiplyMatrix(mat.m, scalar, dst.m);
    }

    public Mat4 multiplyRet(Mat4 mat) {
        Mat4 ret = new Mat4(this);
        ret.multiply(mat);
        return ret;
    }

    public void multiply(Mat4 mat) {
        multiply(this, mat, this);
    }

    public static void multiply(final Mat4 m1, final Mat4 m2, Mat4 dst) {
        MathUtilC.multiplyMatrix(m1.m, m2.m, dst.m);
    }

    public void negate() {
        MathUtilC.negateMatrix(m, m);
    }

    public Mat4 getNegate() {
        Mat4 mat = new Mat4(this);
        mat.negate();

        return mat;
    }

    public void rotate(final Quaternion q) {
        rotate(q, this);
    }

    public void rotate(final Quaternion q, Mat4 dst) {
        Mat4 r = new Mat4();
        createRotation(q, r);
        multiply(this, r, dst);
    }

    public void rotate(final Vec3 axis, float angle) {
        rotate(axis, angle, this);
    }

    public void rotate(final Vec3 axis, float angle, Mat4 dst) {
        Mat4 r = new Mat4();
        createRotation(axis, angle, r);
        multiply(this, r, dst);
    }

    public void rotateX(float angle) {
        rotateX(angle, this);
    }

    public void rotateX(float angle, Mat4 dst) {
        Mat4 r = new Mat4();
        createRotationX(angle, r);
        multiply(this, r, dst);
    }

    public void rotateY(float angle) {
        rotateY(angle, this);
    }

    public void rotateY(float angle, Mat4 dst) {
        Mat4 r = new Mat4();
        createRotationY(angle, r);
        multiply(this, r, dst);
    }

    public void rotateZ(float angle) {
        rotateZ(angle, this);
    }

    public void rotateZ(float angle, Mat4 dst) {
        Mat4 r = new Mat4();
        createRotationZ(angle, r);
        multiply(this, r, dst);
    }

    public void scale(float value) {
        scale(value, this);
    }

    public void scale(float value, Mat4 dst) {
        scale(value, value, value, dst);
    }

    public void scale(float xSclae, float yScale, float zScale) {
        scale(xSclae, yScale, zScale, this);
    }

    public void scale(float xSclae, float yScale, float zScale, Mat4 dst) {
        Mat4 s = new Mat4();
        createScale(xSclae, yScale, zScale, s);
        multiply(this, s, dst);
    }

    public void scale(Vec3 s) {
        scale(s.x, s.y, s.z, this);
    }

    public void scale(Vec3 s, Mat4 dst) {
        scale(s.x, s.y, s.z, dst);
    }

    public void normalize() {

    }

    public void set(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24,
                    float m31, float m32, float m33, float m34, float m41, float m42, float m43, float m44) {

        m[0]  = m11;
        m[1]  = m21;
        m[2]  = m31;
        m[3]  = m41;
        m[4]  = m12;
        m[5]  = m22;
        m[6]  = m32;
        m[7]  = m42;
        m[8]  = m13;
        m[9]  = m23;
        m[10] = m33;
        m[11] = m43;
        m[12] = m14;
        m[13] = m24;
        m[14] = m34;
        m[15] = m44;
    }

    public void set(float[] mat) {
        OpenGlUtils.copyMatrix(m, mat, 16);
//        m = Arrays.copyOf(mat, 16);
    }

    public void set(final Mat4 mat) {
//        m = Arrays.copyOf(mat.m, 16);
        OpenGlUtils.copyMatrix(m, mat.m, 16);
    }

    public void setIdentity() {
//        m = Arrays.copyOf(IDENTITY.m, 16);
        OpenGlUtils.copyMatrix(m, IDENTITY.m, 16);
    }

    public void setZero() {
        Arrays.fill(m, 0);
    }

    public void subtract(final Mat4 mat) {
        subtract(this, mat, this);
    }

    public static void subtract(final Mat4 m1, final Mat4 m2, Mat4 dst) {
        MathUtilC.subtractMatrix(m1.m, m2.m, dst.m);
    }

    public void transformPoint(Vec3 point) {
        transformVector(point.x, point.y, point.z, 1.0f, point);
    }

    public void transformPoint(final Vec3 point, Vec3 dst) {
        transformVector(point.x, point.y, point.z, 1.0f, dst);
    }

    public void transformVector(Vec3 vector) {
        transformVector(vector.x, vector.y, vector.z, 0.0f, vector);
    }

    public void transformVector(final Vec3 vector, Vec3 dst) {
        transformVector(vector.x, vector.y, vector.z, 0.0f, dst);
    }

    public void transformVector(float x, float y, float z, float w, Vec3 dst) {
        MathUtilC.transformVec4(m, x, y, z, w, dst);
    }

    public void transformVector(Vec4 vec4) {
        transformVector(vec4, vec4);
    }

    public void transformVector(final Vec4 vector, Vec4 dst) {
        MathUtilC.transformVec4(m, vector, dst);
    }

    public void translate(float x, float y, float z) {
        translate(x, y, z, this);
    }

    public void translate(float x, float y, float z, Mat4 dst) {
        Mat4 t = new Mat4();
        createTranslation(x, y, z, t);
        multiply(this, t, dst);
    }

    public void translate(final Vec3 t) {
        translate(t.x, t.y, t.z, this);
    }

    public void translate(final Vec3 t, Mat4 dst) {
        translate(t.x, t.y, t.z, dst);
    }

    public void transpose() {
        MathUtilC.transposeMatrix(m, m);
    }

    public Mat4 getTransposed() {
        Mat4 mat = new Mat4(this);
        mat.transpose();
        return mat;
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
