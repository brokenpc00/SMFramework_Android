package com.interpark.smframework.base.sprite;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.texture.Texture.OnTextureAsyncLoadListener;

public class BitmapSprite extends Sprite {
    public static BitmapSprite createFromFile(IDirector director, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int degrees) {
        return createFromFile(director, fileName, loadAsync, listener, degrees, -1);
    }

    public static BitmapSprite createFromFile(IDirector director, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int degrees, int maxSideLength) {
        Texture texture = director.getTextureManager().createTextureFromFile(fileName, loadAsync, listener, degrees, maxSideLength);
        if (texture != null && texture.isValid()) {
            return new BitmapSprite(director, texture, 0, 0);
        }
        return null;
    }

    public static BitmapSprite createFromAsset(IDirector director, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        Texture texture = director.getTextureManager().createTextureFromAssets(fileName, loadAsync, listener);
        return new BitmapSprite(director, texture, 0, 0);
    }

    public static BitmapSprite createFromAsset(IDirector director, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int width, int height) {
        Texture texture = director.getTextureManager().createTextureFromAssets(fileName, loadAsync, listener, width, height);
        return new BitmapSprite(director, texture, width/2f, height/2f);
    }

    public static BitmapSprite createFromAsset(IDirector director, String fileName, boolean loadAsync, OnTextureAsyncLoadListener listener, int width, int height, float cx, float cy) {
        Texture texture = director.getTextureManager().createTextureFromAssets(fileName, loadAsync, listener, width, height);
        return new BitmapSprite(director, texture, cx, cy);
    }

    public static BitmapSprite createFromBitmap(IDirector director, String key, Bitmap bitmap) {
        return createFromBitmap(director, key, bitmap, false);
    }
    public static BitmapSprite createFromBitmap(IDirector director, String key, Bitmap bitmap, boolean alignCenter) {
        Texture texture = director.getTextureManager().createTextureFromBitmap(bitmap, key);
        BitmapSprite s;
        if (alignCenter) {
            s = new BitmapSprite(director, texture, texture.getWidth()/2, texture.getHeight()/2);
        } else {
            s = new BitmapSprite(director, texture, 0, 0);
        }
        return s;
    }

    public static BitmapSprite createFromResource(IDirector director, int resId) {
        return createFromResource(director, resId, false);
    }

    public static BitmapSprite createFromResource(IDirector director, int resId, boolean alignCenter) {
        Texture texture = director.getTextureManager().createTextureFromResource(resId);

        BitmapSprite s;
        if (alignCenter) {
            s = new BitmapSprite(director, texture, texture.getWidth()/2, texture.getHeight()/2);
        } else {
            s = new BitmapSprite(director, texture, 0, 0);
        }

        return s;
    }

    public static BitmapSprite createFromDrawable(IDirector director, Drawable drawable, boolean loadAsync, OnTextureAsyncLoadListener listener) {
        return createFromDrawable(director, drawable, loadAsync, listener, false);
    }

    public static BitmapSprite createFromDrawable(IDirector director, Drawable drawable, boolean loadAsync, OnTextureAsyncLoadListener listener, boolean alignCenter) {
        Texture texture = director.getTextureManager().createTextureFromDrawable(drawable, loadAsync, listener);

        BitmapSprite s;
        if (alignCenter) {
            s = new BitmapSprite(director, texture, texture.getWidth()/2, texture.getHeight()/2);
        } else {
            s = new BitmapSprite(director, texture, 0, 0);
        }

        return s;
    }


    private BitmapSprite(IDirector director, Texture texture, float cx, float cy) {
        super(director,
                texture.getWidth(),
                texture.getHeight(),
                cx,
                cy,
                0,
                0,
                texture);
    }
}
