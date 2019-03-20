package com.interpark.smframework.network.Downloader;

import java.util.HashMap;
import java.util.Map;

public class DownloaderImpl extends IDownloaderImpl implements AndroidDownloader.OnDownloadFinish, AndroidDownloader.OnDownloadProgress {
    public DownloaderImpl(final DownloaderHints hitns) {
        _id = ++sDownloaderCounter;

        _impl = AndroidDownloader.createDownloader(_id, hitns);
        _impl._onDownloadFinish = this;
        _impl._onDownloadProgress = this;

        _insertDownloader(_id, this);
    }

    public void cancelAllRequest() {
        AndroidDownloader.cancelAllRequests(_impl);
        _eraseDownloader(_id);
    }

    private static HashMap<Integer, DownloaderImpl> sDownloaderMap = new HashMap<>();

    private static void _insertDownloader(int id, DownloaderImpl downloader) {
        synchronized (sDownloaderMap) {
            sDownloaderMap.put(id, downloader);
        }
    }

    private static void _eraseDownloader(int id) {
        synchronized (sDownloaderMap) {
            sDownloaderMap.remove(id);
        }
    }

    private static DownloaderImpl _findDownloader(int id) {
        synchronized (sDownloaderMap) {
            return sDownloaderMap.get(id);
        }
    }

    private static int sDownloaderCounter = 0;


    @Override
    public IDownloadTask createCoTask(final DownloadTask task) {
        DownloadTaskImpl coTask = new DownloadTaskImpl();
        coTask.task = task;

        AndroidDownloader.createTask(_impl, coTask.id, task.requestURL, task.storagePath);

        _taskMap.put(coTask.id, coTask);
        return coTask;
    }

    public TransferDataToBuffer _transferDataToBuffer = null;

    public void _onProcess(int taskId, long dl, long dlNow, long dlTotal) {
        DownloadTaskImpl coTask = _taskMap.get(taskId);
        if (coTask==null) {
            return;
        }

        onTaskProgress.onTaskProgress(coTask.task, dl, dlNow, dlTotal, _transferDataToBuffer);
    }



    public void _onFinish(int taskId, int errCode, final String errStr, byte[] data) {
        DownloadTaskImpl coTask = _taskMap.get(taskId);
        if (coTask==null) {
            return;
        }

        _taskMap.remove(taskId);

        onTaskFinish.onTaskFinish(coTask.task, errStr!=""?DownloadTask.ERROR_IMPL_INTERNAL:DownloadTask.ERROR_NO_ERROR, errCode, errStr, data);
    }

//    public static void nativeOnProgress(int id, int taskId, long dl, long dlNow, long dlTotal) {
//        DownloaderImpl downloader = _findDownloader(id);
//        if (downloader==null) {
//            return;
//        }
//
//        downloader._onProcess(taskId, dl, dlNow, dlTotal);
//    }
//
//    public static void nativeOnFinish(int id, int taskId, int errCode, String errStr, byte[] data) {
//        DownloaderImpl downloader = _findDownloader(id);
//        if (downloader==null) {
//            return;
//        }
//
//        if (errStr!=null) {
//            downloader._onFinish(taskId, errCode, errStr, data);
//            return;
//        } else {
//            downloader._onFinish(taskId, errCode, "", data);
//        }
//    }

    protected int _id;

    protected HashMap<Integer, DownloadTaskImpl> _taskMap = new HashMap<>();

    private AndroidDownloader _impl = null;

    public interface OnDownloadProgress {
        public void onDownloadProgress(int id, int taskId, long dl, long dlnow, long dltotal);
    }
    public OnDownloadProgress _onDownloadProgress = null;

    @Override
    public void onDownloadProgress(int id, int taskId, long dl, long dlnow, long dltotal) {

        DownloaderImpl downloader = _findDownloader(id);
        if (downloader==null) {
            return;
        }

        downloader._onProcess(taskId, dl, dlnow, dltotal);

        if (_onDownloadProgress!=null) {
            _onDownloadProgress.onDownloadProgress(id, taskId, dl, dlnow, dltotal);
        }
    }

    public interface OnDownloadFinish {
        public void onDownloadFinish(int id, int taskId, int errCode, String errStr, final byte[] data);
    }
    private OnDownloadFinish _onDownloadFinish = null;

    @Override
    public void onDownloadFinish(int id, int taskId, int errCode, String errStr, final byte[] data) {

        DownloaderImpl downloader = _findDownloader(id);
        if (downloader==null) {
            return;
        }

        if (errStr!=null) {
            downloader._onFinish(taskId, errCode, errStr, data);
            return;
        } else {
            downloader._onFinish(taskId, errCode, "", data);
        }


        if (_onDownloadFinish!=null) {
            _onDownloadFinish.onDownloadFinish(id, taskId, errCode, errStr, data);
        }
    }

}
