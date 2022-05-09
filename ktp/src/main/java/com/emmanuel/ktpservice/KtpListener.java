package com.emmanuel.ktpservice;

import com.emmanuel.ktpservice.interfaces.KtpServerListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class KtpListener {
    private ServerSocket serverSocket;
    private final String host;
    private final int port;
    private final KtpServerListener listener;
    private boolean serverStarted;

    public KtpListener(String host, int port, KtpServerListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public KtpServerListener getListener() {
        return listener;
    }

    public boolean isServerStarted() {
        return serverStarted;
    }

    public void start() throws IOException {
        if (this.serverStarted) {
            throw new UnsupportedOperationException("Server already started!");
        }
        InetAddress add = InetAddress.getByName(host);
        this.serverSocket = new ServerSocket(port, 10, add);
        // server will listen on a background thread.
        serverStarted = true;
        listenAsync();
    }

    private void listenAsync() {
        Runnable r = this::listen;
        new Thread(r).start();
    }

    private void listen() {
        while (serverStarted) {
            try {
                Socket socket = serverSocket.accept();
                //socket client will be processed on the background thread.
                processSocketClientAsync(socket);
            } catch (IOException ignored) {

            }
        }
    }

    private void processSocketClientAsync(Socket socket) {
        Runnable r = () -> processSocketClient(socket);
        new Thread(r).start();
    }

    private void processSocketClient(Socket socket) {
        KtpClient ktpClient = new KtpClient(socket);
        listener.onClientConnected(ktpClient);
    }

    public void stop() throws IOException {
        serverSocket.close();
        serverStarted = false;
    }
}
