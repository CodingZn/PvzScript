package src;

import static src.Request.sendPostAmf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

import static src.GeneralBattle.*;

public class FubenBattle {
    /** 策略0：不使用副本书
     * <HR/>
     * 策略1：刷前一次性使用副本书
     * <HR/>
     * 策略2：刷前一次性使用副本书和怀表
     */
    private static int strategy = 2;
    public static int setStrategy(int newStrategy){
        if (newStrategy>=0 && newStrategy<=2){
            strategy = newStrategy;
        }
        System.out.printf("new strategy: %d\n", strategy);
        return strategy;
    }

    public static final int FUBEN_BOOK_ID = 612;

    public static boolean battle(int caveid, List<Integer> plantIds){
        Object[] value = new Object[2];
        value[0] = caveid;
        Set<Integer> participants = new HashSet<>(plantIds);
        value[1] = Util.integerArr2int(participants.toArray());
        byte[] reqAmf = Util.encodeAMF("api.fuben.challenge", "/1", value);
        System.out.printf("打副本%d: ",caveid);
        System.out.print(resolveFighter(plantIds));
        byte[] resp = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(resp);
        if (msg==null) return false;
        if(Response.isOnStatusException(msg.getBody(0), true)){
            return false;
        }
        System.out.printf("√ ");
        ASObject resObj = (ASObject)msg.getBody(0).getValue();
        boolean res = getAward((String)resObj.get("awards_key"));
        res=BuXie.blindBuxie(resObj, plantIds, BuXie.EMPTY_LIST) && res;
        return res;
        
    }

    public static boolean useFubenBook(int n){
        Object[] value = new Object[2];
        value[0] = FUBEN_BOOK_ID;
        value[1] = n;
        byte[] req= Util.encodeAMF("api.tool.useOf", "/1", value);
        System.out.printf("use %d fuben books: ",n);
        byte[] res=sendPostAmf(req, true);
        AMF0Message msg = Util.decodeAMF(res);
        if (msg==null || Response.isOnStatusException(msg.getBody(0), true)) 
            return false;
        System.out.println(" √");

        return true;
    }

    public static boolean useFubenClock(int fubenCaveId, int n){
        Object[] value = new Object[2];
        value[0] = fubenCaveId;
        value[1] = n;
        byte[] req= Util.encodeAMF("api.fuben.addCaveChallengeCount", "/1", value);
        System.out.printf("for %d, use %d fuben clocks: ", fubenCaveId, n);
        byte[] res=sendPostAmf(req, true);
        AMF0Message msg = Util.decodeAMF(res);
        if (msg==null || Response.isOnStatusException(msg.getBody(0), true)) 
            return false;
        System.out.println(" √");

        return true;
    }

    public static boolean battleRepeat(int caveid, List<Integer> plantIds, int times_n){
        
        if(strategy==1){
            if (!useFubenBook(times_n)) return false;
        }else if(strategy==2){
            if (!useFubenBook(times_n)) return false;
            if (!useFubenClock(caveid, times_n)) return false;
        }
        if (!BuXie.buxie(plantIds, new ArrayList<>(), true)){
            System.out.println("战斗前补血失败！");
            return false;
        }
        for (int i = 0; i < times_n; i++) {
            if (!battle(caveid, plantIds)) return false;
        }
        
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 3){
            int caveid = Integer.parseInt(args[0]);
            List<Integer> plantIds = Util.readIntegersFromFile(args[1]);
            int times_n = Integer.parseInt(args[2]);
            battleRepeat(caveid, plantIds, times_n);
            return;
        }else if (args.length == 2 && args[0].equals("strategy")){
            int newStrategy = Integer.parseInt(args[1]);
            setStrategy(newStrategy);
            return;
        }

        System.out.println("args: caveid plantFile count_n");
        System.out.println("or  : strategy number");
    }
}
