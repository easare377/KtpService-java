package com.emmanuel.ktpservice.interfaces;

import com.emmanuel.ktpservice.SocketMessageHandler;

public interface SocketConnectionListener {
    void onConnected(ISocketConnectionHandler connectionHandler);
    void onConnectionFailed(ISocketConnectionHandler connectionHandler, Exception e);
    void onMessageReceived(ISocketConnectionHandler connectionHandler, SocketMessageHandler messageHandler);
    void onClosed(ISocketConnectionHandler connectionHandler);
}
