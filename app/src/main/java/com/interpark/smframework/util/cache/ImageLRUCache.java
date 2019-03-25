package com.interpark.smframework.util.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;

public class ImageLRUCache extends LruCache<String, ImageCacheEntry> {
    public ImageLRUCache(int maxSize) {
        super(maxSize);
    }
    public ImageLRUCache(Context ctx) {
        this(getCacheSize(ctx));
    }

    protected int sizeOf(final String key, ImageCacheEntry entry) {
        return entry.size();
    }

    public static int getCacheSize(Context ctx) {
        final DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;
        // 4 bytes per pixel
        final int screenBytes = screenWidth * screenHeight * 4;

        return screenBytes * 2;
    }
}
