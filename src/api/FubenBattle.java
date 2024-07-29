package src.api;

import static src.api.GeneralBattle.*;
import static src.api.Request.sendPostAmf;
import static src.api.Util.obj2int;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class FubenBattle {
    /** 策略0：不使用副本书
     * <HR/>
     * 策略1：刷前一次性使用副本书
     * <HR/>
     * 策略2：刷前一次性使用副本书和怀表
     */
    private static int strategy = 0;
    public static int setStrategy(int newStrategy){
        if (newStrategy>=0 && newStrategy<=2){
            strategy = newStrategy;
        }
        Log.log("new strategy: %d\n".formatted(strategy));
        return strategy;
    }

    private static int autoFBbook = 0;
    public static int setAutoFBbook(int newValue){
        if (newValue>=0){
            autoFBbook = newValue;
        }
        Log.log("new autoFBbook: %d\n".formatted(autoFBbook));
        return autoFBbook;
    }

    public static final int FUBEN_BOOK_ID = 612;

    public static boolean battle(int caveid, List<Integer> plantIds){
        Object[] value = new Object[2];
        value[0] = caveid;
        Set<Integer> participants = new HashSet<>(plantIds);
        value[1] = Util.integerArr2int(participants.toArray());
        if (autoFBbook>0 && User.getUser().getFubenCha()<=0){
            if (!useFubenBook(autoFBbook)) return false;
        }
        AMF0Message msg = null;
        do {
            byte[] reqAmf = Util.encodeAMF("api.fuben.challenge", "/1", value);
            Log.log("打副本%s: ".formatted(FubenItem.map.get(caveid)));
            Log.print(resolveFighter(plantIds));
            byte[] resp = Request.sendPostAmf(reqAmf, true);
            msg = Util.decodeAMF(resp);
            if (msg==null) return false;
            if(Response.isOnStatusException(msg.getBody(0), true)){
                String exc = Response.getExceptionDescription(msg.getBody(0));
                if (exc.equals("Exception:副本挑战次数不够")){
                    if (autoFBbook>0 && useFubenBook(autoFBbook)){
                        continue;
                    }
                }
                return false;
            }
            break;
        } while (true);
        Log.println("√ ");
        User.getUser().changeFubenCha(-1);
        ASObject resObj = (ASObject)msg.getBody(0).getValue();
        boolean res = getAward((String)resObj.get("awards_key"));
        res=BuXie.blindBuxie(resObj, plantIds, BuXie.EMPTY_LIST) && res;
        return res;
        
    }

    public static boolean useFubenBook(int n){
        boolean res = Warehouse.useTool(FUBEN_BOOK_ID,n);
        if (res) {
            User.getUser().changeFubenCha(n);
        }
        return res;
    }

    public static boolean useFubenClock(int fubenCaveId, int n){
        Object[] value = new Object[2];
        value[0] = fubenCaveId;
        value[1] = n;
        byte[] req= Util.encodeAMF("api.fuben.addCaveChallengeCount", "/1", value);
        Log.log("对关卡 %s 使用 %d 个怀表: ".formatted(FubenItem.map.get(fubenCaveId), n));
        byte[] res=sendPostAmf(req, true);
        AMF0Message msg = Util.decodeAMF(res);
        if (msg==null || Response.isOnStatusException(msg.getBody(0), true)) 
            return false;
        Log.println(" √");

        return true;
    }

    public static boolean battleRepeat(int caveid, List<Integer> plantIds, int times_n){
        
        if(strategy==1){
            if (!Warehouse.useTool(FUBEN_BOOK_ID,times_n)) return false;
        }else if(strategy==2){
            if (!Warehouse.useTool(FUBEN_BOOK_ID,times_n)) return false;
            if (!useFubenClock(caveid, times_n)) return false;
        }
        if (!BuXie.buxie(plantIds, new ArrayList<>(), true)){
            Log.logln("战斗前补血失败！");
            return false;
        }
        for (int i = 0; i < times_n; i++) {
            if (!battle(caveid, plantIds)) return false;
        }
        
        return true;
    }

    public static boolean openFuben(int id){
        byte[] req = Util.encodeAMF("api.fuben.openCave", "/1", new Object[]{id});
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            if (Response.getExceptionDescription(body).equals("Exception:此关卡已经开启")){
                return true;
            }
            return false;
        }
        ASObject obj = ((ASObject)body.getValue());
        if (obj2int(obj.get("cave_id"))==id){
            Log.logln("成功开启%d-%s".formatted(id,(String)obj.get("name")));
            return true;
        }
        Log.logln("未知错误！");
        return false;
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
        }else if (args.length == 2 && args[0].equals("autofbbook")){
            int newStrategy = Integer.parseInt(args[1]);
            setAutoFBbook(newStrategy);
            return;
        }else if (args.length == 2 && args[0].equals("usebook")){
            int n = Integer.parseInt(args[1]);
            useFubenBook(n);
            return;
        }else if (args.length == 2 && args[0].equals("open")){
            int n = Integer.parseInt(args[1]);
            openFuben(n);
            return;
        }

        System.out.println("args: <caveid> <plantFile> <count_n>");
        System.out.println("or  : strategy <number>");
        System.out.println("or  : autofbbook <number>");
        System.out.println("or  : usebook <number>");
        System.out.println("or  : open <number>");
    }
}
