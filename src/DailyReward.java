package src;

import static src.Util.obj2int;

import java.util.Date;
import java.util.List;
import java.util.Random;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class DailyReward {

    /** 1新签到 */
    @SuppressWarnings({"unchecked"})
    public static boolean newSignIn(){
        byte[] req = Util.encodeAMF("api.active.sign", "/1", new Object[]{});
        Log.log("新签到 ");
        byte[] response = Request.sendPostAmf(req, true);
        if (Response.isOnStatusException(Util.decodeAMF(response).getBody(0), true)){
            return false;
        }
        Log.println("√");
        // 查看累计奖励
        req = Util.encodeAMF("api.active.getSignInfo", "/1", new Object[]{});
        response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, false)){
            return false;
        }
        List<ASObject> rewardList =(List<ASObject>) ((ASObject) body.getValue()).get("signreward");
        for (ASObject reward : rewardList) {
            if (obj2int(reward.get("state"))==1){
                newSignReward(obj2int(reward.get("id")));
            }
        }
        return true;
    }

    /** 签到累计 */
    public static boolean newSignReward(int i){
        byte[] req = Util.encodeAMF("api.active.rewardTimes", "/1", new Object[]{i});
        Log.log("领第%d累计签到 ".formatted(i));
        byte[] response = Request.sendPostAmf(req, true);
        if (Response.isOnStatusException(Util.decodeAMF(response).getBody(0), true)){
            return false;
        }
        Log.println("√");
        return true;
    }

    /** 2领取Vip奖励 */
    @SuppressWarnings({"unchecked"})
    public static boolean getVip(){
        byte[] req = Util.encodeAMF("api.vip.awards", "/1", new Object[]{});
        Log.log("领取Vip奖励 ");
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        Log.println("成功 获得%s".formatted(GeneralBattle.resolveAwardListObj((List<ASObject>) body.getValue(), "tool_id", "amount")));
        
        return true;
    }

    /** 3世界树 */
    public static boolean worldTree(){
        String path = "/pvz/index.php/tree/addheight/sig/eda0868a124ba4c01efe8b4b0cb6d11e?%d".formatted(new Date().getTime());
        Log.log("给世界树施肥 ");
        byte[] response = Request.sendGetRequest(path);
        String msg = Util.getXmlMessage(Util.parseXml(response));
        if (msg==null){
            Log.println("成功");
            return true;
        }
        else{
            Log.println(msg);
            return false;
        }
    }

    /** 4登录奖励 */
    public static boolean getDailyReward(){
        byte[] req = Util.encodeAMF("api.guide.getDailyReward", "/1", new Object[]{});
        Log.log("领取登录奖励 ");
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        Log.println(body.getValue().toString());
        return true;
    }

    /** 5领取斗技场奖励 */
    @SuppressWarnings({"unchecked"})
    public static boolean arenaWeek(){
        // 查看信息
        byte[] req = Util.encodeAMF("api.arena.getAwardWeekInfo", "/1", new Object[]{});
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, false)){
            return false;
        }
        ASObject rankObject =(ASObject) ((ASObject) body.getValue()).get("rank");
        int rank = obj2int(rankObject.get("rank"));
        int is_reward = obj2int(rankObject.get("is_reward"));
        // 领取奖励 
        if (rank!=0 && is_reward==0){
            byte[] req2 = Util.encodeAMF("api.arena.awardWeek", "/1", new Object[]{});
            Log.log("领取斗技场奖励 ");
            byte[] response2 = Request.sendPostAmf(req2, true);
            if (Response.isOnStatusException(Util.decodeAMF(response2).getBody(0), true)
            && (Boolean) Util.decodeAMF(response2).getBody(0).getValue()){
                return false;
            }
            Log.print("√ ");
            List<ASObject> awardArr =(List<ASObject>) ((ASObject) body.getValue()).get("award");
            for (ASObject award : awardArr) {
                if (obj2int(award.get("award"))==1){
                    Log.print("获得 [%s]".formatted(GeneralBattle.resolveAwardParentObj(award, "tool", "id", "amount")));
                    return true;
                }
            }
            Log.println();
        }

        return true;
    }

    /** 6矿坑 */
    @SuppressWarnings({"unchecked"})
    public static boolean beatKuangkeng(){
        Log.log("查看矿坑");
        byte[] req = Util.encodeAMF("api.zombie.getInfo", "/1", new Object[]{});
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        Log.println();
        ASObject obj = (ASObject) body.getValue();
        int count = obj2int(obj.get("count"));
        List<ASObject> zombies = (List<ASObject>) obj.get("zombies");
        for (ASObject zom : zombies) {
            int hp = obj2int(zom.get("hp"));
            int id = obj2int(zom.get("id"));
            if (count>hp){
                beatZombie(id, hp);
                count-=hp;
            }
            else{
                beatZombie(id, count);
                break;
            }
        }
        return true;
    }

    private static boolean beatZombie(int id, int count){
        byte[] req = Util.encodeAMF("api.zombie.beat", "/1", new Object[]{id});
        
        for (int i = 0; i < count; i++) {
            Log.log("攻击矿坑%d: %d".formatted(id,i+1));
            byte[] response = Request.sendPostAmf(req, true);
            if (Response.isOnStatusException(Util.decodeAMF(response).getBody(0), true)){
                return false;
            }
            Log.println("√");
        }
        return true;
    }

    /** 7打斗技场 */
    @SuppressWarnings({"unchecked"})
    public static boolean beatArena(){
        int count;
        do {
            Log.log("查看斗技场 ");
            byte[] req = Util.encodeAMF("api.arena.getArenaList", "/1", new Object[]{});
            byte[] response = Request.sendPostAmf(req, true);
            AMF0Body body = Util.decodeAMF(response).getBody(0);
            if (Response.isOnStatusException(body, true)){
                return false;
            }
            ASObject obj = (ASObject) body.getValue();
            count = obj2int(((ASObject)obj.get("owner")).get("num"));
            if (count==0) {
                Log.println();
                return true;
            }
            List<ASObject> opponents = (List<ASObject>) obj.get("opponent");
            
            ASObject opp = opponents.get(new Random().nextInt(opponents.size()));
            int opp_id = obj2int(opp.get("userid"));

            byte[] req2 = Util.encodeAMF("api.arena.challenge", "/1", new Object[]{opp_id});
            Log.print("挑战%s(%d) ".formatted(opp.get("nickname"), obj2int(opp.get("grade"))));
            byte[] response2 = Request.sendPostAmf(req2, true);
            AMF0Body body2 = Util.decodeAMF(response2).getBody(0);
            if (Response.isOnStatusException(body2, true)){
                return false;
            }
            ASObject fightObj = (ASObject) body2.getValue();
            Log.print((Boolean)fightObj.get("is_winning")?"成功 ":"失败 ");
            Log.println("排名变为%d".formatted(obj2int(fightObj.get("rank"))));
            GeneralBattle.getAward(fightObj.get("awards_key").toString());
            count--;

        } while (count>0);
        
        return true;
    }


    /** 8领取任务 */
    @SuppressWarnings({"unchecked"})
    public static boolean getDuty(){
        byte[] req = Util.encodeAMF("api.duty.getAll", "/1", new Object[]{});
        Log.log("查看每日任务 ");
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        Log.println();
        ASObject obj = (ASObject) body.getValue();
        // 日常任务
        List<ASObject> dailyTasks = (List<ASObject>) obj.get("dailyTask");
        for (ASObject task : dailyTasks) {
            int id = obj2int(task.get("id"));
            int state = obj2int(task.get("state"));
            if (state==1){
                getReward(id,3, processStr(task.get("dis").toString()));
            }
        }
        // 主线任务
        List<ASObject> mainTask = (List<ASObject>) obj.get("mainTask");
        for (ASObject task : mainTask) {
            int id = obj2int(task.get("id"));
            int state = obj2int(task.get("state"));
            if (state==1){
                getReward(id,1, processStr(task.get("dis").toString()));
            }
        }
        // 支线任务
        List<ASObject> sideTask = (List<ASObject>) obj.get("sideTask");
        for (ASObject task : sideTask) {
            int id = obj2int(task.get("id"));
            int state = obj2int(task.get("state"));
            if (state==1){
                getReward(id,2, processStr(task.get("dis").toString()));
            }
        }
        // 活动任务
        List<ASObject> activeTask = (List<ASObject>) obj.get("activeTask");
        for (ASObject task : activeTask) {
            int id = obj2int(task.get("id"));
            int state = obj2int(task.get("state"));
            if (state==1){
                getReward(id,4, processStr(task.get("dis").toString()));
            }
        }
        return true;
    }

    private static String processStr(String str){
        StringBuffer sb = new StringBuffer();
        boolean write = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c=='<'){
                write = false;
            }else if (c=='>'){
                write = true;
            }else if (write){
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private static boolean getReward(int i,int type, String desc){
        int[] content = new int[]{i, type};
        byte[] rewardAmf = Util.encodeAMF("api.duty.reward", "/1", content, AMF0Body.DATA_TYPE_ARRAY);
        
        byte[] response;
        Log.log("领取任务 %s: ".formatted(desc));
        response = Request.sendPostAmf(rewardAmf, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }else{
            Log.print("√ \n");
            return true;
        }
        
    }

    /** 9花园挂机 */
    public static boolean vipGuaji(){
        byte[] req = Util.encodeAMF("api.vip.autoRewards", "/1", new Object[]{});
        Log.log("查看花园挂机 ");
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        Log.println();
        if (body.getValue()!=null){
            stopGuaji();
        }
        startGuaji();
        return true;
    }

    public static boolean stopGuaji(){
        byte[] req = Util.encodeAMF("api.vip.stopAuto", "/1", new Object[]{});
        Log.log("停止花园挂机 ");
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        if (body.getValue()!=null) {
            ASObject obj = (ASObject) body.getValue();
            ASObject reward = (ASObject)obj.get("reward");
            int exp = obj2int(reward.get("user_exp"));
            int money = obj2int(reward.get("money"));
            Log.print("获得%d经验和%d金币 ".formatted(exp, money));
            boolean is_up = (Boolean) obj.get("is_up");
            if (is_up){
                Log.print("升级啦！ ");
            }
        }
        Log.println();
        return true;
    }

    public static boolean startGuaji(){
        byte[] req = Util.encodeAMF("api.vip.startAuto", "/1", new Object[]{});
        Log.log("开始花园挂机 ");
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        if (body.getValue()!=null) {
            Log.print("成功");
        }
        Log.println();
        return true;
    }

    
    
    public static void main(String[] args) {
        if (args.length==1){
            if (args[0].contains("1")) {
                newSignIn();
            }
            if (args[0].contains("2")) {
                getVip();
            }
            if (args[0].contains("3")) {
                worldTree();
            }
            if (args[0].contains("4")) {
                getDailyReward();
            }
            if (args[0].contains("5")) {
                arenaWeek();
            }
            if (args[0].contains("6")) {
                beatKuangkeng();
            }
            if (args[0].contains("7")) {
                beatArena();
            }
            if (args[0].contains("8")) {
                getDuty();
            }
            if (args[0].contains("9")) {
                vipGuaji();
            }
            return;
        }
        System.out.println("args: [1][2][3][4][5][6][7][8][9]");
        System.out.println("1:签到 2:vip 3:世界树 4:登录奖 5:斗技场排名奖 6:矿坑 7:打斗技场 8:领任务 9:花园挂机");
    }
}
