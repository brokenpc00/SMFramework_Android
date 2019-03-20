package com.interpark.smframework.network.SocketIO;

public interface SIODelegate {
    public void onConnect(SIOClient client);
    public void onConnect(SIOClient client, final String data);
    public void onClose(SIOClient client);
    public void onError(SIOClient client, final String data);
    public void fireEventToScript(SIOClient client, final String eventName, final String data);
}
