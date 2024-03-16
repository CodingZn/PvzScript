package src;

import static src.Util.obj2int;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class ServerBattle {
    public static boolean getOpponent(){
        byte[] req = Util.encodeAMF("api.serverbattle.getOpponent", "/1", new Object[]{});
        Log.log("寻找对手...");
        byte[] resp = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(resp).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        ASObject obj = (ASObject) body.getValue();
        ASObject opponent = (ASObject) obj.get("opponent");
        String server_name = (String)opponent.get("server_name");
        String nickname = (String)opponent.get("nickname");
        int grade = obj2int(opponent.get("grade"));
        Log.println(": %s lv.%d %s".formatted(server_name,grade,nickname));
        return true;
    }

    private static ASObject battleOnce(){
        byte[] req = Util.encodeAMF("api.serverbattle.challenge", "/1", new Object[]{});
        Log.log("跨服战斗");
        byte[] resp = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(resp).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return null;
        }
        ASObject obj = (ASObject)body.getValue();
        Boolean is_winning = (Boolean)obj.get("is_winning");
        Log.print(is_winning?" 胜利":" 失败");
        int upi = obj2int(obj.get("upi"));
        Log.print(" 积分%+d".formatted(upi));
        Log.println();
        return obj;
    }

    public static boolean serverBattle(int count){
        if (count==-1) {
            count = Integer.MAX_VALUE;
        }
        for (int i = 0; i < count; i++) {
            if(!getOpponent()) return false;
            ASObject obj = battleOnce();
            if (obj==null) {
                return false;
            }
            String awards_key = (String) obj.get("awards_key");
            GeneralBattle.getAward(awards_key);
            int sc = obj2int(obj.get("sc"));
            if (sc==0) {
                return true;
            }
        }
        return true;
    }

    public static boolean addCount(int n){
        byte[] req = Util.encodeAMF("api.serverbattle.addChallengeCount", "/1", new Object[]{});
        for (int i = 0; i < n; i++) {
            Log.log("增加一次跨服挑战");
            byte[] resp = Request.sendPostAmf(req, true);
            AMF0Body body = Util.decodeAMF(resp).getBody(0);
            if (Response.isOnStatusException(body, true)) {
                return false;
            }
            ASObject obj = (ASObject) body.getValue();
            int cost = obj2int(obj.get("cost"));
            int count = obj2int(obj.get("count"));
            Log.println(" 当前还有%d次，下次将花费%d金券".formatted(count,cost));
        }
        return true;
    }

    public static boolean getReward(){
        byte[] req = Util.encodeAMF("api.serverbattle.qualifyingReward", "/1", new Object[]{});
        Log.log("查看跨服奖励：");
        byte[] resp = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(resp).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        ASObject obj = (ASObject)body.getValue();
        int award_status = obj2int(obj.get("award_status"));
        if (award_status==0) {
            Log.println("没有奖励");
            return true;
        }else if (award_status==2) {
            Log.println("已经领取");
            return true;
        }else if (award_status!=1) {
            Log.println("未知状态%d！".formatted(award_status));
            return false;
        }
        int myOldRank = obj2int(obj.get("myOldRank"));
        ASObject myReward = (ASObject) obj.get("myReward");
        int min_rank = obj2int(myReward.get("min_rank"));
        int max_rank = obj2int(myReward.get("max_rank"));
        Log.println("排名%d，对应%d到%d档奖励".formatted(myOldRank,min_rank,max_rank));
        // 领取奖励
        byte[] req2 = Util.encodeAMF("api.serverbattle.qualifyingAward", "/1", new Object[]{});
        Log.log("领取奖励：");
        byte[] resp2 = Request.sendPostAmf(req2, true);
        AMF0Body body2 = Util.decodeAMF(resp2).getBody(0);
        if (Response.isOnStatusException(body2, true)){
            return false;
        }
        ASObject obj2 = (ASObject)body2.getValue();
        String awardStr = GeneralBattle.resolveAwardObj(obj2, "tool_id", "amount");
        Log.println("[%s]".formatted(awardStr));
        return true;
    }

    public static void main(String[] args) {
        if (args.length==1 && args[0].equals("award")) {
            getReward();
            return;
        }
        else if (args.length==1) {
            int count;
            if (args[0].equals("auto")){
                count = -1;
            }else{
                count = Integer.parseInt(args[0]);
            }
            serverBattle(count);
            return;
        }
        else if (args.length==2 && args[0].equals("add")) {
            int num = Integer.parseInt(args[1]);
            addCount(num);
            return;
        }
        System.out.println("args: <count>|auto");
        System.out.println("or  : award");
        System.out.println("or  : add <count>");
    }
}
