package src;

import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class Quality {
    public static final String[] QLEVEL2NAME = new String[]{"","劣质", "普通", "优秀", "精良", "极品", 
    "史诗", "传说", "神器", "魔王", "战神", "至尊", "魔神", "耀世", "不朽", "永恒", "太上", "混沌", "无极"};
    public static final Map<String, Integer> QNAME2LEVEL;

    static{
        Map<String, Integer> tmp = new HashMap<>();
        for (int l = 1; l < QLEVEL2NAME.length; l++) {
            tmp.put(QLEVEL2NAME[l], l);
        }
        QNAME2LEVEL = tmp;
    }
    
    public static byte[] getQualityUpAmf(int plantId){
        int[] value = {plantId};
        return Util.encodeAMF("api.apiorganism.qualityUp", "/1", value);
    }
    
    /** @return new quality name */
    public static String resolveResponseAmf(byte[] bytes){
        
        AMF0Message message = Util.decodeAMF(bytes);
        if (message==null) return null;
        ASObject value = (ASObject) message.getBody(0).getValue();
        if (value.containsKey("quality_name")) {
            return (String) value.get("quality_name");
        } 
        else 
            return null;
    }

    public static SimpleEntry<Integer,String> qualityUp(int plantId, String iniQuality, String goal, int maximum){
        String now_quality=iniQuality;
        Log.log("%s 当前 %s ".formatted(Organism.getOrganism(plantId).toShortString(),now_quality));
        int now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 0);
        final int goal_level = QNAME2LEVEL.getOrDefault(goal, 0);
        int total_use = 0;
        int currLevelUse = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (now_quality_level<goal_level && total_use < maximum) {
            Log.print("+");
            byte[] resp = Request.sendPostAmf(body, true);
            total_use++;
            currLevelUse++;
            String tmpQ = resolveResponseAmf(resp);
            if (tmpQ==null) continue;
            /** 升级了 */
            if (!tmpQ.equals(now_quality)){
                Log.println();
                Log.log("使用%d本刷新书，升级到 %s ".formatted(currLevelUse, tmpQ));
                now_quality = tmpQ;
                now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 9999);
                currLevelUse = 0;
            }
        }
        Log.logln();

        return new SimpleEntry<Integer,String>(total_use, now_quality);
    }

    public static String getPlantQuality(int plantid){
        Map<Integer, Organism> plants = Organism.getNewestOrganisms();
        Organism plant = plants.get(plantid);
        if (plant==null) return "";
        return plant.quality;
    }

    
    public static void main(String[] args){
        if (args.length == 2 || args.length == 3){
            int plantId = Integer.parseInt(args[0]);
            String goalQuality = args[1];
            String iniQuality = getPlantQuality(plantId);
            int maximum = Integer.MAX_VALUE;
            if (args.length == 3){
                maximum = Integer.parseInt(args[2]);
            }
            SimpleEntry<Integer, String> res = qualityUp(plantId, iniQuality, goalQuality, maximum);
            
            Log.logln("%s 总共使用%d本书从%s升级到了%s".formatted(
                Organism.getOrganism(plantId).toShortString(), res.getKey(), 
                iniQuality, res.getValue()));
            return;
        }
        System.out.println("args: plantid quality_name [max_usage]\n");
    }
}
