package src;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Response {
    
    /** handle onStatus exception */
    public static boolean isOnStatusException(AMF0Body body, boolean printMsg){
        
        ASObject obj = (ASObject)body.getValue();
        if (body.getTarget().equals("/1/onStatus")){
            if (printMsg){
                System.out.print("请求错误：");
                System.out.println(obj.get("description"));
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