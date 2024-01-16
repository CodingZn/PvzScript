package src;

import static src.Util.obj2int;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Warehouse {
    public static final String getPath(){
        String time = Long.toString(new Date().getTime());
        return "/pvz/index.php/Warehouse/index/sig/755f2a102fbea19a61c432205e4a550d?" + time;
    }

    private static Document warehouseDoc = null;

    public static boolean loadWarehouse() {
        byte[] response = Request.sendGetRequest(Warehouse.getPath());
        Document document = Util.parseXml(response);
        if (document == null){
            return false;
        }
        warehouseDoc = document;
        boolean res = true;
        res = Organism.loadOrganisms(document);
        res = res && MyTool.loadTools(document);
        return res;
    }

    public static boolean openGrid(int goal){
        // if (goal>192) goal=192;
        String path = "/pvz/index.php/Warehouse/opengrid/type/organism/sig/c32812e07cebc02a9c37c0409328f8f0?"+Long.toString(new Date().getTime());
        int now_grid_num = 0;
        if (warehouseDoc==null && loadWarehouse()){
            Element wareEle = (Element)(warehouseDoc.getElementsByTagName("warehouse").item(0));
            now_grid_num = Integer.parseInt(wareEle.getAttribute("organism_grid_amount"));
        }
        System.out.printf("curr: %d\n", now_grid_num);
        if (now_grid_num>=goal) return true;
        System.out.printf("opening...");
        do {
            byte[] response = Request.sendGetRequest(path);
            Document doc = Util.parseXml(response);
            Node statusNode = doc.getElementsByTagName("status").item(0);
            if (statusNode.getNodeType()==Node.ELEMENT_NODE && ((Element) statusNode).getTextContent().equals("success")){
                Element warehouseEle = (Element)doc.getElementsByTagName("warehouse").item(0);
                now_grid_num = Integer.parseInt(warehouseEle.getAttribute("organism"));
                System.out.printf("curr: %d\n", now_grid_num);
            }
            else{
                System.out.println(Util.getXmlMessage(doc));
                return false;
            }
            
        } while (now_grid_num < goal);
        
        return true;
    }

    public static final String[] TOOL_USE_MSG = new String[]{
        "",
        "增加了%d次洞口挑战次数",
        "增加了%d次斗技场挑战次数",
        "增加了%d天vip",
        "增加了%d次领地挑战",
        "增加了%d次副本挑战",
        "增加了%d次宝石挑战",
        "增加了%d次花园挑战",
    };

    public static boolean useTool(int toolid, int amount){
        Object[] value = new Object[]{toolid, amount};
        byte[] req = Util.encodeAMF("api.tool.useOf", "/1", value);
        System.out.printf("使用%d个%s ", amount, Tool.getTool(toolid).name);
        byte[] response = Request.sendPostAmf(req, false);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            System.out.println();
            return false;
        }
        else{
            ASObject asObj = (ASObject) body.getValue();
            int msgidx = obj2int(asObj.get("name"));
            int effect = obj2int(asObj.get("effect"));
            System.out.println(TOOL_USE_MSG[msgidx].formatted(effect));
            return true;
        }
    }

    public static void main(String[] args) {
        if (args.length==1 && args[0].equals("open")){
            openGrid(192);
            return;
        }
        else if (args.length==2 && args[0].equals("open")){
            int goal = Integer.parseInt(args[1]);
            openGrid(goal);
            return;
        }

        System.out.println("args: open goal");
        System.out.println("goal: target organism grids number (<=192)");

    }
}
