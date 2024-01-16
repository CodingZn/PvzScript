package src;

import java.io.Serializable;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Tool implements Serializable{
    public final int id;
    public final String name;
    public final String use_condition;
    public final String use_result;
    public final String describe;
    public final int use_level;
    public final int sell_price;

    public Tool(Element element){
        id = Integer.parseInt(element.getAttribute("id"));
        name = element.getAttribute("name");
        use_condition = element.getAttribute("use_condition");
        use_result = element.getAttribute("use_result");
        describe = element.getAttribute("describe");
        use_level = Integer.parseInt(element.getAttribute("use_level"));
        sell_price = Integer.parseInt(element.getAttribute("sell_price"));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("%s(%d) price=%d\n".formatted(name,id, sell_price));
        sb.append("%s %s\n".formatted(use_condition, use_result));
        return sb.toString();
    }

    public String toShortString() {
        StringBuffer sb = new StringBuffer();
        sb.append("%s[id=%d, price=%d]".formatted(name, id, sell_price));
        return sb.toString();
    }

    public String toShortString(int amount) {
        StringBuffer sb = new StringBuffer();
        sb.append("%s(%d)".formatted(name, amount));
        return sb.toString();
    }

    public static final String BINARY_FILENAME = "static/tool";
    public static final String XML_FILENAME = "static/tool.xml";

    private static TreeMap<Integer, Tool> toolMap = new TreeMap<>();

    public static TreeMap<Integer, Tool> getToolMap(){
        return Tool.toolMap;
    }

    public static Tool getTool(int id){
        return toolMap.get(id);
    }

    public static boolean loadMap(){
        if (!loadMapBinary()){
            if (!loadToolXml()){
                System.out.println("道具原型模块数据未加载！");
                System.out.println("请将tool或tool.xml放在static目录下");
                System.out.println("或使用命令手动加载。");
                return false;
            }
            else{
                saveMapBinary();
                System.out.println("已将原型信息存为二进制文件。");
                return true;
            }
        }
        return true;
    }

    public static boolean loadToolXml(){
        toolMap = new TreeMap<>();
        Document document = Util.parseXml(XML_FILENAME);
        if (document==null) return false;
        NodeList tools = ((Element) document.getElementsByTagName("tools").item(0)).getChildNodes();
        for (int i = 0; i < tools.getLength(); i++) {
            Node node = tools.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE){
                Element item = (Element)node;
                if (item.getTagName().equals("item")){
                    Tool tool = new Tool(item);
                    // System.out.println(tool);
                    toolMap.put(tool.id, tool);
                }
            }
        }
        return true;
    }

    public static boolean saveMapBinary(){
        return Util.saveObject(toolMap, BINARY_FILENAME);
    }

    @SuppressWarnings({"unchecked"})
    public static boolean loadMapBinary(){
        Object obj = Util.loadObject(BINARY_FILENAME);
        Tool.toolMap = (TreeMap<Integer, Tool>) obj;
        return obj!=null;
    }
    
    static{
        if (!loadMap()) {
            assert false;
        }
    }

    public static void main(String[] args) {
        if (args.length==2 && args[0].equals("show")){
            System.out.println(getToolMap().get(Integer.parseInt(args[1])));
            return;
        }
        else if (args.length==2 && args[0].equals("search")){
            String str = args[1];
            toolMap.values().stream().forEach(tool->{
                if (tool.name.contains(str)){
                    System.out.println(tool);
                }
            });
            return;
        }
        System.out.println("args: show <id>");
        System.out.println("or  : search <name>");
    }
}
