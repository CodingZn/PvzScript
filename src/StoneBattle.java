package src;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class StoneBattle {
    public static boolean battle(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        Set<Integer> plants = new HashSet<>(zhuli);
        plants.addAll(paohui);
        Object[] value = new Object[3];
        value[0] = caveid;
        value[1] = Util.integerArr2int(plants.toArray());
        value[2] = hard_level;
        byte[] bytes = Util.encodeAMF("api.stone.challenge", "/1", value);
        byte[] response = Request.sendPostAmf(bytes, true);
        System.out.printf("打宝石%d: ", caveid);
        AMF0Message msg = Util.decodeAMF(response);
        if(Response.isOnStatusException(msg.getBody(0), true)){
            return false;
        }
        
        ASObject resObj = (ASObject)msg.getBody(0).getValue();
        boolean res = Battle.getAward((String)resObj.get("awards_key"));
        SimpleEntry<Set<Integer>, Set<Integer>> attacked = BuXie.getAttacked(resObj, zhuli, paohui);
        res=BuXie.blindBuxie(attacked.getKey(), attacked.getValue()) && res;
        return res;
    }

    public static boolean battleRepeat(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui, int n){
        if (!BuXie.buxie(zhuli, paohui)){
            System.out.println("补血失败！");
        }
        boolean res = true;
        for (int i = 0; i < n; i++) {
            res = battle(caveid, hard_level, zhuli, paohui);
            if (!res) break;
        }
        return res;
    }

    public static void main(String[] args) {
        if (args.length==5) {
            int caveid = Integer.parseInt(args[0]);
            int hard_level = Integer.parseInt(args[1]);
            int total_count = Integer.parseInt(args[2]);
            List<Integer> zhuli = Util.readIntegersFromFile(args[3]);
            List<Integer> paohui = Util.readIntegersFromFile(args[4]);
            battleRepeat(caveid, hard_level, zhuli, paohui, total_count);
            return;
        }
        System.out.println("args: caveid hard_level total_count zhuli_file paohui_file");
    }
}
