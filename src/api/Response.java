package src.api;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Response {
    
    /** handle onStatus exception */
    public static boolean isOnStatusException(AMF0Body body, boolean printMsg){
        if (!(body.getValue() instanceof ASObject)){
            return false;
        }
        ASObject obj = (ASObject)body.getValue(); 
        if (body.getTarget().equals("/1/onStatus")){
            if (printMsg){
                Log.println((String)obj.get("description"));
            }
            return true;
        }
        else if (body.getTarget().equals("/1/onResult")){
            return false;
        }
        else{
            Log.println("未知响应：");
            Log.println(obj.toString());
            return true;
        }
    }

    /** nullable if not exception */
    public static String getExceptionDescription(byte[] response){
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (isOnStatusException(body, false)){
            return getExceptionDescription(body);
        }
        return null;
    }

    public static String getExceptionDescription(AMF0Body body){
        return (String) ((ASObject)body.getValue()).get("description");
    }
}
