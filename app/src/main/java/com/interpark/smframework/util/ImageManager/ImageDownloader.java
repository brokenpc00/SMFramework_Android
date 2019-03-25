package com.interpark.smframework.util.ImageManager;

import android.os.ConditionVariable;

import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.SEL_SCHEDULE;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.cache.ImageCacheEntry;
import com.interpark.smframework.util.cache.ImageLRUCache;
import com.interpark.smframework.util.cache.MemoryCacheEntry;
import com.interpark.smframework.util.cache.MemoryLRUCache;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    }

    public void loadImageFromResource(IDownloadProtocol target, final String requestUrl) {
        loadImageFromResource(target, requestUrl, 0, null);
    }
    public void loadImageFromResource(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromResource(target, requestUrl, tag, null);
    }
    public void loadImageFromResource(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {

    }

    public void loadImageFromFile(IDownloadProtocol target, final String requestUrl) {
        loadImageFromFile(target, requestUrl, 0, null);
    }
    public void loadImageFromFile(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromFile(target, requestUrl, tag, null);
    }
    public void loadImageFromFile(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {

    }

    public void loadImageFromThumbnail(IDownloadProtocol target, final String requestUrl) {
        loadImageFromThumbnail(target, requestUrl, 0, null);
    }
    public void loadImageFromThumbnail(IDownloadProtocol target, final String requestUrl, final int tag) {
        loadImageFromThumbnail(target, requestUrl, tag, null);
    }
    public void loadImageFromThumbnail(IDownloadProtocol target, final String requestUrl, final int tag, DownloadConfig config) {

    }

    public void loadImageFromThumbnail(IDownloadProtocol target) {

    }

    public boolean isCachedForNetwork(final String requestPath) {
        return isCachedForNetwork(requestPath, null);
    }
    public boolean isCachedForNetwork(final String requestPath, DownloadConfig config) {
        return false;
    }

    public void clearCache() {

    }

    public boolean saveToFileCache(final String requestPath, byte[] data, int length) {
        return saveToFileCache(requestPath, data, length, null);
    }
    public boolean saveToFileCache(final String requestPath, byte[] data, int length, DownloadConfig config) {
        return false;
    }

    protected MemoryLRUCache getMemCache() {return _memCache;}
    protected ImageLRUCache getImageCache() {return _imageCache;}

    public void queueDownloadTask(IDownloadProtocol target, final DownloadTask task) {

    }

    public void addDownloadTask(final PERFORM_SEL task) {

    }
    public void addDecodeTask(final PERFORM_SEL task) {

    }

    public void handleState(DownloadTask task, State state) {
        handleState(task, state, null);
    }
    public void handleState(DownloadTask task, State state, ImageCacheEntry imageEntry) {

    }

    public void writeToFileCache(String cacheKey, MemoryCacheEntry cacheEntry) {

    }

    protected ImageDownloader(final int memCacheSize, final int imageCacheSize, final int downloadPoolSize, final int decodePoolSize) {

    }

    protected void init() {

    }

    private static final int MEM_CACHE_SIZE = (2*1024*1024);
    private static final int IMAGE_CACHE_SIZE = (4*1080*1920);
    private static final int CORE_POOL_SIZE = 8; // thread count 8
    private static final int MAXIMUM_POOL_SIZE = 8; //

    private ThreadPool _downloadThreadPool = null;
    private ThreadPool _decodeThreadPool = null;
    private ThreadPool _fileCacheWriteThreadPool = null;
    private ThreadPool _decompressThreadPool = null;

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



    public final int DEFAULT_POOL_SIZE = 4;

    public class ThreadPool {
        public ThreadPool(int threadCount) {
            _running = false;

            for (int i=0; i<threadCount; i++) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            threadFunc();
                        } catch (InterruptedException e) {

                        }
                    }
                });
                _workers.add(t);
            }
        }

        public void interrupt() {
            _running = false;
            _mutex.lock();
            _cond.signalAll();
            _mutex.unlock();
        }

        public void addTask(final PERFORM_SEL task) {
            _mutex.lock();
            _queue.add(task);
            _cond.notify();
            _mutex.unlock();
        }

        private void threadFunc() throws InterruptedException {
            while (true) {
                PERFORM_SEL task = null;

                _mutex.lock();
                if (!_running) {
                    break;
                }

                if (!_queue.isEmpty()) {
                    task = _queue.poll();
                } else {
                    _cond.wait();
                    if (!_running) {
                        _mutex.unlock();
                        break;
                    }
                    _mutex.unlock();
                    continue;
                }

                _mutex.unlock();

                if (task!=null) {
                    task.performSelector();
                }
            }
        }

        private final Lock _mutex = new ReentrantLock(true);
        private final Condition _cond = _mutex.newCondition();
        private ArrayList<Thread> _workers = new ArrayList<>();

        private Queue<PERFORM_SEL> _queue = new LinkedList<>();
        private boolean _running;;
    }
}
