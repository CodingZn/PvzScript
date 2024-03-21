package src;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Cave {
    public static final int[] LAYERS_GRADE = new int[]{1,54,90,116};

    public static final int[] PUBLIC_CAVE_ID = new int[]{0,
        1,2,3,4,5,6,7,8,9,10,11,12,
        34,35,36,37,38,39,40,41,42,43,44,45,
        67,68,69,70,71,72,73,74,75,76,77,78,
        100,101,102,103,104,105,106,107,108,109,110,111
    };

    public static final int[] GEREN_CAVE_ID = new int[]{0,
        13,14,15,16,17,18,19,20,21,22,23,24,
        46,47,48,49,50,51,52,53,54,55,56,57,
        79,80,81,82,83,84,85,86,87,88,89,90,
        112,113,114,115,116,117,118,119,120,121,122,123
    };

    public static final int[] ANYE_CAVE_ID = new int[]{0,
        25,26,27,28,29,30,31,32,33,
        58,59,60,61,62,63,64,65,66,
        124,125,126,127,128,129,130,131,132
    };

    public static byte[] getCaveInfo(int pvz_id, int layer, boolean is_private){
        String type = is_private?"private":"public";
        String path = "/pvz/index.php/cave/index/id/%d/type/%s_%d/sig/d243257468da0845977dc79db199221e?"
        .formatted(pvz_id,type, layer)+Long.toString(new Date().getTime());
        return Request.sendGetRequest(path);
    }

    /** 将指定等级之上的好友的公洞信息保存到文件 */
    public static boolean saveCavesToDir(String dirName, final int least_grade){
        Path rootDir = Path.of(dirName);
        rootDir.toFile().mkdirs();
        Path[] layerDirs = new Path[4];
        for (int i = 0; i < 4; i++) {
            layerDirs[i] = rootDir.resolve("layer%d".formatted(i+1));
            layerDirs[i].toFile().mkdir();
        }
        LinkedHashMap<Integer, Friend> fMap = Friend.getFriendMap();
        for(Entry<Integer, Friend> entry : fMap.entrySet()){
            Friend friend = entry.getValue();
            Log.log("resolving %s(%d,%d)...layer:".formatted(friend.name, friend.grade, friend.id_pvz));
            if (friend.grade < least_grade) break;

            for (int l = 1; l <= 4; l++) {
                if (friend.grade >= LAYERS_GRADE[l-1]){
                    byte[] bytes = getCaveInfo(friend.id_pvz, l, false);
                    Path file = layerDirs[l-1].resolve("lv%03d_%d_%d.xml".
                    formatted(friend.grade, friend.id_pvz, l));
                    try {
                        FileOutputStream fOutputStream = new FileOutputStream(file.toFile());
                        fOutputStream.write(bytes);
                        fOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    Log.print("%d ".formatted(l));
                }
                else break;
            }
            Log.println();
            
        }
        
        return true;
    }


    private static String publicNo2Id(int no){
        return String.valueOf(PUBLIC_CAVE_ID[no]);
    }

    @SuppressWarnings({"unchecked"})
    public static boolean extractCave(String dirName, Set<Integer> cave_public_nos){
        Object[] ids = new Object[4];
        Path rootPath = Path.of(dirName);
        for (int i = 0; i < 4; i++) {
            ids[i] = new TreeSet<String>();
        }
        TreeMap<String, Set<String>> caveInstance = new TreeMap<>();
        // 登记所有转换后的caveId
        for (Integer cave_public_no : cave_public_nos) {
            int layer_index = (cave_public_no-1)/12;
            if (layer_index >4 || layer_index < 0) return false;
            String caveId = publicNo2Id(cave_public_no);
            ((Set<String>)ids[layer_index]).add(caveId);
            caveInstance.put(caveId, new HashSet<String>());
        }
        for (int i = 0; i < 4; i++) {
            int l = i+1;
            if (((Set<String>)ids[i]).size()==0) continue;
            Path layerPath = rootPath.resolve("layer%d".formatted(l));
            File layerDir = layerPath.toFile();
            File[] xmlFiles = layerDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });
            // 遍历每个xml文件
            for (File file : xmlFiles) {
                Document document = Util.parseXml(file);
                Log.logln("extracting file %s".formatted(file.getName()));
                NodeList hList = document.getElementsByTagName("h");
                for (int j = 0; j < hList.getLength(); j++) {
                    if (hList.item(j).getNodeType()==Node.ELEMENT_NODE){
                        Element h = (Element)hList.item(j);
                        String hId = h.getAttribute("id");
                        if ( ((Set<String>)ids[i]).contains(hId) && 
                        h.getElementsByTagName("oi").getLength()==1){
                            String oi = ((Element)h.getElementsByTagName("oi").item(0)).getTextContent();
                            Set<String> instances = caveInstance.get(hId);
                            instances.add(oi);
                        }
                    }
                    
                }
            }
        }
        for (Integer cave_public_no : cave_public_nos) {
            String caveId = publicNo2Id(cave_public_no);
            Set<String> instances = caveInstance.get(caveId);
            Log.logln("generating file public%d.txt".formatted(cave_public_no));
            Path outFile = rootPath.resolve("public%d.txt".formatted(cave_public_no));
            try {
                outFile.toFile().createNewFile();
                PrintStream pStream = new PrintStream(outFile.toFile());
                for (String ins : instances) {
                    pStream.println(ins);
                }
                pStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            
        }
        return true;
    }

    private static List<Integer> extractCavesById(Document document, Collection<Integer> toTake){
        List<Integer> res = new ArrayList<>();
        NodeList hList = document.getElementsByTagName("h");
        for (int j = 0; j < hList.getLength(); j++) {
            if (hList.item(j).getNodeType()==Node.ELEMENT_NODE){
                Element h = (Element)hList.item(j);
                int hId = Integer.parseInt(h.getAttribute("id"));
                if (toTake.contains(hId) && h.getElementsByTagName("oi").getLength()==1){
                    String oi = ((Element)h.getElementsByTagName("oi").item(0)).getTextContent();
                    if (oi.length()>0) res.add(Integer.parseInt(oi));
                }
            }
        }
        return res;
    }

    public static boolean saveAn(String filename, int minNo, int maxNo){
        if (minNo>maxNo) return false;
        
        Set<Integer> take = new HashSet<>();
        List<Integer> res = new ArrayList<>();
        for (int i = minNo; i <= maxNo; i++) {
            take.add(ANYE_CAVE_ID[i]);
        }
        // 暗洞1层
        if (minNo<=9){
            Log.log("提取暗洞1层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 2, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        // 暗洞2层
        if (minNo<=18 && maxNo >= 10){
            Log.log("提取暗洞2层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 4, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        // 暗洞3层
        if (maxNo >= 19){
            Log.log("提取暗洞3层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 6, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        Util.saveIntegersToFile(res, filename);
        Log.logln("保存成功！");
        return true;
    }

    public static boolean saveGe(String filename, int minNo, int maxNo){
        if (minNo>maxNo) return false;
        
        Set<Integer> take = new HashSet<>();
        List<Integer> res = new ArrayList<>();
        for (int i = minNo; i <= maxNo; i++) {
            take.add(GEREN_CAVE_ID[i]);
        }
        // 个洞1层
        if (minNo<=12){
            Log.log("提取个洞1层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 1, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        // 个洞2层
        if (minNo<=24 && maxNo >= 13){
            Log.log("提取个洞2层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 3, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        // 个洞3层
        if (minNo<=36 && maxNo >= 25){
            Log.log("提取个洞3层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 5, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        // 个洞4层
        if (maxNo >= 37){
            Log.log("提取个洞4层... ");
            byte[] lay1 = getCaveInfo(User.getUser().id, 7, true);
            Document document = Util.parseXml(lay1);
            List<Integer> ois = extractCavesById(document, take);
            res.addAll(ois);
            Log.println("成功");
        }
        Util.saveIntegersToFile(res, filename);
        Log.logln("保存成功！");
        return true;
        
    }

    public static void main(String[] args) {
        if ((args.length == 2 || args.length==3) && args[0].equals("save")){
            int least_grade = 0;
            if (args.length==3){
                least_grade = Integer.parseInt(args[2]);
            }
            saveCavesToDir(args[1], least_grade);
            return;
        }
        else if (args.length >= 3 && args[0].equals("extract")){
            TreeSet<Integer> pCaveSet = new TreeSet<>();
            for (int i = 2; i < args.length; i++) {
                pCaveSet.add(Integer.parseInt(args[i]));
            }
            extractCave(args[1], pCaveSet);
            return;
        }
        else if (args.length >= 4 && args[0].equals("savean")){
            int min_no = Integer.parseInt(args[2]);
            int max_no = Integer.parseInt(args[3]);
            saveAn(args[1], min_no, max_no);
            return;
        }
        else if (args.length >= 4 && args[0].equals("savege")){
            int min_no = Integer.parseInt(args[2]);
            int max_no = Integer.parseInt(args[3]);
            saveGe(args[1], min_no, max_no);
            return;
        }
        
        System.out.println("args: save <dir_to> [<least_grade>]");
        System.out.println("or  : extract <dir> <cave_public_no>...");
        System.out.println("or  : savean <filename> <min_no> <max_no>");
        System.out.println("or  : savege <filename> <min_no> <max_no>");

    }
}
