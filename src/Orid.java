package src;

import java.io.Serializable;
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

    public Orid(Element element){
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
        sb.append("%s(%d) %s %s %d格\n[".formatted(name,id,attribute, use_condition, occupy));
        if(evolution1!=null){
            sb.append(evolution1);
            if (evolution2!=null){
                sb.append("\n");
                sb.append(evolution2);
            }
        }
        sb.append("]");
        return sb.toString();
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
            if (!loadOridXml()){
                System.out.println("植物原型模块数据未加载！");
                System.out.println("请将organism或organism.xml放在static目录下");
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
                    // System.out.println(orid);
                    oridMap.put(orid.id, orid);
                }
            }
        }
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
    }
}
