package com.interpark.smframework.util.ImageManager;

import com.interpark.smframework.util.FileManager;
import com.interpark.smframework.util.cache.MemoryCacheEntry;

public class FileCacheWriteTask {
    public FileCacheWriteTask() {

    }

    public static FileCacheWriteTask createTaskForCache(String cacheKey, MemoryCacheEntry cacheEntry) {
        FileCacheWriteTask task = new FileCacheWriteTask();

        task._cacheKey = cacheKey;
        task._cacheEntry = cacheEntry;
        return task;
    }

    public void procFileCacheWriteThread() {
        FileManager.getInstance().writeToFile(FileManager.FileType.Image, _cacheKey, _cacheEntry.getData(), _cacheEntry.size());
    }

    private MemoryCacheEntry _cacheEntry = null;
    private String _cacheKey;
}
