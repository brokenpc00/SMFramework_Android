package com.interpark.smframework.network.HttpClient;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Ref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HttpRequest extends Ref {
    public HttpRequest(IDirector director) {
        super(director);
    }

    public enum Type {
        GET,
        POST,
        PUT,
        DELETE,
        UNKNOWN,
    }

    public void setRequestType(Type type) {
        _requestType = type;
    }

    public Type getRequestType() {
        return _requestType;
    }

    public void setUrl(final String url) {
        _url = url;
    }

    public String getUrl() {
        return _url;
    }

    public void setRequestData(byte[] buffer) {
        _requestData = buffer;
    }

    public byte[] getRequestData() {
        return _requestData;
    }

    public int getRequestDataSize() {
        return _requestData.length;
    }

    public void setTag(String tag) {
        _tag = tag;
    }

    public void setiTag(int itag) {
        _iTag = itag;
    }

    public String getTag() {
        return _tag;
    }

    public int getiTag() {
        return _iTag;
    }

    public void setUserData(byte[] userData) {
        _userData = userData;
    }

    public byte[] getUserData() {
        return _userData;
    }

    public void setResponseCallback(Ref target, SEL_HttpResponse selector) {
        if (_target!=null) {
            _target = null;
        }

        _target = target;
        _selector = selector;
    }

    public void setResponseCallback(final HttpRequestCallback callback) {
        _httpRequestCallback = callback;
    }

    public void setPrecedenceCallback(final PrecedenceCallback callback) {
        _precedenceCallback = callback;
    }

    public void setFlushCallback(final FlushDataCallback callback) {
        _flushDataCallback = callback;
    }

    public Ref getTarget() {
        return _target;
    }

    public class _proxy {
        public _proxy(SEL_HttpResponse cb) {
            _cb = cb;
        }

        public SEL_HttpResponse get() {return _cb;}

        protected SEL_HttpResponse _cb;
    }

    public SEL_HttpResponse getSelector() {
        _proxy p = new _proxy(_selector);
        return p.get();
    }

    public HttpRequestCallback getCallback() {
        return _httpRequestCallback;
    }

    public PrecedenceCallback getPreCallback() {
        return _precedenceCallback;
    }

    public FlushDataCallback getFlushCallback() {
        return _flushDataCallback;
    }

    public void setHeaders(final ArrayList<String> headers) {
        if (headers!=null && !headers.isEmpty()) {
            Collections.copy(_headers, headers);
        }
    }

    public ArrayList<String> getHeaders() {
        if (!_headers.isEmpty()) {
            return _headers;
        } else {
            return null;
        }
    }

    public interface PrecedenceCallback {
        public byte[] precedenceCallback(HttpResponse response);
    }

    public interface FlushDataCallback {
        public void flushDataCallback(HttpResponse response);
    }

    public interface HttpRequestCallback {
        public void onHttpRequest(HttpClient client, HttpResponse response);
    }

    public interface SEL_HttpResponse {
        public void onHttpResponse(HttpClient client, HttpResponse response);
    }

    protected Type _requestType = Type.UNKNOWN;
    protected String _url = "";
    protected byte[] _requestData = null;
    protected String _tag;
    protected int _iTag;
    protected Ref _target = null;
    protected SEL_HttpResponse _selector = null;
    protected HttpRequestCallback _httpRequestCallback = null;
    protected PrecedenceCallback _precedenceCallback = null;
    protected FlushDataCallback _flushDataCallback = null;

    protected byte[] _userData = null;
    protected ArrayList<String> _headers = new ArrayList<>();
}
