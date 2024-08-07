package src.api;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Orid implements Serializable{
    public final int id;
    public final String name;
    public final String attribute;
    public final String use_condition;
    public final int height;
    public final int width;

    /** 格数 */
    public final int occupy;

    public final Evolution evolution1;
    public final Evolution evolution2;

    protected Set<Orid> predecessors = new HashSet<>();

    private Orid(Element element){
        id = Integer.parseInt(element.getAttribute("id"));
        name = element.getAttribute("name");
        attribute = element.getAttribute("attribute");
        use_condition = element.getAttribute("use_condition");
        height = Integer.parseInt(element.getAttribute("height"));
        width = Integer.parseInt(element.getAttribute("width"));
        occupy = height * width;
        NodeList evols = element.getElementsByTagName("item");

        if (evols.getLength()==0) {
            evolution1 = null;
            evolution2 = null;
        } else if (evols.getLength()==1) {
            evolution1 = new Evolution((Element)evols.item(0));
            evolution2 = null;
        } else {
            evolution1 = new Evolution((Element)evols.item(0));
            evolution2 = new Evolution((Element)evols.item(1));
        }

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("%s(%d) %s %s %d格\n{".formatted(name,id,attribute, use_condition, occupy));
        if(evolution1!=null){
            sb.append(evolution1);
            if (evolution2!=null){
                sb.append("}\n{");
                sb.append(evolution2);
            }
        }
        sb.append("}");
        sb.append("\n前驱[");
        this.predecessors.stream().forEach(o->{
            sb.append(o.toShortString());
            sb.append(" ");
        });
        sb.append("]\n");
        return sb.toString();
    }

    public String toShortString(){
        return "%s(%d)".formatted(this.name, this.id);
    }

    public static final String BINARY_FILENAME = "static/organism";
    public static final String XML_FILENAME = "static/organism.xml";

    private static TreeMap<Integer, Orid> oridMap = new TreeMap<>();

    public static TreeMap<Integer, Orid> getOridMap(){
        return Orid.oridMap;
    }

    public static Orid getOrid(int id){
        return oridMap.get(id);
    }

    public static boolean loadMap(){
        if (!loadMapBinary()){
            Log.logln("重新加载xml文件...");
            if (!loadOridXml()){
                Log.logln("植物原型模块数据未加载！");
                Log.print("请将organism或organism.xml放在static目录下");
                Log.print("或使用命令手动加载。");
                return false;
            }
            else{
                saveMapBinary();
                Log.logln("已将原型信息存为二进制文件。");
                return true;
            }
        }
        return true;
    }

    public static boolean loadOridXml(){
        oridMap = new TreeMap<>();
        Document document = Util.parseXml(XML_FILENAME);
        if (document==null) return false;
        NodeList organisms = ((Element) document.getElementsByTagName("organisms").item(0)).getChildNodes();
        for (int i = 0; i < organisms.getLength(); i++) {
            Node node = organisms.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE){
                Element item = (Element)node;
                if (item.getTagName().equals("item")){
                    Orid orid = new Orid(item);
                    oridMap.put(orid.id, orid);
                }
            }
        }
        for (Orid orid : oridMap.values()) {
            if (orid.evolution1!=null){
                int succ = orid.evolution1.target;
                oridMap.get(succ).predecessors.add(orid);
                if (orid.evolution2!=null){
                    succ = orid.evolution2.target;
                    oridMap.get(succ).predecessors.add(orid);
                }
            }
        }
        // 去掉死神植物相互转化
        Orid yeyi = oridMap.get(896);
        Orid baizai = oridMap.get(874);
        Orid dongshilang = oridMap.get(918);
        // 冬狮郎->夜一
        yeyi.predecessors.removeIf(pre->pre.id==918);
        // 夜一->白哉
        baizai.predecessors.removeIf(pre->pre.id==896);
        // 白哉->冬狮郎
        dongshilang.predecessors.removeIf(pre->pre.id==874);
        return true;
    }

    public static boolean saveMapBinary(){
        return Util.saveObject(oridMap, BINARY_FILENAME);
    }

    @SuppressWarnings({"unchecked"})
    public static boolean loadMapBinary(){
        Object obj = Util.loadObject(BINARY_FILENAME);
        Orid.oridMap = (TreeMap<Integer, Orid>) obj;
        return obj!=null;
    }
    
    static{
        if (!loadMap()) {
            assert false;
        }
    }

    public static void main(String[] args) {
        if (args.length==2 && args[0].equals("show")){
            System.out.println(getOridMap().get(Integer.parseInt(args[1])));
        }
        else if (args.length==2 && args[0].equals("search")){
            String str = args[1];
            oridMap.values().stream().forEach(orid->{
                if (orid.name.contains(str)){
                    System.out.println(orid);
                }
            });
        }
        else {
            System.out.println("args: orid show <id>");
            System.out.println("or  : orid search <name>");
        }
    }
}
