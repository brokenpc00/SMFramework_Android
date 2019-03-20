package com.interpark.smframework.network.Downloader;

import java.util.ArrayList;
import java.util.Vector;

public abstract class IDownloaderImpl {
    public interface TransferDataToBuffer {
        public int transferDataToBuffer(byte[] buffer, int len);
    }

    public interface OnTaskProgress {
        public void onTaskProgress(final DownloadTask task, long byteReceived, long totalBytesReceived, long totalBytesExpected, TransferDataToBuffer transferDataToBuffer);
    }
    public OnTaskProgress onTaskProgress=null;

    public interface OnTaskFinish {
        public void onTaskFinish(final DownloadTask task, int errorCode, int errorCodeInternal, final String errorStr, byte[] data);
    }
    public OnTaskFinish onTaskFinish=null;

    public IDownloadTask createCoTask(DownloadTask task){return null;};
}
