package src;

import java.util.AbstractMap.SimpleEntry;

import static src.Util.obj2int;

import java.util.ArrayList;
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

    public static double getThreshold(){
        return threshold;
    }

    /** second arg: 13, 14, 15 */
    public static boolean bu1xie(int plantId, int xiepingId){
        int[] value = new int[]{plantId, xiepingId};
        byte[] reqAmf = Util.encodeAMF("api.apiorganism.refreshHp", "/1", value);
        byte[] response = Request.sendPostAmf(reqAmf, false);

        System.out.printf("plant %d use %d", plantId, xiepingId);
        Object obj = Util.decodeAMF(response).getBody(0).getValue();
        if (obj instanceof String){
            System.out.printf(" hp=%s\n", obj);
            return true;
        }
        System.out.printf(" failed\n", obj);
        return !Response.isOnStatusException(Util.decodeAMF(response).getBody(0), true);

    }

    /** 不获取仓库直接补血 */
    public static boolean blindBuxie(Set<Integer> zhuli, Set<Integer> paohui){
        for (Integer integer : paohui) {
            if (!bu1xie(integer, DIJIXIE_ID)) return false;
        }
        for (Integer integer : zhuli) {
            if (!bu1xie(integer, DIJIXIE_ID)) return false;
        }
        return true;
    }

    /** 策略：主力使用最少的血瓶补到阈值以上；炮灰保证有血 */
    public static boolean buxie(Set<Integer> zhuli, Set<Integer> paohui){
        return buxie(new ArrayList<>(zhuli), new ArrayList<>(paohui));
    }
    /** 策略：主力使用最少的血瓶补到阈值以上；炮灰保证有血 */
    public static boolean buxie(List<Integer> zhuli, List<Integer> paohui){
        if(Warehouse.loadWarehouse()){
            Map<Integer, Organism> organisms = Organism.getOrganisms();
            Map<Integer, Integer> tools = Warehouse.getTools();
            int diji = tools.getOrDefault(DIJIXIE_ID, 0);
            int zhongji = tools.getOrDefault(ZHONGJIXIE_ID, 0);
            int gaoji = tools.getOrDefault(GAOJIXIE_ID, 0);

            boolean res = true;
            
            for (Integer plantid : paohui) {
                Organism plant = organisms.get(plantid); 
                if (plant.hp_now > 0L){
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
                Organism plant = organisms.get(plantid); 
                double now_percent = (double) plant.hp_now / plant.hp_max;
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
        return false;
    }


    /** 获取被攻击的主力和炮灰 */
    @SuppressWarnings({"unchecked"})
    public static SimpleEntry<Set<Integer>, Set<Integer>> getAttacked(ASObject fo, List<Integer> zhuli, List<Integer> paohui){
        Set<Integer> attacked_zhuli = new HashSet<>();
        Set<Integer> attacked_paohui = new HashSet<>();
        Map<Integer, Long> fightersHp = new HashMap<>();
        for (ASObject fighter : (List<ASObject> )fo.get("assailants")) {
            Object id_obj = fighter.get("id");
            int id = obj2int(id_obj);
            long hp_max = Long.parseLong(fighter.get("hp_max").toString());
            fightersHp.put(id, hp_max);
        }
        // 分析每次攻击
        for (ASObject process : (List<ASObject> )fo.get("proceses")) {
            // 仅考虑己方被攻击的情况
            String assailantType = (String) ((ASObject) process.get("assailant")).get("type");
            if (assailantType.equals("assailant")) continue;
            // 对被攻击的植物进行检查
            for (ASObject defender : (List<ASObject> )process.get("defenders")) {
                Integer id = Integer.parseInt(defender.get("id").toString());
                Long hp = Long.parseLong(defender.get("hp").toString());
                if (zhuli.contains(id)){
                    long max_hp = fightersHp.getOrDefault(id, Long.MAX_VALUE);
                    double percent = (double)hp/max_hp;
                    if (percent <= BuXie.getThreshold())
                        attacked_zhuli.add(id);
                }
                else if (paohui.contains(id) && hp.equals(0L)){
                    attacked_paohui.add(id);
                }
            }
        }
        return new SimpleEntry<Set<Integer>, Set<Integer>>(attacked_zhuli, attacked_paohui);
    }

    public static void main(String[] args) {
        
    }
}
