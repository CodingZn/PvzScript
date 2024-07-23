package src.api;


import static src.api.Util.delay;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

import static src.api.GeneralBattle.*;


public class Battle {

    /** 战斗后同步仓库信息的频率。
     * 0表示不同步，1表示每次都同步，n>=1表示每打n次后同步一次 
     * */
    protected static int updateFreq = 10;
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
    protected static int maxLevel = 0;
    public static int setMaxLevel(int newl){
        if (newl>=-1){
            maxLevel = newl;
        }
        return maxLevel;
    }

    /** 可带级炮灰无法填满时，是否使用带级完成的炮灰填满战斗格子 */
    protected static boolean kpFull = true;
    public static boolean setKeepFull(boolean f){
        kpFull = f;
        return kpFull;
    }

    /** 无挑战次数时自动使用挑战书的个数。
     * n=0表示不使用挑战书。
     * 要求1<=n<=25
    */
    protected static int autobook = 0;
    public static int setAutoBook(int newa){
        if (newa>=0 && newa <= 25){
            autobook = newa;
        }
        return autobook;
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

    public static boolean useTimesand(int caveid){
        byte[] req = Util.encodeAMF("api.cave.useTimesands", "/1", new Object[]{caveid});
        Log.log("对洞口%d使用时之沙 ".formatted(caveid));
        byte[] resp = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(resp).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        Log.logln("成功");
        return true;
    }

    public static boolean addChallengeCave(int amount){
        if (amount==0) return true;
        long chaAmount = MyTool.getTool(CHA_BOOK_TOOL_ID).getAmount();
        long advAmount = MyTool.getTool(ADV_CHA_BOOK_TOOL_ID).getAmount();
        if (chaAmount + advAmount*5 < amount){
            Log.logln("挑战书不足以增加%d次挑战".formatted(amount));
            return false;
        }
        boolean res = true;
        if (advAmount*5<=amount){
            res = Warehouse.useTool(ADV_CHA_BOOK_TOOL_ID, (int)advAmount);
            amount -= advAmount*5;
        }
        else{
            int use = (int)amount/5;
            res = Warehouse.useTool(ADV_CHA_BOOK_TOOL_ID, (int)use);
            amount -= use*5;
        }
        if (!res) {
            return false;
        }
        if (amount==0) return true;
        res = Warehouse.useTool(CHA_BOOK_TOOL_ID, amount);
        return res;
    }
    /** 一次战斗，不包括领奖、补血 */
    public static AMF0Body battle(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        byte[] reqAmf = shuaDongAmf(caveid, hard_level, zhuli, paohui);
        byte[] response;
        Log.log("刷洞%d: %s".formatted(caveid,resolveFighter(zhuli, paohui)));
        response = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(response);
        return msg.getBody(0);
    }

    /** 一批战斗 */
    public static boolean battleRepeat(List<Integer> caves, int hard_level, List<Integer> zhuli, List<Integer> paohui, boolean useSand){
        if (!BuXie.buxie(zhuli, paohui, true)){
            Log.logln("战斗前补血失败！");
            return false;
        }
        List<Integer> paohui_actual;

        PaohuiPool paohuiPool = new PaohuiPool(zhuli, paohui, maxLevel, kpFull);

        int blindCount = 0;
        int expect_cha_count = User.loadUser().getCaveCha();
        for (int ci=0; ci<caves.size(); ci++) {
            Integer c = caves.get(ci);
            boolean res = true;
            // 获取炮灰信息
            paohui_actual = paohuiPool.getChosenPaohuis();
            if (!paohuiPool.hasValidPaohui()) {
                Log.logln("炮灰均带级完成！");
                return false;
            }
            // 处理挑战次数和时之沙
            if (expect_cha_count == 0 && autobook > 0){
                res = addChallengeCave(autobook) && res;
                if (!res) return false;
                expect_cha_count += autobook;
            }
            if (useSand && ci!=0) {
                useTimesand(c);
            }
            // 战斗
            AMF0Body body = battle(c, hard_level, zhuli, paohui_actual); 
            if (Response.isOnStatusException(body, true)){
                String exc = Response.getExceptionDescription(body);
                if (exc.equals("Exception:今日狩猎场挑战次数已达上限，明天再来吧")){
                    if (autobook==0) return false;
                    res = addChallengeCave(autobook) && res;
                    if (!res) return false;
                    ci--;
                    expect_cha_count++;
                }
                else if (exc.equals("Exception:僵尸已被其它人挑战了") && useSand){
                    ci--;
                    if (!useTimesand(c)) return false;
                }
                delay(9000);
                continue;
            }
            // 正常打洞后续处理
            Log.println("√ ");
            expect_cha_count--;
            ASObject resObj = (ASObject)body.getValue();
            res = getAward((String)resObj.get("awards_key")) && res;
            blindCount++;
            // 请求仓库同步信息
            if (Battle.updateFreq!=0 && blindCount >= Battle.updateFreq){
                blindCount=0;
                Log.logln("同步仓库信息...");
                res = BuXie.buxie(zhuli, paohui, true)&&res;
                paohuiPool = new PaohuiPool(zhuli, paohui, maxLevel, kpFull);
            }
            // 继续盲打
            else{
                SimpleEntry<Set<Integer>, Set<Integer>> attacked = BuXie.getAttacked(resObj, zhuli, paohui);
                res = BuXie.blindBuxie(attacked.getKey(), attacked.getValue())&&res;
                paohuiPool.updateExcept(paohui_actual, attacked.getValue());
            }
            
            if (!res) return false;
            delay(1000);
        }
        return true;
    }

    public static void main(String[] args) {
        if ((args.length == 6 || args.length == 5)&&args[0].equals("repeat")) {
            try {
                int count_n = Integer.parseInt(args[1]);
                int cave = Integer.parseInt(args[2]);
                int hard_level = Integer.parseInt(args[3]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[4]);
                List<Integer> paohui = new ArrayList<>();
                if (args.length == 6){
                    paohui = Util.readIntegersFromFile(args[5]);
                }
                List<Integer> caves = Collections.nCopies(count_n, cave);
                battleRepeat(caves, hard_level, zhuli, paohui, true);
                return;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 4) {
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = Util.readIntegersFromFile(args[3]);
                battleRepeat(caves, hard_level, zhuli, paohui, false);
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
                battleRepeat(caves, hard_level, zhuli, paohui, false);
                return;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 2 && args[0].equals("updatefreq")){
            setUpdateFreq(Integer.parseInt(args[1]));
            Log.log("new update freq: %d\n".formatted(updateFreq));
            return;
        }
        else if (args.length == 2 && args[0].equals("maxlevel")){
            setMaxLevel(Integer.parseInt(args[1]));
            Log.log("new maxLevel: %d\n".formatted(maxLevel));
            return;
        }
        else if (args.length == 2 && args[0].equals("kpfull")){
            if (args[1].equals("on")) {
                setKeepFull(true);
                return;
            }else if (args[1].equals("off")) {
                setKeepFull(false);
                return;
            }
        }
        else if (args.length == 2 && args[0].equals("autobook")){
            setAutoBook(Integer.parseInt(args[1]));
            Log.log("new autobook: %d\n".formatted(autobook));
            return;
        }
        else if (args.length == 2 && args[0].equals("book")){
            int amount;
            if (args[1].equals("full")) {
                User me = User.loadUser();
                amount = me.cave_cha_max - me.getCaveCha();
            }
            else{
                amount = Integer.parseInt(args[1]);
            }
            addChallengeCave(amount);
            return;
        }

        System.out.println("args: <cave_file> <hard_level> <zhuli_file> [<paohui_file>]");
        System.out.println("or  : repeat <count> <cave_id> <hard_level> <zhuli_file> [<paohui_file>]");
        System.out.println("or  : maxlevel <grade>");
        System.out.println("or  : kpfull on|off");
        System.out.println("or  : updatefreq <freq>");
        System.out.println("or  : book <amount>|full");
        System.out.println("or  : autobook <amount>");
    }
}
