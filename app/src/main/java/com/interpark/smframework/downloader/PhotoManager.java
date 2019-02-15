/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.interpark.smframework.downloader;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.BitmapTexture;
import com.interpark.smframework.base.texture.FileTexture;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.texture.TextureManager;
import com.interpark.smframework.util.AppUtil;
import com.interpark.smframework.util.KeyGenerateUtil;
import com.interpark.smframework.view.DownloaderView;

/**
 * This class creates pools of background threads for downloading
 * Picasa images from the web, based on URLs retrieved from Picasa's featured images RSS feed.
 * The class is implemented as a singleton; the only way to get an PhotoManager instance is to
 * call {@link #getInstance}.
 * <p>
 * The class sets the pool size and cache size based on the particular operation it's performing.
 * The algorithm doesn't apply to all situations, so if you re-use the code to implement a pool
 * of threads for your own app, you will have to come up with your choices for pool size, cache
 * size, and so forth. In many cases, you'll have to set some numbers arbitrarily and then
 * measure the impact on performance.
 * <p>
 * This class actually uses two threadpools in order to limit the number of
 * simultaneous image decoding threads to the number of available processor
 * cores.
 * <p>
 * Finally, this class defines a handler that communicates back to the UI
 * thread to change the bitmap to reflect the state.
 */
public class PhotoManager {
    /*
     * Status indicators
     */
    public static final int DOWNLOAD_FAILED = -1;
    public static final int DOWNLOAD_STARTED = 1;
    public static final int DOWNLOAD_COMPLETE = 2;
    public static final int DECODE_STARTED = 3;
    public static final int TASK_COMPLETE = 4;
    public static final int TASK_QUEUED = 5;
    public static final int TASK_NONE = 100;

    // Sets the size of the storage that's used to cache images
    private static final int IMAGE_CACHE_SIZE = 1024 * 1024 * 4;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // Sets the initial threadpool size to 8
    private static final int CORE_POOL_SIZE = 8;

    // Sets the maximum threadpool size to 8
    private static final int MAXIMUM_POOL_SIZE = 8;

    /**
     * NOTE: This is the number of total available cores. On current versions of
     * Android, with devices that use plug-and-play cores, this will return less
     * than the total number of cores. The total number of cores is not
     * available in current Android implementations.
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    public String mDiskCachePath;
    /*
     * Creates a cache of byte arrays indexed by image URLs. As new items are added to the
     * cache, the oldest items are ejected and subject to garbage collection.
     */
    private final LruCache<String, byte[]> mPhotoCache;

    // A queue of Runnables for the image download pool
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    // A queue of Runnables for the image decoding pool
    private final BlockingQueue<Runnable> mDecodeWorkQueue;

    // A queue of PhotoManager tasks. Tasks are handed to a ThreadPool.
    private final Queue<PhotoTask> mPhotoTaskWorkQueue;

    // A managed pool of background download threads
    private final ThreadPoolExecutor mDownloadThreadPool;

    // A managed pool of background decoder threads
    private final ThreadPoolExecutor mDecodeThreadPool;

    // A single instance of PhotoManager, used to implement the singleton pattern
    private static PhotoManager sInstance = null;

    // A static block that sets class fields
    static {

        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    }

    /*
     *  Creates a single static instance of PhotoManager
     */
    public static void initInstance(IDirector director) {
        if (sInstance == null) {
            sInstance = new PhotoManager(director);
        }
    }

    public static void initNewInstance(IDirector director) {
        sInstance = new PhotoManager(director);
    }

    /**
     * Constructs the work queues and thread pools used to download and decode images.
     */
    private PhotoManager(IDirector director) {

        /*
         * Creates a work queue for the pool of Thread objects used for downloading, using a linked
         * list queue that blocks when the queue is empty.
         */
        mDownloadWorkQueue = new LinkedBlockingQueue<>();

        /*
         * Creates a work queue for the pool of Thread objects used for decoding, using a linked
         * list queue that blocks when the queue is empty.
         */
        mDecodeWorkQueue = new LinkedBlockingQueue<>();

        /*
         * Creates a work queue for the set of of task objects that control downloading and
         * decoding, using a linked list queue that blocks when the queue is empty.
         */
        mPhotoTaskWorkQueue = new LinkedBlockingQueue<>();

        /*
         * Creates a new pool of Thread objects for the download work queue
         */
        mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        /*
         * Creates a new pool of Thread objects for the decoding work queue
         */
        mDecodeThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDecodeWorkQueue);

        // Instantiates a new cache based on the cache size estimate
        mPhotoCache = new LruCache<String, byte[]>(IMAGE_CACHE_SIZE) {

            /*
             * This overrides the default sizeOf() implementation to return the
             * correct size of each cache entry.
             */

            @Override
            protected int sizeOf(String paramPath, byte[] paramArrayOfByte) {
                return paramArrayOfByte.length;
            }
        };

        File diskCacheDirectory = AppUtil.getExternalFilesDir(director.getContext(), "network_cache");
        mDiskCachePath = diskCacheDirectory.getAbsolutePath();
    }

    public void handleMessage(final int message, final PhotoTask photoTask) {

        photoTask.getDirector().runOnDraw(new Runnable() {
            @Override
            public void run() {
                // Sets an PhotoView that's a weak reference to the
                // input ImageView
                DownloaderView localView = photoTask.getPhotoView();

                // If this input view isn't null
                if (localView != null) {
                    /*
                     * Gets the URL of the *weak reference* to the input
                     * ImageView. The weak reference won't have changed, even if
                     * the input ImageView has.
                     */
//                    String localPath = localView.getImagePath();
                    String localPath = "";
                    /*
                     * Compares the URL of the input ImageView to the URL of the
                     * weak reference. Only updates the bitmap in the ImageView
                     * if this particular Thread is supposed to be serving the
                     * ImageView.
                     */
                    if (photoTask.getImagePath() == localPath) {

                        /*
                         * Chooses the action to take, based on the incoming message
                         */
                        switch (message) {

                            // If the download has started, sets background color to dark green
                            case DOWNLOAD_STARTED:
                                localView.setStatus(DOWNLOAD_STARTED);
                                break;
                            /*
                             * If the download is complete, but the decode is waiting, sets the
                             * background color to golden yellow
                             */
                            case DOWNLOAD_COMPLETE:
                                // Sets background color to golden yellow
                                localView.setStatus(DOWNLOAD_COMPLETE);
                                break;
                            // If the decode has started, sets background color to orange
                            case DECODE_STARTED:
                                localView.setStatus(DECODE_STARTED);
                                break;
                            /*
                             * The decoding is done, so this sets the
                             * ImageView's bitmap to the bitmap in the
                             * incoming message
                             */
                            case TASK_COMPLETE:
                                localView.setStatus(TASK_COMPLETE);

                                Texture texture = getExistTexture(photoTask);
                                if (texture == null) {
                                    texture = createTexture(photoTask);
                                }

                                if (texture != null) {
                                    Sprite sprite = new Sprite(photoTask.getDirector(), texture, texture.getWidth() / 2, texture.getHeight() / 2);
                                    localView.setSprite(sprite);
                                }

                                recycleTask(photoTask);
                                break;
                            // The download failed, sets the background color to dark red
                            case DOWNLOAD_FAILED:
                                localView.setStatus(DOWNLOAD_FAILED);

                                // Attempts to re-use the Task object
                                recycleTask(photoTask);
                                break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Returns the PhotoManager object
     * @return The global PhotoManager object
     */
    public static PhotoManager getInstance() {
        return sInstance;
    }

    /**
     * Handles state messages for a particular task object
     * @param photoTask A task object
     * @param state The state of the task
     */
    public void handleState(PhotoTask photoTask, int state) {
        switch (state) {

            // The task finished downloading and decoding the image
            case TASK_COMPLETE:

                // Puts the image into cache
                if (photoTask.isCacheEnabled()) {
                    // If the task is set to cache the results, put the buffer
                    // that was
                    // successfully decoded into the cache
                    mPhotoCache.put(photoTask.getImagePath(), photoTask.getByteBuffer());
                }

                // Gets a Message object, stores the state in it, and sends it to the Handler
                handleMessage(state, photoTask);
                break;

            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:
                /*
                 * Decodes the image, by queuing the decoder object to run in the decoder
                 * thread pool
                 */
                mDecodeThreadPool.execute(photoTask.getPhotoDecodeRunnable());

                // In all other cases, pass along the message without any other action.
            default:
                handleMessage(state, photoTask);
                break;
        }
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    public static void cancelAll() {

        /*
         * Creates an array of tasks that's the same size as the task work queue
         */
        PhotoTask[] taskArray = new PhotoTask[sInstance.mDownloadWorkQueue.size()];

        // Populates the array with the task objects in the queue
        sInstance.mDownloadWorkQueue.toArray(taskArray);

        // Stores the array length in order to iterate over the array
        int taskArraylen = taskArray.length;

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads, then
         * iterates over the array of tasks and interrupts the task's current Thread.
         */
        synchronized (sInstance) {

            // Iterates over the array of tasks
            for (int taskArrayIndex = 0; taskArrayIndex < taskArraylen; taskArrayIndex++) {

                // Gets the task's current thread
                Thread thread = taskArray[taskArrayIndex].mThreadThis;

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * Stops a download Thread and removes it from the threadpool
     *
     * @param downloaderTask The download task associated with the Thread
     * @param imagePath The URL being downloaded
     */
    static public void removeDownload(PhotoTask downloaderTask, String imagePath) {

        // If the Thread object still exists and the download matches the specified URL
        if (downloaderTask != null && downloaderTask.getImagePath().equals(imagePath)) {

            /*
             * Locks on this class to ensure that other processes aren't mutating Threads.
             */
            synchronized (sInstance) {

                // Gets the Thread that the downloader task is running on
                Thread thread = downloaderTask.getCurrentThread();

                // If the Thread exists, posts an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
            /*
             * Removes the download Runnable from the ThreadPool. This opens a Thread in the
             * ThreadPool's work queue, allowing a task in the queue to start.
             */
            sInstance.mDownloadThreadPool.remove(downloaderTask.getHTTPDownloadRunnable());
        }
    }

    /**
     * Starts an image download and decode
     *
     * @param imageView The ImageView that will get the resulting Bitmap
     * @param cacheFlag Determines if caching should be used
     * @return The task instance that will handle the work
     */
    static public PhotoTask startDownload(IDirector director, DownloaderView imageView, boolean cacheFlag, int width, int height, int degrees) {

        /*
         * Gets a task from the pool of tasks, returning null if the pool is empty
         */
        PhotoTask downloadTask = sInstance.mPhotoTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new PhotoTask(director);
        }
        // Set current direcotr
        downloadTask.setDirector(director);

        // Initializes the task
        downloadTask.initializeDownloaderTask(PhotoManager.sInstance, imageView, cacheFlag, width, height, degrees);


        Texture texture = getExistTexture(downloadTask);
        if (texture != null) {
            Sprite sprite = new Sprite(director, texture, texture.getWidth() / 2, texture.getHeight() / 2);
            imageView.setSprite(sprite);
            sInstance.recycleTask(downloadTask);
            return null;
        } else {

            /*
             * Provides the download task with the cache buffer corresponding to the URL to be
             * downloaded.
             */
            downloadTask.setByteBuffer(sInstance.mPhotoCache.get(downloadTask.getImagePath()));

            // If the byte buffer was empty, the image wasn't cached
            if (null == downloadTask.getByteBuffer()) {

                /*
                 * "Executes" the tasks' download Runnable in order to download the image. If no
                 * Threads are available in the thread pool, the Runnable waits in the queue.
                 */
                sInstance.mDownloadThreadPool.execute(downloadTask.getHTTPDownloadRunnable());

                // Sets the display to show that the image is queued for downloading and decoding.
                imageView.setStatus(TASK_QUEUED);

                // The image was cached, so no download is required.
            } else {

                /*
                 * Signals that the download is "complete", because the byte array already contains the
                 * undecoded image. The decoding starts.
                 */

                sInstance.handleState(downloadTask, DOWNLOAD_COMPLETE);
            }
        }

        // Returns a task object, either newly-created or one from the task pool
        return downloadTask;
    }

    /**
     * Recycles tasks by calling their internal recycle() method and then putting them back into
     * the task queue.
     * @param downloadTask The task to recycle
     */
    void recycleTask(PhotoTask downloadTask) {

        // Frees up memory in the task
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mPhotoTaskWorkQueue.offer(downloadTask);
    }

    public static String getCachePath(String inputPath) {
        return sInstance.mDiskCachePath + File.separator + KeyGenerateUtil.generate(inputPath);
    }

    private static Texture getExistTexture(PhotoTask downloadTask) {
        // 텍스처가 이미 있는지 확인
        TextureManager textureManager = downloadTask.getDirector().getTextureManager();
        Texture texture = null;

        switch(downloadTask.getMediaType()) {
            case Constants.MEDIA_ASSETS:
                texture = textureManager.getTextureFromAssets(downloadTask.getImagePath());
                break;
            case Constants.MEDIA_SDCARD:
                texture = textureManager.getTextureFromFile(downloadTask.getImagePath());
                break;
            default:
            case Constants.MEDIA_NETWORK:
                texture = textureManager.getTextureFromNetwork(downloadTask.getImagePath());
                break;
        }

        return texture;
    }

    private static Texture createTexture(PhotoTask downloadTask) {
        // 텍스처가 이미 있는지 확인
        TextureManager textureManager = downloadTask.getDirector().getTextureManager();
        Texture texture = null;

        switch(downloadTask.getMediaType()) {
            case Constants.MEDIA_ASSETS:
                texture = textureManager.getTextureFromAssets(downloadTask.getImagePath());
                break;
            case Constants.MEDIA_SDCARD:
                texture = textureManager.getTextureFromFile(downloadTask.getImagePath());
                break;
            default:
            case Constants.MEDIA_NETWORK:
                texture = textureManager.getTextureFromNetwork(downloadTask.getImagePath());
                break;
        }

        if (texture != null) {
            return texture;
        }

        final Bitmap bitmap = downloadTask.getImage();
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        final String tempKey = "DOWNLOADER_TEMP";
        BitmapTexture fakeTexture = (BitmapTexture)textureManager.createTextureFromBitmap(bitmap, tempKey);
        fakeTexture.updateTexture(downloadTask.getDirector(), bitmap);

        downloadTask.setImage(null);
        bitmap.recycle();

        switch(downloadTask.getMediaType()) {
            case Constants.MEDIA_ASSETS:
                texture = textureManager.createFakeAssetsTexture(downloadTask.getImagePath(), false, null, fakeTexture);
                break;
            case Constants.MEDIA_SDCARD:
                texture = textureManager.createFakeFileTexture(downloadTask.getImagePath(), false, null,
                        downloadTask.getTargetDegrees(),
                        Math.max(downloadTask.getTargetWidth(), downloadTask.getTargetHeight()),
                        fakeTexture);

                break;
            default:
            case Constants.MEDIA_NETWORK:
                texture = textureManager.createFakeFileTexture(downloadTask.getDiskCachePath(), false, null, 0,
                        Math.max(downloadTask.getTargetWidth(), downloadTask.getTargetHeight()),
                        fakeTexture);
                ((FileTexture)texture).setWebPFormat();
                break;
        }
        textureManager.removeFakeTexture(fakeTexture);

        return texture;
    }
}
