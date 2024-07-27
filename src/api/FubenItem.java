package src.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import flex.messaging.io.ASObject;

public class FubenItem {
    
    public final int caveid;
    public final String name;
    public final int scene;

    private FubenItem(ASObject obj, int sc){
        caveid = Util.obj2int(obj.get("cave_id"));
        name = ((String)obj.get("name")).replaceAll("\\n", "");
        scene = sc;
    }
    @Override
    public String toString() {
        return "%d-%d-%s".formatted(scene,caveid,name);
    }

    static{
        List<List<FubenItem>> listlist = new ArrayList<>();
        Map<Integer, FubenItem> tmp= new TreeMap<>();
        listlist.add(new ArrayList<>());
        for (int i = 1; i <= 5; i++) {
            List<FubenItem> list0 = new ArrayList<>();
            String nm = "static/fuben%d".formatted(i);
            ASObject obj = (ASObject)Util.decodeAMF(nm).getBody(0).getValue();
            @SuppressWarnings("unchecked")
            List<ASObject> caves =(List<ASObject>) obj.get("_caves");
            for (ASObject c : caves) {
                FubenItem it = new FubenItem(c, i);
                // 去除沙漠到其他副本的显示
                if (it.caveid>=143 && it.caveid<=146) continue;
                list0.add(it);
                tmp.put(it.caveid, it);
            }
            listlist.add(list0);
        }
        map = tmp;
        list = listlist;
    }

    /** id->item */
    public static final Map<Integer, FubenItem> map;
    /** scene(1-5)->items */
    public static final List<List<FubenItem>> list;

    public static final String[] sceneList = new String[]{"","炽热沙漠","幽静树海","冰火世界","死亡峡谷","荒原驿道"};

    public static void main(String[] args) {
        for (int i = 1; i < 6; i++) {
            List<FubenItem> l = list.get(i);
            for (FubenItem fubenItem : l) {
                System.out.println(fubenItem);
            }
            System.out.println();
        }
    }
}
