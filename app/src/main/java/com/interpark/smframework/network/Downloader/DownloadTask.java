package com.interpark.smframework.network.Downloader;

public final class DownloadTask {
    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_INVALID_PARAMS = -1;
    public static final int ERROR_FILE_OP_FAILED = -2;
    public static final int ERROR_IMPL_INTERNAL = -3;

    public String identifier;
    public String requestURL;
    public String storagePath;

    public DownloadTask() {

    }

    // mine.
    public IDownloadTask _coTask;
}
