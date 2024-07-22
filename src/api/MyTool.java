package src.api;

import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import src.api.Warehouse.SellType;

public class MyTool {
    public final int id;
    public final Tool toolOri;
    private long amount;

    public long getAmount(){
        return amount;
    }
    public void changeAmount(int change){
        amount += change;
    }
    
    private MyTool(int toolid, long amount){
        toolOri = Tool.getTool(toolid);
        id = toolid;
        this.amount = amount;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("%s(%d) %d个".formatted(toolOri.name,toolOri.id,amount));
        return sb.toString();
    }

    private static TreeMap<Integer, MyTool> toolsMap = new TreeMap<>();

    public static TreeMap<Integer, MyTool> getTools(){
        if (toolsMap==null || toolsMap.size()==0){
            Warehouse.loadWarehouse();
        }
        return toolsMap;
    }

    public static MyTool getTool(int id){
        if (toolsMap==null || toolsMap.size()==0){
            Warehouse.loadWarehouse();
        }
        return toolsMap.getOrDefault(id, new MyTool(id, 0));
    }

    public static boolean loadTools(Document document){
        clear();
        Element toolsEle = (Element) document.getElementsByTagName("tools").item(0);
        NodeList toolsList = toolsEle.getElementsByTagName("item");
        for (int i = 0; i < toolsList.getLength(); i++) {
            Node node = toolsList.item(i);
            Element element = (Element) node;
            int id = Integer.parseInt(element.getAttribute("id"));
            long amount = Long.parseLong(element.getAttribute("amount"));
            MyTool myTool = new MyTool(id, amount);
            toolsMap.put(id, myTool);
        }
        return true;
    }

    public static void clear(){
        if (toolsMap==null) toolsMap = new TreeMap<>();
        else toolsMap.clear();
    }

    public static boolean sell(int id, int count){
        return Warehouse.sell(SellType.TOOL_TYPE, id, count);
    }


    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("show")){
            getTools().values().stream().forEach(t->{
                System.out.println(t);
            });
            return;
        }
        else if (args.length==2 && args[0].equals("search")){
            String str = args[1];
            getTools().values().stream().forEach(tool->{
                if (tool.toolOri.name.contains(str)){
                    System.out.println(tool);
                }
            });
            return;
        }
        else if ((args.length == 3) && args[0].equals("sell")) {
            int id = Integer.parseInt(args[1]);
            int count = Integer.parseInt(args[2]);
            sell(id, count);
            return;
        }
        System.out.println("args: show");
        System.out.println("or  : search <name>");
        System.out.println("or  : sell <id> <count>");
    }
}
