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

import static src.GeneralBattle.*;


public class Battle {

    /** 战斗后同步仓库信息的频率。
     * 0表示不同步，1表示每次都同步，n>=1表示每打n次后同步一次 
     * */
    private static int updateFreq = 10;
    public static int setUpdateFreq(int newFreq){
        if (newFreq>=0){
            updateFreq = newFreq;
        }
        return updateFreq;
    }

    /** 炮灰等级阈值。-1表示不启用，每次让所有炮灰上场。
     * n>=0时，每次选择最低等级的炮灰，当无炮灰时停止。
     * n==0表示没有阈值，可以无限带级。
     * n>=1表示设定有效炮灰的最大等级，超过该等级则不会上场。
    */
    private static int maxLevel = 295;
    public static int setMaxLevel(int newl){
        if (newl>=-1){
            maxLevel = newl;
        }
        return maxLevel;
    }

    public static final Integer CHA_BOOK_TOOL_ID = 6;
    public static final Integer ADV_CHA_BOOK_TOOL_ID = 7;

    
    private static byte[] shuaDongAmf(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        Object[] value = new Object[3];
        value[0] = caveid;
        Set<Integer> participants = new HashSet<>(paohui);
        participants.addAll(zhuli);
        
        value[1] = Util.integerArr2int(participants.toArray());
        value[2] = hard_level;
        return Util.encodeAMF("api.cave.challenge", "/1", value);

    }

    /** 一次战斗，不包括补血 */
    public static SimpleEntry<Boolean, ASObject> battle(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        byte[] reqAmf = shuaDongAmf(caveid, hard_level, zhuli, paohui);
        byte[] response;
        System.out.printf("刷洞%d: ",caveid);
        System.out.print(resolveFighter(zhuli, paohui));
        response = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(response);
        AMF0Body body= msg.getBody(0);
        if(Response.isOnStatusException(body, true)){
            System.out.println();
            String exc = Response.getExceptionDescription(body);
            if (exc.equals("Exception:今日狩猎场挑战次数已达上限，明天再来吧")){
                return new SimpleEntry<Boolean,ASObject>(false, null);
            }
            else{
                return new SimpleEntry<Boolean,ASObject>(true, null);
            }
        }
        else{
            System.out.printf("√ ");
            ASObject resObj = (ASObject)body.getValue();
            boolean res = getAward((String)resObj.get("awards_key"));
            return new SimpleEntry<Boolean,ASObject>(res, resObj);
        }
    }

    /** 返回值代表是否继续 */
    public static boolean battleRepeat(List<Integer> caves, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        if (!BuXie.buxie(zhuli, paohui, true)){
            System.out.println("战斗前补血失败！");
            return false;
        }
        List<Integer> paohui_actual;

        PaohuiPool paohuiPool = new PaohuiPool(zhuli, paohui, maxLevel, true);

        int blindCount = 0;
        for (Integer c : caves) {
            paohui_actual = paohuiPool.getChosenPaohuis();
            if (!paohuiPool.hasValidPaohui()) {
                System.out.println("炮灰均带级完成！");
                return false;
            }
            SimpleEntry<Boolean,ASObject> resEntry = battle(c, hard_level, zhuli, paohui_actual);
            boolean res = resEntry.getKey();
            if (!res) return false;
            ASObject asObject = resEntry.getValue();
            // 其他报错导致没打洞，跳过
            if (asObject==null) continue;
            blindCount++;
            // 请求仓库同步信息
            if (Battle.updateFreq!=0 && blindCount >= Battle.updateFreq){
                blindCount=0;
                System.out.println("同步仓库信息...");
                res = BuXie.buxie(zhuli, paohui, true)&&res;
                paohuiPool = new PaohuiPool(zhuli, paohui, maxLevel, true);
            }
            // 继续盲打
            else{
                SimpleEntry<Set<Integer>, Set<Integer>> attacked = BuXie.getAttacked(asObject, zhuli, paohui);
                res = BuXie.blindBuxie(attacked.getKey(), attacked.getValue())&&res;
                paohuiPool.updateExcept(paohui_actual, attacked.getValue());
            }
            if (!paohuiPool.hasValidPaohui()) {
                System.out.println("炮灰均带级完成！");
                return false;
            }
            if (!res) return false;
            delay(2000);
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 4) {
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = Util.readIntegersFromFile(args[3]);
                battleRepeat(caves, hard_level, zhuli, paohui);
                return;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 3){
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = new ArrayList<>();
                battleRepeat(caves, hard_level, zhuli, paohui);
                return;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 2 && args[0].equals("updatefreq")){
            setUpdateFreq(Integer.parseInt(args[1]));
            System.out.printf("new update freq: %d\n", updateFreq);
            return;
        }
        else if (args.length == 2 && args[0].equals("maxlevel")){
            setMaxLevel(Integer.parseInt(args[1]));
            System.out.printf("new maxLevel: %d\n", maxLevel);
            return;
        }
        else if (args.length == 2 && args[0].equals("book")){
            int amount;
            if (args[1].toLowerCase().equals("full")) {
                User me = User.loadUser();
                amount = me.cave_cha_max - me.getCaveCha();
            }
            else{
                amount = Integer.parseInt(args[1]);
            }
            if (amount%5==0){
                long chaAmount = MyTool.getTool(CHA_BOOK_TOOL_ID).getAmount();
                long advAmount = MyTool.getTool(ADV_CHA_BOOK_TOOL_ID).getAmount();
                if (advAmount > chaAmount / 6) {
                    Warehouse.useTool(ADV_CHA_BOOK_TOOL_ID, amount/5);
                    return;
                }
            }
            Warehouse.useTool(CHA_BOOK_TOOL_ID, amount);
            return;
        }

        System.out.println("args: cave_file hard_level zhuli_file [ paohui_file ]");
        System.out.println("hard_level: 1 or 2 or 3");
        System.out.println("or  : maxlevel grade");
        System.out.println("or  : updatefreq freq");
        System.out.println("or  : book <amount>|full");
    }
}
