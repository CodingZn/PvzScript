package src;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    public static final int[] PUBLIC_CAVE_ID = new int[]{
        0,1,2,3,4,5,6,7,8,9,10,11,12,
        34,35,36,37,38,39,40,41,42,43,44,45,
        67,68,69,70,71,72,73,74,75,76,77,78,
        100,101,102,103,104,105,106,107,108,109,110,111
    };

    public static byte[] getCaveInfo(int pvz_id, int layer){
        String path = "/pvz/index.php/cave/index/id/%d/type/public_%d/sig/d243257468da0845977dc79db199221e?"
        .formatted(pvz_id, layer)+Long.toString(new Date().getTime());
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
                    byte[] bytes = getCaveInfo(friend.id_pvz, l);
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
        
        System.out.println("args: save dir_to [least_grade]");
        System.out.println("or  : extract dir cave_public_no...");

    }
}
