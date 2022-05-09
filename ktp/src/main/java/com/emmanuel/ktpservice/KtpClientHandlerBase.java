package com.emmanuel.ktpservice;

import com.emmanuel.ktpservice.interfaces.ISocketConnectionHandler;
import com.emmanuel.ktpservice.interfaces.SocketConnectionListener;
import com.emmanuel.utils.customTimers.CustomCountDownTimer;

import java.io.IOException;

public abstract class KtpClientHandlerBase implements ISocketConnectionHandler {
    protected boolean connected;
    protected SocketConnectionListener listener;
    protected KtpClient ktpClient;
    protected int pingInterval;
    protected boolean keepAliveResponse;
    protected CustomCountDownTimer pingCountDownTimer;

    public KtpClientHandlerBase(SocketConnectionListener listener) {
        this.listener = listener;
    }

    protected KtpClientHandlerBase() {
        this.listener = null;
    }

    /**
     * Gets the ip address of the connected remote client.
     * @return A string value representing the remote ip address.
     */
    @Override
    public String getRemoteAddress() {
        return ktpClient.getRemoteAddress();
    }

    /**
     * Gets the port of the connected remote client.
     * @return An integer value representing the remote port.
     */
    @Override
    public int getRemotePort() {
        return ktpClient.getRemotePort();
    }

    /**
     * Indicates if the Ktp client is connected to a remote client.
     * @return a boolean value indicating the state of the connection.
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Asynchronously connects the client to a remote Ktp host using the specified IP address and port number.
     * @param host The IPAddress of the host to which you intend to connect.
     * @param port The port number to which you intend to connect.
     * @param connectionData The data initial data sent to the remote Ktp host.
     */
    @Override
    public void connectAsync(String host, int port, Object connectionData) {
        Runnable runnable = () -> {
            try {
                connect(host, port, connectionData);
                listener.onConnected(this);
            } catch (Exception e) {
                listener.onConnectionFailed(KtpClientHandlerBase.this, e);
            }
            try {
                listenForMessages();
            } catch (Exception e) {
                //connected = false;
                KtpClientHandlerBase.this.close();
            }
        };
        new Thread(runnable).start();
    }

    public abstract void connect(String host, int port, Object connectionData) throws IOException;

    abstract void listenForMessages() throws IOException;

    public abstract void sendObjectAsync(Object value);

    protected abstract  void sendObject(Object value) throws IOException;

    /**
     * Closes the Ktp connection and frees resources.
     */
    @Override
    public void close() {
        if(!connected){
            return;
        }
        connected = false;
        ktpClient.close();
        listener.onClosed(KtpClientHandlerBase.this);
    }

    protected void pingServer() {
        if (pingCountDownTimer != null) {
            pingCountDownTimer.cancel();
        }
        pingCountDownTimer = new CustomCountDownTimer(pingInterval + 5000, pingInterval) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (keepAliveResponse) {
                    keepAliveResponse = false;
                } else {
//                    this.cancel();
//                    KtpClientHandler.this.close();
                }
            }
        };
        pingCountDownTimer.startTimer();
    }
}
