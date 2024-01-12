package src;

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
                System.out.print(obj.get("description"));
                System.out.print(" ");
            }
            return true;
        }
        else if (body.getTarget().equals("/1/onResult")){
            return false;
        }
        else{
            System.out.println("未知响应：");
            System.out.println(obj);
            return true;
        }
    }

    public static String getExceptionDescription(AMF0Body body){
        return (String) ((ASObject)body.getValue()).get("description");
    }
}
