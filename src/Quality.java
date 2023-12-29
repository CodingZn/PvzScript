package src;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;
import lib.MyAMF0Serializer;

public class Quality {
    
    public static byte[] getQualityUpAmf(int plantId) throws IOException{
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream doo = new DataOutputStream(ba);
        MyAMF0Serializer serializer = new MyAMF0Serializer(doo);
        int[] value = {plantId};
        AMF0Body mBody = new AMF0Body("api.apiorganism.qualityUp", "/1", value, AMF0Body.DATA_TYPE_ARRAY);
        AMF0Message msg = new AMF0Message();
        msg.addBody(mBody);
        msg.setVersion(3);
        serializer.serializeMessage(msg);
        
        return ba.toByteArray();
        
    }
    
    /** @return new quality name */
    public static String resolveResponseAmf(byte[] bytes)throws IOException{
        
        AMF0Message message = Util.decodeAMF(bytes);
        ASObject value = (ASObject) message.getBody(0).getValue();
        if (value.containsKey("quality_name")) {
            return (String) value.get("quality_name");
        } 
        else 
            return null;
    }

    public static SimpleEntry<Integer, String> qualityUp(int plantId, String goal, int maximum) throws IOException{
        String now_quality="";
        int total_use = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (!now_quality.equals(goal) && total_use < maximum) {
            byte[] resp = Request.sendPostAmf(body, true);
            total_use++;
            now_quality = resolveResponseAmf(resp);
            System.out.printf("used: %d; curr: %s;\n", total_use, now_quality);
        }

        return new SimpleEntry<Integer,String>(total_use, now_quality);
    }

    public static SimpleEntry<Integer, String> qualityUp(int plantId, int maximum) throws IOException{
        String now_quality="";
        int total_use = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (total_use < maximum) {
            byte[] resp = Request.sendPostAmf(body, true);
            total_use++;
            now_quality = resolveResponseAmf(resp);
            System.out.printf("used: %d; curr: %s;\n", total_use, now_quality);
        }

        return new SimpleEntry<Integer,String>(total_use, now_quality);
    }

    
    public static void main(String[] args) throws IOException{
        // System.out.println(args[0]);
        // return;
        if (args.length == 3 || args.length == 4){
            if (args[0].equals("goal")){
                int plantId = Integer.parseInt(args[1]);
                String goalQuality = args[2];
                int maximum = Integer.MAX_VALUE;
                if (args.length == 4){
                    maximum = Integer.parseInt(args[3]);
                }
                SimpleEntry<Integer, String> res = qualityUp(plantId, goalQuality, maximum);
                System.out.printf("plant %d: %d books --> %s.\n", plantId, res.getKey(), res.getValue());
                return;
            }else if (args[0].equals("count") && args.length == 3){
                int plantId = Integer.parseInt(args[1]);
                int maximum = Integer.parseInt(args[2]);

                SimpleEntry<Integer, String> res = qualityUp(plantId,  maximum);
                System.out.printf("plant %d: %d books --> %s.\n", plantId, res.getKey(), res.getValue());
                return;
            }
        }
        System.out.println("args: goal plantid quality_name [max_usage]\n");
        System.out.println("or  : count plantid maximum\n");
    }
}
