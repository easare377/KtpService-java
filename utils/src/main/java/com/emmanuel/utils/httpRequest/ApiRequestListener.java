package com.emmanuel.utils.httpRequest;

/**
 * Created by Emmanuel on 25/6/2019.
 */

public interface ApiRequestListener {
    void processFinish(RequestAsyncTaskResponse response, Exception e);

    void processFinish(RequestAsyncTaskResponse response, Exception e, Object args);
}
