package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.SMAsyncTask;

import java.lang.ref.WeakReference;

public abstract class Texture {
    public interface OnTextureAsyncLoadListener {
        public void onTextureLoaded(Texture texture);
        public void onTextureLoadedBitmap(Bitmap bitmap);
    }

    protected OnTextureAsyncLoadListener mAsyncLoadListener;
    protected TextureLoaderTask mAsyncTask;

    public interface TextureLoader {
        boolean onCreateOrUpdateTexture(IDirector director, Bitmap bitmap);
    }
    private TextureLoader mTextureLoader;
    public void setOnTextureLoader(TextureLoader loader) {
        mTextureLoader = loader;
    }

    public static final int  NO_TEXTURE = -1;
    private int mRefCount = 0;
    private boolean mIsValid = true;

    // texture cache key
    protected final String mKey;

    // texture gl id
    protected final int[] mTextureId;

    // texture width
    protected int mWidth;

    // texture height
    protected int mHeight;

    protected int mOriginalWidth;
    protected int mOriginalHeight;

    // async load flag
    private final boolean mLoadAsync;

    protected boolean mDoNotRecycleBitmap = false;

    protected Texture(IDirector director, String key, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        mKey = key;
        mTextureId = new int[1];
        mTextureId[0] = NO_TEXTURE;
        mLoadAsync = loadAsync;
        mAsyncLoadListener = listener;
    }

    public boolean isAsyncLoader() {
        return mLoadAsync;
    }

    public boolean isLoading() {
        return mAsyncTask != null;
    }

    public String getKey() {
        return mKey;
    }

    public int getId() {
        return mTextureId[0];
    }

    public int[] getIdRef() {
        return mTextureId;
    }

    public void setId(int textureId) {
        mTextureId[0] = textureId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    // origin image width
    public int getUnscaledWidth() {
        return mOriginalWidth;
    }

    // origin image height
    public int getUnscaledHeight() {
        return mOriginalHeight;
    }

    // like arc
    public void incRefCount() {
        mRefCount++;
    }

    public int decRefCount() {
        return --mRefCount;
    }

    public int getRefCount() {
        return mRefCount;
    }

    public void deleteTexture(boolean isGLThread) {
        if (mAsyncTask != null) {
            mAsyncTask.stop();
            mAsyncTask = null;
        }
        if (mTextureId[0] != NO_TEXTURE) {
            if (isGLThread) {
                GLES20.glDeleteTextures(1, mTextureId, 0);
            }
            mTextureId[0] = NO_TEXTURE;
        }
    }

    public boolean isValid() {
        return mIsValid;
    }

    public void setValid(boolean isValid) {
        mIsValid = isValid;
    }

    abstract public boolean loadTexture(IDirector director, Bitmap bitmap);
    protected void loadTextureAsync(IDirector director) {
        if (mAsyncTask != null && !mAsyncTask.isCancelled()) {
            mAsyncTask.stop();
        }
        mAsyncTask = new TextureLoaderTask(director, mAsyncLoadListener);
        mAsyncTask.executeOnExecutor(SMAsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected abstract void initTextureDimen(Context context);
    protected abstract Bitmap loadTextureBitmap(Context context);

    class TextureLoaderTask extends SMAsyncTask<Void, Void, Bitmap> {
        private final WeakReference<OnTextureAsyncLoadListener> mWeakReference;
        private boolean mCancelled = false;

        public TextureLoaderTask(IDirector director, OnTextureAsyncLoadListener listener) {
            super(director);
            mWeakReference = new WeakReference<OnTextureAsyncLoadListener>(listener);
        }

        public void stop() {
            if (mWeakReference != null) {
                final OnTextureAsyncLoadListener l = mWeakReference.get();
                if (l != null) {
                }
                mWeakReference.clear();
            }
            mCancelled = true;
            cancel(true);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            if (!mCancelled && !isCancelled()) {
                Bitmap bitmap = loadTextureBitmap(getDirector().getContext());
                return bitmap;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mAsyncTask = null;
            if (mCancelled || isCancelled()) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                return;
            }
            OnTextureAsyncLoadListener l = mWeakReference.get();
            if (createOrUpdate(getDirector(), bitmap)) {
                if (l != null) {
                    l.onTextureLoaded(Texture.this);
                    if (mDoNotRecycleBitmap) {
                        l.onTextureLoadedBitmap(bitmap);
                    }
                }
            } else {
                if (l != null) {
                    l.onTextureLoaded(null);
                }
            }
        }
    }

    public boolean createOrUpdate(IDirector director, Bitmap bitmap) {
        if (mTextureLoader != null) {
            return mTextureLoader.onCreateOrUpdateTexture(director, bitmap);
        }
        return loadTexture(director, bitmap);
    }

    public void setDoNotRecycleBitmap(boolean doNotRecycleBitmap) {}
}
