package src.api;

import static src.api.Util.obj2int;

import java.util.Collection;
import java.util.List;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class GeneralBattle {

    /** 解析单个奖励对象 */
    public static String resolveAwardObj(ASObject awardObj, String tool_field, String amount_field){
        StringBuffer sb = new StringBuffer();
        int toolid = obj2int(awardObj.get(tool_field));
        int amount = obj2int(awardObj.get(amount_field));
        sb.append(Tool.getTool(toolid).toShortString(amount));
        sb.append(" ");
        // 增加数量
        MyTool.getTool(toolid).changeAmount(amount);
        return sb.toString();
    }
    
    /** 解析奖励列表 */
    public static String resolveAwardListObj(List<ASObject> awardArr, String tool_field, String amount_field){
        StringBuffer sb = new StringBuffer();
        for (ASObject object : awardArr) {
            sb.append(resolveAwardObj(object, tool_field, amount_field));
        }
        return sb.toString();
    }
    
    /** obj中有属性 field，代表奖励列表 */
    @SuppressWarnings({"unchecked"})
    public static String resolveAwardParentObj(ASObject obj, String field, String tool_field, String amount_field){
        List<ASObject> toolList = (List<ASObject>) obj.get(field);
        return resolveAwardListObj(toolList, tool_field, amount_field);
    }

    public static boolean getAward(String award_key){
        byte[] reqAmf = Util.encodeAMF("api.reward.lottery", "/1", new Object[]{award_key});
        byte[] response = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(response);
        Log.log("award: ");
        if (Response.isOnStatusException(msg.getBody(0), true)){
            return false;
        }else{
            String awardString = resolveAwardParentObj((ASObject)msg.getBody(0).getValue(), "tools", "id", "amount");
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
