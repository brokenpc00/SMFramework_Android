package com.interpark.smframework.network.Downloader;

public final class Downloader {
    public Downloader(){
        DownloaderHints hints = new DownloaderHints();
        hints.countOfMaxProcessingTasks = 6;
        hints.timeoutInSeconds = 45;
        hints.tempFileNameSuffix = ".tmp";
        set(hints);
    }
    public Downloader(final DownloaderHints hints) {
        this();
        set(hints);
    }

    public void set(final DownloaderHints hints) {
        _impl = new DownloaderImpl(hints);

        _impl.onTaskProgress = new IDownloaderImpl.OnTaskProgress() {
            @Override
            public void onTaskProgress(DownloadTask task, long byteReceived, long totalBytesReceived, long totalBytesExpected, IDownloaderImpl.TransferDataToBuffer transferDataToBuffer) {
                if (_onTaskProgress!=null) {
                    _onTaskProgress.onTaskProgress(task, byteReceived, totalBytesReceived, totalBytesExpected);
                }
            }
        };

        _impl.onTaskFinish = new IDownloaderImpl.OnTaskFinish() {
            @Override
            public void onTaskFinish(DownloadTask task, int errorCode, int errorCodeInternal, String errorStr, byte[] data) {
                if (DownloadTask.ERROR_NO_ERROR != errorCode) {
                    if (_onTaskError!=null) {
                        _onTaskError.onTaskError(task, errorCode, errorCodeInternal, errorStr);
                    }
                    return;
                }

                if (task.storagePath.length()>0) {
                    if (_onFileTaskSuccess!=null) {
                        _onFileTaskSuccess.onFileTaskSuccess(task);
                    }
                } else {
                    if (_onDataTaskSuccess!=null) {
                        _onDataTaskSuccess.onDataTaskSuccess(task, data);
                    }
                }
            }
        };
    }

    public interface OnDataTaskSuccess {
        public void onDataTaskSuccess(final DownloadTask task, byte[] data);
    }
    public OnDataTaskSuccess _onDataTaskSuccess = null;

    public interface OnFileTaskSuccess {
        public void onFileTaskSuccess(final DownloadTask task);
    }
    public OnFileTaskSuccess _onFileTaskSuccess = null;

    public interface OnTaskProgress {
        public void onTaskProgress(final DownloadTask task, long bytesReceived, long totalBytesReceived, long totalBytesExpected);
    }
    public OnTaskProgress _onTaskProgress = null;

    public interface OnTaskError {
        public void onTaskError(final DownloadTask task, int errorCode, int errorCodeInteral, final String errorStr);
    }
    public OnTaskError _onTaskError = null;

    public void setOnFileTaskSuccess(final OnFileTaskSuccess callback) {_onFileTaskSuccess = callback;}

    public void setOnTaskProgress(final OnTaskProgress callback) {_onTaskProgress = callback;}

    public void setOnTaskError(final OnTaskError callback) {_onTaskError = callback;}

    public DownloadTask createDownloadDataTask(final String srcUrl) {
        return createDownloadDataTask(srcUrl, "");
    }
    public DownloadTask createDownloadDataTask(final String srcUrl, final String identifier) {
        DownloadTask task = new DownloadTask();
        do {
            task.requestURL = srcUrl;
            task.identifier = identifier;

            if (0==srcUrl.length()) {
                if (_onTaskError!=null) {
                    _onTaskError.onTaskError(task, DownloadTask.ERROR_INVALID_PARAMS, 0, "URL is Empty.");
                }
                break;
            }

            task._coTask = _impl.createCoTask(task);
        } while (false);

        return task;
    }

    public DownloadTask createDownloadFileTask(final String srcUrl, final String storagePath) {
        return createDownloadFileTask(srcUrl, storagePath, "");
    }
    public DownloadTask createDownloadFileTask(final String srcUrl, final String storagePath, final String idientifier) {
        DownloadTask task = new DownloadTask();
        do {
            task.requestURL = srcUrl;
            task.storagePath = storagePath;
            task.identifier = idientifier;
            if (0==srcUrl.length() || 0==storagePath.length()) {
                if (_onTaskError!=null) {
                    _onTaskError.onTaskError(task, DownloadTask.ERROR_INVALID_PARAMS, 0, "URL or Storage Path is Empty.");
                }
                break;
            }
            task._coTask = _impl.createCoTask(task);
        } while (false);

        return task;
    }

    private IDownloaderImpl _impl;
 }
