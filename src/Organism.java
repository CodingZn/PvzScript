package src;

import java.util.Map;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static src.Util.obj2long;
public class Organism {

    public static LinkedHashMap<Integer, Organism> getNewestOrganisms(){
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

    private static LinkedHashMap<Integer, Organism> organismMap = new LinkedHashMap<>();

    public static Organism getOrganism(int id){
        if (organismMap==null || organismMap.size()==0) {
            loadOrganisms();
        }
        return organismMap.get(id);
    }

    public static void setGrade(int id, int new_grade){
        Organism plant = organismMap.get(id);
        plant.grade_pre = new_grade;
        organismMap.put(id, plant);
    }

    public static LinkedHashMap<Integer, Organism> getOrganisms(){
        return organismMap;
    }


    // TODO 数据溢出
    public Organism(Element element){
        this.id = Integer.parseInt(element.getAttribute("id"));
        this.pid = Integer.parseInt(element.getAttribute("pid"));
        this.orid = Orid.getOrid(pid);
        this.attack = new BigInteger(element.getAttribute("at"));
        this.hujia = new BigInteger(element.getAttribute("mi"));
        this.speed = obj2long(element.getAttribute("sp"));
        this.hp_now = new BigInteger(element.getAttribute("hp"));
        this.hp_max = new BigInteger(element.getAttribute("hm"));
        this.grade = Integer.parseInt(element.getAttribute("gr"));
        this.grade_pre = grade;
        this.chuantou = new BigInteger(element.getAttribute("pr"));
        this.miss = new BigInteger(element.getAttribute("new_miss"));
        this.precision = new BigInteger(element.getAttribute("new_precision"));
        this.quality = element.getAttribute("qu");
        this.fight = new BigInteger(element.getAttribute("fight"));
    }

    public String toShortString(){
        if (this.grade_pre > this.grade){
            return "Lv%d %s(%d)".formatted(this.grade_pre, this.orid.name, this.id);
        }
        else{
            return "Lv.%d %s(%d)".formatted(this.grade, this.orid.name, this.id);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s(%d)   \t pid=%-5d  等级=%-3d  品质=%s\n".formatted(this.orid.name, this.id,this.pid, this.grade, this.quality))
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
    public final Orid orid;
    /** 攻击 */
    public final BigInteger attack;
    /** 护甲 */
    public final BigInteger hujia;
    /** 速度 */
    public final long speed;
    /** 当前血量 */
    public BigInteger hp_now;
    /** 满血量 */
    public final BigInteger hp_max;
    /** 等级 */
    public final int grade;
    /** 预测等级 */
    public int grade_pre;

    /** 穿透 */
    public final BigInteger chuantou;
    /** 闪避 */
    public final BigInteger miss;
    /** 命中 */
    public final BigInteger precision;
    /** 品质字符串 */
    public final String quality;
    /** 战斗力 */
    public final BigInteger fight;

    private static void show(boolean byGrade){
        if (loadOrganisms()){
            LinkedHashMap<Integer, Organism> map = getOrganisms();
            if (byGrade){
                List<Organism> orgList = new ArrayList<>(map.values());
                Collections.sort(orgList, new Comparator<Organism>() {
                    @Override
                    public int compare(Organism o1, Organism o2) {
                        if (o1.grade > o2.grade){
                            return -1;
                        }else if (o1.grade == o2.grade && o1.id < o2.id){
                            return -1;
                        }else return 1;
                    }
                });
                for (Organism organism : orgList) {
                    System.out.println(organism);
                }
            }
            else{
                for (Map.Entry<Integer, Organism> orga : map.entrySet()) {
                    System.out.println(orga.getValue());
                }
            }
        }
        return;
    }

    public static void main(String[] args) {
        if ((args.length == 1 || args.length == 2) && args[0].equals("show")) {
            if (args.length==1){
                show(true);
                return;
            }
            else if (args[1].equals("id")){
                show(false);
                return;
            }
        }
        System.out.println("args: show ['id']");
    }
}
