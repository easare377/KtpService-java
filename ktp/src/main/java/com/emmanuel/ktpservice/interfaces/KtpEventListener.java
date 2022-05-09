package com.emmanuel.ktpservice.interfaces;

public interface KtpEventListener {
    void onConnected(ISocketConnectionHandler connectionHandler);
    void onConnectionFailed(ISocketConnectionHandler connectionHandler, Exception e);
    void onClosed(ISocketConnectionHandler connectionHandler);
}
