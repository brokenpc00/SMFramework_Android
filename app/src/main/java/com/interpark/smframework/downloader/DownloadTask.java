package com.interpark.smframework.downloader;

import android.graphics.Bitmap;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.downloader.DownloadRunnable.DownloadRunnableTaskMethods;
import com.interpark.smframework.downloader.PhotoDecodeRunnable.TaskRunnableDecodeMethods;
import com.interpark.smframework.view.DownloaderView;

import java.lang.ref.WeakReference;

public class DownloadTask implements DownloadRunnableTaskMethods, TaskRunnableDecodeMethods {
    // for Image download
    private WeakReference<DownloaderView> mImageWeakRef;

    private IDirector _director;
    private int mMediaType;
    private String mFilePath;
    private String mDiskCachePath;

    // For Image
    private int mTargetHeight;
    private int mTargetWidth;
    private int mTargetDegrees;

    private boolean mCacheEnabled;

    Thread mThreadThis;

    private Runnable mDownloadRunnable;
    // for image
    private Runnable mDecodeRunnable;

    byte[] mDataBuffer;

    private Bitmap mDecodedImage;

    private Thread mCurrentThread;


    private static DownloadManager sDownloadManager;

    DownloadTask(IDirector director) {
        _director = director;

        mDownloadRunnable = new DownloadRunnable(director, this);
        mDecodeRunnable = new PhotoDecodeRunnable(this);
//        sDownloadManager = DownloadManager.getInstance();
    }

    public IDirector getDirector() {
        return _director;
    }

    public void setDirector(IDirector director) {
        _director = director;
    }

    void initializeDownloaderTask(
            DownloadManager downloadManager,
            DownloaderView photoView,
            boolean cacheFlag, int width, int height, int degrees)
    {
        // Sets this object's ThreadPool field to be the input argument
        sDownloadManager = downloadManager;

        // Gets the URL for the View
        mMediaType = photoView.getMediaType();
        mFilePath = photoView.getImagePath();
        mDiskCachePath = photoView.getDiskCachePath();

        // Instantiates the weak reference to the incoming view
        mImageWeakRef = new WeakReference<>(photoView);

        // Sets the cache flag to the input argument
        mCacheEnabled = cacheFlag;

        // Gets the width and height of the provided ImageView
        mTargetWidth = width;
        mTargetHeight = height;
        mTargetDegrees = degrees;
    }

    @Override
    public byte[] getByteBuffer() {

        // Returns the global field
        return mDataBuffer;
    }

    void recycle() {

        // for Image download view

        // Deletes the weak reference to the imageView
        if ( null != mImageWeakRef ) {
            mImageWeakRef.clear();
            mImageWeakRef = null;
        }

        if (mDecodedImage != null && !mDecodedImage.isRecycled()) {
            mDecodedImage.recycle();
        }

        // Releases references to the byte buffer and the BitMap
        mDataBuffer = null;
        mDecodedImage = null;
    }

    @Override
    public int getTargetWidth() {
        return mTargetWidth;
    }

    @Override
    public int getTargetHeight() {
        return mTargetHeight;
    }

    @Override
    public int getTargetDegrees() {
        return mTargetDegrees;
    }

    boolean isCacheEnabled() {
        return mCacheEnabled;
    }

    @Override
    public int getStorageMediaType() {
        return mMediaType;
    }

    @Override
    public int getMediaType() {
        return mMediaType;
    }

    @Override
    public String getDownloadedPath() {
        return mFilePath;
    }

    @Override
    public String getDiskCachePath() {
        return mDiskCachePath;
    }

    @Override
    public void setByteBuffer(byte[] dataBuffer) {
        mDataBuffer = dataBuffer;
    }


    void handleState(int state) {
//        sDownloadManager.handleState(this, state);
    }

    Bitmap getImage() {
        return mDecodedImage;
    }

    Runnable getHTTPDownloadRunnable() {
        return mDownloadRunnable;
    }

    Runnable getPhotoDecodeRunnable() {
        return mDecodeRunnable;
    }

    // for image
    public DownloaderView getPhotoView() {
        if ( null != mImageWeakRef ) {
            return mImageWeakRef.get();
        }
        return null;
    }

    public Thread getCurrentThread() {
        synchronized(sDownloadManager) {
            return mCurrentThread;
        }
    }

    public void setCurrentThread(Thread thread) {
        synchronized(sDownloadManager) {
            mCurrentThread = thread;
        }
    }

    @Override
    public void setImage(Bitmap decodedImage) {
        mDecodedImage = decodedImage;
    }

    @Override
    public void setDownloadThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }

    @Override
    public void handleDownloadState(int state) {
        int outState;

        // Converts the download state to the overall state
        switch(state) {
            case DownloadRunnable.HTTP_STATE_COMPLETED: // download 끝났음.
                outState = DownloadManager.DOWNLOAD_COMPLETE;
                break;
            case DownloadRunnable.HTTP_STATE_FAILED:    // download 실패
                outState = DownloadManager.DOWNLOAD_FAILED;
                break;
            default:
                outState = DownloadManager.DOWNLOAD_STARTED;   // download 시작
                break;
        }
        // Passes the state to the ThreadPool object.
        handleState(outState);
    }

    @Override
    public void setImageDecodeThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }



    @Override
    public void handleDecodeState(int state) {
        int outState;

        // Converts the decode state to the overall state.
        switch(state) {
            case PhotoDecodeRunnable.DECODE_STATE_COMPLETED:
                outState = DownloadManager.TASK_COMPLETE;
                break;
            case PhotoDecodeRunnable.DECODE_STATE_FAILED:
                outState = DownloadManager.DOWNLOAD_FAILED;
                break;
            default:
                outState = DownloadManager.DECODE_STARTED;
                break;
        }

        // Passes the state to the ThreadPool object.
        handleState(outState);
    }










}
