package src.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Eat {
    public enum EatType{
        HP(445,"HP","hp"), 
        ATTACK(446,"攻击","attack"), 
        HUJIA(447,"护甲","miss"),
        CHUANTOU(448,"穿透","precision"),
        SPEED(449,"速度","speed"), 
        SHANBI(1093,"闪避","new_miss"),
        MINGZHONG(1094,"命中","new_precision");
    
        public final int value;
        public final String desc;
        public final String field;
        private EatType(int v, String s, String f){
            value=v;
            desc=s;
            field=f;
        }
        public static final EatType get(String nm){
            switch (nm.toLowerCase()) {
                case "hp"->{return HP;}
                case "gj"->{return ATTACK;}
                case "hj"->{return HUJIA;}
                case "ct"->{return CHUANTOU;}
                case "sb"->{return SHANBI;}
                case "mz"->{return MINGZHONG;}
                case "sd"->{return SPEED;}
                default->{return null;}
            }
        }
    }
    
    private static boolean confirm=true;
    public static boolean getConfirm(){return confirm;}
    public static void setConfirm(boolean v){confirm=v;}

    private static boolean checkThreshold(EatType type, Organism fu){
        if (type==EatType.HP) {
            return fu.hp_max.compareTo(gunHPThreshold) <0;
        }else if (type==EatType.ATTACK) {
            return fu.attack.compareTo(gunGJThreshold) <0;
        }else {
            return fu.getProperty(type).compareTo(gunOTThreshold) <0;
        }
    }

    public static boolean eatPlant(int zhuId, int fuId, EatType type, int zengqiang){
        Map<Integer,Organism> map = Organism.getOrganisms();
        Organism zhu =map.get(zhuId);
        Organism fu =map.get(fuId);
        if (zhu==null || fu==null) {
            System.out.println("有植物不存在！");
            return false;
        }
        if (zengqiang<0 || zengqiang>10 || type==null){
            System.out.println("参数错误！");
            return false;
        }
        if (!checkThreshold(type, fu)){
            System.out.println("属性已达到阈值！");
            Evolution.evolve(fuId, fnevoRoute);
            return false;
        }
        byte[] req = Util.encodeAMF("api.tool.synthesis", "/1", new Object[]{
            zhuId,fuId,type.value,zengqiang
        });
        Log.log("%s{%s}吃%s{%s}的%s ".formatted(zhu.toShortString(),zhu.getProperty(type),
        fu.toShortString(),fu.getProperty(type),type.desc));
        if (confirm){
            System.out.print("?");
            if (!Util.confirm()) return false;
        }
        byte[] response = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(response).getBody(0);
        String exc = Response.getExceptionDescription(response);
        if (exc==null){
            String v = (String) ((ASObject) body.getValue()).get(type.field);
            BigInteger vi = new BigInteger(v);
            String f = (String) ((ASObject) body.getValue()).get("fight");
            Log.println("%s增加%s".formatted(type.desc,Util.bigInt2String(vi)));
            zhu.increaseProperty(type, v, f);
            map.remove(fuId);
            return true;
        }
        else{
            Log.println(exc);
            return false;
        }
    }

    /** 会去除已用过的植物 */
    public static boolean gunXueQiu(int iniPlantId, List<Integer> foods, EatType type){
        foods.remove(Integer.valueOf(iniPlantId));
        Integer baoId = iniPlantId;
        while (!foods.isEmpty()) {
            Integer id = foods.get(0);
            if (!eatPlant(id, baoId, type, 10)){
                return false;
            }
            foods.remove(0);
            baoId = id;
        }
        Evolution.evolve(baoId, fnevoRoute);
        return true;
    }
    public static final BigInteger BASE_12 = new BigInteger("100000000000000000000");
    public static final BigInteger BASE_11 = new BigInteger("10000000000000000000");

    private static BigInteger gunHPThreshold = new BigInteger("320000000000000000000");
    private static BigInteger gunGJThreshold = new BigInteger("97751710000000000000");
    private static BigInteger gunOTThreshold = new BigInteger("320000000000000000000");

    public static void setHPThresh(double input){
        long vv = (long) input*100;
        BigInteger mtvv=BigInteger.valueOf(vv);
        gunHPThreshold=BASE_12.multiply(mtvv).divide(BigInteger.valueOf(100));
    }
    public static void setGJThresh(double input){
        long vv = (long) input*100;
        BigInteger mtvv=BigInteger.valueOf(vv);
        gunGJThreshold=BASE_11.multiply(mtvv).divide(BigInteger.valueOf(100));
    }
    public static void setOTThresh(double input){
        long vv = (long) input*100;
        BigInteger mtvv=BigInteger.valueOf(vv);
        gunOTThreshold=BASE_12.multiply(mtvv).divide(BigInteger.valueOf(100));
    }
    public static BigInteger getHPTh(){return gunHPThreshold;}
    public static BigInteger getGJTh(){return gunGJThreshold;}
    public static BigInteger getOTTh(){return gunOTThreshold;}

    private static List<Integer> fnevoRoute=new ArrayList<>();

    /** 0：不进化 1：绿叶飞飞 2：火龙果美女 3：旋风火龙果 4：周瑜蕉弩 */
    public static void setFnevo(int i){
        switch (i) {
            case 0->{
                fnevoRoute.clear();
                Log.logln("滚完不进化");
            }
            case 1->{
                fnevoRoute.clear();
                fnevoRoute.add(32);
                Log.logln("滚完进化到绿叶飞飞");
            }
            case 2->{
                fnevoRoute.clear();
                fnevoRoute.add(103);
                Log.logln("滚完进化到火龙果美女");
            }
            case 3->{
                fnevoRoute.clear();
                fnevoRoute.add(103);
                fnevoRoute.add(40);
                Log.logln("滚完进化到旋风火龙果");
            }
            case 4->{
                fnevoRoute.clear();
                fnevoRoute.add(103);
                fnevoRoute.add(106);
                Log.logln("滚完进化到周瑜蕉弩");
            }
        
        }
    }



    public static void main(String[] args) {
        if (args.length==4 && args[0].equals("gun")) {
            EatType type = EatType.get(args[1]);
            if (type!=null) {
                Integer ini = Integer.parseInt(args[2]);
                List<Integer> foods = Util.readIntegersFromFile(args[3]);
                gunXueQiu(ini, foods, type);
                Util.saveIntegersToFile(foods, args[3]);
                return;
            }
        }
        else if(args.length==2 && args[0].equals("confirm")){
            if (args[1].equals("on")) {
                setConfirm(true);
                return;
            }else if (args[1].equals("off")) {
                setConfirm(false);
                return;
            }
        }
        else if(args.length==3 && args[0].equals("gunth")){
            if (args[1].equals("hp")) {
                setHPThresh(Double.parseDouble(args[2]));
                return;
            }else if (args[1].equals("gj")) {
                setGJThresh(Double.parseDouble(args[2]));
                return;
            }else if (args[1].equals("ot")){
                setOTThresh(Double.parseDouble(args[2]));
                return;
            }
        }
        else if(args.length==2 && args[0].equals("fnevo")){
            if (args[1].length()==1&&"01234".contains(args[1])){
                setFnevo(Integer.parseInt(args[1]));
                return;
            }
        }
        System.out.println("args: gun <type> <ini_id> <plants_filename>");
        System.out.println("type: hp gj hj ct sb mz sd");
        System.out.println("or  : confirm on|off");
        System.out.println("or  : gunth hp|gj|ot <value>");
        System.out.println("value: float, unit=10^12亿(except 攻击) or 10^11亿(攻击)");
        System.out.println("or  : fnevo 0|1|2|3|4");
        System.out.println("fnevo: 0：不进化 1：绿叶飞飞 2：火龙果美女 3：旋风火龙果 4：周瑜蕉弩 ");
    }
}
