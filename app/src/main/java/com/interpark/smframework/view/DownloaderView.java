package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base._UIContainerView;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.downloader.PhotoManager;
import com.interpark.smframework.downloader.PhotoTask;

import java.util.ArrayList;

public class DownloaderView extends _UIContainerView {
    public DownloaderView(IDirector director) {
        super(director);
    }

    private int mStatus = PhotoManager.TASK_NONE;

    private Sprite mSprite;

    // Indicates if caching should be used
    private boolean mCacheFlag;

    // Status flag that indicates if onDraw has completed
    private boolean mIsDrawn;

    private int mMdeiaType = 0;

    // The URL that points to the source of the image for this ImageView
    private String mImagePath;
    private String mDiskCachePath;

    // The Thread that will be used to download the image for this ImageView
    private PhotoTask mDownloadThread;

    // 아이템
    private ArrayList<DownloaderView> mSiblingView;

    private int mImageWidth;
    private int mImageHeight;
    private int mImageDegrees;

    public final int getMediaType() {
        return mMdeiaType;
    }

    public final String getImagePath() {
        return mImagePath;
    }

    public final String getDiskCachePath() {
        return mDiskCachePath;
    }

    private void releaseResource() {
        if (mSiblingView != null) {
            for (DownloaderView view : mSiblingView) {
                if (view != null) {
                    view.releaseResource();
                }
            }
            mSiblingView.clear();
            mSiblingView = null;
        }
        setImagePath(0, null, false, null, 0, 0, 0);
        setSprite(null);
        mDownloadThread = null;
    }

    @Override
    public void onDestoryView() {
        super.onDestoryView();
        releaseResource();
    }

    @Override
    protected void onRemoveFromParent(SMView parent) {
        super.onRemoveFromParent(parent);
        releaseResource();
    }

    /*
     * This callback is invoked when the system tells the View to draw itself. If the View isn't
     * already drawn, and its URL isn't null, it invokes a Thread to download the image. Otherwise,
     * it simply passes the existing Canvas to the super method
     */
    @Override
    public void render(float a) {
        super.render(a);

        if ((!mIsDrawn) && (mImagePath != null)) {

            // Starts downloading this View, using the current cache setting
            mDownloadThread = PhotoManager.startDownload(getDirector(), this, mCacheFlag, mImageWidth, mImageHeight, mImageDegrees);

            // After successfully downloading the image, this marks that it's available.
            mIsDrawn = true;
        }
    }
//    public void updateFrame() {
//        super.updateFrame();
//        if ((!mIsDrawn) && (mImagePath != null)) {
//
//            // Starts downloading this View, using the current cache setting
//            mDownloadThread = PhotoManager.startDownload(getDirector(), this, mCacheFlag, mImageWidth, mImageHeight, mImageDegrees);
//
//            // After successfully downloading the image, this marks that it's available.
//            mIsDrawn = true;
//        }
//    }

    /**
     * Attempts to set the picture URL for this ImageView and then download the picture.
     * <p>
     * If the picture URL for this view is already set, and the input URL is not the same as the
     * stored URL, then the picture has moved and any existing downloads are stopped.
     * <p>
     * If the input URL is the same as the stored URL, then nothing needs to be done.
     * <p>
     * If the stored URL is null, then this method starts a download and decode of the picture
     */
    public void setImagePath(int mediaType, String imagePath, boolean cacheFlag, String diskCachePath, int width, int height, int degrees) {
        mStatus = PhotoManager.TASK_NONE;

        if (mImagePath != null && !mImagePath.equals(imagePath)) {
            PhotoManager.removeDownload(mDownloadThread, mImagePath);
        }

        // If the picture URL for this ImageView is already set
        if (imagePath != null) {

            // If the stored URL doesn't match the incoming URL, then the picture has changed.
            if (!imagePath.equals(mImagePath)) {
                // Stops any ongoing downloads for this ImageView
                PhotoManager.removeDownload(mDownloadThread, imagePath);
            } else {
                // The stored URL matches the incoming URL. Returns without doing any work.
                return;
            }
        } else if (mImagePath != null) {
            PhotoManager.removeDownload(mDownloadThread, mImagePath);
        }

        // Stores the picture URL for this ImageView
        mImagePath = imagePath;
        mDiskCachePath = diskCachePath;

        mMdeiaType = mediaType;
        mImageWidth = width;
        mImageHeight = height;
        mImageDegrees = degrees;

        // If the draw operation for this ImageVIew has completed, and the picture URL isn't empty
        if ((mIsDrawn) && (imagePath != null)) {

            // Sets the cache flag
            mCacheFlag = cacheFlag;

            /*
             * Starts a download of the picture file. Notice that if caching is on, the picture
             * file's contents may be taken from the cache.
             */
            mDownloadThread = PhotoManager.startDownload(getDirector(), this, cacheFlag, width, height, degrees);
        }
    }

    public void clearSiblings() {
        if (mSiblingView == null || mSiblingView.size() <= 0)
            return;

        int numSiblings = mSiblingView.size();
        for (int i = numSiblings-1; i >= 0; i--) {
            DownloaderView view = mSiblingView.get(i);
            if (view != null) {
                view.setImagePath(0, null, false, null, 0, 0, 0);
            }
            mSiblingView.remove(i);
        }
        mSiblingView.clear();
    }

    public void setSiblingImagePath(int index, int mediaType, String imagePath, boolean cacheFlag, String diskCachePath, int width, int height, int degrees) {
        if (imagePath == null)
            return;

        if (mSiblingView == null) {
            mSiblingView = new ArrayList<>();
        }

        DownloaderView view;
        if (mSiblingView.size() > index) {
            view = mSiblingView.get(index);
        } else {
            view = new DownloaderView(getDirector());
            mSiblingView.add(view);
        }
        if (view != null) {
            view.setImagePath(mediaType, imagePath, cacheFlag, diskCachePath, width, height, degrees);
        }
    }

    public void setStatus(int status) {
        mStatus = status;

        switch (status) {
            case PhotoManager.TASK_COMPLETE:
//                if (mSiblingView != null) {
//                    for (DownloaderView view : mSiblingView) {
//                        view.updateFrame();
//                    }
//                }
                break;
        }

    }

    public void setSprite(Sprite sprite) {
        if (sprite != null) {
            if (mSprite != null && mSprite.getTexture() != sprite.getTexture()) {
                mSprite.removeTexture();
            }
        } else {
            if (mSprite != null) {
                mSprite.removeTexture();
            }
        }
        mSprite = sprite;
    }

    public Sprite getSprite() {
        return mSprite;
    }

    public boolean isSiblingViewComplete() {
        if (mSiblingView == null || mSiblingView.size() <= 0)
            return false;

        int numView = mSiblingView.size();
        for (int i = 0; i < numView; i++) {
            DownloaderView view = mSiblingView.get(i);
            if (view != null && view.mStatus != PhotoManager.TASK_COMPLETE)
                return false;
        }

        return true;
    }

    public int getSiblinSpriteCount() {
        if (mSiblingView != null) {
            return mSiblingView.size();
        }
        return 0;
    }

    public Sprite getSiblinSprite(int index) {
        if (mSiblingView != null && mSiblingView.size() > index && mSiblingView.get(index) != null) {
            return mSiblingView.get(index).mSprite;
        }
        return null;
    }

//    public void startSiblingDownload() {
//        if (mSiblingView != null) {
//            for (DownloaderView view : mSiblingView) {
//                if (view != null) {
//                    view.updateFrame();
//                }
//            }
//        }
//    }
}
