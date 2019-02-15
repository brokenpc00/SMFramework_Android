package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;

public class ResourceTexture extends Texture {
    private int mResourceId;

    protected ResourceTexture(IDirector director, String key, int resId, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        super(director, key, loadAsync, listener);
        mResourceId = resId;
        initTextureDimen(director.getContext());
    }

    @Override
    public boolean loadTexture(IDirector director, Bitmap bitmap) {
        if (bitmap == null && !isAsyncLoader()) {
            bitmap = loadTextureBitmap(director.getContext());
        }
        if (bitmap != null) {
            GLES20.glGenTextures(1, mTextureId, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mTextureId[0]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            return true;
        }
        return false;
    }

    @Override
    protected void initTextureDimen(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), mResourceId, options);
        mOriginalWidth = mWidth = options.outWidth;
        mOriginalHeight = mHeight = options.outHeight;
    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), mResourceId, options);
    }

}
