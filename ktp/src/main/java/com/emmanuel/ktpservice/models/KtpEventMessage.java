package com.emmanuel.ktpservice.models;


import com.emmanuel.ktpservice.models.enums.MessageType;

import java.util.Map;
import java.util.UUID;

public class KtpEventMessage {
    private final UUID activityId;
    private final String activityName;
    private final MessageType messageType;
    private final Map<String, Object> paramMap;

    public KtpEventMessage(UUID activityId, String activityName, MessageType messageType, Map<String, Object> paramMap) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.messageType = messageType;
        this.paramMap = paramMap;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Map<String, Object> getParams() {
        return paramMap;
    }
}
