package src;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static src.Request.sendGetRequest;

public class Evolution implements Serializable{

    public final int id;
    public final int grade;
    public final int target;
    public final int tool_id;
    public final int money;

    public Evolution(Element element){
        id = Integer.parseInt(element.getAttribute("id"));
        grade = Integer.parseInt(element.getAttribute("grade"));
        target = Integer.parseInt(element.getAttribute("target"));
        tool_id = Integer.parseInt(element.getAttribute("tool_id"));
        money = Integer.parseInt(element.getAttribute("money"));
    }

    @Override
    public String toString() {
        String toolname = Tool.getTool(tool_id).name;
        String targetName = Orid.getOrid(target).name;
        return "route %d: -(lv.%d, %s, %d)-> %s(%d)".formatted(id,grade,toolname,money,targetName,target);
    }


    private static List<Integer[]> EVOLUTION_PATHS = new ArrayList<>();

    private static String getPath(String plantId, String routeId){
            String time = Long.toString(new Date().getTime());
            return "/pvz/index.php/organism/evolution/id/" + plantId + "/route/" + routeId + 
            "/shortcut/2/sig/520a1059b91423de592b8283e11d251b?" + time;
        }

    static {
        //0 太阳花妹妹->天使皇后+3
        EVOLUTION_PATHS.add(new Integer[]{1,178,179,180,181,197,6,7,266,267,268,269,270,271,272,273,274});
        //1 悟空射手->海盗女王+3
        EVOLUTION_PATHS.add(new Integer[]{45,46,47,93,48,115,13,14,15,277,278,279,280,281,282,283,284,285});
        //2 飞飞萝卜->蝠王榴莲Max
        EVOLUTION_PATHS.add(new Integer[]{103,40,41,42,43,44,321,322,323,324,325,326,327,328,329,330,331});
        //3 太阳花妹妹->超五悟空+3
        EVOLUTION_PATHS.add(new Integer[]{1,178,179,180,196,95,50,51,332,333,334,335,336,337,338,339,340});
        //4 莲花战车->葵花战车Max
        EVOLUTION_PATHS.add(new Integer[]{86,387,388,389,390,391,392,393,394,395,396,397});
        //5 石榴娃娃->极鬼椒王+3
        EVOLUTION_PATHS.add(new Integer[]{134,135,136,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155});
        //6 茄子战车->莲花战车
        EVOLUTION_PATHS.add(new Integer[]{80,81,82,83,84,85});
        //7 草莓小红->极鬼椒王+3
        EVOLUTION_PATHS.add(new Integer[]{102,134,135,136,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155});
        //8 草莓小红->刺炎榴莲+3
        EVOLUTION_PATHS.add(new Integer[]{102,73,74,75,76,77,78,79,376,377,378,379,380,381,382,383,384});
        //9 葵花战车Max->葵花战车☆圣
        EVOLUTION_PATHS.add(new Integer[]{527,552,577,602});
        
    }

    private static boolean evolve(String plantId, int pathno){
        return evolve(plantId, pathno, 0, -1);
    }

    private static boolean evolve(String plantId, int pathno, int start, int end){
        Integer[] thispath = EVOLUTION_PATHS.get(pathno);
        if (end == -1){
            end = thispath.length;
        }
        System.out.printf("evolue: id=%s\n", plantId);
        for (int i = start; i < end; i++) {
            String path = getPath(plantId, thispath[i].toString());
            System.out.printf("route=%d ", thispath[i]);
            byte[] body = sendGetRequest(path);
            Document document = Util.parseXml(body);
            String msg = Util.getXmlMessage(document);
            if (msg!=null){
                System.out.println(msg+"x ");
                return false;
            }
            else{
                System.out.println("√");
            }
        }
        return true;
    }

    public static void main(String[] args){
        if (args.length < 2 || args.length > 4){
            System.out.println("need argument: plantId pathno [start [end] ]");
            assert false;
        }

        String plantId = args[0];
        int pathno = Integer.parseInt(args[1]);
        switch (args.length) {
            case 2 ->{
                evolve(plantId, pathno);
                break;
            }
            case 3 ->{
                int start = Integer.parseInt(args[2]);
                evolve(plantId, pathno, start, -1);
                break;
            }
            case 4 ->{
                int start = Integer.parseInt(args[2]);
                int end = Integer.parseInt(args[3]);
                evolve(plantId, pathno, start, end);
                break;
            }
        }
        
    }
    
}
