package com.interpark.smframework.network.Downloader;


import com.interpark.smframework.ClassHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

class DataTaskHandler extends BinaryHttpResponseHandler {
    int _id;
    private AndroidDownloader _downloader;
    private long _lastBytesWritten;

    void LogD(String msg) {
        android.util.Log.d("AndroidDownloader", msg);
    }

    public DataTaskHandler(AndroidDownloader downloader, int id) {
        super(new String[]{".*"});
        _downloader = downloader;
        _id = id;
        _lastBytesWritten = 0;
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        //LogD("onProgress(bytesWritten:" + bytesWritten + " totalSize:" + totalSize);
        long dlBytes = bytesWritten - _lastBytesWritten;
        long dlNow = bytesWritten;
        long dlTotal = totalSize;
        _downloader.onProgress(_id, dlBytes, dlNow, dlTotal);
        _lastBytesWritten = bytesWritten;
    }

    @Override
    public void onStart() {
        _downloader.onStart(_id);
    }

    @Override
    public void onFailure(int i, Header[] headers, byte[] errorResponse, Throwable throwable) {
//        LogD("onFailure(i:" + i + " headers:" + headers + " throwable:" + throwable);
        String errStr = "";
        if (null != throwable) {
            errStr = throwable.toString();
        }
        _downloader.onFinish(_id, i, errStr, null);
    }

    @Override
    public void onSuccess(int i, Header[] headers, byte[] binaryData) {
//        LogD("onSuccess(i:" + i + " headers:" + headers);
        _downloader.onFinish(_id, 0, null, binaryData);
    }

    @Override
    public void onFinish() {
        // onFinish called after onSuccess/onFailure
        _downloader.runNextTaskIfExists();
    }
}

class HeadTaskHandler extends AsyncHttpResponseHandler {
    int _id;
    String _host;
    String _url;
    String _path;
    private AndroidDownloader _downloader;

    void LogD(String msg) {
        android.util.Log.d("AndroidDownloader", msg);
    }

    public HeadTaskHandler(AndroidDownloader downloader, int id, String host, String url, String path) {
        super();
        _downloader = downloader;
        _id = id;
        _host = host;
        _url = url;
        _path = path;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        Boolean acceptRanges = false;
        for (int i = 0; i < headers.length; ++i) {
            Header elem = headers[i];
            if (elem.getName().equals("Accept-Ranges")) {
                acceptRanges = elem.getValue().equals("bytes");
                break;
            }
        }
        AndroidDownloader.setResumingSupport(_host, acceptRanges);
        AndroidDownloader.createTask(_downloader, _id, _url, _path);
    }

    @Override
    public void onFinish() {
        // onFinish called after onSuccess/onFailure
        _downloader.runNextTaskIfExists();
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable throwable) {
//        LogD("onFailure(code:" + statusCode + " headers:" + headers + " throwable:" + throwable + " id:" + _id);
        String errStr = "";
        if (null != throwable) {
            errStr = throwable.toString();
        }
        _downloader.onFinish(_id, statusCode, errStr, null);
    }
}

class FileTaskHandler extends FileAsyncHttpResponseHandler {
    int _id;
    File _finalFile;

    private long _initFileLen;
    private long _lastBytesWritten;
    private AndroidDownloader _downloader;

    void LogD(String msg) {
        android.util.Log.d("AndroidDownloader", msg);
    }

    public FileTaskHandler(AndroidDownloader downloader, int id, File temp, File finalFile) {
        super(temp, true);
        _finalFile = finalFile;
        _downloader = downloader;
        _id = id;
        _initFileLen = getTargetFile().length();
        _lastBytesWritten = 0;
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
//        LogD("onProgress(bytesWritten:" + bytesWritten + " totalSize:" + totalSize);
        long dlBytes = bytesWritten - _lastBytesWritten;
        long dlNow = bytesWritten + _initFileLen;
        long dlTotal = totalSize + _initFileLen;
        _downloader.onProgress(_id, dlBytes, dlNow, dlTotal);
        _lastBytesWritten = bytesWritten;
    }

    @Override
    public void onStart() {
        _downloader.onStart(_id);
    }

    @Override
    public void onFinish() {
        // onFinish called after onSuccess/onFailure
        _downloader.runNextTaskIfExists();
    }

    @Override
    public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
        LogD("onFailure(i:" + i + " headers:" + headers + " throwable:" + throwable + " file:" + file);
        String errStr = "";
        if (null != throwable) {
            errStr = throwable.toString();
        }
        _downloader.onFinish(_id, i, errStr, null);
    }

    @Override
    public void onSuccess(int i, Header[] headers, File file) {
        LogD("onSuccess(i:" + i + " headers:" + headers + " file:" + file);
        String errStr = null;
        do {
            // rename temp file to final file
            // if final file exist, remove it
            if (_finalFile.exists()) {
                if (_finalFile.isDirectory()) {
                    errStr = "Dest file is directory:" + _finalFile.getAbsolutePath();
                    break;
                }
                if (false == _finalFile.delete()) {
                    errStr = "Can't remove old file:" + _finalFile.getAbsolutePath();
                    break;
                }
            }

            File tempFile = getTargetFile();
            tempFile.renameTo(_finalFile);
        } while (false);
        _downloader.onFinish(_id, 0, errStr, null);
    }
}

class AndoirdDownloadTask {

    AndoirdDownloadTask() {
        handle = null;
        handler = null;
        resetStatus();
    }

    void resetStatus() {
        bytesReceived = 0;
        totalBytesReceived = 0;
        totalBytesExpected = 0;
        data = null;
    }

    RequestHandle handle;
    AsyncHttpResponseHandler handler;

    // progress
    long bytesReceived;
    long totalBytesReceived;
    long totalBytesExpected;
    byte[] data;

}

public class AndroidDownloader {
    private int _id;
    private AsyncHttpClient _httpClient = new AsyncHttpClient();
    private String _tempFileNameSufix;
    private int _countOfMaxProcessingTasks;
    private HashMap _taskMap = new HashMap();
    private Queue<Runnable> _taskQueue = new LinkedList<Runnable>();
    private int _runningTaskCount = 0;
    private static HashMap<String, Boolean> _resumingSupport = new HashMap<String, Boolean>();

    void onProgress(final int id, final long downloadBytes, final long downloadNow, final long downloadTotal) {
        AndoirdDownloadTask task = (AndoirdDownloadTask)_taskMap.get(id);
        if (null != task) {
            task.bytesReceived = downloadBytes;
            task.totalBytesReceived = downloadNow;
            task.totalBytesExpected = downloadTotal;
        }
        ClassHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                if (_onDownloadProgress!=null) {
                    _onDownloadProgress.onDownloadProgress(_id, id, downloadBytes, downloadNow, downloadTotal);
                }
            }
        });
    }

    public void onStart(int id) {
        AndoirdDownloadTask task = (AndoirdDownloadTask)_taskMap.get(id);
        if (null != task) {
            task.resetStatus();
        }
    }

    public void onFinish(final int id, final int errCode, final String errStr, final byte[] data) {
        AndoirdDownloadTask task = (AndoirdDownloadTask)_taskMap.get(id);
        if (null == task) return;
        _taskMap.remove(id);
        ClassHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                if (_onDownloadFinish!=null) {
                    _onDownloadFinish.onDownloadFinish(_id, id, errCode, errStr, data);
                }
            }
        });
    }

    public static void setResumingSupport(String host, Boolean support) {
        AndroidDownloader._resumingSupport.put(host, support);
    }

    public static AndroidDownloader createDownloader(int id, DownloaderHints hints) {
        return createDownloader(id, hints.timeoutInSeconds, hints.tempFileNameSuffix, hints.countOfMaxProcessingTasks);
    }
    public static AndroidDownloader createDownloader(int id, int timeoutInSeconds, String tempFileNameSufix, int countOfMaxProcessingTasks) {
        AndroidDownloader downloader = new AndroidDownloader();
        downloader._id = id;

        downloader._httpClient.setEnableRedirects(true);
        if (timeoutInSeconds > 0) {
            downloader._httpClient.setTimeout(timeoutInSeconds * 1000);
        }
        // downloader._httpClient.setMaxRetriesAndTimeout(3, timeoutInSeconds * 1000);
        downloader._httpClient.allowRetryExceptionClass(javax.net.ssl.SSLException.class);

        downloader._httpClient.setURLEncodingEnabled(false);

        downloader._tempFileNameSufix = tempFileNameSufix;
        downloader._countOfMaxProcessingTasks = countOfMaxProcessingTasks;
        return downloader;
    }

    public static void createTask(final AndroidDownloader downloader, int id_, String url_, String path_) {
        final int id = id_;
        final String url = url_;
        final String path = path_;

        Runnable taskRunnable = new Runnable() {
            @Override
            public void run() {
                AndoirdDownloadTask task = new AndoirdDownloadTask();
                if (0 == path.length()) {
                    // data task
                    task.handler = new DataTaskHandler(downloader, id);
                    task.handle = downloader._httpClient.get(ClassHelper.getActivity(), url, task.handler);
                }

                do {
                    if (0 == path.length()) break;

                    String domain;
                    try {
                        URI uri = new URI(url);
                        domain = uri.getHost();
                    }
                    catch (URISyntaxException e) {
                        break;
                    }
                    final String host = domain.startsWith("www.") ? domain.substring(4) : domain;
                    Boolean supportResuming = false;
                    Boolean requestHeader = true;
                    if (_resumingSupport.containsKey(host)) {
                        supportResuming = _resumingSupport.get(host);
                        requestHeader = false;
                    }

                    if (requestHeader) {
                        task.handler = new HeadTaskHandler(downloader, id, host, url, path);
                        task.handle = downloader._httpClient.head(ClassHelper.getActivity(), url, null, null, task.handler);
                        break;
                    }

                    // file task
                    File tempFile = new File(path + downloader._tempFileNameSufix);
                    if (tempFile.isDirectory()) break;

                    File parent = tempFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) break;

                    File finalFile = new File(path);
                    if (finalFile.isDirectory()) break;

                    task.handler = new FileTaskHandler(downloader, id, tempFile, finalFile);
                    Header[] headers = null;
                    long fileLen = tempFile.length();
                    if (supportResuming && fileLen > 0) {
                        // continue download
                        List<Header> list = new ArrayList<Header>();
                        list.add(new BasicHeader("Range", "bytes=" + fileLen + "-"));
                        headers = list.toArray(new Header[list.size()]);
                    }
                    else if (fileLen > 0) {
                        // Remove previous downloaded context
                        try {
                            PrintWriter writer = new PrintWriter(tempFile);
                            writer.print("");
                            writer.close();
                        }
                        // Not found then nothing to do
                        catch (FileNotFoundException e) {}
                    }
                    task.handle = downloader._httpClient.get(ClassHelper.getActivity(), url, headers, null, task.handler);
                } while (false);

                if (null == task.handle) {
                    final String errStr = "Can't create DownloadTask for " + url;
                    ClassHelper.runOnGLThread(new Runnable() {
                        @Override
                        public void run() {
                            if (downloader._onDownloadFinish!=null) {
                                downloader._onDownloadFinish.onDownloadFinish(downloader._id, id, 0, errStr, null);
                            }
                        }
                    });
                } else {
                    downloader._taskMap.put(id, task);
                }
            }
        };
        downloader.enqueueTask(taskRunnable);
    }

    public static void cancelAllRequests(final AndroidDownloader downloader) {
        ClassHelper.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //downloader._httpClient.cancelAllRequests(true);
                Iterator iter = downloader._taskMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    //Object key = entry.getKey();
                    AndoirdDownloadTask task = (AndoirdDownloadTask) entry.getValue();
                    if (null != task.handle) {
                        task.handle.cancel(true);
                    }
                }
            }
        });
    }


    public void enqueueTask(Runnable taskRunnable) {
        synchronized (_taskQueue) {
            if (_runningTaskCount < _countOfMaxProcessingTasks) {
                ClassHelper.getActivity().runOnUiThread(taskRunnable);
                _runningTaskCount++;
            } else {
                _taskQueue.add(taskRunnable);
            }
        }
    }

    public void runNextTaskIfExists() {
        synchronized (_taskQueue) {
            Runnable taskRunnable = AndroidDownloader.this._taskQueue.poll();
            if (taskRunnable != null) {
                ClassHelper.getActivity().runOnUiThread(taskRunnable);

            } else {
                _runningTaskCount--;
            }
        }
    }

    public interface OnDownloadProgress {
        public void onDownloadProgress(int id, int taskId, long dl, long dlnow, long dltotal);
    }
    public OnDownloadProgress _onDownloadProgress = null;

    public interface OnDownloadFinish {
        public void onDownloadFinish(int id, int taskId, int errCode, String errStr, final byte[] data);
    }
    public OnDownloadFinish _onDownloadFinish = null;

}