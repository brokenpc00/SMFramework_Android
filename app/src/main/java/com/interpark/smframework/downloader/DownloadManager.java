package com.interpark.smframework.downloader;

import android.support.v4.util.LruCache;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.AppUtil;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    public static final int DOWNLOAD_FAILED = -1;
    public static final int DOWNLOAD_STARTED = 1;
    public static final int DOWNLOAD_COMPLETE = 2;
    // image의 경우 download 끝나고 decode까지
    public static final int DECODE_STARTED = 3;
    public static final int TASK_COMPLETE = 4;
    public static final int TASK_QUEUED = 5;
    public static final int TASK_NONE = 100;


    private static final int DOWNLOAD_CACHE_SIZE = 1024 * 1024 * 10;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // thread는 4개만 돌리자..
    private static final int CORE_POOL_SIZE = 4;
    // thread 최대는 8개
    private static final int MAXIMUM_POOL_SIZE = 8;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    public String mDiskCachePath;

    // image의 경우 LRU Cache를 사용
    private final LruCache<String, byte[]> mPhotoCache;

    // download queue
    private final BlockingQueue<Runnable> mDownloadBlockingQueue;

    // image의 경우 decode 하는 queue
    private final BlockingQueue<Runnable> mDecodeBlockingQueue;


    private final Queue<DownloadTask> mDownloadTaskWorkQueue;

    private final ThreadPoolExecutor mDownloadThreadPool;

    private final ThreadPoolExecutor mDecodeThreadPool;

    private static DownloadManager sInstance = null;

    static {
        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    }

    public static void initInstance(IDirector director) {
        if (sInstance == null) {
            sInstance = new DownloadManager(director);
        }
    }

    public static void initNewInstance(IDirector director) {
        sInstance = new DownloadManager(director);
    }


    private DownloadManager(IDirector director) {
        mDownloadBlockingQueue = new LinkedBlockingQueue<>();

        mDecodeBlockingQueue = new LinkedBlockingQueue<>();

        mDownloadTaskWorkQueue= new LinkedBlockingQueue<>();

        mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadBlockingQueue);

        mDecodeThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDecodeBlockingQueue);


        mPhotoCache = new LruCache<String, byte[]>(DOWNLOAD_CACHE_SIZE) {
            @Override
            protected int sizeOf(String paramPath, byte[] paramArrayOfByte) {
                return paramArrayOfByte.length;
            }
        };

        File diskCacheDirectory = AppUtil.getExternalFilesDir(director.getContext(), "network_cache");
        mDiskCachePath = diskCacheDirectory.getAbsolutePath();
    }

    // start, complete, failed 등 handle 처리
    public void handleMessage(final int message, final DownloadTask downloadTask) {

    }




}
