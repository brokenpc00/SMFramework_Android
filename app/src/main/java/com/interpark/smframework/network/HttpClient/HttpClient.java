package com.interpark.smframework.network.HttpClient;

import android.os.ConditionVariable;
import android.service.notification.Condition;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SMDirector;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.Ref;
import com.interpark.smframework.base.types.Scheduler;
import com.interpark.smframework.network.HttpClient.HttpCookie;
import com.interpark.smframework.util.FileUtils;
import com.interpark.smframework.util.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HttpClient extends Ref {
    public static final int RESPONSE_BUFFER_SIZE = 256;
    public static HttpClient _httpClient = null;

    private HttpClient _this = null;

    private interface OnWriteCallback {
        public int onWrite(byte[] data, int size, int nmemb, byte[] stream);
    }

    public class HttpURLConnectionWrapper extends Ref {
        public HttpURLConnectionWrapper(IDirector director, HttpClient httpClient) {
            super(director);
            _client = httpClient;
            _requestmethod = "";
            _responseCookies = "";
            _cookieFileName = "";
            _contentLength = 0;
        }

        public void setRequestMethod(final String method) {
            _requestmethod = method;

            AndroidHttpURLConnection.setRequestMethod(_httpURLConnection, _requestmethod);
        }

        public void createHttpURLConnection(final String url) {
            _url = url;
            _httpURLConnection = AndroidHttpURLConnection.createHttpURLConnection(_url);
        }

        public boolean init(HttpRequest request) {
            createHttpURLConnection(request.getUrl());

            if (!configure()) {
                return false;
            }

            ArrayList<String> headers = request.getHeaders();
            if (headers!=null && !headers.isEmpty()) {
                for (String header : headers) {
                    int len = header.length();
                    int pos = header.indexOf(':');
                    if (pos==-1 || pos>=len) {
                        continue;
                    }
                    String str1 = header.substring(0, pos);
                    String str2 = header.substring(pos+1, len-pos-1);
                    addRequestHeader(str1, str2);
                }
            }

            addCookiesForRequestHeader();

            return true;
        }

        public int connect() {
            return AndroidHttpURLConnection.connect(_httpURLConnection);
        }

        public void disconnect() {
            AndroidHttpURLConnection.disconnect(_httpURLConnection);
        }

        public int getResponseCode() {
            return AndroidHttpURLConnection.getResponseCode(_httpURLConnection);
        }

        public String getResponseMessage() {
            return AndroidHttpURLConnection.getResponseMessage(_httpURLConnection);
        }

        public void sendRequest(HttpRequest request) {
            AndroidHttpURLConnection.sendRequest(_httpURLConnection, request.getRequestData());
        }

        public int saveResponseCookies(final String reponseCookies) {
            if (null==reponseCookies || reponseCookies.isEmpty()) {
                return 0;
            }

            if (_cookieFileName.isEmpty()) {
                _cookieFileName = FileUtils.getInstance().getWritablePath() + "cookieFile.txt";
            }

            File file = new File(_cookieFileName);

            try {
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(reponseCookies.getBytes());
                stream.close();
            } catch (IOException e) {

            }

            return reponseCookies.length();
        }

        public String getResponseHeaders() {
            return AndroidHttpURLConnection.getResponseHeaders(_httpURLConnection);
        }

        public byte[] getResponseContent(HttpResponse response) {
            if (null==response) {
                return null;
            }

            byte[] content = AndroidHttpURLConnection.getResponseContent(_httpURLConnection);
            _contentLength = content.length;

            return content;
        }

        public String getResponseHeaderByKey(String key) {
            return AndroidHttpURLConnection.getResponseHeaderByKey(_httpURLConnection, key);
        }

        public int getResponseHeaderByKeyInt(String key) {
            return AndroidHttpURLConnection.getResponseHeaderByKeyInt(_httpURLConnection, key);
        }

        public String getResponseHeaderByIdx(int idx) {
            return AndroidHttpURLConnection.getResponseHeaderByIdx(_httpURLConnection, idx);
        }

        public String getCookieFileName() {
            return _cookieFileName;
        }

        public void setCookieFileName(String fileName) {
            _cookieFileName = fileName;
        }

        public int getContentLength() {
            return _contentLength;
        }

//        private class CookiesInfo {
//            String domain;
//            boolean tailmatch;
//            String path;
//            boolean secure;
//            String key;
//            String value;
//            String expires;
//        };

        public void addCookiesForRequestHeader() {
            if (_client.getCookieFilename().isEmpty()) return;

            _cookieFileName = FileUtils.getInstance().fullPathForFilename(_client.getCookieFilename());

            String cookiesInfo = FileUtils.getInstance().getStringFromFile(_cookieFilename);

            if (cookiesInfo.isEmpty()) return;

            ArrayList<String> cookiesVec = new ArrayList<>();

            Scanner cookieRead = new Scanner(cookiesInfo);
            cookieRead.useDelimiter("/n");

            while (cookieRead.hasNext()) {
                cookiesVec.add(cookieRead.next());
            }

            if (cookiesVec.isEmpty()) return;

            ArrayList<HttpCookie.CookiesInfo> cookiesInfoVec = new ArrayList<>();

            for (String cookies : cookiesVec) {
                if (cookies.contains("#HttpOnly_")) {
                    cookies = cookies.substring(10);
                }

                if (cookies.substring(0, 1).equals("#")) {
                    continue;
                }

                HttpCookie.CookiesInfo co = new HttpCookie.CookiesInfo();
                ArrayList<String> elems = new ArrayList<>();

                Scanner infoRead = new Scanner(cookies);
                infoRead.useDelimiter("/t");

                while (infoRead.hasNext()) {
                    elems.add(infoRead.next());
                }

                co.domain = elems.get(0);
                if (co.domain.substring(0, 1).equals(".")) {
                    co.domain = co.domain.substring(1);
                }
                co.tailmatch = elems.get(1).equals("TRUE");
                co.path = elems.get(2);
                co.secure = elems.get(3).equals("TRUE");
                co.expires = elems.get(4);
                co.name = elems.get(5);
                co.value = elems.get(6);
                cookiesInfoVec.add(co);
            }

            StringBuffer sendCookiesInfo = new StringBuffer("");
            int cookiesCount = 0;
            for (HttpCookie.CookiesInfo cookieInfo : cookiesInfoVec) {
                if (_url.contains(cookieInfo.domain)) {
                    StringBuffer keyValue = new StringBuffer(cookieInfo.name);
                    keyValue.append("=");
                    keyValue.append(cookieInfo.value);
                    if (cookiesCount!=0) {
                        sendCookiesInfo.append(";");
                    }

                    sendCookiesInfo.append(keyValue);
                }
                cookiesCount++;
            }

            addRequestHeader("Cookie", sendCookiesInfo.toString());
        }

        public void addRequestHeader(String key, String value) {
            AndroidHttpURLConnection.addRequestHeader(_httpURLConnection, key, value);
        }

        public boolean configure() {
            if (null==_httpURLConnection) return false;

            if (null==_client) return false;

            setReadAndConnectTimeout(_client.getTimeoutForRead() * 1000, _client.getTimeoutForConnect() * 1000);

            setVerifySSL();

            return true;
        }

        public void setVerifySSL() {
            if (_client.getSSLVerification().isEmpty()) {
                return;
            }

            String fullPath = FileUtils.getInstance().fullPathForFilename(_client.getSSLVerification());

            AndroidHttpURLConnection.setVerifySSL(_httpURLConnection, fullPath);
        }

        public void setReadAndConnectTimeout(int readMiliseconds, int connectMiliseconds) {
            AndroidHttpURLConnection.setReadAndConnectTimeout(_httpURLConnection, readMiliseconds, connectMiliseconds);
        }

        public String getCookieString() {
            return _responseCookies;
        }

        private HttpClient _client = null;
        private HttpURLConnection _httpURLConnection = null;
        private String _requestmethod;
        private String _responseCookies;
        private String _cookieFileName;
        private String _url;
        private int _contentLength;
    } // HttpURLConnectionWrapper class

    public void processResponse(HttpResponse response, String responseMessage) {
        HttpRequest request = response.getHttpRequest();
        HttpRequest.Type requestType = request.getRequestType();

        if (HttpRequest.Type.GET != requestType &&
                HttpRequest.Type.POST != requestType &&
                HttpRequest.Type.PUT != requestType &&
                HttpRequest.Type.DELETE != requestType) {

            return;
        }

        long responseCode = 0;
        int retValue = 0;

        HttpURLConnectionWrapper urlConnection = new HttpURLConnectionWrapper(getDirector(), this);
        if (!urlConnection.init(request)) {
            response.setSucceed(false);
            response.setErrorBuffer("HttpURLConnetcion init failed");
        }

        switch (requestType) {
            case GET:
            {
                urlConnection.setRequestMethod("GET");
            }
            break;
            case POST:
            {
                urlConnection.setRequestMethod("POST");
            }
            break;
            case PUT:
            {
                urlConnection.setRequestMethod("PUT");
            }
            break;
            case DELETE:
            {
                urlConnection.setRequestMethod("DELETE");
            }
            break;
        }

        int suc = urlConnection.connect();
        if (0 != suc) {
            response.setSucceed(false);
            response.setErrorBuffer("Connect Failed");
            response.setResponseCode(responseCode);
            return;
        }

        if (HttpRequest.Type.POST==requestType || HttpRequest.Type.PUT==requestType) {
            urlConnection.sendRequest(request);
        }

        responseCode = urlConnection.getResponseCode();

        if (0==responseCode) {
            response.setSucceed(false);
            response.setErrorBuffer("Connect Failed");
            response.setResponseCode(-1);
            return;
        }

        String headers = urlConnection.getResponseHeaders();
        if (headers!=null) {
            response.setResponseHeader(headers.getBytes());
        }

        String cookiesInfo = urlConnection.getResponseHeaderByKey("set-cookie");
        if (cookiesInfo!=null) {
            urlConnection.saveResponseCookies(cookiesInfo);
        }

        int contentLength = urlConnection.getResponseHeaderByKeyInt("Content-Length");
        byte[] contentInfo = urlConnection.getResponseContent(response);
        if (null!=contentInfo) {

            response.setResponseData(contentInfo);
        }

        String messageInfo = urlConnection.getResponseMessage();
        if (!messageInfo.isEmpty()) {
            responseMessage = messageInfo;
        }

        urlConnection.disconnect();

        response.setResponseCode(responseCode);

        if (responseCode==-1) {
            response.setSucceed(false);
            response.setErrorBuffer(responseMessage);
        } else {
            response.setSucceed(true);
        }
    }

    public void networkThread() {
        increaseThreadCount();

        try {
            while (true) {
                HttpRequest request;

                synchronized (_requestQueue) {
                    while (_requestQueue.isEmpty()) {

                        synchronized (_networkThread) {
                            _networkThread.wait();
                        }
                    }

                    request = _requestQueue.get(0);
                    _requestQueue.remove(0);
                }


                if (request==_requestSentinel) {
                    break;
                }

                HttpResponse response = new HttpResponse(_director, request);
                processResponse(response, _responseMessage);

                _responseQueueMutex.lock();
                _responseQueue.add(response);
                _responseQueueMutex.unlock();

                final HttpRequest.PrecedenceCallback preCallback = request.getPreCallback();
                if (preCallback!=null) {
                    response.setParseData(preCallback.precedenceCallback(response));
                }

                _schedulerMutex.lock();
                if (_scheduler!=null) {
                    _scheduler.performFunctionInMainThread(new PERFORM_SEL() {
                        @Override
                        public void performSelector() {
                            dispatchResponseCallbacks();
                        }
                    });
                }
                _schedulerMutex.unlock();
            }

        } catch (InterruptedException e) {
            Log.i("HttpClient", "[[[[[ error : " + e.toString());
        }

        _requestQueueMutex.lock();
        _requestQueue.clear();
        _requestQueueMutex.unlock();


        _responseQueueMutex.lock();
        _responseQueue.clear();
        _responseQueueMutex.unlock();

        decreaseThreadCountAndMayDeleteThis();
    }

    public void networkThreadAlone(final HttpRequest request, final HttpResponse response) {
        increaseThreadCount();

        String responseMessage = "";
        processResponse(response, responseMessage);

        final HttpRequest.PrecedenceCallback preCallback = request.getPreCallback();
        if (preCallback!=null) {
            response.setParseData(preCallback.precedenceCallback(response));
        }

        _schedulerMutex.lock();
        if (null!=_scheduler) {
            _scheduler.performFunctionInMainThread(new PERFORM_SEL() {
                @Override
                public void performSelector() {
                    final HttpRequest.HttpRequestCallback callback = request.getCallback();
                    Ref target = request.getTarget();
                    HttpRequest.SEL_HttpResponse selector = request.getSelector();

                    if (callback!=null) {
                        callback.onHttpRequest(_this, response);
                    } else if (target!=null && selector!=null) {
                        selector.onHttpResponse(_this, response);
                    }

                    final HttpRequest.FlushDataCallback flushCallback = request.getFlushCallback();
                    if (flushCallback!=null) {
                        flushCallback.flushDataCallback(response);
                    }
                }
            });
        }
        _schedulerMutex.unlock();

        decreaseThreadCountAndMayDeleteThis();
    }

    public static HttpClient getInstance() {
        if (_httpClient==null) {
            _httpClient = new HttpClient(SMDirector.getDirector());
        }

        return _httpClient;
    }


    public static void destroyInstance() {
        if (_httpClient==null) return;

        HttpClient thiz = _httpClient;
        _httpClient = null;

        thiz._scheduler.unscheduleAllForTarget(thiz);
        thiz._schedulerMutex.lock();
        thiz._scheduler = null;
        thiz._schedulerMutex.unlock();

        synchronized (thiz._requestQueue) {
            thiz._requestQueue.add(thiz._requestSentinel);
        }

        synchronized (thiz._networkThread) {
            thiz._networkThread.notify();
        }

        thiz.decreaseThreadCountAndMayDeleteThis();
    }

    public void enableCookies(final String cookieFile) {
        synchronized (_cookieFilename) {
            if (cookieFile!=null) {
                _cookieFilename = cookieFile;
            } else {
                _cookieFilename = FileUtils.getInstance().getWritablePath() + "cookieFile.txt";
            }
        }
    }

    public void setSSLVerification(final String caFile) {
        synchronized (_sslCaFilename) {
            _sslCaFilename = caFile;
        }
    }

    public HttpClient(IDirector director) {
        super(director);
        _isInited = false;
        _timeoutForConnect = 30;
        _timeoutForRead = 60;
        _threadCount = 0;
        _cookie = null;
        _requestSentinel = new HttpRequest(_director);
        _responseMessage = "";
        _scheduler = _director.getScheduler();
        increaseThreadCount();
        _this = this;
    }

    private boolean lazyInitThreadSemaphore() {

        if (_isInited) {
            return true;
        } else {
            _networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    networkThread();
                }
            });
            _networkThread.start();;

            _isInited = true;
        }

        return true;
    }

    private Thread _networkThread = null;

    public void send(HttpRequest request) {
        if (!lazyInitThreadSemaphore()) {
            return;
        }

        if (null==request) {
            return;
        }

        _requestQueueMutex.lock();
        _requestQueue.add(request);
        _requestQueueMutex.unlock();

        synchronized (_networkThread) {
            _networkThread.notify();
        }
    }

    public void cancelSend() {
        _requestQueueMutex.lock();
        _requestQueue.remove(_requestQueue.size()-1);
        _requestQueueMutex.unlock();
    }

    public void cancelSend(final char tag) {

        if (_requestQueue.isEmpty()) return;

        _requestQueueMutex.lock();
        for (int i=0; i<_requestQueue.size(); i++) {
            if (_requestQueue.get(i).getTag().equals(tag)) {
                _requestQueue.remove(i);
                break;
            }
        }
        _requestQueueMutex.unlock();

    }

    public void cancelSend(final int itag) {
        if (_requestQueue.isEmpty()) return;

        _requestQueueMutex.lock();
        for (int i=0; i<_requestQueue.size(); i++) {
            if (_requestQueue.get(i).getiTag()==itag) {
                _requestQueue.remove(i);
                break;
            }
        }
        _requestQueueMutex.unlock();
    }

    public void sendImmediate(final HttpRequest request) {
        if (null==request) return;

        final HttpResponse response = new HttpResponse(getDirector(), request);

        new Thread(new Runnable() {
            @Override
            public void run() {
                networkThreadAlone(request, response);
            }
        }).start();

    }

    public void dispatchResponseCallbacks() {
        HttpResponse response = null;

        _responseQueueMutex.lock();
        if (!_responseQueue.isEmpty()) {
            response = _responseQueue.get(0);
            _responseQueue.remove(0);
        }
        _responseQueueMutex.unlock();

        if (response!=null) {
            HttpRequest request = response.getHttpRequest();
            final HttpRequest.HttpRequestCallback callback = request.getCallback();
            Ref target = request.getTarget();
            HttpRequest.SEL_HttpResponse selector = request.getSelector();

            if (callback!=null) {
                callback.onHttpRequest(this, response);
            } else if (target!=null && selector!=null) {
                selector.onHttpResponse(this, response);
            }

            final HttpRequest.FlushDataCallback flushCallback = request.getFlushCallback();
            if (flushCallback!=null) {
                flushCallback.flushDataCallback(response);
            }
        }
    }

    public void increaseThreadCount() {
        _threadCountMutex.lock();
        ++_threadCount;
        _threadCountMutex.unlock();
    }

    public void decreaseThreadCountAndMayDeleteThis() {
        _threadCountMutex.lock();
        --_threadCount;
        _threadCountMutex.unlock();
    }


    public void setTimeoutForConnect(int value) {
        _timeoutForConnectMutex.lock();
        _timeoutForConnect = value;
        _timeoutForConnectMutex.unlock();
    }

    public int getTimeoutForConnect() {
        int ret = 0;
        _timeoutForConnectMutex.lock();
        ret = _timeoutForConnect;
        _timeoutForConnectMutex.unlock();

        return ret;
    }

    public void setTimeoutForRead(int value) {
        _timeoutForReadMutex.lock();
        _timeoutForRead = value;
        _timeoutForReadMutex.unlock();
    }

    public int getTimeoutForRead() {
        int ret = 0;
        _timeoutForReadMutex.lock();
        ret = _timeoutForRead;
        _timeoutForReadMutex.unlock();

        return ret;
    }

    public final String getCookieFilename() {
        synchronized (_cookieFilename) {
            return _cookieFilename;
        }
    }

    public String getSSLVerification() {
        synchronized (_sslCaFilename) {
            return _sslCaFilename;
        }
    }

    public HttpCookie getCookie() {
        return _cookie;
    }


    private ConditionVariable _sleepCondition = new ConditionVariable();

    private final Lock _timeoutForConnectMutex = new ReentrantLock(true);
    private final Lock _timeoutForReadMutex = new ReentrantLock(true);
    private final Lock _threadCountMutex = new ReentrantLock(true);
    private final Lock _schedulerMutex = new ReentrantLock(true);
    private final Lock _requestQueueMutex = new ReentrantLock(true);
    private final Lock _responseQueueMutex = new ReentrantLock(true);
    private final Lock _cookieFileMutex = new ReentrantLock(true);
    private final Lock _sslCaFileMutex = new ReentrantLock(true);

    public Lock getCookieFileMutex() {return _cookieFileMutex;}
    public Lock getSSLCaFileMutex() {return _sslCaFileMutex;}

    private boolean _isInited = false;
    private int _timeoutForConnect = 0;
    private int _timeoutForRead = 0;
    private int _threadCount = 0;
    private Scheduler _scheduler = null;
    private ArrayList<HttpRequest> _requestQueue = new ArrayList<>();
    private ArrayList<HttpResponse>  _responseQueue = new ArrayList<>();
    private String _cookieFilename = "";
    private String _sslCaFilename = "";
    private HttpCookie _cookie = null;
    private String _responseMessage = "";
    private HttpRequest _requestSentinel = null;
}
