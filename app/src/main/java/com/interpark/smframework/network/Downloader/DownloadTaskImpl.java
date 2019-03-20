package com.interpark.smframework.network.Downloader;

public class DownloadTaskImpl extends IDownloadTask {
    private static int sTaskCounter = 0;

    public DownloadTaskImpl() {
        id = ++sTaskCounter;
    }

    public int id;
    public DownloadTask task = null;
}
