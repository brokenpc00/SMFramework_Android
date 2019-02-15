package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.BitmapLoader;
import com.interpark.webp.WebPFactory;

// local saved file texture
public class FileTexture extends Texture {
    private final String mFileName;
    private final int mMaxSideLength;
    private final int mDegrees;
    private boolean mIsWebPFormat;

    public FileTexture(IDirector director, String key, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int degrees, int maxSideLength) {
        super(director, key, loadAsync, listener);
        mFileName = fileName;
        mMaxSideLength = maxSideLength;
        mDegrees = degrees;
        initTextureDimen(director.getContext());
    }

    public FileTexture(IDirector director, String key, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int degrees, int maxSideLength, Texture srcTexture) {
        super(director, key, loadAsync, listener);
        mFileName = fileName;
        mMaxSideLength = maxSideLength;
        mDegrees = degrees;

        mOriginalWidth = mWidth = srcTexture.getWidth();
        mOriginalHeight = mHeight = srcTexture.getHeight();

        setId(srcTexture.getId());
        setValid(true);
    }

    public String getFileName() {
        return mFileName;
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
            if (!mDoNotRecycleBitmap || !isAsyncLoader()) {
                bitmap.recycle();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void initTextureDimen(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mFileName, options);

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            setValid(false);
            return;
        }

        int outWidth;
        int outHeight;
        if (mDegrees == 90 || mDegrees == 270) {
            outWidth = options.outHeight;
            outHeight = options.outWidth;
        } else {
            outWidth = options.outWidth;
            outHeight = options.outHeight;
        }

        mOriginalWidth = outWidth;
        mOriginalHeight = outHeight;

        if (mMaxSideLength > 0 && (outWidth > mMaxSideLength || outHeight > mMaxSideLength)) {
            float scale = Math.min((float)mMaxSideLength/outWidth, (float)mMaxSideLength/outHeight);
            outWidth = (int)(outWidth*scale);
            outHeight = (int)(outHeight*scale);
        }

        if (outWidth%4 != 0) {
            outWidth += 4-outWidth%4;
        }

        mWidth = outWidth;
        mHeight = outHeight;

    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        Bitmap bitmap = null;
        if (mIsWebPFormat) {
            bitmap = WebPFactory.decodeFileScaled(mFileName, mWidth, mHeight);
        } else {
            bitmap = BitmapLoader.loadBitmap(context, mFileName, mDegrees, mWidth, mHeight);
        }
        if (bitmap == null) {
            setValid(false);
        }
        return bitmap;
    }

    @Override
    public void setDoNotRecycleBitmap(boolean doNotRecycleBitmap) {
        if (isAsyncLoader()) {
            mDoNotRecycleBitmap = doNotRecycleBitmap;
        }
    }

    public void setWebPFormat() {
        mIsWebPFormat = true;
    }
}
