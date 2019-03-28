package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.BitmapLoader;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.util.IOUtils;
import com.interpark.smframework.util.KeyGenerateUtil;
import com.interpark.smframework.util.NetworkStreamRequest;
import com.interpark.smframework.util.SMAsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class NetworkTexture extends Texture implements Response.Listener<ByteArrayInputStream>, Response.ErrorListener {
    private IDirector _director;
    private final String mUrl;
    private final int mMaxSideLength;
    private int mReqWidth;
    private int mReqHeight;
    private NetworkStreamRequest mRequest;
    private boolean mCached;
    private final WeakReference<OnTextureAsyncLoadListener> mWeakReference;
    private CheckTask mCheckTesk = null;

    class CheckTask extends SMAsyncTask<Void, Void, Void> {

        public CheckTask(IDirector director) {
            super(director);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!isCancelled()) {
                File dir = new File(getDirector().getContext().getExternalFilesDir(null), "network_cache");
                if (dir.exists()) {
                    File file = new File(dir, KeyGenerateUtil.generate(mUrl));
                    if (file.exists()) {
                        mCached = true;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!isCancelled()) {
                if (!mCached) {
                    mRequest = new NetworkStreamRequest(mUrl, NetworkTexture.this, NetworkTexture.this);
                    getDirector().getRequestQueue().add(mRequest);
                } else {
                    NetworkTexture.super.loadTextureAsync(getDirector());
                }
                mCheckTesk = null;
            }
        }

    }


    public NetworkTexture(IDirector director, String key, String url, int width, int height, OnTextureAsyncLoadListener listener, int maxSideLength) {
        super(director, key, true, listener);
        _director = director;
        mUrl = url;
        mReqWidth = width;
        mReqHeight = height;
        mMaxSideLength = maxSideLength;
        mCached = false;
        initTextureDimen(director.getContext());
        mWeakReference = new WeakReference<OnTextureAsyncLoadListener>(listener);
    }

    public NetworkTexture(IDirector director, String key, String url, int width, int height, OnTextureAsyncLoadListener listener, int maxSideLength, Texture srcTexture) {
        super(director, key, true, listener);
        _director = director;
        mUrl = url;
        mReqWidth = width;
        mReqHeight = height;
        mMaxSideLength = maxSideLength;
        mCached = false;
        initTextureDimen(director.getContext());
        mWeakReference = new WeakReference<OnTextureAsyncLoadListener>(listener);
    }

    public boolean isLoading() {
        return mCheckTesk != null || mRequest != null || super.isLoading();
    }

    @Override
    protected void loadTextureAsync(IDirector director) {
        mCheckTesk = new CheckTask(director);
        mCheckTesk.execute();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mRequest = null;
        setValid(false);
//		Log.e("NetworkTexture", "ERROR Loading url : " + mUrl);
    }

    @Override
    public void onResponse(final ByteArrayInputStream response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = new File(_director.getContext().getExternalFilesDir(null), "network_cache");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, KeyGenerateUtil.generate(mUrl));

                InputStream is = null;
                OutputStream os = null;

                byte data[] = new byte[1024];
                int count = 0;
                try {
                    is = new BufferedInputStream(response, 8192);
                    os = new FileOutputStream(file);

                    while ((count = is.read(data)) != -1) {
                        os.write(data, 0, count);
                    }
                    os.flush();
                } catch (IOException e) {
//		            Log.e("Error: ", e.getMessage());
                } finally {
                    IOUtils.closeSilently(os);
                    IOUtils.closeSilently(is);
                }

                final Bitmap bitmap = getCachedBitmap(_director.getContext(), dir);
//                Log.i("NT", "[[[[[ onResponse runOnDraw~~~");
//                _director.runOnDraw(new Runnable() {
//                    @Override
//                    public void run() {
                _director.getScheduler().performFunctionInMainThread(new PERFORM_SEL() {
                    @Override
                    public void performSelector() {
                        mCached = true;
                        if (bitmap != null) {
                            OnTextureAsyncLoadListener l = mWeakReference.get();
                            if (createOrUpdate(_director, bitmap)) {
                                if (l != null) {
                                    l.onTextureLoaded(NetworkTexture.this);
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
                        mRequest = null;
                    }
                });
            }
        }).start();

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
        int outWidth = mReqWidth;
        int outHeight = mReqHeight;

        mOriginalWidth = mReqWidth;
        mOriginalHeight = mReqHeight;

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
        File dir = new File(context.getExternalFilesDir(null), "network_cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Bitmap bitmap = getCachedBitmap(context, dir);
        if (bitmap != null) {
            return bitmap;
        }

        return null;
    }

    private Bitmap getCachedBitmap(Context context, File dir) {

        File file = new File(dir, KeyGenerateUtil.generate(mUrl));
        Bitmap bitmap = null;
        if (file.exists()) {
            bitmap = BitmapLoader.loadBitmap(context, file.getAbsolutePath(), 0, mWidth, mHeight);
            if (bitmap == null) {
                // 파일이 있는데 읽기 실패했으면 파일 삭제하고
                // TODO : 재 다운로드 하는 로직을 추가해야 한다.
                if (file.delete()) {
                }
                setValid(false);
            }
        }
        return bitmap;
    }

    @Override
    public void setDoNotRecycleBitmap(boolean doNotRecycleBitmap) {
        if (isAsyncLoader()) {
            mDoNotRecycleBitmap = doNotRecycleBitmap;
        }
    }

    public void deleteTexture(boolean isGLThread) {
        if (mRequest != null) {
            mRequest.cancel();
            mRequest = null;
        }
        super.deleteTexture(isGLThread);
    }
}
