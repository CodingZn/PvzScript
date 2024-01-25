package src;

import static src.Util.obj2int;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

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

    public static void clear(){
        warehouseDoc = null;
        Organism.clear();
        MyTool.clear();
    }

    public static boolean openGrid(int goal){
        // if (goal>192) goal=192;
        String path = "/pvz/index.php/Warehouse/opengrid/type/organism/sig/c32812e07cebc02a9c37c0409328f8f0?"+Long.toString(new Date().getTime());
        int now_grid_num = 0;
        if (warehouseDoc==null && loadWarehouse()){
            Element wareEle = (Element)(warehouseDoc.getElementsByTagName("warehouse").item(0));
            now_grid_num = Integer.parseInt(wareEle.getAttribute("organism_grid_amount"));
        }
        Log.log("当前格子数: %d\n".formatted(now_grid_num));
        if (now_grid_num>=goal) return true;
        do {
            byte[] response = Request.sendGetRequest(path);
            Document doc = Util.parseXml(response);
            Node statusNode = doc.getElementsByTagName("status").item(0);
            if (statusNode.getNodeType()==Node.ELEMENT_NODE && ((Element) statusNode).getTextContent().equals("success")){
                Element warehouseEle = (Element)doc.getElementsByTagName("warehouse").item(0);
                now_grid_num = Integer.parseInt(warehouseEle.getAttribute("organism"));
                Log.logln("当前格子数: %d".formatted(now_grid_num));
            }
            else{
                Log.logln(Util.getXmlMessage(doc));
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
        Log.log("使用%d个%s ".formatted(amount, Tool.getTool(toolid).name));
        byte[] response = Request.sendPostAmf(req, false);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if (Response.isOnStatusException(body, true)){
            Log.println();
            return false;
        }
        else{
            ASObject asObj = (ASObject) body.getValue();
            int msgidx = obj2int(asObj.get("name"));
            int effect = obj2int(asObj.get("effect"));
            Log.println(TOOL_USE_MSG[msgidx].formatted(effect));
            return true;
        }
    }

    public static boolean skillUp(int plantId, int ori_skill_id, int grade_by){
        int succ_count = 0;
        int now_skill_id = ori_skill_id;
        Log.log("%s 当前 %s ".formatted(Organism.getOrganism(plantId).toShortString(),Skill.getSkill(now_skill_id).toShortString()));
        while (succ_count < grade_by) {
            byte[] req = Util.encodeAMF("api.apiorganism.skillUp", "/1", 
            new Object[]{plantId, now_skill_id});
            int use_count = 0;
            while (true) {
                Log.print("+");
                byte[] resp = Request.sendPostAmf(req, true);
                use_count++;
                AMF0Message msg = Util.decodeAMF(resp);
                if (Response.isOnStatusException(msg.getBody(0), true)){
                    return false;
                }
                ASObject resObj = (ASObject) msg.getBody(0).getValue();
                int now_id = obj2int(resObj.get("now_id"));
                if (now_id != now_skill_id){
                    succ_count++;
                    now_skill_id = now_id;
                    break;
                }
            }
            Skill newSkill = Skill.getSkill(now_skill_id);
            Log.println();
            Log.log("使用%d个%s，升级到%s "
            .formatted(use_count,newSkill.learn_tool.name,newSkill.toShortString()));

        }
        Log.println();
        return true;
    }

    public static void skillUp(String[] args){
        if (args.length==3){
            int plantId = Integer.parseInt(args[0]);
            Organism org = Organism.getOrganism(plantId);
            if (org==null){
                Log.logln("plant %d doesn't exist!".formatted(plantId));
                return;
            }
            Skill oriSkill = org.getSkillByName(args[1]);
            if (oriSkill==null){
                try {
                    int ori_skill_id = Integer.parseInt(args[1]);
                    oriSkill = org.getSkillById(ori_skill_id);
                    if (oriSkill==null){
                        Log.logln("skill %s doesn't exist!".formatted(args[1]));
                        return;
                    }
                } catch (NumberFormatException e) {
                    Log.logln("skill %s doesn't exist!".formatted(args[1]));
                    return;
                }
            }
            int grade_by;
            if (args[2].substring(0,2).equals("lv")){
                int targetLevel = Integer.parseInt(args[2].substring(2));
                grade_by = targetLevel - oriSkill.grade;
            }
            else{
                grade_by = Integer.parseInt(args[2]);
            }
            skillUp(plantId, oriSkill.id, grade_by);
            return;
        }
        System.out.println("args: <plantId> <skillId>|<skillName> <upgradeCount>|(lv<targetLevel>)");
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
