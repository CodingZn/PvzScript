package src;

import static src.Util.obj2int;

import java.util.Collection;
import java.util.List;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class GeneralBattle {
    
    public static String resolveAwardObj(List<ASObject> awardArr, String tool_field, String amount_field){
        StringBuffer sb = new StringBuffer();
        for (ASObject object : awardArr) {
            int toolid = obj2int(object.get(tool_field));
            int amount = obj2int(object.get(amount_field));
            sb.append(Tool.getTool(toolid).toShortString(amount));
            sb.append(" ");
        }
        return sb.toString();
    }
    
    @SuppressWarnings({"unchecked"})
    public static String resolveAwardObj(ASObject awardObj, String field, String tool_field, String amount_field){
        List<ASObject> toolList = (List<ASObject>) awardObj.get(field);
        return resolveAwardObj(toolList, tool_field, amount_field);
    }

    public static boolean getAward(String award_key){
        byte[] reqAmf = Util.encodeAMF("api.reward.lottery", "/1", new Object[]{award_key});
        byte[] response = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(response);
        Log.log("award: ");
        if (Response.isOnStatusException(msg.getBody(0), true)){
            return false;
        }else{
            String awardString = resolveAwardObj((ASObject)msg.getBody(0).getValue(), "tools", "id", "amount");
            Log.println("[%s]".formatted(awardString));
            return true;
        }
    }

    public static String resolveFighter(Collection<Integer> plants){
        StringBuffer sb = new StringBuffer();
        sb.append("参战[");
        for (Integer integer : plants) {
            sb.append(Organism.getOrgShortStr(integer));
            sb.append(" ");
        }
        sb.append("] ");
        return sb.toString();
    }

    public static String resolveFighter(Collection<Integer> zhuli, Collection<Integer> paohui){
        StringBuffer sb = new StringBuffer();
        sb.append("主力[");
        for (Integer integer : zhuli) {
            sb.append(Organism.getOrgShortStr(integer));
            sb.append(" ");
        }
        sb.append("] 炮灰[");
        for (Integer integer : paohui) {
            sb.append(Organism.getOrgShortStr(integer));
            sb.append(" ");
        }
        sb.append("] ");
        return sb.toString();
    }

}
