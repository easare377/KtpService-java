package com.emmanuel.ktpservice;

import android.os.Handler;
import android.os.Looper;

import com.emmanuel.ktpservice.interfaces.SocketConnectionListener;
import com.emmanuel.utils.Utils;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Date;


/**
 * Extends the KtpClient to provide more functionalities.
 */
public class KtpClientHandler extends KtpClientHandlerBase {

    public KtpClientHandler(SocketConnectionListener listener) {
        super(listener);
    }

    protected KtpClientHandler() {
        super.listener = null;
    }

    /**
     * Connects the client to a remote Ktp host using the specified IP address and port number.
     *
     * @param host           The IPAddress of the host to which you intend to connect.
     * @param port           The port number to which you intend to connect.
     * @param connectionData The data initial data sent to the remote Ktp host.
     * @throws IOException
     */
    public void connect(String host, int port, Object connectionData) throws IOException {
        //a new ktpClient object is created and connected to a remote host.
        ktpClient = new KtpClient();
        ktpClient.connect(host, port);
        //After the connection has been initiated, the remote host sends a ping interval value
        //which is used by the current connection to periodically send ping or keep alive messages
        //to detect broken connections.
        pingInterval = ktpClient.receiveData(JsonObject.class).get("pingInterval").getAsInt();
        //An OK status 200 is sent to the remote host to indicate the handshake is successful.
        ktpClient.sendObject(200);
        // connection data is sent to the host.
        ktpClient.sendObject(connectionData);
        connected = true;
        //the ping mechanism is started.
        pingServer();
    }

    //this method is run async to listen for incoming messages from the remote client.
    protected void listenForMessages() throws IOException {
        //A loop is created to continuously receive messages sent by the remote client.
        while (connected) {
            JsonObject jsonObject = ktpClient.receiveData(JsonObject.class);
            keepAliveResponse = true;
            pingServer();
            if (jsonObject == null) {
                synchronized (ktpClient) {
                    //ping message is a null object
                    ktpClient.sendObject(null);
                }
                continue;
            }
            //If the message received is not a keep alive message/null then a connection listener is
            // notified.
            String json = Utils.serializeObjectToJson(jsonObject);
            //The message is handled on another thread.
            Runnable runnable = () -> {
                try {
                    listener.onMessageReceived(KtpClientHandler.this, new SocketMessageHandler(json));
                } catch (Exception ignored) {
                }
            };
            new Thread(runnable).start();
            //The main thread is used to process the received message.
//            new Handler(Looper.getMainLooper()).post(() -> {
//                try {
//                    listener.onMessageReceived(KtpClientHandler.this, new SocketMessageHandler(json));
//                } catch (Exception ignored) {
//                }
//            });
        }
    }

    /**
     * Asynchronously sends an object to the remote client.
     *
     * @param value The object to send to the remote device
     */
    @Override
    public void sendObjectAsync(Object value) {
        Runnable runnable = () -> {
            try {
                sendObject(value);
            } catch (Exception e) {
                listener.onClosed(KtpClientHandler.this);
            }
        };
        new Thread(runnable).start();
    }

    protected void sendObject(Object value) throws IOException {
        synchronized (ktpClient) {
            ktpClient.sendObject(value);
        }
    }

//    private void pingServer() {
//        if (pingCountDownTimer != null) {
//            pingCountDownTimer.cancel();
//        }
//        pingCountDownTimer = new CustomCountDownTimer(pingInterval + 5000, pingInterval) {
//            @Override
//            public void onTick(long l) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                if (keepAliveResponse) {
//                    keepAliveResponse = false;
//                } else {
////                    this.cancel();
////                    KtpClientHandler.this.close();
//                }
//            }
//        };
//        pingCountDownTimer.startTimer();
//    }
}
