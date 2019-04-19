package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Mat4;

import java.nio.ByteBuffer;

// 속도 때문에 YUV2로 한다.

public class CameraPreviewTexture extends Texture {
    /**
     * Framebuffer id
     */
    private int[] mFrameBufferId;
    private int[] mYTextureId;
    private int[] mUVTextureId;

    /**
     * Render Target state
     */
    private boolean mRenderTargetEnabled;

    private float[] mUtilMatrix;

    private int mStoredFrameBufferId;

    CameraPreviewTexture(IDirector director, String key, int width, int height) {
        super(director, key, false, null);

        mOriginalWidth = mWidth = width;
        mOriginalHeight = mHeight = height;

        mUtilMatrix = new float[16];
    }

    public void onResume() {
        if (mFrameBufferId != null) {
            mFrameBufferId[0] = NO_TEXTURE;
        }
        if (mYTextureId != null) {
            mYTextureId[0] = NO_TEXTURE;
        }
        if (mUVTextureId != null) {
            mUVTextureId[0] = NO_TEXTURE;
        }
    }

    public boolean setRenderTarget(IDirector director, boolean turnOn) {
        mRenderTargetEnabled = true;

        if (turnOn) {
            mStoredFrameBufferId = director.getFrameBufferId();

            if (mFrameBufferId == null) {
                mFrameBufferId = new int[1];
                mFrameBufferId[0] = NO_TEXTURE;
            }

            if (mFrameBufferId[0] == NO_TEXTURE) {
                GLES20.glGenFramebuffers(1, mFrameBufferId, 0);
            }

            if (mTextureId[0] == NO_TEXTURE) {
                loadTexture(null, null);
            }


            if (director.bindTexture(this)) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId[0]);
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId[0], 0);

                GLES20.glViewport(0, 0, mWidth, mHeight);

                Matrix.setIdentityM(mUtilMatrix, 0);

                Matrix.orthoM(mUtilMatrix, 0, 0, mWidth, mHeight, 0, -1000, 1000);
                Matrix.translateM(mUtilMatrix, 0, 0, mHeight, 0);
                Matrix.scaleM(mUtilMatrix, 0, 1, -1, 1);

                director.pushMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW);
                director.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, new Mat4(mUtilMatrix));

                mRenderTargetEnabled = true;
            }
        } else {
            if (mRenderTargetEnabled) {
                mRenderTargetEnabled = false;
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mStoredFrameBufferId);
                director.getTextureManager().bindTexture(null);

                // Viewport, Projection matrix 원상복구
                GLES20.glViewport(0, 0, director.getWidth(), director.getHeight());
                director.popMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW);
            }
        }

        return mRenderTargetEnabled;
    }

    @Override
    public void deleteTexture(boolean isGLThread) {
        super.deleteTexture(isGLThread);
        if (mFrameBufferId != null) {
            if (mFrameBufferId[0] != NO_TEXTURE) {
                if (isGLThread) {
                    GLES20.glDeleteFramebuffers(1, mFrameBufferId, 0);
                }
            }
            mFrameBufferId = null;
        }

        if (mYTextureId != null) {
            if (mYTextureId[0] != NO_TEXTURE) {
                if (isGLThread) {
                    GLES20.glDeleteTextures(1, mYTextureId, 0);
                }
            }
            mYTextureId = null;
        }

        if (mUVTextureId != null) {
            if (mUVTextureId[0] != NO_TEXTURE) {
                if (isGLThread) {
                    GLES20.glDeleteTextures(1, mUVTextureId, 0);
                }
            }
            mUVTextureId = null;
        }

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
    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        return null;
    }

    public void setupPreviewBuffer(IDirector director, int previewWidth, int previewHeight, ByteBuffer yBuffer, ByteBuffer uvBuffer) {

        if (mYTextureId == null) {
            mYTextureId = new int[1];
            mYTextureId[0] = NO_TEXTURE;
        }

        if (mUVTextureId == null) {
            mUVTextureId = new int[1];
            mUVTextureId[0] = NO_TEXTURE;
        }

        if (mYTextureId[0] == NO_TEXTURE) {
            GLES20.glGenTextures(1, mYTextureId, 0);
        }

        if (mUVTextureId[0] == NO_TEXTURE) {
            GLES20.glGenTextures(1, mUVTextureId, 0);
        }

        //-----------------------------------------------------------------------------------------
        // Y Texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mYTextureId[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0 , GLES20.GL_LUMINANCE, previewWidth, previewHeight, 0 , GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuffer);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //-----------------------------------------------------------------------------------------
        // UV Texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mUVTextureId[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0 , GLES20.GL_LUMINANCE_ALPHA, previewWidth/2, previewHeight/2, 0 , GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uvBuffer);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //-----------------------------------------------------------------------------------------
    }
}
