package com.interpark.smframework.network.SocketIO;

import java.util.HashMap;

public class SocketIO {
    public static SocketIO getInstance() {
        return null;
    }

    public static void destroyInstance() {

    }

    public static SIOClient connect(final String uri, SIODelegate delegate) {
        return null;
    }

    public static SIOClient connect(final String uri, SIODelegate delegate, final String caFilePath) {
        return null;
    }

    private SocketIO() {

    }

    private static SocketIO _inst = null;
    private HashMap<String, SIOClientImpl> _sockets = new HashMap<>();

    private SIOClientImpl getSocket(final String uri) {
        return null;
    }

    private void addSocket(final String uri, SIOClientImpl socket) {

    }

    public void removeSocket(final String uri) {

    }
}
