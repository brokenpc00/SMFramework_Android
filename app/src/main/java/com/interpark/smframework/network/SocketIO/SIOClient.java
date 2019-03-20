package com.interpark.smframework.network.SocketIO;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Ref;

import java.util.HashMap;

public class SIOClient extends Ref {
    public SIOClient (IDirector director, final String path, SIOClientImpl impl, SIODelegate delegate) {
        super(director);
        _path = path;
        _connected = false;
        _socket = impl;
        _delegate = delegate;
    }
    public interface SIOEvent {
        public void onEvent(SIOClient client, final String uri);
    }

    public SIODelegate getDelegate() {return _delegate;}

    public void release() {
        if (_connected) {
//            _socket
        }
    }

    private String _path, _tag="";
    private boolean _connected;
    private SIOClientImpl _socket;
    private SIODelegate _delegate;
    private HashMap<String, SIOEvent> _eventRegistry = new HashMap<>();

    private void fireEvent(final String eventName, final String data) {

    }

    private void onOpen() {
        if (!_path.equals("/")) {

        }
    }

    private void onConnect() {

    }

    private void socketClosed() {

    }

    public void disconnect() {

    }

    public void send(final String s) {

    }

    public void emit(final String eventname, final String args) {

    }

    public void on(final String eventName, SIOEvent e) {

    }

    public void setTag(final String tag) {
        _tag = tag;
    }

    public String getTag() {return _tag;}



}
