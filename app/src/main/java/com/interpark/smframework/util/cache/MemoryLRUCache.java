package com.interpark.smframework.util.cache;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;

public class MemoryLRUCache extends LruCache<String, MemoryCacheEntry> {
    public MemoryLRUCache(int maxsize) {
        super(maxsize);
    }

    protected int sizeOf(final String key, MemoryCacheEntry entry) {
        return entry.size();
    }
}
