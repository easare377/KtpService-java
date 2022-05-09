package com.emmanuel.ktpservice;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class KtpEventActivity {
    private final String activityName;
    private final Map<String, Object> paramMap;

    public KtpEventActivity(String activityName) {
        this.activityName = activityName;
        paramMap = new LinkedHashMap<>();
    }

    public KtpEventActivity addParam(String key, Object value) {
        paramMap.put(key, value);
        return this;
    }

    public <T> T invoke(Class<T> typeParameterClass, int timeoutMillis) {
        return onInvoke(typeParameterClass, activityName, paramMap);
    }

    public abstract <T> T onInvoke(Class<T> typeParameterClass, String activityName, Map<String, Object> paramDict);

    public abstract void onTimeout(int timeoutMillis);
}
