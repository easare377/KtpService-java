package com.emmanuel.ktpservice.interfaces;

public interface ISocketConnectionHandler {
    String getRemoteAddress();

    int getRemotePort();

    boolean isConnected();

    void connectAsync(String host, int port, Object connectionData);

    void sendObjectAsync(Object value);

    void close();
}
