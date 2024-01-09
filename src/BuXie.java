package src;

import java.util.List;
import java.util.TreeMap;

public class BuXie {
    private static double threshold = 70.0;
    private static final double DIJI_EFFECT = 20.0;
    private static final double ZHONGJI_EFFECT = 50.0;
    public static final int DIJIXIE_ID = 13;
    public static final int ZHONGJIXIE_ID = 14;
    public static final int GAOJIXIE_ID = 15;


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


    /** 策略：主力使用最少的血瓶补到阈值以上；炮灰保证有血 */
    public static boolean buxie(List<Integer> zhuli, List<Integer> paohui){
        if(Warehouse.loadWarehouse()){
            TreeMap<Integer, Organism> organisms = Organism.getOrganisms();
            TreeMap<Integer, Integer> tools = Warehouse.getTools();
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

    public static void main(String[] args) {
        
    }
}