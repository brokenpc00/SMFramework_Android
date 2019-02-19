package com.interpark.smframework.shader;

import android.opengl.GLES20;

public class BlendFunc {
    public BlendFunc(int src, int dst) {
        this.src = src;
        this.dst = dst;
    }

    public int src;
    public int dst;

    public boolean equal(BlendFunc a) {
        return src==a.src && dst==a.dst;
    }

    public boolean notequal(BlendFunc a) {
        return src!=a.src || dst!=a.dst;
    }

    public boolean greaterthan(BlendFunc a) {
        return src < a.src || (src==a.src && dst < a.dst);
    }

    public static final BlendFunc DISABLE = new BlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
    public static final BlendFunc ALPHA_PREMULTIPLIED = new BlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    public static final BlendFunc ALPHA_NON_PREMULTIPLIED = new BlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    public static final BlendFunc ADDITIVE = new BlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
}
