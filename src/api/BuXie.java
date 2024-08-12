package src.api;

import java.util.AbstractMap.SimpleEntry;

import static src.api.Util.obj2bigint;
import static src.api.Util.obj2int;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import flex.messaging.io.ASObject;

import java.util.Map;

public class BuXie {
    private static double threshold = 0.70;
    private static final double DIJI_EFFECT = 0.20;
    private static final double ZHONGJI_EFFECT = 0.50;
    public static final int DIJIXIE_ID = 13;
    public static final int ZHONGJIXIE_ID = 14;
    public static final int GAOJIXIE_ID = 15;
    
    protected static int low_reserve = 0;
    protected static int mid_reserve = 0;
    protected static int high_reserve = 0;

    private static boolean enable=true;

    public static double getThreshold(){
        return threshold;
    }

    /** 0 <= ratio <= 1 */
    public static boolean setThreshold(double ratio){
        if (ratio >= 0 && ratio <= 1) {
            threshold = ratio;
            Log.logln("new threshold: %.12f%%".formatted(threshold*100));
            return true;
        }
        Log.logln("threshold unchanged: %.12f%%".formatted(threshold*100));
        return false;
    }

    public static boolean setReserve(int low, int mid, int high){
        boolean res = true;
        if (low >= 0) low_reserve = low;
        else res = false;
        if (mid >= 0) mid_reserve = mid;
        else res = false;
        if (high >= 0) high_reserve = high;
        else res = false;
        Log.logln("reserve strategy: low=%d, mid=%d, high=%d".formatted(
            low_reserve, mid_reserve, high_reserve
        ));
        return res;
    }

    public static void setEnable(boolean enable_){
        enable=enable_;
    }
    public static boolean isEnabled(){return enable;}

    /** type: 1|2|3 or 13|14|15 */
    private static long getXiepingAmount(int type){
        if (type==1||type==13) {
            return MyTool.getTool(DIJIXIE_ID).getAmount() - low_reserve;
        }
        if (type==2||type==14) {
            return MyTool.getTool(ZHONGJIXIE_ID).getAmount() - mid_reserve;
        }
        if (type==3||type==15) {
            return MyTool.getTool(GAOJIXIE_ID).getAmount() - high_reserve;
        }
        return 0L;
    }

    public static final List<Integer> EMPTY_LIST = new ArrayList<>();

    /** second arg: 13, 14, 15 */
    private static boolean bu1xie(int plantId, int xiepingId){
        int[] value = new int[]{plantId, xiepingId};
        byte[] reqAmf = Util.encodeAMF("api.apiorganism.refreshHp", "/1", value);
        byte[] response = Request.sendPostAmf(reqAmf, false);

        Log.log("%s 使用 %s".formatted(Organism.getOrganism(plantId).toShortString(), Tool.getTool(xiepingId).name));
        String exception = Response.getExceptionDescription(response);
        if (exception==null){
            Object obj = Util.decodeAMF(response).getBody(0).getValue();
            if (obj instanceof String){
                Log.println(" hp=%s".formatted(obj));
                MyTool.getTool(xiepingId).changeAmount(-1);
                Organism.getOrganism(plantId).setNowHp((String)obj);
                return true;
            }
            else return false;
        }
        else if (exception.equals("Exception:该植物血量已满")){
            Log.println(exception);
            return true;
        }
        else{
            Log.println(exception);
            return false;
        }
        

    }

    /** 不获取仓库，根据战斗情况补血 */
    public static boolean blindBuxie(ASObject fo, Collection<Integer> zhuli, Collection<Integer> paohui){
        SimpleEntry<Set<Integer>, Set<Integer>> res = BuXie.getAttacked(fo, zhuli, paohui);
        return BuXie.blindBuxie(res.getKey(), res.getValue());
    }

    /** 对所有植物补血，炮灰补低血，主力预测 */
    public static boolean blindBuxie(Collection<Integer> zhuli, Collection<Integer> paohui){
        if (!enable) return true;
        long diji = getXiepingAmount(DIJIXIE_ID);
        long zhongji = getXiepingAmount(ZHONGJIXIE_ID);
        long gaoji = getXiepingAmount(GAOJIXIE_ID);

        for (Integer integer : paohui) {
            if (diji > 0){
                if (!bu1xie(integer, DIJIXIE_ID)) return false;
                diji--;
            }else if (zhongji > 0){
                if (!bu1xie(integer, ZHONGJIXIE_ID)) return false;
                zhongji--;
            }else if (gaoji > 0){
                if (!bu1xie(integer, GAOJIXIE_ID)) return false;
                gaoji--;
            }else{
                return false;
            }
        }
        for (Integer integer : zhuli) {
            if (diji > 0){
                if (!bu1xie(integer, DIJIXIE_ID)) return false;
                diji--;
            }else if (zhongji > 0){
                if (!bu1xie(integer, ZHONGJIXIE_ID)) return false;
                zhongji--;
            }else if (gaoji > 0){
                if (!bu1xie(integer, GAOJIXIE_ID)) return false;
                gaoji--;
            }else{
                return false;
            }
        }
        return true;
    }

    /** 主力使用最少的血瓶补到阈值以上；炮灰保证有血
     * @param requestWare 是否请求仓库最新信息
     * 若disable 仍可能请求仓库。
     */
    public static boolean buxie(Collection<Integer> zhuli, Collection<Integer> paohui, boolean requestWare){
        if(requestWare){
            Warehouse.loadWarehouse();
        }
        if (!enable) return true;

        long diji = getXiepingAmount(DIJIXIE_ID);
        long zhongji = getXiepingAmount(ZHONGJIXIE_ID);
        long gaoji = getXiepingAmount(GAOJIXIE_ID);

        boolean res = true;
        
        for (Integer plantid : paohui) {
            Organism plant = Organism.getOrganism(plantid); 
            if (plant.hp_now.compareTo(BigInteger.ZERO)>0){
                continue;
            }else if (diji > 0){
                res = res && bu1xie(plantid, DIJIXIE_ID);
                diji--;
            }else if (zhongji > 0){
                res = res && bu1xie(plantid, ZHONGJIXIE_ID);
                zhongji--;
            }else if (gaoji > 0){
                res = res && bu1xie(plantid, GAOJIXIE_ID);
                gaoji--;
            }else{
                res = false;
            }
            if (!res) return false;
        }

        for (Integer plantid : zhuli) {
            Organism plant = Organism.getOrganism(plantid); 
            double now_percent = plant.hp_now.doubleValue() / plant.hp_max.doubleValue();
            if (now_percent >= threshold){
                continue;
            }else if (now_percent + DIJI_EFFECT >= threshold && diji > 0){
                res = res && bu1xie(plantid, DIJIXIE_ID);
                diji--;
            }else if (now_percent + ZHONGJI_EFFECT >= threshold && zhongji > 0){
                res = res && bu1xie(plantid, ZHONGJIXIE_ID);
                zhongji--;
            }else if (gaoji > 0){
                res = res && bu1xie(plantid, GAOJIXIE_ID);
                gaoji--;
            }else if (zhongji > 0){
                res = res && bu1xie(plantid, ZHONGJIXIE_ID);
                zhongji--;
                now_percent += ZHONGJI_EFFECT;
                if (now_percent >= threshold) continue;
                else if (now_percent + DIJI_EFFECT >= threshold){
                    if (diji>0){
                        res = res && bu1xie(plantid, DIJIXIE_ID);
                        diji--;
                    }
                    else if (zhongji>0){
                        res = res && bu1xie(plantid, ZHONGJIXIE_ID);
                        zhongji--;
                    }
                    else res = false;
                }else {
                    if (zhongji>0){
                        res = res && bu1xie(plantid, ZHONGJIXIE_ID);
                        zhongji--;
                    }
                    else res = false;
                }
                
            }else{
                res = false;
            }
            if (!res) return false;
        }

        return true;
    }


    /** 获取被攻击的主力和炮灰 */
    @SuppressWarnings({"unchecked"})
    public static SimpleEntry<Set<Integer>, Set<Integer>> getAttacked(ASObject fo, Collection<Integer> zhuli, Collection<Integer> paohui){
        Set<Integer> attacked_zhuli = new HashSet<>();
        Set<Integer> attacked_paohui = new HashSet<>();
        Map<Integer, BigInteger> fightersHp = new HashMap<>();
        for (ASObject fighter : (List<ASObject> )fo.get("assailants")) {
            int id = obj2int(fighter.get("id"));
            BigInteger hp_max = obj2bigint(fighter.get("hp_max"));
            fightersHp.put(id, hp_max);
        }
        // 分析每次攻击
        for (ASObject process : (List<ASObject> )fo.get("proceses")) {
            // 仅考虑己方被攻击的情况
            String assailantType = (String) ((ASObject) process.get("assailant")).get("type");
            if (assailantType.equals("assailant")) continue;
            // 对被攻击的植物进行检查
            for (ASObject defender : (List<ASObject> )process.get("defenders")) {
                Integer id = obj2int(defender.get("id"));
                BigInteger hp = obj2bigint(defender.get("hp"));
                if (zhuli.contains(id)){
                    BigInteger max_hp = fightersHp.get(id);
                    double percent = hp.doubleValue()/max_hp.doubleValue();
                    if (percent <= BuXie.getThreshold())
                        attacked_zhuli.add(id);
                }
                else if (paohui.contains(id) && hp.equals(BigInteger.ZERO)){
                    attacked_paohui.add(id);
                }
            }
        }
        return new SimpleEntry<Set<Integer>, Set<Integer>>(attacked_zhuli, attacked_paohui);
    }

    /** 仅获取被攻击死掉的炮灰 */
    @SuppressWarnings({"unchecked"})
    public static Set<Integer> getAttackedPaohui(ASObject fo, Collection<Integer> paohui){
        Set<Integer> attacked_paohui = new HashSet<>();
        // 分析每次攻击
        for (ASObject process : (List<ASObject> )fo.get("proceses")) {
            // 仅考虑己方被攻击的情况
            String assailantType = (String) ((ASObject) process.get("assailant")).get("type");
            if (assailantType.equals("assailant")) continue;
            // 对被攻击的植物进行检查
            for (ASObject defender : (List<ASObject> )process.get("defenders")) {
                Integer id = obj2int(defender.get("id"));
                BigInteger hp = obj2bigint(defender.get("hp"));
                if (paohui.contains(id) && hp.equals(BigInteger.ZERO)){
                    attacked_paohui.add(id);
                }
            }
        }
        return attacked_paohui;
    }

    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("threshold")){
            double percent = Double.parseDouble(args[1]);
            setThreshold(percent/100);
            return;
        }
        else if (args.length == 2){
            int plantid = Integer.parseInt(args[0]);
            int xieping = Integer.parseInt(args[1]);
            if (xieping == 13 || xieping == 14 || xieping == 15){
                bu1xie(plantid, xieping);
                return;
            }else if (xieping == 1 || xieping == 2 || xieping == 3 ){
                xieping = xieping+12;
                bu1xie(plantid, xieping);
                return;
            }
        }
        else if (args.length==4 && args[0].equals("reserve")){
            int low = Integer.parseInt(args[1]);
            int mid = Integer.parseInt(args[2]);
            int high = Integer.parseInt(args[3]);
            setReserve(low, mid, high);
            return;
        }
        else if (args.length==1){
            if (args[0].equals("enable")){
                setEnable(true);
                return;
            }else if (args[0].equals("disable")){
                setEnable(false);
                return;
            }
        }
        System.out.println("args: <plantid> <xiepingid>|1|2|3");
        System.out.println("1|2|3 means xiepingid: 13|14|15");
        System.out.println("or  : enable|disable");
        System.out.println("or  : threshold <percent>");
        System.out.println("or  : reserve <low_n> <mid_n> <high_n>");
    }
}
