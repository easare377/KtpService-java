package com.emmanuel.ktpservice.models;


import com.emmanuel.ktpservice.interfaces.RemoteActivityListener;

public class ResponseActivity<T> {
    private final RemoteActivityListener listener;
    private final Class<T> typeParameterClass;

    public ResponseActivity(RemoteActivityListener listener, Class<T> typeParameterClass) {
        this.listener = listener;
        this.typeParameterClass = typeParameterClass;
    }

    public RemoteActivityListener getListener() {
        return listener;
    }

    public Class<T> getTypeParameterClass() {
        return typeParameterClass;
    }
}
