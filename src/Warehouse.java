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

    public static boolean loadWarehouse() {
        byte[] response = Request.sendGetRequest(Warehouse.getPath());
        Document document = Util.parseXml(response);
        if (document == null){
            return false;
        }
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

    public static void main(String[] args) {
        Document document = null;
        try{
            FileInputStream file = new FileInputStream("data/test.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //从DOM工厂中获取解析器
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            
            //使用解析器生成Document实例
            document = documentBuilder.parse(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        loadTools(document);
    }
}
