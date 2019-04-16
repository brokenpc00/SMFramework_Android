package com.interpark.smframework.util.ImageManager;

import com.interpark.smframework.base.sprite.Sprite;

import java.util.ArrayList;

public interface IDownloadProtocol {
    public enum DownloadStartState {
        DOWNLOAD,
        MEM_CACHE,
        IMAGE_CACHE
    }

    public void onImageLoadComplete(Sprite sprite, int tag, boolean direct);
    public void onImageCacheComplete(boolean success, int tag);
    //    public void onPolygonInfoComplete(Poly)
    public void onImageLoadStart(DownloadStartState state);
    public void onDataLoadComplete(byte[] data, int size, int tag);
    public void onDataLoadStart(DownloadStartState state);


    public void resetDownload();
    public void removeDownloadTask(DownloadTask task);
    public boolean isDownloadRunning(final String requestPath, int requestTag);
    public boolean addDownloadTask(DownloadTask task);

//    public ArrayList<DownloadTask> _downloadTask = new ArrayList<>();
}

/*
// must override
    // IDownloadProtocol
    private ArrayList<DownloadTask> _downloadTask = new ArrayList<>();
    @Override
    public void onImageLoadComplete(Sprite sprite, int tag, boolean direct) {
        if (sprite!=null) {

        }
    }
    @Override
    public void onImageCacheComplete(boolean success, int tag) { }
    @Override
    public void onImageLoadStart(DownloadStartState state) { }
    @Override
    public void onDataLoadComplete(byte[] data, int size, int tag) { }
    @Override
    public void onDataLoadStart(DownloadStartState state) { }
    @Override
    public void resetDownload() {
        synchronized (_downloadTask) {
            for (DownloadTask task : _downloadTask) {
                if (task.isTargetAlive()) {
                    if (task.isRunning()) {
                        task.interrupt();
                    }
                }
                task = null;
            }

            _downloadTask.clear();

        }
    }

    @Override
    public void removeDownloadTask(DownloadTask task) {
        synchronized (_downloadTask) {
            for (DownloadTask t : _downloadTask) {
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t);
                } else if (task!=null && t!=null && (t.equals(task) || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    task.interrupt();
                    _downloadTask.remove(t);
                    t = null;
                }
            }
        }
    }

    @Override
    public boolean isDownloadRunning(final String requestPath, int requestTag) {
        synchronized (_downloadTask) {
            for (DownloadTask t : _downloadTask) {
                if (t.getRequestPath().compareTo(requestPath)==0 && t.getTag()==requestTag) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean addDownloadTask(DownloadTask task) {
        synchronized (_downloadTask) {
            for (DownloadTask t : _downloadTask) {
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t);
                } else if (task!=null && t!=null && t.isRunning() && (t.equals(task) || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    return false;
                }
            }

            _downloadTask.add(task);
            return true;
        }
    }
 */