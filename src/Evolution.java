package src;
import java.io.Serializable;
import java.util.Date;

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

    public String toShortString(){
        Orid target = Orid.getOrid(this.target);
        return "-->%s".formatted(target.name);
    }

    private static String getPath(int plantId, int routeId){
        String time = Long.toString(new Date().getTime());
        return "/pvz/index.php/organism/evolution/id/%d/route/%d".formatted( plantId,routeId) + 
        "/shortcut/2/sig/520a1059b91423de592b8283e11d251b?" + time;
    }

    public static boolean evolve(int plantId, EvolRoute route){
        Integer[] thispath = route.toIntArr();
        
        System.out.printf("当前: %s\n", Organism.getOrganism(plantId).toShortString());
        for (int i = 0; i < thispath.length; i++) {
            String path = getPath(plantId, thispath[i]);
            System.out.printf("route %d ", thispath[i]);
            byte[] body = sendGetRequest(path);
            Document document = Util.parseXml(body);
            if (document==null){
                i--;
                continue;
            }
            String msg = Util.getXmlMessage(document);
            if (msg!=null){
                System.out.println(msg);
                return false;
            }
            else{
                System.out.println("-->%s".formatted(route.oridList.get(i+1).toShortString()));
            }
        }
        return true;
    }

    public static void main(String[] args){
        if (args.length == 3 && args[0].equals("file")){
            int plantId = Integer.parseInt(args[1]);
            EvolRoute route = EvolRoute.loadRoute(args[2]);
            if (route==null) return;
            evolve(plantId, route);
            return;
        }
        else if (args.length == 2){
            int plantId = Integer.parseInt(args[0]);
            int route_number = Integer.parseInt(args[1]);
            EvolRoute route = EvolRoute.getRoute(route_number);
            if (route==null) {
                System.out.println("无此路线！");
                return;
            }
            evolve(plantId, route);
            return;
        }
        
        System.out.println("args: <plantId> <route_number>");
        System.out.println("or  : file <plantId> <filename>");
    }
    
}
