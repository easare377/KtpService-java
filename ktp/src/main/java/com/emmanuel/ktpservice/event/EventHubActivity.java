package com.emmanuel.ktpservice.event;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class EventHubActivity {
    private final String activityName;
    private final Map<String, Object> paramMap;


    public EventHubActivity(String activityName) {
        this.activityName = activityName;
        paramMap = new LinkedHashMap<>();
    }

    public EventHubActivity addParam(String key, Object value) {
        paramMap.put(key, value);
        return this;
    }

    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        return onInvoke(activityName, paramMap);
    }

    public abstract Object onInvoke(String activityName, Map<String, Object> paramDict) throws InvocationTargetException, IllegalAccessException;
}
