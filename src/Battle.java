package src;


import static src.Util.delay;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;


public class Battle {
    /** 
     * 策略0：不补血，有植物死亡直接停止。 
     * <p/> 
     * 策略1：仅在有植物死亡后触发补血。（适合无伤带级）。
     * <p/> 
     * 策略2：每次挑战后根据战斗状况获取植物最新血量补血。（适合有伤带级）。
     * <p/> 
     * 策略3：每次挑战后获取仓库信息进行补血。（请求过多，但准确）
     * <p/>
     * TODO: 战后补血策略、奖励领取策略、炮灰自动选择策略、挑战次数策略
     */
    private static int strategy = 1;
    private static int setStrategy(int newStrategy){
        if (newStrategy>=0 && newStrategy<=3){
            strategy = newStrategy;
        }
        return strategy;
    }
    
    private static byte[] shuaDongAmf(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        Object[] value = new Object[3];
        value[0] = caveid;
        Set<Integer> participants = new HashSet<>(paohui);
        participants.addAll(zhuli);
        
        value[1] = Util.integerArr2int(participants.toArray());
        value[2] = hard_level;
        return Util.encodeAMF("api.cave.challenge", "/1", value);

    }

    @SuppressWarnings({"unchecked"})
    public static String resolveAwardObj(ASObject awardObj){
        StringBuffer sb = new StringBuffer();
        List<ASObject> toolList = (List<ASObject>) awardObj.get("tools");
        for (ASObject object : toolList) {
            sb.append("%s(%d) ".formatted(
                (String)object.get("id"), Util.obj2int(object.get("amount"))));
            
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

    private static boolean zhanhouBuxie(ASObject fo, List<Integer> zhuli, List<Integer> paohui){
        if (strategy==2){
            SimpleEntry<Set<Integer>, Set<Integer>> res = BuXie.getAttacked(fo, zhuli, paohui);
            return BuXie.blindBuxie(res.getKey(), res.getValue());
        }
        else if (strategy==3){
            return BuXie.buxie(zhuli, paohui);
        }
        else return false;
    }

    /** 返回值代表是否继续 */
    public static boolean shuaDongDaiji(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        byte[] reqAmf = shuaDongAmf(caveid, hard_level, zhuli, paohui);
        byte[] response;
        do {
            System.out.printf("刷洞%d开始",caveid);
            response = Request.sendPostAmf(reqAmf, true);
            AMF0Message msg = Util.decodeAMF(response);
            AMF0Body body= msg.getBody(0);
            if(Response.isOnStatusException(body, true)){
                String exc = Response.getExceptionDescription(body);
                if (exc.equals("Exception:参与战斗的生物HP不能小于1")){
                    if (strategy==0) return false;
                    boolean res = BuXie.buxie(zhuli, paohui);
                    if (!res){
                        System.out.printf("血瓶不足！\n");
                        return false;
                    }
                    continue;
                }
                else if (exc.equals("Exception:今日狩猎场挑战次数已达上限，明天再来吧")){
                    return false;
                }
                else{
                    return true;
                }
            }
            else{
                System.out.printf("√");
                ASObject resObj = (ASObject)body.getValue();
                boolean res = getAward((String)resObj.get("awards_key"));
                if (strategy == 2 || strategy == 3) {
                    res = zhanhouBuxie(resObj, zhuli, paohui) && res;
                }
                return res;
            }
        } while (true);
    }

    public static void main(String[] args) {
        if (args.length == 4) {
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = Util.readIntegersFromFile(args[3]);
                for (Integer c : caves) {
                    boolean res = shuaDongDaiji(c, hard_level, zhuli, paohui);
                    if (!res) break;
                    delay(2000);
                }
                return;
            } catch (NumberFormatException e) {
            }
        }
        else if (args.length == 3){
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = new ArrayList<>();
                for (Integer c : caves) {
                    boolean res = shuaDongDaiji(c, hard_level, zhuli, paohui);
                    if (!res) break;
                    delay(2000);
                }
                return;
            } catch (NumberFormatException e) {
            }
        }
        else if (args.length == 2 && args[0].equals("strategy")){
            setStrategy(Integer.parseInt(args[1]));
            System.out.printf("new battle strategy: %d\n", strategy);
            return;
        }

        System.out.println("args: cave_file hard_level zhuli_file [ paohui_file ]");
        System.out.println("hard_level: 1 or 2 or 3");
        System.out.println("file format: id seperated by \\n");
        System.out.println("or  : strategy no");
        System.out.println("no: 0 to 3");
    }
}
