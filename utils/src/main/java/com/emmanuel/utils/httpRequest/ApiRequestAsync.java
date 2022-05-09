package com.emmanuel.utils.httpRequest;

import android.os.AsyncTask;
import android.os.CountDownTimer;

import com.emmanuel.utils.IOUtils;
import com.emmanuel.utils.Utils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Emmanuel on 1/7/2019.
 */

public class ApiRequestAsync extends AsyncTask<String, RequestAsyncTaskResponse, RequestAsyncTaskResponse> {
    public enum Method {
        Post,
        Put,
        Get
    }

    public ApiRequestListener listener;
    public Object args;
    private Method httpMethod;
    private Object object;
    private Exception exception;
    private int timeOutMillis;
    private InputStream inputStream;
    private OutputStream outputStream;
    private CountDownTimer timeoutTimer;

    public ApiRequestAsync(ApiRequestListener listener) {
        this.listener = listener;
    }

    public ApiRequestAsync(ApiRequestListener listener, int timeOutMillis) {
        this.listener = listener;
        this.timeOutMillis = timeOutMillis;
    }

//    public ApiRequestAsync(ApiRequestListener listener, Object args) {
//        this.listener = listener;
//        this.args = args;
//    }

    public ApiRequestAsync(ApiRequestListener listener, int timeOutMillis, Object args) {
        this.listener = listener;
        this.timeOutMillis = timeOutMillis;
        this.args = args;
    }

    public void uploadObject(String url, Method method, Object object) {
        uploadObject(url, method, object, true);
    }

    public void uploadObject(String url, Method method, Object object, boolean executeOnExecutor) {
        if (method == Method.Get)
            throw new IllegalArgumentException("GET method cannot be used with this operation");
        this.httpMethod = method;
        this.object = object;
        if (this.timeOutMillis < 1)
            startTimeOutTimer(Integer.MAX_VALUE, Integer.MAX_VALUE);
        else
            startTimeOutTimer(timeOutMillis, timeOutMillis);
        if (executeOnExecutor)
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        else
            this.execute(url);
    }

    public void downloadObject(String url) {
        downloadObject(url,true);
    }

    public void downloadObject(String url, boolean executeOnExecutor){
        this.httpMethod = Method.Get;
        if (this.timeOutMillis < 1)
            startTimeOutTimer(Integer.MAX_VALUE, Integer.MAX_VALUE);
        else
            startTimeOutTimer(timeOutMillis, timeOutMillis);
        if(executeOnExecutor)
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        else
            this.execute(url);
    }

    @Override
    protected RequestAsyncTaskResponse doInBackground(String... uri) {
        RequestAsyncTaskResponse response = null;
        try {
            URL url = new URL(uri[0].replace(" ", "%20"));

            switch (httpMethod) {
                case Post:
                    String json = new Gson().toJson(object);
                    byte[] data = json.getBytes("UTF-8");
                    response = sendData(url.toString(), "POST", data);
                    break;
                case Put:
                    json = new Gson().toJson(object);
                    data = json.getBytes("UTF-8");
                    response = sendData(url.toString(), "PUT", data);
                    break;
                case Get:
                    response = getData(url.toString());
                    break;
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception ignored) {
            }
        }
        inputStream = null;
        outputStream = null;
        return response;
    }

    private RequestAsyncTaskResponse sendData(String uri, String method, byte[] data) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        conn.setRequestMethod(method);
        outputStream = conn.getOutputStream();
        outputStream.write(data);
        int responseCode = conn.getResponseCode();
        String responseString = null;
        //InputStream responseStream;
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            inputStream = conn.getInputStream();
            // Do normal input or output stream reading
            responseString = IOUtils.convertStreamToString(inputStream);
        } else {
            inputStream = conn.getErrorStream();
            if (inputStream != null)
                responseString = IOUtils.convertStreamToString(inputStream);
        }
        return new RequestAsyncTaskResponse(conn, responseCode, responseString);
    }

    private RequestAsyncTaskResponse getData(String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        int responseCode = conn.getResponseCode();
        String responseString = null;
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            // Do normal input or output stream reading
            responseString = IOUtils.convertStreamToString(conn.getInputStream());

        } else {
            inputStream = conn.getErrorStream();
            if (inputStream != null)
                responseString = IOUtils.convertStreamToString(inputStream);
        }
        return new RequestAsyncTaskResponse(conn, responseCode, responseString);
    }

    private void startTimeOutTimer(long timeOutMillis, int intervalMillis) {
        timeoutTimer = new CountDownTimer(timeOutMillis, intervalMillis) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                stop();
            }
        }.start();
    }

    public void stop() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        } catch (Exception ignored) {
        }
        this.cancel(true);
    }

    @Override
    protected void onPostExecute(RequestAsyncTaskResponse response) {
        timeoutTimer.cancel();
        super.onPostExecute(response);
        listener.processFinish(response, exception);
        listener.processFinish(response, exception, args);
    }

    @Override
    protected void onCancelled() {
        timeoutTimer.cancel();
        super.onCancelled();
        exception = new TimeoutException("The operation failed to complete due to TimeOut");
        listener.processFinish(null, exception);
        listener.processFinish(null, exception, args);
    }


}

