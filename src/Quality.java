package src;
import static src.Util.dateFormatNow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
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
        System.out.printf("%s 当前 %s ".formatted(Organism.getOrganism(plantId),now_quality));
        int now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 0);
        final int goal_level = QNAME2LEVEL.getOrDefault(goal, 0);
        int total_use = 0;
        int currLevelUse = 0;
        byte[] body = getQualityUpAmf(plantId);
        while (now_quality_level<goal_level && total_use < maximum) {
            System.out.print("+");
            byte[] resp = Request.sendPostAmf(body, true);
            total_use++;
            currLevelUse++;
            String tmpQ = resolveResponseAmf(resp);
            if (tmpQ==null) continue;
            /** 升级了 */
            if (!tmpQ.equals(now_quality)){
                System.out.printf("\n使用%d本刷新书，升级到 %s".formatted(currLevelUse, tmpQ));
                now_quality = tmpQ;
                now_quality_level = QNAME2LEVEL.getOrDefault(now_quality, 9999);
                currLevelUse = 0;
            }
            else{
                System.out.print("\b");
            }
        }
        System.out.println();

        return new SimpleEntry<Integer,String>(total_use, now_quality);
    }

    public static String getPlantQuality(int plantid){
        Map<Integer, Organism> plants = Organism.getNewestOrganisms();
        Organism plant = plants.get(plantid);
        if (plant==null) return "";
        return plant.quality;
    }

    
    public static void main(String[] args){
        PrintStream printStream;
        if (args.length == 2 || args.length == 3){
            int plantId = Integer.parseInt(args[0]);
            String goalQuality = args[1];
            String iniQuality = getPlantQuality(plantId);
            int maximum = Integer.MAX_VALUE;
            if (args.length == 3){
                maximum = Integer.parseInt(args[2]);
            }
            SimpleEntry<Integer, String> res = qualityUp(plantId, iniQuality, goalQuality, maximum);
            try {
                File logDir = new File("log");
                logDir.mkdirs();
                printStream = new PrintStream(new FileOutputStream(
                    "log/shuapin_%s.txt".formatted(dateFormatNow("yyyyMMdd")), 
                true),true, Charset.forName("UTF-8")) ;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            printStream.printf("%s %s 使用%d本书从%s升级到了%s\n",dateFormatNow("HH:mm:ss"),
                Organism.getOrganism(plantId).toShortString(), res.getKey(), iniQuality, res.getValue());
            printStream.close();
            return;
        }
        System.out.println("args: plantid quality_name [max_usage]\n");
    }
}
