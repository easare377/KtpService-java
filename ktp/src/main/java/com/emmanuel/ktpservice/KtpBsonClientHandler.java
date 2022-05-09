package com.emmanuel.ktpservice;

import com.emmanuel.utils.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class KtpBsonClientHandler extends  KtpClientHandlerBase {


    @Override
    public void connect(String host, int port, Object connectionData) throws IOException {
        ktpClient = new KtpClient();
        ktpClient.connect(host, port);
        byte[] bson = ktpClient.read();
        HashMap<String, Integer> map = Utils.fromBson(HashMap.class,bson);
        pingInterval = map.get("pingInterval");
        map.clear();
        map.put("respCode", 200);
        bson =  Utils.toBson(map);
        ktpClient.write(bson);
        bson =  Utils.toBson(connectionData);
        ktpClient.write(bson);
        connected = true;
        pingServer();
        listener.onConnected(this);
    }

    @Override
    void listenForMessages() throws IOException {
        while (connected) {
             byte[] bson = ktpClient.read();
            //JsonObject jsonObject = ktpClient.receiveData(JsonObject.class);
            keepAliveResponse = true;
            pingServer();
            if (bson == null) {
                synchronized (ktpClient) {
                    ktpClient.sendObject(null);
                }
                //System.out.println("Ping sent " + new Date().toString());
                continue;
            }
            //HashMap<String, Integer> map = Functions.fromBson(HashMap.class,bson);
            String json =""; //Functions.serializeObjectToJson(jsonObject);
            //A new thread is started to process received message.
            Runnable runnable = () -> {
                try {
                    listener.onMessageReceived(this, new SocketMessageHandler(json));
                    System.out.println("Message received " + new Date().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            new Thread(runnable).start();
        }
    }

    @Override
    public void sendObjectAsync(Object value) {

    }

    @Override
    protected void sendObject(Object value) throws IOException {

    }
}
