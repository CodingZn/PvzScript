package src;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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

    public static SimpleEntry<Integer, String> qualityUp(int plantId, String iniQuality, String goal, int maximum){
        String now_quality=iniQuality;
        int now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 0);
        final int goal_level = QNAME2LEVEL.getOrDefault(goal, 0);
        int total_use = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (now_quality_level<goal_level && total_use < maximum) {
            byte[] resp = Request.sendPostAmf(body, true);
            total_use++;
            now_quality = resolveResponseAmf(resp);
            if (now_quality==null) continue;
            now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 9999);
            System.out.printf("used: %d; curr: %s;\n", total_use, now_quality);
        }

        return new SimpleEntry<Integer,String>(total_use, now_quality);
    }

    public static SimpleEntry<Integer, String> qualityUp(int plantId, int maximum){
        String now_quality="";
        int total_use = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (total_use < maximum) {
            byte[] resp = Request.sendPostAmf(body, true);
            total_use++;
            now_quality = resolveResponseAmf(resp);
            if (now_quality==null) continue;
            System.out.printf("used: %d; curr: %s;\n", total_use, now_quality);
        }

        return new SimpleEntry<Integer,String>(total_use, now_quality);
    }

    public static String getPlantQuality(int plantid){
        TreeMap<Integer, Organism> plants = Organism.getNewestOrganisms();
        Organism plant = plants.get(plantid);
        if (plant==null) return "";
        return plant.quality;
    }

    
    public static void main(String[] args){
        PrintStream printStream;
        if (args.length == 3 || args.length == 4){
            if (args[0].equals("goal")){
                int plantId = Integer.parseInt(args[1]);
                String goalQuality = args[2];
                String iniQuality = getPlantQuality(plantId);
                int maximum = Integer.MAX_VALUE;
                if (args.length == 4){
                    maximum = Integer.parseInt(args[3]);
                }
                SimpleEntry<Integer, String> res = qualityUp(plantId, iniQuality, goalQuality, maximum);
                try {
                    printStream = new PrintStream(new FileOutputStream("output/shuapin.txt", 
                    true),true, Charset.forName("UTF-8")) ;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                printStream.printf("plant %d: %s --%d books--> %s.\n", plantId, iniQuality, res.getKey(), res.getValue());
                printStream.close();
                return;
            }else if (args[0].equals("count") && args.length == 3){
                int plantId = Integer.parseInt(args[1]);
                int maximum = Integer.parseInt(args[2]);
                String iniQuality = getPlantQuality(plantId);
                SimpleEntry<Integer, String> res = qualityUp(plantId,  maximum);
                try {
                    printStream = new PrintStream(new FileOutputStream("output/shuapin.txt", 
                    true),true, Charset.forName("UTF-8")) ;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                printStream.printf("plant %d: %s --%d books--> %s.\n", plantId, iniQuality, res.getKey(), res.getValue());
                printStream.close();
                return;
            }
        }
        System.out.println("args: goal plantid quality_name [max_usage]\n");
        System.out.println("or  : count plantid maximum\n");
    }
}
