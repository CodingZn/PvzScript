package src;

import java.io.FileInputStream;
import java.util.Date;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Warehouse {
    public static final String getPath(){
        String time = Long.toString(new Date().getTime());
        return "/pvz/index.php/Warehouse/index/sig/755f2a102fbea19a61c432205e4a550d?" + time;
    }

    private static TreeMap<Integer, Integer> toolsMap = new TreeMap<>();

    public static TreeMap<Integer, Integer> getTools(){
        return toolsMap;
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
        res = res && loadTools(document);
        return res;
    }

    private static boolean loadTools(Document document){
        toolsMap.clear();
        Element toolsEle = (Element) document.getElementsByTagName("tools").item(0);
        NodeList toolsList = toolsEle.getChildNodes();
        for (int i = 0; i < toolsList.getLength(); i++) {
            Node node = toolsList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("item")){
                Element element = (Element) node;
                if (element.hasAttribute("id") && 
                    element.hasAttribute("amount")){
                    int id = Integer.parseInt(element.getAttribute("id"));
                    int amount = Integer.parseInt(element.getAttribute("amount"));
                    toolsMap.put(id, amount);
                }
            }
            
        }
        return true;
    }

    public static final String toolString(int id, int amount){
        return "Tool: id=%-5d, amount=%-9d ".formatted(id, amount);
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

    public static void main(String[] args) {
        // Document doc = Util.parseXml("resources/开仓库.xml");
        // Node statusNode = doc.getElementsByTagName("status").item(0);
        // if (statusNode.getNodeType()==Node.ELEMENT_NODE && ((Element) statusNode).getTextContent().equals("success")){
        //         Element warehouseEle = (Element)doc.getElementsByTagName("warehouse").item(0);
        //         int a = Integer.parseInt(warehouseEle.getAttribute("organism"));
        //         System.out.println(a);
        //     }
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
