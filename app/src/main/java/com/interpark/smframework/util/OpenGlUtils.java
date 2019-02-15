package com.interpark.smframework.util;

import android.opengl.Matrix;

public class OpenGlUtils {
    public static void getPerspectiveMatrix(float[] result, float fovy, float aspect,
                                            float zNear, float zFar) {
        float top = zNear * (float) Math.tan(fovy * (Math.PI / 360.0));
        float bottom = -top;
        float left = bottom * aspect;
        float right = top * aspect;
        Matrix.frustumM(result, 0, left, right, bottom, top, zNear, zFar);
    }

    public static void getLookAtMatrix(float[] matrix, float eyeX, float eyeY, float eyeZ,
                                       float centerX, float centerY, float centerZ, float upX, float upY,
                                       float upZ) {

        Matrix.setLookAtM(matrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ,
                upX, upY, upZ);
    }

    public static void copyMatrix(float[] dst, float[] src) {
        for (int i = 0; i < 16; i++) {
            dst[i] = src[i];
        }
    }
}
