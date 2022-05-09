package com.emmanuel.ktpservice.models;



import com.emmanuel.ktpservice.models.enums.MessageType;

import java.util.UUID;

public class KtpEventResponse<T> {
    private final UUID activityId;
    private final MessageType messageType;
    private final T result;
    private String error;

    public KtpEventResponse(UUID activityId, MessageType messageType, T result, String error) {
        this.activityId = activityId;
        this.messageType = messageType;
        this.result = result;
        this.error = error;
    }

    public KtpEventResponse(UUID activityId, MessageType messageType, T result) {
        this.activityId = activityId;
        this.messageType = messageType;
        this.result = result;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public T getResult() {
        return result;
    }

    public String getError() {
        return error;
    }
}
