package com.interpark.smframework.util.cache;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.lang.ref.WeakReference;

public class ImageCacheEntry {
    public static ImageCacheEntry createEntry(Bitmap bmp) {
        ImageCacheEntry cacheEntry = new ImageCacheEntry();
        cacheEntry._image = bmp;

        return cacheEntry;
    }

    public Bitmap getImage() {return _image;}

    public int size() {
        if (_image!=null) {
            return _image.getByteCount();
        } else {
            return 0;
        }
    }

    public ImageCacheEntry() {
        _image = null;
    }

    private Bitmap _image = null;
}
