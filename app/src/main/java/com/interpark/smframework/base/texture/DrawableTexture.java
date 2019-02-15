package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;

public class DrawableTexture extends Texture {
    private Drawable mDrawable;

    protected DrawableTexture(IDirector director, String key, Drawable drawable, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        super(director, key, loadAsync, listener);
        mDrawable = drawable;
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
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            return true;
        }
        return false;
    }

    @Override
    protected void initTextureDimen(Context context) {
        if (mDrawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable)mDrawable).getBitmap();
            mOriginalWidth = mWidth = bitmap.getWidth();
            mOriginalHeight = mHeight = bitmap.getHeight();
        } else {
            int width = mDrawable.getIntrinsicWidth();
            mOriginalWidth = mWidth = width > 0 ? width : 1;
            int height = mDrawable.getIntrinsicHeight();
            mOriginalHeight = mHeight = height > 0 ? height : 1;
        }
    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mDrawable.setBounds(0, 0, mWidth, mHeight);
        mDrawable.draw(canvas);

        return bitmap;
    }
}
