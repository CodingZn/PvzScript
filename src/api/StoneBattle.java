package src.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

import static src.api.GeneralBattle.*;

public class StoneBattle {

    private static boolean checkCount = false;

    public static boolean battle(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui, Collection<Integer> died){
        Set<Integer> plants = new HashSet<>(zhuli);
        plants.addAll(paohui);
        Object[] value = new Object[3];
        value[0] = caveid;
        value[1] = Util.integerArr2int(plants.toArray());
        value[2] = hard_level;
        byte[] bytes = Util.encodeAMF("api.stone.challenge", "/1", value);
        Log.log("打宝石%d: ".formatted(caveid));
        Log.print(resolveFighter(zhuli, paohui));
        byte[] response = Request.sendPostAmf(bytes, true);
        AMF0Message msg = Util.decodeAMF(response);
        if(Response.isOnStatusException(msg.getBody(0), true)){
            return false;
        }
        Log.println("√ ");
        ASObject resObj = (ASObject)msg.getBody(0).getValue();
        boolean res = getAward((String)resObj.get("awards_key"));
        SimpleEntry<Set<Integer>, Set<Integer>> attacked = BuXie.getAttacked(resObj, zhuli, paohui);
        res = BuXie.blindBuxie(attacked.getKey(), attacked.getValue())&&res;
        died.addAll(attacked.getValue());
        User.getUser().changeStoneCha(-1);
        return res;
    }

    public static boolean battleRepeat(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui, int n){
        if (!BuXie.buxie(zhuli, paohui, true)){
            Log.logln("战斗前补血失败！");
            return false;
        }
        if (checkCount) {
            if (User.getUser().getStoneCha() < n) {
                Log.logln("宝石挑战次数不足%d次。".formatted(n));
                return false;
            }
        }
        boolean res = true;
        PaohuiPool paohuiPool = new PaohuiPool(zhuli, paohui, 0, true);
        List<Integer> paohui_actual;
        Collection<Integer> died = new HashSet<>();
        for (int i = 0; i < n; i++) {
            paohui_actual = paohuiPool.getChosenPaohuis();
            died.clear();
            res = battle(caveid, hard_level, zhuli, paohui_actual, died);
            paohuiPool.updateExcept(paohui_actual, died);
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
        }else if (args.length==2 && args[0].equals("checkcount")){
            if (args[1].equals("on")) {
                checkCount=true;
                return;
            }else if (args[1].equals("off")){
                checkCount=false;
                return;
            }
        }
        System.out.println("args: <caveid> <hard_level> <total_count> <zhuli_file> <paohui_file>");
        System.out.println("or  : checkcount on|off");
    }
}
