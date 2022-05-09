package com.emmanuel.utils.httpRequest;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.net.HttpURLConnection;

public class RequestAsyncTaskResponse {
    private final int statusCode;
    //private HttpURLConnection connection;
    private final String responseJson;


    public RequestAsyncTaskResponse(HttpURLConnection connection, int statusCode, String responseJson) {
        //this.connection = connection;
        this.statusCode = statusCode;
        this.responseJson = responseJson;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public <T> T getResponseObject(Class<T> typeParameterClass) {
        JsonReader reader = new JsonReader(new StringReader(responseJson));
        reader.setLenient(true);
        return new Gson().fromJson(reader, typeParameterClass);
    }
}
