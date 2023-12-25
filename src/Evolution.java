package src;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static src.Request.sendGetRequest;

public class Evolution {

    private static List<Integer[]> EVOLUTION_PATHS = new ArrayList<>();

    private static String getPath(String plantId, String routeId){
            String time = Long.toString(new Date().getTime());
            return "/pvz/index.php/organism/evolution/id/" + plantId + "/route/" + routeId + 
            "/shortcut/2/sig/520a1059b91423de592b8283e11d251b?" + time;
        }

    static {
        // 太阳花妹妹->天使皇后+3
        EVOLUTION_PATHS.add(new Integer[]{1,178,179,180,181,197,6,7,266,267,268,269,270,271,272,273,274});
        // 悟空射手->海盗女王+3
        EVOLUTION_PATHS.add(new Integer[]{45,46,47,93,48,115,13,14,15,277,278,279,280,281,282,283,284,285});
        // 飞飞萝卜->蝠王榴莲Max
        EVOLUTION_PATHS.add(new Integer[]{103,40,41,42,43,44,321,322,323,324,325,326,327,328,329,330,331});
    }

    private static boolean evolve(String plantId, int pathno){
        return evolve(plantId, pathno, 0, -1);
    }

    private static boolean evolve(String plantId, int pathno, int start, int end){
        Integer[] thispath = EVOLUTION_PATHS.get(pathno);
        if (end == -1){
            end = thispath.length;
        }
        for (int i = start; i < end; i++) {
            String path = getPath(plantId, thispath[i].toString());
            System.out.printf("evolue: id=%s, route=%d ---> ", plantId, thispath[i]);
            int retlen = sendGetRequest(path);
            System.out.printf("length: %d\n",retlen);
            if (retlen == -1 || retlen == 191){
                return false;
            }
            int sleepTime = 1000;
            if (retlen == 2441){
                sleepTime = 15000;
                i--;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            
        }
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException{
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
