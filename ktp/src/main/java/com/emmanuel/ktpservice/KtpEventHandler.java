package com.emmanuel.ktpservice;

import com.emmanuel.ktpservice.interfaces.ISocketConnectionHandler;
import com.emmanuel.ktpservice.interfaces.KtpEventListener;
import com.emmanuel.ktpservice.interfaces.RemoteActivityListener;

import com.emmanuel.ktpservice.interfaces.SocketConnectionListener;
import com.emmanuel.ktpservice.interfaces.annotations.EventHubActivityAnnotation;
import com.emmanuel.ktpservice.models.KtpEventMessage;
import com.emmanuel.ktpservice.models.KtpEventResponse;
import com.emmanuel.ktpservice.models.Parameter;
import com.emmanuel.ktpservice.models.ResponseActivity;
import com.emmanuel.ktpservice.models.enums.MessageType;
import com.emmanuel.utils.Utils;
import com.google.gson.JsonObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public abstract class KtpEventHandler extends KtpClientHandler {

    private final Map<UUID, ResponseActivity> remoteActivityListenerMap;
    public KtpEventHandler(KtpEventListener listener) {
        super(null);
        remoteActivityListenerMap = new HashMap<>();
        super.listener = new SocketConnectionListener() {
            @Override
            public void onConnected(ISocketConnectionHandler connectionHandler) {
                listener.onConnected(connectionHandler);
            }

            @Override
            public void onConnectionFailed(ISocketConnectionHandler connectionHandler, Exception e) {
                listener.onConnectionFailed(connectionHandler, e);
            }

            @Override
            public void onMessageReceived(ISocketConnectionHandler connectionHandler, SocketMessageHandler messageHandler) {
                JsonObject resp = messageHandler.getMessageObject(JsonObject.class);
                //If the message type is equals to request,
                //it indicates that a remote device has sent a request through the connection and is
                //awaiting a response.
                //If the message type is equals to response,
                //it indicates that a request was sent from this device and the remote device has
                //processed the request and sent a response.
                if(MessageType.valueOf(resp.get("messageType").getAsString()) == MessageType.Request){
                    KtpEventMessage mess =
                            Utils.deserializeObjectFromJson(KtpEventMessage.class,
                                    Utils.serializeObjectToJson(resp));
                    KtpEventResponse ktpResponse;
                    try {
                        Object result = handleInstructions(KtpEventHandler.this, mess.getActivityName(), mess.getParams());
                        ktpResponse = new KtpEventResponse(mess.getActivityId(), MessageType.Response, result);
                    } catch (Exception e) {
                        ktpResponse = new KtpEventResponse(mess.getActivityId(), MessageType.Response,
                                null, "remoteInvocationError");
                    }
                    connectionHandler.sendObjectAsync(ktpResponse);
                }else{
                    KtpEventResponse mess =
                            Utils.deserializeObjectFromJson(KtpEventResponse.class,
                                    Utils.serializeObjectToJson(resp));
                    if(remoteActivityListenerMap.containsKey(mess.getActivityId())){
                        ResponseActivity respActivity = remoteActivityListenerMap.remove(mess.getActivityId());
                        String json = Utils.serializeObjectToJson(mess.getResult());
                        Object o = Utils.deserializeObjectFromJson(respActivity.getTypeParameterClass(),json);
                        Exception e = null;
                        if (mess.getError() != null){
                            e = new Exception("Could not invoke specified activity.");
                        }
                        respActivity.getListener().onResponse(o, e);
                    }
                }
            }

            @Override
            public void onClosed(ISocketConnectionHandler connectionHandler) {
                listener.onClosed(connectionHandler);
                //If the connection is closed before
                for (ResponseActivity value : remoteActivityListenerMap.values()) {
                    value.getListener().onResponse(null, new SocketException());
                }
            }
        };

    }

    public KtpEventActivity remoteActivity(String activityName, RemoteActivityListener listener) {
        return new KtpEventActivity(activityName) {
            UUID activityId;
            @Override
            public <T> T onInvoke(Class<T> typeParameterClass, String activityName, Map<String, Object> paramDict) {
                //If there is no connection an error response is sent to the caller.
                if (!isConnected()) {
                    listener.onResponse(null, new SocketException());
                    return null;
                }
                //Send message
                activityId = UUID.randomUUID();
                ResponseActivity<T> responseActivity = new ResponseActivity<>(listener, typeParameterClass);
                remoteActivityListenerMap.put(activityId, responseActivity);
                sendObjectAsync(new KtpEventMessage(activityId, activityName, MessageType.Request, paramDict));
                return null;
            }

            @Override
            public void onTimeout(int timeoutMillis) {
                remoteActivityListenerMap.remove(activityId).getListener().onResponse(null, new TimeoutException());
            }
        };
    }


//    private static Method getAnnotationMethod(Object obj, String activityName, String[] params) {
//        Method[] var3 = obj.getClass().getMethods();
//        int var4 = var3.length;
//
//        for(int var5 = 0; var5 < var4; ++var5) {
//            Method m = var3[var5];
//            Annotation[] var7 = m.getAnnotations();
//            int var8 = var7.length;
//
//            for(int var9 = 0; var9 < var8; ++var9) {
//                Annotation a = var7[var9];
//                if (a instanceof EventHubActivityAnnotation) {
//                    EventHubActivityAnnotation activity = (EventHubActivityAnnotation)a;
//                    if (!activity.activityName().equals(activityName)) {
//                        break;
//                    }
//
//                    boolean arrayElementsEqual = true;
//
//                    for(int i = 0; i < params.length; ++i) {
//                        if (!params[i].equals(activity.params()[i])) {
//                            arrayElementsEqual = false;
//                            break;
//                        }
//                    }
//
//                    if (arrayElementsEqual) {
//                        return m;
//                    }
//                    break;
//                }
//            }
//        }
//
//        return null;
//    }

    private static Method getAnnotationMethod(Object obj, String activityName, String[] params) {
        //Get all methods in this class instance.
        for (Method m : obj.getClass().getMethods()) {
            //get all annotations of this method.
            for (Annotation a : m.getAnnotations()) {
                //Check if annotation is an instance of the activity annotation.
                if (a instanceof EventHubActivityAnnotation) {
                    EventHubActivityAnnotation activity = (EventHubActivityAnnotation) a;
                    //If an instance of the Activity Annotation is found for this method,
                    //check if the Activity Annotation activityName field is equals to the
                    // activityName provided and the params field matches the supplied params.
                    //if the conditions are true the method is returned.
                    if (activity.activityName().equals(activityName)) {
                        boolean arrayElementsEqual = true;
                        for (int i = 0; i < params.length; i++) {
                            if (!params[i].equals(activity.params()[i])) {
                                arrayElementsEqual = false;
                                break;
                            }
                        }
                        if (arrayElementsEqual) {
                            return m;
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    private static Object handleInstructions(Object obj, String activityName, Map<String, Object> args)
            throws InvocationTargetException, IllegalAccessException {
        String[] paramNames = new String[args.keySet().size()];
        paramNames = (String[])args.keySet().toArray(paramNames);
        //Get method or function with EventHubActivityAnnotation and the right signature.
        Method m = getAnnotationMethod(obj, activityName, paramNames);
        if (m == null) {
            throw new UnsupportedOperationException("Method does not exist");
        } else if (m.getParameterTypes().length != paramNames.length) {
            throw new IndexOutOfBoundsException("The parameters provided does not match the parameters in " + m.getName());
        } else {
            Parameter[] params = new Parameter[paramNames.length];
            Class[] types = m.getParameterTypes();
            for(int i = 0; i < paramNames.length; ++i) {
                if (types[1].isArray()){

                }
                params[i] = new Parameter(paramNames[i], m.getParameterTypes()[i]);
            }

            Object[] paramValues = new Object[params.length];

            for(int i = 0; i < params.length; ++i) {
                Parameter p = params[i];
                Object value = args.get(p.getName());
                paramValues[i] = value;
            }
//            if (paramValues.length == 0){
//                return m.invoke(obj, paramValues);
//            }else{
//                return m.invoke(obj);
//            }
            return m.invoke(obj, paramValues);
        }
    }

}
