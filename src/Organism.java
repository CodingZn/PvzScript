package src;

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Organism {

    public static TreeMap<Integer, Organism> getNewestOrganisms(){
        if (loadOrganisms()){
            return getOrganisms();
        }
        else return null;
    }

    public static boolean loadOrganisms(Document document){
        organismMap.clear();
        Element organismsEle = (Element) document.getElementsByTagName("organisms").item(0);
        NodeList organismList = organismsEle.getChildNodes();
        for (int i = 0; i < organismList.getLength(); i++) {
            Node node = organismList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("item")){
                Element element = (Element) node;
                Organism plant = new Organism(element);
                organismMap.put(plant.id, plant);
            }
            
        }
        return true;
    }
    
    public static boolean loadOrganisms(){
        byte[] response = Request.sendGetRequest(Warehouse.getPath());
        Document document = Util.parseXml(response);
        if (document == null){
            return false;
        }
        return loadOrganisms(document);
    }

    private static TreeMap<Integer, Organism> organismMap = new TreeMap<>();

    public static TreeMap<Integer, Organism> getOrganisms(){
        return organismMap;
    }

    public Organism(Element element){
        this.id = Integer.parseInt(element.getAttribute("id"));
        this.pid = Integer.parseInt(element.getAttribute("pid"));
        this.attack = Long.parseLong(element.getAttribute("at"));
        this.hujia = Long.parseLong(element.getAttribute("mi"));
        this.speed = Long.parseLong(element.getAttribute("sp"));
        this.hp_now = Long.parseLong(element.getAttribute("hp"));
        this.hp_max = Long.parseLong(element.getAttribute("hm"));
        this.grade = Integer.parseInt(element.getAttribute("gr"));
        this.chuantou = Long.parseLong(element.getAttribute("pr"));
        this.miss = Long.parseLong(element.getAttribute("new_miss"));
        this.precision = Long.parseLong(element.getAttribute("new_precision"));
        this.quality = element.getAttribute("qu");
        this.fight = Long.parseLong(element.getAttribute("fight"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("植物id=\t%-9d  原型id=%-5d  等级=%-3d  品质=%s\n".formatted(this.id, this.pid, this.grade, this.quality))
        .append("当前hp=\t%d\n".formatted(this.hp_now))
        .append("战力=\t%d\n".formatted(this.fight))
        .append("总血量=\t%d\n".formatted(this.hp_max))
        .append("攻击=\t%d\n".formatted(this.attack))
        .append("护甲=\t%d\n".formatted(this.hujia))
        .append("穿透=\t%d\n".formatted(this.chuantou))
        .append("闪避=\t%d\n".formatted(this.miss))
        .append("命中=\t%d\n".formatted(this.precision))
        .append("速度=\t%d\n".formatted(this.speed));
        return sb.toString();
    }

    /** 植物id */
    public final int id;
    /** 原型id */
    public final int pid;
    /** 攻击 */
    public final long attack;
    /** 护甲 */
    public final long hujia;
    /** 速度 */
    public final long speed;
    /** 当前血量 */
    public long hp_now;
    /** 满血量 */
    public final long hp_max;
    /** 等级 */
    public final int grade;

    /** 穿透 */
    public final long chuantou;
    /** 闪避 */
    public final long miss;
    /** 命中 */
    public final long precision;
    /** 品质字符串 */
    public final String quality;
    /** 战斗力 */
    public final long fight;

    private static void show(){
        if (loadOrganisms()){
            TreeMap<Integer, Organism> map = getOrganisms();
            for (Map.Entry<Integer, Organism> orga : map.entrySet()) {
                System.out.println(orga.getValue());
            }
        }
        return;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            if (args[0].equals("show")) {
                show();
                return;
            }
        }
        System.out.println("args: show");
    }
}
