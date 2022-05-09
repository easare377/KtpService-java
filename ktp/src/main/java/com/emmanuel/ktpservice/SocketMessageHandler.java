package com.emmanuel.ktpservice;

import com.emmanuel.utils.Utils;

public class SocketMessageHandler {
    private final String json;

    public SocketMessageHandler(String json) {
        this.json = json;
    }

    public <T> T getMessageObject(Class<T> typeParameterClass){
        return Utils.deserializeObjectFromJson(typeParameterClass,json);
    }
}
