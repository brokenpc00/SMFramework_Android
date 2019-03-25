package com.interpark.smframework.util.ImageManager;

import com.interpark.smframework.base.sprite.BitmapSprite;

public interface IDownloadProtocol {
    public enum DownloadStartState {
        DOWNLOAD,
        MEM_CACHE,
        IMAGE_CACHE
    }

    public void resetDownload();
    public void onImageLoadComplete(BitmapSprite sprite, int tag, boolean direct);
    public void onImageCacheComplete(boolean success, int tag);
    //    public void onPolygonInfoComplete(Poly)
    public void onImageLoadStart(DownloadStartState state);
    public void onDataLoadComplete(byte[] data, int size, int tag);
    public void onDataLoadStart(DownloadStartState state);


    public void removeDownloadTask(DownloadTask task);
    public boolean isDownloadRunning(final String requestPath, int requestTag);
    public boolean addDownloadTask(DownloadTask task);
}
