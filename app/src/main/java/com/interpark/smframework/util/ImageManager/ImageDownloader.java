package com.interpark.smframework.util.ImageManager;

import android.graphics.Bitmap;
import android.os.ConditionVariable;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.interpark.smframework.SMDirector;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.Scheduler;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.FileManager;
import com.interpark.smframework.util.cache.ImageCacheEntry;
import com.interpark.smframework.util.cache.ImageLRUCache;
import com.interpark.smframework.util.cache.MemoryCacheEntry;
import com.interpark.smframework.util.cache.MemoryLRUCache;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cz.msebera.android.httpclient.cookie.SM;


public class ImageDownloader {
    public static final DownloadConfig DEFAULT = new DownloadConfig(DownloadConfig.CachePolycy.DEFAULT);
    public static final DownloadConfig NO_CACHE = new DownloadConfig(DownloadConfig.CachePolycy.NO_CACHE);
    public static final DownloadConfig NO_DISK = new DownloadConfig(DownloadConfig.CachePolycy.NO_DISK);
    public static final DownloadConfig NO_IMAGE = new DownloadConfig(DownloadConfig.CachePolycy.NO_IMAGE);
    public static final DownloadConfig CACHE_ONLY = new DownloadConfig(DownloadConfig.CachePolycy.DEFAULT, true);
    public static final DownloadConfig CACHE_ONLY_NO_IMAGE = new DownloadConfig(DownloadConfig.CachePolycy.NO_IMAGE, true);
    public static final DownloadConfig CACHE_ONLY_NO_DISK = new DownloadConfig(DownloadConfig.CachePolycy.NO_DISK, true);
    public static final DownloadConfig CACHE_ONLY_DISK_ONLY = new DownloadConfig(DownloadConfig.CachePolycy.DISK_ONLY, true);
    public static final DownloadConfig IMAGE_ONLY = new DownloadConfig(DownloadConfig.CachePolycy.IMAGE_ONLY, true);

    public enum State {
        DOWNLOAD_STARTED,
        DOWNLOAD_SUCCESS,
        DOWNLOAD_FAILED,

        DECODE_STARTED,
        DECODE_SUCCESS,
        DECODE_FAILED,

        IMAGE_CACHE_DIRECT,
    }

    private static ImageDownloader _imageDownloader = null;
    public static ImageDownloader getInstance() {
        if (_imageDownloader==null) {
            _imageDownloader = new ImageDownloader(MEM_CACHE_SIZE, MEM_CACHE_SIZE, 4, 4);
        }

        return _imageDownloader;
    }

    public void loadImageFromNetwork(IDownloadProtocol target, final String requestUrl) {
        loadImageFromNetwork(target, requestUrl, 0, null);
    }
    public void loadImageFromNetwork(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromNetwork(target, requestUrl, tag, null);
    }
    public void loadImageFromNetwork(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {
        if (requestUrl.length()==0) {
            target.onImageLoadComplete(null, tag, true);
            return;
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return;
        }

        DownloadTask task = DownloadTask.createTaskForTarget(this, target);
        task.init(DownloadTask.MediaType.NETWORK, requestUrl, config);
        task.setTag(tag);

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task);
        }
    }

    public void loadImageFromResource(IDownloadProtocol target, final String requestUrl) {
        loadImageFromResource(target, requestUrl, 0, null);
    }
    public void loadImageFromResource(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromResource(target, requestUrl, tag, null);
    }
    public void loadImageFromResource(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {
        if (requestUrl.length()==0) {
            target.onImageLoadComplete(null, tag, true);
            return;
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return;
        }

        DownloadTask task = DownloadTask.createTaskForTarget(this, target);
        task.init(DownloadTask.MediaType.RESOURCE, requestUrl, config);
        task.setTag(tag);

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task);
        }
    }

    public void loadImageFromFile(IDownloadProtocol target, final String requestUrl) {
        loadImageFromFile(target, requestUrl, 0, null);
    }
    public void loadImageFromFile(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromFile(target, requestUrl, tag, null);
    }
    public void loadImageFromFile(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {
        if (requestUrl.length()==0) {
            target.onImageLoadComplete(null, tag, true);
            return;
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return;
        }

        DownloadTask task = DownloadTask.createTaskForTarget(this, target);
        task.init(DownloadTask.MediaType.FILE, requestUrl, config);
        task.setTag(tag);

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task);
        }
    }

    public void loadImageFromThumbnail(IDownloadProtocol target, final String requestUrl) {
        loadImageFromThumbnail(target, requestUrl, 0, null);
    }
    public void loadImageFromThumbnail(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromThumbnail(target, requestUrl, tag, null);
    }
    public void loadImageFromThumbnail(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {
        if (requestUrl.length()==0) {
            target.onImageLoadComplete(null, tag, true);
            return;
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return;
    }

        DownloadTask task = DownloadTask.createTaskForTarget(this, target);
        task.init(DownloadTask.MediaType.THUMB, requestUrl, config);
        task.setTag(tag);

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task);
        }
    }

    public void cancelImageDownload(IDownloadProtocol target) {
        if (target!=null) {
            target.resetDownload();
        }
    }

    public boolean isCachedForNetwork(final String requestPath) {
        return isCachedForNetwork(requestPath, null);
    }
    public boolean isCachedForNetwork(final String requestPath, DownloadConfig config) {
        String cacheKey = getCacheKeyForNetwork(requestPath, config);
        ImageCacheEntry imageEntry = _imageCache.get(cacheKey);

        return imageEntry!=null;
    }

    public boolean isFileCachedForNetwork(final String requestPath) {
        return isFileCachedForNetwork(requestPath, null);
    }
    public boolean isFileCachedForNetwork(final String requestPath, DownloadConfig config) {
        String cacheKey = getCacheKeyForNetwork(requestPath, config);
        return FileManager.getInstance().isFileEixst(FileManager.FileType.Image, cacheKey);
    }

    public String getCacheKeyForNetwork(final String requestUrl, DownloadConfig config) {
        return DownloadTask.makeCacheKey(DownloadTask.MediaType.NETWORK, requestUrl, config, null);
    }

    public boolean registCacheData(final String requestPath, byte[] data, int length, boolean memCache, boolean disckCache) {
        if (requestPath.isEmpty() || data==null || length==0) {
        return false;
    }

        if (!memCache && !disckCache) {
            return false;
        }

        DownloadTask task = DownloadTask.createTaskForTarget(this, null);
        task.init(DownloadTask.MediaType.NETWORK, requestPath, DEFAULT);

        MemoryCacheEntry cacheEntry = MemoryCacheEntry.createEntry();
        cacheEntry.appendData(data, length);

        if (memCache) {
            _memCache.put(task.getCacheKey(), cacheEntry);
        }

        if (disckCache) {
            writeToFileCache(task.getCacheKey(), cacheEntry);
        }

        return true;
    }

    public boolean registCacheImage(final String requestPath, Bitmap bmp) {
        if (requestPath.isEmpty() || bmp==null) {
            return false;
        }

        DownloadTask task = DownloadTask.createTaskForTarget(this, null);
        task.init(DownloadTask.MediaType.NETWORK, requestPath, DEFAULT);

        ImageCacheEntry imageEntry = ImageCacheEntry.createEntry(bmp);
        _imageCache.put(task.getCacheKey(), imageEntry);

        return true;
    }


    public void clearCache() {
        _imageCache.evictAll();
        _memCache.evictAll();
    }

    public boolean saveToFileCache(final String requestPath, byte[] data, int length) {
        return saveToFileCache(requestPath, data, length, null);
    }
    public boolean saveToFileCache(final String requestPath, byte[] data, int length, DownloadConfig config) {
        // not use
        return true;
    }

    protected MemoryLRUCache getMemCache() {return _memCache;}
    protected ImageLRUCache getImageCache() {return _imageCache;}

    public void queueDownloadTask(IDownloadProtocol target, final DownloadTask task) {
        if (task.getConfig().isEnableImageCache()) {
            String key = task.getCacheKey();
            ImageCacheEntry imageEntry = _imageCache.get(key);

            if (imageEntry!=null) {
                Bitmap image = imageEntry.getImage();
                if (image!=null) {
                    target.onImageLoadStart(IDownloadProtocol.DownloadStartState.IMAGE_CACHE);
                    handleState(task, State.IMAGE_CACHE_DIRECT, imageEntry);
                    return;
                } else {
                    _imageCache.remove(key);
                }
            }
    }

        if (task.getConfig().isEnableMemoryCache()) {
            MemoryCacheEntry memoryEntry = _memCache.get(task.getCacheKey());

            if (memoryEntry!=null && memoryEntry.size()>0) {
                task.setMemoryCacheEntry(memoryEntry);
            }
    }

        if (task.getMemoryCacheEntry()!=null) {
            target.onImageLoadStart(IDownloadProtocol.DownloadStartState.MEM_CACHE);
            handleState(task, State.DECODE_STARTED);
        } else {
            target.onImageLoadStart(IDownloadProtocol.DownloadStartState.DOWNLOAD);
            handleState(task, State.DOWNLOAD_STARTED);
        }
    }

    public void addDownloadTask(final PERFORM_SEL task) {
        synchronized (_downloadThreadPool) {
            _downloadThreadPool.addTask(task);
        }
    }
    public void addDecodeTask(final PERFORM_SEL task) {
//        Log.i("ImageDownloader", "[[[[[ addDecodeTask.... addtask call");
        synchronized (_decodeThreadPool) {
            _decodeThreadPool.addTask(task);
        }
    }

    public void handleState(DownloadTask task, State state) {
        handleState(task, state, null);
    }
    public void handleState(final DownloadTask task, State state, ImageCacheEntry imageEntry) {
        switch (state) {
            case DOWNLOAD_STARTED:
            {
                _mutex_download.lock();
                try {
                    switch (task.getMediaType()) {
                        case NETWORK:
                        {
                            addDecodeTask(new PERFORM_SEL() {
                                @Override
                                public void performSelector() {
                                    task.procDownloadThread();
                                }
                            });
                        }
                        break;
                        case RESOURCE:
                        {
                            addDecodeTask(new PERFORM_SEL() {
                                @Override
                                public void performSelector() {
                                    task.procLoadFromResourceThread();
                                }
                            });
                        }
                        break;
                        case FILE:
                        {
                            addDecodeTask(new PERFORM_SEL() {
                                @Override
                                public void performSelector() {
                                    task.procLoadFromFileThread();
                                }
                            });
                        }
                        break;
                        case THUMB:
                        {
                            addDecodeTask(new PERFORM_SEL() {
                                @Override
                                public void performSelector() {
                                    task.procLoadFromThumbnailThread();
                                }
                            });
                        }
                        break;
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_download.unlock();
                }
            }
            break;
            case DOWNLOAD_SUCCESS:
            {
                _mutex_download.lock();
                try {
                    if (task.getConfig().isCacheOnly() && !task.getConfig().isEnableImageCache()) {
                        if (task.isTargetAlive()) {
                            Scheduler scheduler = SMDirector.getDirector().getScheduler();
                            scheduler.performFunctionInMainThread(new PERFORM_SEL() {
                                @Override
                                public void performSelector() {
                                    if (task.isTargetAlive()) {
                                        task.getTarget().onImageCacheComplete(true, task.getTag());
                                        task.getTarget().removeDownloadTask(task);
                                    }
                                }
                            });
                        }
                    } else {
                        if (task.isTargetAlive()) {
                            handleState(task, State.DECODE_STARTED);
                        }
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_download.unlock();
                }
            }
            break;
            case DOWNLOAD_FAILED:
            {
                _mutex_download.lock();
                try {
                    ImageCacheEntry entry = task.getImageCacheEntry();
                    if (entry!=null) {
                        task.setImageCacheEntry(null);
                    }

                    if (task.isTargetAlive()) {
                        Scheduler scheduler = SMDirector.getDirector().getScheduler();
                        scheduler.performFunctionInMainThread(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                if (task.isTargetAlive()) {
                                    if (task.getConfig().isCacheOnly()) {
                                        task.getTarget().onImageCacheComplete(true, task.getTag());
                                    } else {
                                        task.getTarget().onImageLoadComplete(null, task.getTag(), false);
                                    }
                                    task.getTarget().removeDownloadTask(task);
                                }
                            }
                        });
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_download.unlock();
                }
            }
            break;
            case DECODE_STARTED:
            {
                _mutex_decode.lock();
                try {
                    if (task.isTargetAlive()) {
                        addDecodeTask(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                task.procDecodeThread();
                            }
                        });
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_decode.unlock();
                }
            }
            break;
            case DECODE_SUCCESS:
            {
                _mutex_decode.lock();
                try {
                    if (task.isTargetAlive()) {
                        Scheduler scheduler = SMDirector.getDirector().getScheduler();
                        scheduler.performFunctionInMainThread(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                ImageCacheEntry entry = task.getImageCacheEntry();
                                if (task.getConfig().isEnableImageCache()) {
                                    ImageLRUCache imageCache = task.getDownloader().getImageCache();
                                    imageCache.put(task.getCacheKey(), entry);
//                                    Log.i("ImageDownloader", "[[[[[ IMG CACHE");
                                }

                                if (task.isTargetAlive()) {
                                    if (task.getConfig().isCacheOnly()) {
                                        task.getTarget().onImageCacheComplete(true, task.getTag());
                                    } else {
                                        Bitmap bmp = entry.getImage();
                                        BitmapSprite sprite = BitmapSprite.createFromBitmap(SMDirector.getDirector(), task.getCacheKey(), bmp);

                                        task.getTarget().onImageLoadComplete(sprite, task.getTag(), false);

                                    }
                                    task.getTarget().removeDownloadTask(task);
                                }
                                task.setImageCacheEntry(null);
                            }
                        });
                    } else {
                        ImageCacheEntry entry = task.getImageCacheEntry();
                        if (task.getConfig().isEnableImageCache()) {
                            ImageLRUCache imageCache = task.getDownloader().getImageCache();
                            imageCache.put(task.getCacheKey(), entry);
//                            Log.i("ImageDownloader", "[[[[[ IMG CACHE");
                        }

                        task.setImageCacheEntry(null);
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_decode.unlock();
                }
            }
            break;
            case DECODE_FAILED:
            {
                _mutex_decode.lock();
                try {
                    if (task.isTargetAlive()) {
                        Scheduler scheduler = SMDirector.getDirector().getScheduler();
                        scheduler.performFunctionInMainThread(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                if (task.isTargetAlive()) {
                                    if (task.getConfig().isCacheOnly()) {
                                        task.getTarget().onImageCacheComplete(false, task.getTag());
                                    } else {
                                        task.getTarget().onImageLoadComplete(null, task.getTag(), false);
                                    }
                                    task.getTarget().removeDownloadTask(task);
                                }
                            }
                        });
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_decode.unlock();
                }
            }
            break;
            case IMAGE_CACHE_DIRECT:
            {
                if (task.getConfig().isCacheOnly()) {
                    task.getTarget().onImageCacheComplete(true, task.getTag());
                } else {
                    if (task.isTargetAlive()) {
                        Bitmap bmp = imageEntry.getImage();
                        BitmapSprite sprite = BitmapSprite.createFromBitmap(SMDirector.getDirector(), task.getCacheKey(), bmp);
                        task.getTarget().onImageLoadComplete(sprite, task.getTag(), false);
                    }
                }
                task.getTarget().removeDownloadTask(task);
            }
            break;
        }
    }

    public void writeToFileCache(String cacheKey, MemoryCacheEntry cacheEntry) {
        _mutex_file.lock();
        try {
            final FileCacheWriteTask task = FileCacheWriteTask.createTaskForCache(cacheKey, cacheEntry);
            synchronized (_fileCacheWriteThreadPool) {
                _fileCacheWriteThreadPool.addTask(new PERFORM_SEL() {
                    @Override
                    public void performSelector() {
                        task.procFileCacheWriteThread();
                    }
                });
            }
        } catch (Exception e) {

        } finally {
            _mutex_file.unlock();
        }
    }

    protected ImageDownloader(final int memCacheSize, final int imageCacheSize, final int downloadPoolSize, final int decodePoolSize) {
        _memCacheSize = memCacheSize;
        _imageCacheSize = imageCacheSize;
        _downloadPoolSize = downloadPoolSize;
        _decodePoolSize = decodePoolSize;
        _memCache = null;
        _imageCache = null;
        _decodeThreadPool = null;
        _downloadThreadPool = null;
        _fileCacheWriteThreadPool = null;
        init();
    }

    protected void init() {
        _memCache = new MemoryLRUCache(_memCacheSize);
        _imageCache = new ImageLRUCache(_imageCacheSize);
        _decodeThreadPool = new ImageThreadPool(_decodePoolSize);
        _downloadThreadPool = new ImageThreadPool(_downloadPoolSize);
        _fileCacheWriteThreadPool = new ImageThreadPool(1);
    }

    private static final int MEM_CACHE_SIZE = (32*1024*1024);
    private static final int IMAGE_CACHE_SIZE = (4*1080*1920);
    private static final int CORE_POOL_SIZE = 8; // thread count 8
    private static final int MAXIMUM_POOL_SIZE = 8; //

    private ImageThreadPool _downloadThreadPool = null;
    private ImageThreadPool _decodeThreadPool = null;
    private ImageThreadPool _fileCacheWriteThreadPool = null;
    private ImageThreadPool _decompressThreadPool = null;

    private MemoryLRUCache _memCache = null;
    private ImageLRUCache _imageCache = null;

    private Lock _mutex_download = new ReentrantLock(true);
    private Lock _mutex_decode = new ReentrantLock(true);
    private Lock _mutex_file = new ReentrantLock(true);
    private Lock _mutex_physics = new ReentrantLock(true);

    private int _memCacheSize;
    private int _imageCacheSize;
    private int _downloadPoolSize;
    private int _decodePoolSize;

    @Override
    public void finalize() throws Throwable {
        try {
            _decodeThreadPool.interrupt();
            _downloadThreadPool.interrupt();
            _fileCacheWriteThreadPool.interrupt();

            if (_memCache!=null) {
                _memCache.evictAll();
                _memCache = null;
            }

            if (_imageCache!=null) {
                _imageCache.evictAll();
                _imageCache = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.finalize();
        }
    }


    public final int DEFAULT_POOL_SIZE = 4;
}
