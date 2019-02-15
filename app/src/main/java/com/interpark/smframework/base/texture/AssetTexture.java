package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.IOUtils;
import com.interpark.webp.WebPFactory;

import java.io.InputStream;

public class AssetTexture extends Texture {
    private final String mFileName;

    public AssetTexture(IDirector director, String key, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        super(director, key, loadAsync, listener);
        mFileName = fileName;
        initTextureDimen(director.getContext());
    }

    public AssetTexture(IDirector director, String key, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, Texture srcTexture) {
        super(director, key, loadAsync, listener);
        mFileName = fileName;

        mOriginalWidth = mWidth = srcTexture.getWidth();
        mOriginalHeight = mHeight = srcTexture.getHeight();

        setId(srcTexture.getId());
        setValid(true);
    }

    public AssetTexture(IDirector director, String key, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int width, int height) {
        super(director, key, loadAsync, listener);
        mFileName = fileName;

        mOriginalWidth = mWidth = width;
        mOriginalHeight = mHeight = height;

        setValid(true);
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
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            is = context.getAssets().open(mFileName);
            if (is != null) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
            }
        }
        catch (Exception e) {
            setValid(false);
        } finally {
            IOUtils.closeSilently(is);
        }

        mOriginalWidth = mWidth = options.outWidth;
        mOriginalHeight = mHeight = options.outHeight;
    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getAssets().open(mFileName);
            // 나중에 구현하자
            if (is != null) {
                bitmap = WebPFactory.decodeStream(is);
            }
        }
        catch (Exception e) {
            setValid(false);
        } finally {
            IOUtils.closeSilently(is);
        }

        return bitmap;
    }
}
