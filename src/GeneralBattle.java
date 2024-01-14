package src;

import java.util.Collection;
import java.util.List;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class GeneralBattle {
    
    @SuppressWarnings({"unchecked"})
    public static String resolveAwardObj(ASObject awardObj){
        StringBuffer sb = new StringBuffer();
        List<ASObject> toolList = (List<ASObject>) awardObj.get("tools");
        for (ASObject object : toolList) {
            sb.append("%s(%d) ".formatted(
                object.get("id").toString(), Util.obj2int(object.get("amount"))));
            
        }
        return sb.toString();
    }

    public static boolean getAward(String award_key){
        byte[] reqAmf = Util.encodeAMF("api.reward.lottery", "/1", new Object[]{award_key});
        byte[] response = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(response);
        System.out.printf("award: ");
        if (Response.isOnStatusException(msg.getBody(0), true)){
            System.out.print("x\n");
            return false;
        }else{
            String awardString = resolveAwardObj((ASObject)msg.getBody(0).getValue());
            System.out.print("[%s]\n".formatted(awardString));
            return true;
        }
    }

    public static String resolveFighter(Collection<Integer> plants){
        StringBuffer sb = new StringBuffer();
        sb.append("参战[");
        for (Integer integer : plants) {
            sb.append(Organism.getOrganism(integer).toShortString());
            sb.append(" ");
        }
        sb.append("] ");
        return sb.toString();
    }

    public static String resolveFighter(Collection<Integer> zhuli, Collection<Integer> paohui){
        StringBuffer sb = new StringBuffer();
        sb.append("主力[");
        for (Integer integer : zhuli) {
            sb.append(Organism.getOrganism(integer).toShortString());
            sb.append(" ");
        }
        sb.append("] 炮灰[");
        for (Integer integer : paohui) {
            sb.append(Organism.getOrganism(integer).toShortString());
            sb.append(" ");
        }
        sb.append("] ");
        return sb.toString();
    }

}
