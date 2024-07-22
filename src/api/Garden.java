package src.api;

import static src.api.Util.obj2int;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Garden {

    public static final int[] MIN_LEVEL = new int[]{1,21,41,51,61,71,81,86,91,96,101,106,111,116,121};
    public static final int[] MAX_LEVEL = new int[]{20,40,50,60,70,80,85,90,95,100,105,110,115,120,125};

    private static int gradeToIndex(int grade){
        if (grade>=1 && grade<=40) {
            return (grade-1)/20;
        }
        if (grade>=41 && grade<=80) {
            return (grade-1)/10-2;
        }
        if (grade>=81 && grade<=125) {
            return (grade-1)/5-10;
        }
        return -1;
    }

    private static List<Integer> preferGrades = null;
    private static String plantFilename = null;

    private static int remainBeatCount = 5;
    private static int remainToBeat = 5;

    public static Document lookGarden(int grade, int pvzid, String name){
        Log.logln("查看lv.%d %s(%d)的花园".formatted(grade,name,pvzid));
        byte[] response = Request.sendGetRequest("/pvz/index.php/garden/index/id/%d/sig/c3f56017a56ae0e93716fdffbb3ade91?%d".formatted(
            pvzid, new Date().getTime()
        ));
        Document document = Util.parseXml(response);
        return document;
    }

    private static int getRemainBeatCount(Document gardenDoc){
        Element garden = (Element) gardenDoc.getElementsByTagName("garden").item(0);
        return Integer.parseInt(garden.getAttribute("cn"));
    }

    private static List<Garden.Monster> getMonsters(Document gardenDoc){
        Element monster = (Element) gardenDoc.getElementsByTagName("monster").item(0);
        NodeList monList = monster.getElementsByTagName("mon");
        List<Monster> res = new ArrayList<>();
        for (int i = 0; i < monList.getLength(); i++) {
            if (monList.item(i).getNodeType()==Node.ELEMENT_NODE){
                Monster monst = new Monster((Element)monList.item(i));
                res.add(monst);
            }
        }
        return res;
    }

    static class Monster {
        final int ownerId;
        final int lx;
        final int ly;
        final String name;
        
        public Monster(Element ele){
            name = ele.getAttribute("name");
            ownerId = Integer.parseInt(ele.getAttribute("owid"));
            Element position = (Element)ele.getElementsByTagName("position").item(0);
            lx = Integer.parseInt(position.getAttribute("lx"));
            ly = Integer.parseInt(position.getAttribute("ly"));
        }
    }

    public static boolean beatOnce(Monster monster){
        Object[] value = new Object[4];
        value[0] = monster.ownerId;
        value[1] = monster.lx;
        value[2] = monster.ly;
        List<Integer> plants = Util.readIntegersFromFile(plantFilename);
        if (plantFilename==null || plants==null) {
            Log.logln("请先设置好花园战斗的植物！");
            return false;
        }
        value[3] = Util.integerArr2int(plants.toArray());
        byte[] req = Util.encodeAMF("api.garden.challenge", "/1", value);
        Log.log("挑战%s(%d,%d)".formatted(monster.name,monster.lx,monster.ly));
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        if(Response.isOnStatusException(body, true)){
            return false;
        }
        ASObject obj = (ASObject)body.getValue();
        boolean is_winning = (Boolean)obj.get("is_winning");
        if (is_winning) {
            int exp = obj2int(obj.get("exp"));
            Log.println(" 成功，获得%d经验".formatted(exp));
            String awards_key = obj.get("awards_key").toString();
            GeneralBattle.getAward(awards_key);
            remainToBeat--;
        }else{
            Log.println(" 失败");
        }
        return true;
    }

    public static boolean checkBeatOneGarden(int grade, int pvzid, String name){
        Document gardenDoc = lookGarden(grade,pvzid, name);
        return beatOneGarden(gardenDoc);
    }

    public static boolean beatOneGarden(Document gardenDoc){
        List<Monster> monsters = getMonsters(gardenDoc);
        int actualCount = Math.min(remainToBeat, monsters.size());
        for (int i = 0; i < actualCount; i++) {
            if (!beatOnce(monsters.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean gardenBattleRepeat(int count){
        User me = User.getUser();
        List<Friend> friendList = me.friends;
        Document myGarden = lookGarden(me.grade,me.id, me.name);
        remainBeatCount = getRemainBeatCount(myGarden);
        if (count==-1) remainToBeat=remainBeatCount;
        else remainToBeat=Math.min(count, remainBeatCount);
        
        if (preferGrades==null) {
            Log.logln("请先设置花园打怪等级优先顺序！");
            return false;
        }
        // 按照设置的等级顺序去打
        for (Integer index : preferGrades) {
            if (remainToBeat<=0) return true;
            if (me.grade >= MIN_LEVEL[index] && me.grade <= MAX_LEVEL[index]) {
                if (!beatOneGarden(myGarden)){
                    return false;
                }
            }
            List<Friend> tofight = friendList.stream().filter(new Predicate<Friend>() {
                @Override
                public boolean test(Friend t) {
                    return t.grade>=MIN_LEVEL[index]&& t.grade <= MAX_LEVEL[index];
                }
            }).toList();
            for (Friend friend : tofight) {
                if (remainToBeat<=0) return true;
                if (!checkBeatOneGarden(friend.grade, friend.id_pvz, friend.name)){
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length==2&&args[0].equals("battle")) {
            int count;
            if (args[1].equals("auto")) {
                count=-1;
            }else{
                count=Integer.parseInt(args[1]);
            }
            gardenBattleRepeat(count);
            return;
        }
        else if (args.length>=1 && args[0].equals("bgrade")){
            preferGrades = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                int tmpgrade = Integer.parseInt(args[i]);
                preferGrades.add(gradeToIndex(tmpgrade));
            }
            return;
        }
        else if (args.length==2 && args[0].equals("bplant")){
            plantFilename = args[1];
            return;
        }
        System.out.println("args: battle auto|<count>");
        System.out.println("or  : bplant <plant_filename>");
        System.out.println("or  : bgrade <grade_to_beat>...");
    }
    
}