package src;

import java.util.Date;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Organism {
    private static String getPath(){
        String time = Long.toString(new Date().getTime());
        return "/pvz/index.php/Warehouse/index/sig/755f2a102fbea19a61c432205e4a550d?" + time;
    }
    
    public static boolean loadOrganisms(){
        byte[] response = Request.sendGetRequest(getPath());
        Document document = Util.parseXml(response);
        if (document == null){
            return false;
        }
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

    private static Map<Integer, Organism> organismMap;

    public static Map<Integer, Organism> getOrganisms(){
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
        sb.append("植物id=%-9d  原型id=%-5d  等级=%-3d  品质=%s\n".formatted(this.id, this.pid, this.grade, this.quality))
        .append("当前hp=%ld\n".formatted(this.hp_now))
        .append("战力=%ld\n".formatted(this.fight))
        .append("总血量=%ld\n".formatted(this.hp_max))
        .append("攻击=%ld\n".formatted(this.attack))
        .append("护甲=%ld\n".formatted(this.hujia))
        .append("穿透=%ld\n".formatted(this.chuantou))
        .append("闪避=%ld\n".formatted(this.miss))
        .append("命中=%ld\n".formatted(this.precision))
        .append("速度=%ld\n".formatted(this.speed));
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

}
