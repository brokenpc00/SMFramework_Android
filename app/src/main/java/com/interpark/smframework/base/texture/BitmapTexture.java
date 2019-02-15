package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.interpark.smframework.IDirector;

public class BitmapTexture extends Texture {
    private Bitmap mTempBitamp;

    BitmapTexture(IDirector director, String key, Bitmap bitmap) {
        super(director, key, false, null);
        mOriginalWidth = mWidth = bitmap.getWidth();
        mOriginalHeight = mHeight = bitmap.getHeight();
        mTempBitamp = bitmap;
    }

    void setBitmap(Bitmap bitmap) {
        mTempBitamp = bitmap;
    }

    @Override
    public boolean loadTexture(IDirector director, Bitmap unused) {
        if (mTempBitamp != null && !mTempBitamp.isRecycled()) {
            Bitmap bitmap = mTempBitamp;
            mTempBitamp = null;

            GLES20.glGenTextures(1, mTextureId, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mTextureId[0]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            return true;
        }
        return false;
    }

    public boolean updateTexture(IDirector director, Bitmap bitmap) {
        if (mTextureId[0] == NO_TEXTURE) {
            GLES20.glGenTextures(1, mTextureId, 0);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mTextureId[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return true;
    }

    @Override
    protected void initTextureDimen(Context context) {
    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        return null;
    }
}
