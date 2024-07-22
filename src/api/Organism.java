package src.api;

import java.util.Map;
import java.util.function.Predicate;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import src.api.Warehouse.SellType;

import static src.api.Util.obj2long;
public class Organism {

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
        this.qualityLevel = Quality.QNAME2LEVEL.getOrDefault(quality, 0);
        this.fight = new BigInteger(element.getAttribute("fight"));

        List<Skill> tmp = new ArrayList<>();
        Element skillsEle = (Element) element.getElementsByTagName("sk").item(0);
        NodeList skillList = skillsEle.getElementsByTagName("item");
        for (int i = 0; i < skillList.getLength(); i++) {
            int skillId = Integer.parseInt(((Element)skillList.item(i)).getAttribute("id"));
            tmp.add(Skill.getSkill(skillId));
        }
        skills = tmp;
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
        for (int i=0; i<skills.size(); i++) {
            sb.append(skills.get(i).toShortString());
            if (i%2==0){
                if (i==skills.size()-1) {
                    sb.append("\n");
                }
                else{
                    sb.append("\t\t");
                }
            }
            else {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** @return null if no such skill */
    public Skill getSkillByName(String skillName){
        for (Skill skill : this.skills) {
            if (skill.name.equals(skillName)){
                return skill;
            }
        }
        return null;
    }
    /** @return null if no such skill */
    public Skill getSkillById(int skillid){
        for (Skill skill : this.skills) {
            if (skill.id == skillid){
                return skill;
            }
        }
        return null;
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
    /** 品质等级 */
    public final Integer qualityLevel;
    /** 战斗力 */
    public final BigInteger fight;

    public final List<Skill> skills;

    public static LinkedHashMap<Integer, Organism> getNewestOrganisms(){
        if (Warehouse.loadWarehouse()){
            return getOrganisms();
        }
        else return null;
    }

    public static boolean loadOrganisms(Document document){
        clear();
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

    public static void clear(){
        if (organismMap==null) organismMap = new LinkedHashMap<>();
        else organismMap.clear();
    }

    private static LinkedHashMap<Integer, Organism> organismMap = new LinkedHashMap<>();

    private static boolean isEmpty(){
        return organismMap==null || organismMap.size()==0;
    }

    /** 安全获取植物摘要（获取不到返回id） */
    public static String getOrgShortStr(int id){
        if (isEmpty()) {
            Warehouse.loadWarehouse();
        }
        Organism o = organismMap.get(id);
        return o==null?String.valueOf(id):o.toShortString();
    }

    /** 懒加载 */
    public static Organism getOrganism(int id){
        if (isEmpty() || organismMap.get(id)==null) {
            Warehouse.loadWarehouse();
        }
        return organismMap.get(id);
    }

    public static void setGrade(int id, int new_grade){
        Organism plant = organismMap.get(id);
        plant.grade_pre = new_grade;
        organismMap.put(id, plant);
    }

    public static LinkedHashMap<Integer, Organism> getOrganisms(){
        if (isEmpty()){
            Warehouse.loadWarehouse();
        }
        return organismMap;
    }

    private static void show(boolean byGrade, String filter){
        if (Warehouse.loadWarehouse()){
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

                if (filter==null) {
                    for (Organism organism : orgList) {
                        System.out.println(organism);
                    }
                }
                else{
                    orgList.stream().forEach(org->{
                        if (org.orid.name.contains(filter)){
                            System.out.println(org);
                        }
                    });
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

    public static boolean sell(int id){
        return Warehouse.sell(SellType.ORGANISM_TYPE, id, 1);
    }

    private static List<List<Integer>> recordedList = new ArrayList<>();

    private static Predicate<Organism> resolveFilter(String[] args){
        Predicate<Organism> res = new Predicate<Organism>() {
            @Override
            public boolean test(Organism t) {
                return true;
            }
        };

        for (String condi : args) {
            String attr = condi.substring(0, 2);
            String opr = condi.substring(2,4);
            String value = condi.substring(4);
            switch (attr) {
                case "nm":{
                    if (opr.equals("==")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.orid.name.equals(value);
                            }
                        });
                    }
                    else if (opr.equals("={")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.orid.name.contains(value);
                            }
                        });
                    }
                    else return null;
                    break;
                }
                case "gr":{
                    int gvalue = Integer.parseInt(value);
                    if (opr.equals("==")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.grade == gvalue;
                            }
                        });
                    }
                    else if (opr.equals(">=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.grade >= gvalue;
                            }
                        });
                    }
                    else if (opr.equals("<=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.grade <= gvalue;
                            }
                        });
                    }
                    else if (opr.equals("<<")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.grade < gvalue;
                            }
                        });
                    }
                    else if (opr.equals(">>")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.grade > gvalue;
                            }
                        });
                    }
                    else if (opr.equals("!=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.grade != gvalue;
                            }
                        });
                    }
                    else return null;
                    break;
                }
                case "id":{
                    
                    int ivalue = Integer.parseInt(value);
                    if (opr.equals("==")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.id == ivalue;
                            }
                        });
                    }
                    else if (opr.equals(">=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.id >= ivalue;
                            }
                        });
                    }
                    else if (opr.equals("<=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.id <= ivalue;
                            }
                        });
                    }
                    else if (opr.equals("<<")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.id < ivalue;
                            }
                        });
                    }
                    else if (opr.equals(">>")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.id > ivalue;
                            }
                        });
                    }
                    else if (opr.equals("!=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.id != ivalue;
                            }
                        });
                    }
                    else return null;
                    break;
                }
                case "ql":{
                    Integer qvalue = Quality.QNAME2LEVEL.get(value);
                    if (qvalue==null) {
                        return null;
                    }
                    if (opr.equals("==")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.qualityLevel == qvalue;
                            }
                        });
                    }
                    else if (opr.equals(">=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.qualityLevel >= qvalue;
                            }
                        });
                    }
                    else if (opr.equals("<=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.qualityLevel <= qvalue;
                            }
                        });
                    }
                    else if (opr.equals("<<")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.qualityLevel < qvalue;
                            }
                        });
                    }
                    else if (opr.equals(">>")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.qualityLevel > qvalue;
                            }
                        });
                    }
                    else if (opr.equals("!=")){
                        res = res.and(new Predicate<Organism>() {
                            @Override
                            public boolean test(Organism t) {
                                return t.qualityLevel != qvalue;
                            }
                        });
                    }
                    else return null;

                    break;
                }
                    
            
                default:{
                    return null;
                }
            }
        }
        return res;
    }

    private static int record(Predicate<Organism> conditions){
        List<Organism> organisms = new ArrayList<>(getNewestOrganisms().values().stream().
        filter(conditions).toList());
        List<Integer> ids = new ArrayList<>(organisms.stream().map(o->o.id).toList());
        int no = recordedList.size();
        recordedList.add(ids);
        return no;
    }

    private static int record(List<Integer> uList, Predicate<Organism> conditions){
        List<Organism> organisms = new ArrayList<>(uList.stream().map(id->getOrganism(id)).filter(conditions).toList());
        List<Integer> ids = new ArrayList<>(organisms.stream().map(o->o.id).toList());
        int no = recordedList.size();
        recordedList.add(ids);
        return no;
    }

    private static void show(List<Integer> ids){
        for (Integer id : ids) {
            Organism organism = Organism.getOrganism(id);
            System.out.println(organism);
        }
    }
    public static void main(String[] args) {
        if ((args.length == 1 || args.length == 2) && args[0].equals("show")) {
            if (args.length==1){
                show(true,null);
                return;
            }
            else if (args[1].equals("id")){
                show(false,null);
                return;
            }
        }
        else if ((args.length == 2) && args[0].equals("search")) {
            String str = args[1];
            show(true, str);
            return;
        }
        else if ((args.length == 2) && args[0].equals("sell")) {
            int id = Integer.parseInt(args[1]);
            sell(id);
            return;
        }
        else if ((args.length == 2) && args[0].equals("sellall")) {
            List<Integer> plants = Util.readIntegersFromFile(args[1]);
            for (Integer plt : plants) {
                sell(plt);
            }
            return;
        }
        else if (args.length >= 1 && args[0].equals("filter")){
            Predicate<Organism> predicate;
            try {
                predicate = resolveFilter(Arrays.copyOfRange(args, 1,args.length));
            } catch (Exception e) {
                predicate=null;
            }
            
            if (predicate!=null){
                int no = record(predicate);
                List<Integer> list = recordedList.get(no);
                System.out.println("┌------ Group %d ------┐  共%d个".formatted(no,list.size()));
                show(list);
                System.out.println("└------ Group %d ------┘  共%d个".formatted(no,list.size()));
                return;
            }
        }
        else if (args.length >= 2 && args[0].equals("filterp")){
            int nold = Integer.parseInt(args[1]);
            if (nold>=recordedList.size()){
                System.out.println("Group %d don't exist!".formatted(nold));
                return;
            }

            Predicate<Organism> predicate;
            try {
                predicate = resolveFilter(Arrays.copyOfRange(args, 2,args.length));
            } catch (Exception e) {
                predicate=null;
            }
            
            if (predicate!=null){
                int no = record(recordedList.get(nold), predicate);
                List<Integer> list = recordedList.get(no);
                System.out.println("┌------ Group %d ------┐  共%d个".formatted(no,list.size()));
                show(list);
                System.out.println("└------ Group %d ------┘  共%d个".formatted(no,list.size()));
                return;
            }
        }
        else if (args.length==3 && args[0].equals("save")){
            Integer no = Integer.parseInt(args[1]);
            if (no<recordedList.size()){
                Util.saveIntegersToFile(recordedList.get(no), args[2]);
                return;
            }
        }
        else if (args.length==2 && args[0].equals("showf")){
            show(Util.readIntegersFromFile(args[1]));
            return;
        }
        System.out.println("args: show [id]");
        System.out.println("or  : search <name>");
        System.out.println("or  : sell <id>");
        System.out.println("or  : sellall <filename>");
        System.out.println("or  : filter <conditions>...");
        System.out.println("or  : filterp <from_group> <conditions>...");
        System.out.println("filters: nm == ={ <value>");
        System.out.println("filters: gr == <= >= << >> != <value>");
        System.out.println("filters: id == <= >= << >> != <value>");
        System.out.println("filters: ql == <= >= << >> != <stringvalue>");
        System.out.println("or  : save <group_no> <filename>");
        System.out.println("or  : showf <filename>");
    }
}
