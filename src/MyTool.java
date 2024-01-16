package src;

import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    public MyTool(int toolid, long amount){
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
        return toolsMap.get(id);
    }

    public static boolean loadTools(Document document){
        toolsMap.clear();
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
        System.out.println("args: show");
        System.out.println("or  : search <name>");
    }
}
