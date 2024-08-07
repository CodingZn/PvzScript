package src.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public static byte[] getQuality12UpAmf(int plantId){
        int[] value = {plantId};
        return Util.encodeAMF("api.apiorganism.quality12Up", "/1", value);
    }
    
    /** @return new quality name */
    public static String resolveResponseAmf(byte[] bytes){
        
        AMF0Message message = Util.tryDecodeAMF(bytes);
        if (message==null) return null;
        ASObject value = (ASObject) message.getBody(0).getValue();
        if (value.containsKey("quality_name")) {
            return (String) value.get("quality_name");
        } 
        else 
            return null;
    }

    /** 不自动获取最新仓库植物 */
    public static boolean qualityUp(int plantId, String goal, int maximum){
        // String now_quality=iniQuality;
        Organism org = Organism.getOrganism(plantId);
        String iniQuality = org.quality;
        Log.log("%s 当前 %s ".formatted(org.toShortString(),org.quality));
        // int now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 0);
        final int goal_level = QNAME2LEVEL.getOrDefault(goal, 0);
        int total_use = 0;
        int currLevelUse = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (org.qualityLevel<goal_level && total_use < maximum) {
            Log.print("+");
            byte[] resp = Request.sendPostAmf(body, true);
            if (Response.isOnStatusException(Util.tryDecodeAMF(resp).getBody(0), true)){
                return false;
            }
            total_use++;
            currLevelUse++;
            String tmpQ = resolveResponseAmf(resp);
            if (tmpQ==null) continue;
            /** 升级了 */
            if (!tmpQ.equals(org.quality)){
                Log.println();
                Log.log("使用%d本刷新书，升级到 %s ".formatted(currLevelUse, tmpQ));
                org.setQuality(tmpQ);
                currLevelUse = 0;
            }
        }
        // Log.logln();

        Log.logln("%s 总共使用%d本书从%s升级到了%s".formatted(
            org.toShortString(), total_use, 
            iniQuality, org.quality));
        return true;
    }

    public static String getPlantQuality(int plantid){
        Map<Integer, Organism> plants = Organism.getNewestOrganisms();
        Organism plant = plants.get(plantid);
        if (plant==null) return "";
        return plant.quality;
    }

    /** 使用魔神刷新书 */
    public static boolean quality12Up(int plantId){
        Organism plant = Organism.getOrganism(plantId);
        Log.log("%s 当前 %s ".formatted(plant.toShortString(), plant.quality));
        int total_use = 0;
        byte[] body = getQuality12UpAmf(plantId);
        while (true) {
            Log.print("*");
            byte[] resp = Request.sendPostAmf(body, true);
            if (Response.isOnStatusException(Util.tryDecodeAMF(resp).getBody(0), true)){
                return false;
            }
            total_use++;
            String tmpQ = resolveResponseAmf(resp);
            if (tmpQ==null) continue;
            /** 成功了 */
            if (tmpQ.equals("魔神")){
                Log.println();
                Log.log("使用%d本魔神刷新书，升级到 魔神 ".formatted(total_use));
                plant.setQuality(tmpQ);
                break;
            }
        }
        Log.println();
        return true;
    }

    public static boolean batchQualityUp(String filename, String goal){
        List<Integer> plantList = Util.readIntegersFromFile(filename);
        for (Integer plant : plantList) {
            boolean res = qualityUp(plant, goal, Integer.MAX_VALUE);
            if (!res) return res;
        }
        return true;
    }
    
    public static boolean batchQuality12Up(String filename){
        List<Integer> plantList = Util.readIntegersFromFile(filename);
        for (Integer plant : plantList) {
            boolean res = quality12Up(plant);
            if (!res) return res;
        }
        return true;
    }
    
    public static void main(String[] args){
        if (args.length==2 && args[0].equals("moshen")) {
            int plantId = Integer.parseInt(args[1]);
            quality12Up(plantId);
            return;
        }
        else if (args.length==3 && args[0].equals("batch")) {
            String goalQuality = args[2];
            batchQualityUp(args[1], goalQuality);
            return;
        }
        else if (args.length==2 && args[0].equals("mbatch")) {
            batchQuality12Up(args[1]);
            return;
        }
        else if (args.length == 2 || args.length == 3){
            int plantId = Integer.parseInt(args[0]);
            String goalQuality = args[1];
            int maximum = Integer.MAX_VALUE;
            if (args.length == 3){
                maximum = Integer.parseInt(args[2]);
            }
            qualityUp(plantId, goalQuality, maximum);
            
            return;
        }
        System.out.println("args: <plantid> <quality_name> [max_usage]");
        System.out.println("or  : moshen <plantid>");
        System.out.println("or  : batch <plant_file> <quality_name>");
        System.out.println("or  : mbatch <plant_file>");
    }
}
