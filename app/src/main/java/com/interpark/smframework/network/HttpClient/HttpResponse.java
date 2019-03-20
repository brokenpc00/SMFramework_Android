package com.interpark.smframework.network.HttpClient;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Ref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HttpResponse extends Ref {
    public HttpResponse(IDirector director) {
        this(director, null);
    }
    public HttpResponse(IDirector director, HttpRequest request) {
        super(director);
        _httpRequest = request;
    }

    public HttpRequest getHttpRequest() {
        return _httpRequest;
    }

    public boolean isSucceed() {
        return _succeed;
    }

    public byte[] getResponseData() {
        return _responseData;
    }

    public void setResponseData(byte[] array) {
        _responseData = array;
    }

    public byte[] getResponseHeader() {
        return _responseHeader;
    }

    public long getResponseCode() {
        return _responseCode;
    }

    public String getErrorBuffer() {
        return _errorBuffer;
    }

    public void setSucceed(boolean value) {
        _succeed = value;
    }

    public void setResponseHeader(byte[] data) {

        if (data==null) {
            _responseHeader = null;
            return;
        }

        _responseHeader = data;

    }

    public void setResponseCode(long value) {
        _responseCode = value;
    }

    public void setErrorBuffer(String value) {
        _errorBuffer = value;
    }

    public void setResponseDataString(final String value ) {
        _responseDataString = value;
    }

    public String getResponseDataString() {
        return _responseDataString;
    }

    public byte[] getParseData() {
        return _parseData;
    }

    public void setParseData(byte[] p) {
        _parseData = p;
    }

    public void setParseRetCode(int v) {
        setParseRetCode(v, "");
    }
    public void setParseRetCode(int v, final String msg) {
        _parseRetCode = v;
        _parseRetMessage = msg;
    }

    public int getParseRetCode() {
        return _parseRetCode;
    }

    public String getParseRetMesage() {
        return _parseRetMessage;
    }

    protected boolean initWithRequest(HttpRequest request) {
        _httpRequest = request;
        return true;
    }

    protected HttpRequest _httpRequest;
    protected boolean _succeed = false;
    protected byte[] _responseData = null;
    protected byte[] _responseHeader = null;
    protected long _responseCode;
    protected String _errorBuffer;
    protected String _responseDataString = "";
    protected byte[] _parseData = null;
    protected int _parseRetCode = 500;
    protected String _parseRetMessage;
}
