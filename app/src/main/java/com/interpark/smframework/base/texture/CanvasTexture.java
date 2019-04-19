package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Mat4;

public class CanvasTexture extends Texture {
    private boolean mRenderTargetEnabled;

    private int mStoredFrameBufferId = 0;

    private static float[] sMatrix = new float[16];

    CanvasTexture(IDirector director, String key, int width, int height) {
        super(director, key, false, null);
        mOriginalWidth = mWidth = width;
        mOriginalHeight = mHeight = height;
    }

    public boolean setRenderTarget(IDirector director, boolean turnOn) {
        mRenderTargetEnabled = true;

        if (turnOn) {
            mStoredFrameBufferId = director.getFrameBufferId();

            if (director.bindTexture(this)) {
                if (mTextureId[0] == NO_TEXTURE) {
                    loadTexture(null, null);
                }
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getId());
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId[0], 0);

                GLES20.glViewport(0, 0, mWidth, mHeight);

                float[] matrix = setProjectionMatrix();

                director.pushMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW);
                director.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, new Mat4(matrix));

                mRenderTargetEnabled = true;
            }
        } else {
            if (mRenderTargetEnabled) {
                mRenderTargetEnabled = false;
                director.setFrameBufferId(mStoredFrameBufferId);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mStoredFrameBufferId);

                // TODO : bindTexture(null) 필요한지 검증할것.
                director.getTextureManager().bindTexture(null);
                // Viewport, Projection matrix 원상복구
                GLES20.glViewport(0, 0, director.getWidth(), director.getHeight());
                director.popMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW);
            }
        }

        return mRenderTargetEnabled;
    }

    private float[] setProjectionMatrix() {
        Matrix.orthoM(sMatrix, 0, 0, mWidth, mHeight, 0, -1000, 1000);
        Matrix.translateM(sMatrix, 0, 0, mHeight, 0);
        Matrix.scaleM(sMatrix, 0, 1, -1, 1);
        return sMatrix;
    }

    @Override
    public boolean loadTexture(IDirector director, Bitmap bitmap) {
        GLES20.glGenTextures(1, mTextureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mTextureId[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0 , GLES20.GL_RGBA, mWidth, mHeight, 0 , GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        return true;
    }

    @Override
    protected void initTextureDimen(Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isRenderTargerEnables() {
        return mRenderTargetEnabled;
    }

    public void clear() {
        if (mRenderTargetEnabled) {
            GLES20.glClearColor(0, 0, 0, 0);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    public boolean setFrameBuffer(IDirector director, boolean turnOn) {
        if (turnOn) {
            if (director.bindTexture(this)) {
                if (mTextureId[0] == NO_TEXTURE) {
                    loadTexture(null, null);
                }
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getId());
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId[0], 0);

                GLES20.glViewport(0, 0, mWidth, mHeight);
                director.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, director.getFrameBufferMatrix());

                mRenderTargetEnabled = true;
            }
        } else {
            if (mRenderTargetEnabled) {
                mRenderTargetEnabled = false;
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }

        return mRenderTargetEnabled;
    }
}
