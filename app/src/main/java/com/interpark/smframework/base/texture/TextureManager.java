package com.interpark.smframework.base.texture;

import android.graphics.Bitmap;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import com.interpark.smframework.base.texture.Texture.OnTextureAsyncLoadListener;

import com.interpark.smframework.IDirector;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextureManager {
    private final Map<String, Texture> mTextureMap = new ConcurrentHashMap<String, Texture>();
    private IDirector _director;
    private int mActiveTextureId;

    public TextureManager(IDirector director) {
        _director = director;
        mActiveTextureId = Texture.NO_TEXTURE;
    }

    public boolean bindTexture(Texture texture) {
        if (texture != null) {
            if (texture.getId() == Texture.NO_TEXTURE) {
                if (texture.isAsyncLoader()) {
                    if (texture.isLoading()) {
                        return false;
                    } else {
                        texture.loadTextureAsync(_director);
                        return false;
                    }
                } else {
                    if (!texture.createOrUpdate(_director, null)) {
                        return false;
                    }
                }
            }
            if (mActiveTextureId != texture.getId()) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getId());
                mActiveTextureId = texture.getId();
            }
            return true;
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _director.getFrameBufferId());
            mActiveTextureId = Texture.NO_TEXTURE;
        }
        return false;
    }

    public void onResume() {
        mActiveTextureId = Texture.NO_TEXTURE;
    }

    public void onPause() {

        Collection<Texture> textures = mTextureMap.values();
        for (Texture texture : textures) {
            texture.setId(Texture.NO_TEXTURE);
            if (texture.getRefCount() == 0) {
                mTextureMap.remove(texture.getKey());
            }
        }

        mActiveTextureId = Texture.NO_TEXTURE;
    }

    public int size() {
        return mTextureMap.size();
    }

    public Texture createCanvasTexture(final int width, final int height, final String keyName) {

        final String key = makeCanvasTextureKey(keyName, width, height);
        Texture texture = mTextureMap.get(key);
        if (texture == null) {
            texture = new CanvasTexture(_director, key, width, height);
            mTextureMap.put(key, texture);
        }

        return texture;
    }

    public Texture createPreviewTexture(final int width, final int height, final String keyName) {

        final String key = makePreviewTextureKey(keyName, width, height);
        Texture texture = mTextureMap.get(key);
        if (texture == null) {
            texture = new CameraPreviewTexture(_director, key, width, height);
            mTextureMap.put(key, texture);
        }

        return texture;
    }

    public Texture createTextureFromBitmap(final Bitmap bitmap, final String key) {

        Texture texture = mTextureMap.get(key);
        if (texture != null && texture.getId() == Texture.NO_TEXTURE) {
            if (texture instanceof BitmapTexture) {
                ((BitmapTexture)texture).setBitmap(bitmap);
            }
        }
        if (texture == null) {
            texture = new BitmapTexture(_director, key, bitmap);
            mTextureMap.put(key, texture);
        }

        return texture;
    }

    // file resour (texture packer)
    public Texture createTextureFromResource(final int resId) {

        final String key = makeResourceTextureKey(resId);
        Texture texture = mTextureMap.get(key);
        if (texture == null) {
            texture = new ResourceTexture(_director, key, resId, false, null);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                texture.incRefCount();
            }
        } else {
            texture.incRefCount();
        }

        return texture;
    }

    // drawable resource
    public Texture createTextureFromDrawable(final Drawable drawable, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        final String key = makeDrawableTextureKey(drawable);
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new DrawableTexture(_director, key, drawable, loadAsync, listener);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                if (loadAsync) {
                    texture.loadTextureAsync(_director);
                }
            } else {
                texture = null;
            }
        } else {
            if (loadAsync && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }

    // asset resource (webp... 이거는 나중에 구현해야함.)
    public Texture createTextureFromAssets(final String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        final String key = makeAssetTextureKey(fileName);
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new AssetTexture(_director, key, fileName, loadAsync, listener);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                if (loadAsync) {
                    texture.loadTextureAsync(_director);
                }
            } else {
                texture = null;
            }
        } else {
            if (loadAsync && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }

    public Texture createTextureFromAssets(final String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int width, int height) {
        final String key = makeAssetTextureKey(fileName);
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new AssetTexture(_director, key, fileName, loadAsync, listener, width, height);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                if (loadAsync) {
                    texture.loadTextureAsync(_director);
                }
            } else {
                texture = null;
            }
        } else {
            if (loadAsync && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }

    public Texture findFileTexture(final String fileName, int maxSideLength) {
        return findTextureByKey(makeFileTextureKey(fileName+"_"+maxSideLength+"_"));
    }

    public Texture createTextureFromFile(final String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int degrees, int maxSideLength) {
        final String key = makeFileTextureKey(fileName+"_"+maxSideLength+"_");
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new FileTexture(_director, key, fileName, loadAsync, listener, degrees, maxSideLength);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                if (loadAsync) {
                    texture.loadTextureAsync(_director);
                }
            } else {
                texture = null;
            }
        } else {
            if (texture.isValid() && loadAsync && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }


    public Texture createFakeAssetsTexture(final String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, Texture srcTexture) {
        final String key = makeAssetTextureKey(fileName);
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new AssetTexture(_director, key, fileName, loadAsync, listener, srcTexture);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                if (loadAsync) {
                    texture.loadTextureAsync(_director);
                }
            } else {
                texture = null;
            }
        } else {
            if (loadAsync && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }

    public Texture createFakeFileTexture(final String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int degrees, int maxSideLength, Texture srcTexture) {
        final String key = makeFileTextureKey(fileName+"_"+maxSideLength+"_");
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new FileTexture(_director, key, fileName, loadAsync, listener, degrees, maxSideLength, srcTexture);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
            } else {
                texture = null;
            }
        } else {
            texture.setId(srcTexture.getId());
            if (texture.isValid() && loadAsync && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }


    public Texture createTextureFromNetwork(final String url, int width, int height, OnTextureAsyncLoadListener listener, int maxSideLength) {
        final String key = makeNetworkTextureKey(url+"_"+maxSideLength+"_");
        Texture texture = mTextureMap.get(key);

        if (texture == null) {
            texture = new NetworkTexture(_director, key, url, width, height, listener, maxSideLength);
            if (texture.isValid()) {
                mTextureMap.put(key, texture);
                texture.loadTextureAsync(_director);
            } else {
                texture = null;
            }
        } else {
            if (texture.isValid() && listener != null) {
                final OnTextureAsyncLoadListener l = listener;
                final Texture t = texture;
                _director.runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        l.onTextureLoaded(t);
                    }
                });
            }
        }

        return texture;
    }

    public Texture createTextureFromString(final String text, final float fontSize, final Align align,
                                           final boolean bold, final boolean italic, final boolean strikeThru,
                                           int maxWidth, int maxLines) {

        final String key = makeStringTextureKey(text, fontSize, bold, italic, strikeThru);
        Texture texture = mTextureMap.get(key);
        if (texture == null) {
            texture = new TextTexture(_director, key, text, fontSize, align, bold, italic, strikeThru, maxWidth, maxLines);
            mTextureMap.put(key, texture);
        }

        return texture;
    }

    public Texture createTextureFromHtmlString(final String text, final float fontSize,
                                               final boolean bold, final boolean italic, final boolean strikeThru) {

        final String key = makeHtmlTextureKey(text, fontSize);
        Texture texture = mTextureMap.get(key);
        if (texture == null) {
            texture = new TextTexture(_director, key, text, fontSize, Align.LEFT, bold, italic, strikeThru, true);
            mTextureMap.put(key, texture);
        }

        return texture;
    }


    public Texture getTextureFromResource(final int resId) {
        return mTextureMap.get(makeResourceTextureKey(resId));
    }

    public Texture getTextureFromAssets(final String fileName) {
        return mTextureMap.get(makeAssetTextureKey(fileName));
    }

    public Texture getTextureFromFile(final String fileName) {
        return mTextureMap.get(makeFileTextureKey(fileName));
    }

    public Texture getTextureFromNetwork(final String url) {
        return mTextureMap.get(makeNetworkTextureKey(url));
    }

    public Texture getTexture(String key) {
        return mTextureMap.get(key);
    }

    public boolean removeTexture(Texture texture) {
        if (texture != null) {
            if (texture.decRefCount() <= 0) {
                texture.deleteTexture(_director.isGLThread());
                mTextureMap.remove(texture.getKey());

                return true;
            }
        }
        return false;
    }

    public Texture findTextureByKey(String key) {
        return mTextureMap.get(key);
    }


    public boolean removeFakeTexture(Texture texture) {
        if (texture != null) {
            mTextureMap.remove(texture.getKey());
            return true;
        }
        return false;
    }

    public static String makeDrawableTextureKey(final Drawable drawable) {
        return "drawable:@"+
                String.valueOf(drawable.hashCode());
    }

    public static String makeResourceTextureKey(final int resId) {
        return "resource:@"+
                String.valueOf(resId);
    }

    public static String makeAssetTextureKey(final String fileName) {
        return "assets:@"+
                String.valueOf(fileName.hashCode());
    }

    public static String makeFileTextureKey(final String fileName) {
        return "file:@"+
                String.valueOf(fileName.hashCode());
    }

    public static String makeNetworkTextureKey(final String url) {
        return "url:@"+
                String.valueOf(url.hashCode());
    }

    public static String makeStringTextureKey(final String text, final float fontSize, final boolean bold, final boolean italic, final boolean strikeThru) {
        return "string:@"+
                String.valueOf(text.hashCode())+"_"+
                String.valueOf(fontSize)+"_"+
                String.valueOf(bold)+"_"+
                String.valueOf(italic)+"_"+
                String.valueOf(strikeThru);
    }

    public static String makeHtmlTextureKey(final String text, final float fontSize) {
        return "html:@"+
                String.valueOf(text.hashCode())+"_"+
                String.valueOf(fontSize);
    }

    public static String makeCanvasTextureKey(final String keyName, final int width, final int height) {
        return "canvas:@"+
                String.valueOf(keyName.hashCode()) + String.valueOf(width) + "x" + String.valueOf(height);
    }

    public static String makePreviewTextureKey(final String keyName, final int width, final int height) {
        return "preview:@"+
                String.valueOf(keyName.hashCode()) + String.valueOf(width) + "x" + String.valueOf(height);
    }

    public int getTextureCount() {
        return mTextureMap.size();
    }
}
