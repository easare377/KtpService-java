package com.emmanuel.ktpservice.event;

import com.emma.general_backend_library.interfaces.annotations.EventHubActivityAnnotation;
import com.emma.general_backend_library.models.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EventHub {
    private static final Map<String, Object> eventHubMap = new HashMap<>();

    public static void register(String channel, Object obj){
        if (eventHubMap.containsKey(channel)) {
            throw new UnsupportedOperationException("This channel has already been registered.");
        }
        eventHubMap.put(channel, obj);
    }

    public static void release(String channel){
        eventHubMap.remove(channel);
    }

    public static boolean channelExists(String channel){
        return eventHubMap.containsKey(channel);
    }

    public static EventHubActivity event(String channel, String activityName) {
        Object o = eventHubMap.get(channel);
        return new EventHubActivity(activityName) {
            @Override
            public Object onInvoke(String activityName, Map<String, Object> paramDict) throws InvocationTargetException, IllegalAccessException {
                    return handleInstructions(o, activityName, paramDict);
            }
        };
    }

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

    private static Object handleInstructions(Object obj, String activityName, Map<String, Object> args) throws InvocationTargetException, IllegalAccessException {
        //args.addProperty("cameraType", String.valueOf(CameraType.Front));
        String[] paramNames = new String[args.keySet().size()];
        paramNames = args.keySet().toArray(paramNames);

        Method m = getAnnotationMethod(obj, activityName, paramNames);
        if (m != null) {
            if (m.getParameterTypes().length != paramNames.length) {
                throw new
                        IndexOutOfBoundsException("The parameters provided does not match the parameters in " + m.getName());
            }
            Parameter[] params = new Parameter[paramNames.length];
            for (int i = 0; i < paramNames.length; i++) {
                params[i] = new Parameter(paramNames[i], m.getParameterTypes()[i]);
            }
            Object[] paramValues = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                //String json = Functions.serializeObjectToJson(args.get(params[i].getName()).getAsJsonObject());
                Parameter p = params[i];
                Object value = args.get(p.getName());
                paramValues[i] = value;
                //Functions.deserializeObjectFromJson(params[i].getType(), json);
            }
            //m.setAccessible(true); // Need to do this so we can run private methods
            return m.invoke(obj, paramValues);
        }
        throw new UnsupportedOperationException("Method does not exist");
    }

}
