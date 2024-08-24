package src.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Eat {
    private static boolean confirm=true;
    public static boolean getConfirm(){return confirm;}
    public static void setConfirm(boolean v){confirm=v;}
    
    /** 传承=1，自动传承=2，不传承=0 */
    private static int chuancheng=1;
    public static int getChuancheng(){return chuancheng;}
    public static void setChuancheng(int v){chuancheng=v;}

    /** 确保baoid植物存在 */
    private static boolean checkThreshold(PropType type, Organism fu){
        if (type==PropType.HP) {
            return fu.hp_max.compareTo(gunHPThreshold) <=0;
        }else if (type==PropType.ATTACK) {
            return fu.attack.compareTo(gunGJThreshold) <=0;
        }else {
            return fu.getProperty(type).compareTo(gunOTThreshold) <=0;
        }
    }

    public static boolean eatPlant(int zhuId, int fuId, PropType type, int zengqiang){
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
        Log.log("%s吃%s的%s ".formatted(zhu.toShortString(type),
        fu.toShortString(type),
        type.desc));
        if (confirm){
            System.out.print("?");
            if (!Util.confirm()) return false;
        }
        byte[] req = Util.encodeAMF("api.tool.synthesis", "/1", new Object[]{
            zhuId,fuId,type.value,zengqiang
        });
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
    public static boolean gunXueQiu(int iniPlantId, List<Integer> foods, PropType type, int chuan){
        foods.remove(Integer.valueOf(iniPlantId));
        Integer baoId = iniPlantId;
        while (!foods.isEmpty()) {
            Integer zhuid = foods.get(0);
            Organism fu = Organism.getOrganism(baoId);
            if (fu==null){
                Log.logln("植物%d不存在！".formatted(baoId));
                foods.remove(zhuid);
                return false;
            }
            // 超过阈值，允许传承滚包
            if (chuan>=1){
                BigInteger prop = fu.getProperty(type);
                int jz;
                if (type==PropType.HP){
                    jz = computeChuan(prop, getHPTh());
                }else if (type==PropType.ATTACK){
                    jz = computeChuan(prop, getGJTh());
                }else {
                    jz = computeChuan(prop, getOTTh());
                }
                
                if (jz==-1){
                    Evolution.evolve(baoId, fnevoRoute);
                    if (chuan==2){
                        Log.logln("副植物%s属性达到阈值2倍！分成2个。".formatted(fu.toShortString(type)));
                        if (!chuanCheng(baoId, zhuid, type, 5)){
                            Log.logln("分裂失败！".formatted(baoId));
                            return false;
                        }
                        foods.remove(zhuid);
                        // 将传入植物滚到阈值
                        if (!gunXueQiu(zhuid, foods, type, 1)) return false;
                        // 将传出植物继续滚
                        continue;
                    }
                    else{
                        Log.logln("副植物%s属性超限！".formatted(fu.toShortString(type)));
                        return true;
                    }
                }

                // 传承一些值给主植物
                if ((jz>=1&&jz<=5)){
                    if (!chuanCheng(baoId, zhuid, type, jz)){
                        return false;
                    }
                    else{
                        foods.remove(zhuid);
                    }
                }
                if (!eatPlant(zhuid, baoId, type, 10)){
                    return false;
                }
                foods.remove(zhuid);
                baoId = zhuid;
                continue;
            }
            // 不允许传承
            else{
                if (!checkThreshold(type, fu)) {
                    Log.logln("属性已达到阈值！");
                    Evolution.evolve(baoId, fnevoRoute);
                    return true;
                } 
                // 包在阈值之下
                if (!eatPlant(zhuid, baoId, type, 10)){
                    return false;
                }
                foods.remove(zhuid);
                baoId = zhuid;
                continue;
            }
        }
        Evolution.evolve(baoId, fnevoRoute);
        return true;
    }
    public static final BigInteger BASE_12 = new BigInteger("100000000000000000000");
    public static final BigInteger BASE_11 = new BigInteger("10000000000000000000");

    private static BigInteger gunHPThreshold = new BigInteger("360000000000000000000");
    private static BigInteger gunGJThreshold = new BigInteger("97751710000000000000");
    private static BigInteger gunOTThreshold = new BigInteger("500000000000000000000");

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

    public static boolean chuanCheng(int chuId, int ruId, PropType type, int juanzhou){
        Map<Integer,Organism> map = Organism.getOrganisms();
        Organism zhu =map.get(chuId);
        Organism fu =map.get(ruId);
        Log.log("%s传承%d%% %s 给%s ".formatted(zhu.toShortString(type),
        juanzhou*10,type.desc, fu.toShortString(type)));
        if (confirm){
            System.out.print("?");
            if (!Util.confirm()) return false;
        }
        byte[] req = Util.encodeAMF("api.apiorganism.exchangeOne", "/1", new Object[]{
            ruId,chuId,type.chuanId,juanzhou
        });
        byte[] res = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(res).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        @SuppressWarnings({ "unchecked" })
        List<ASObject> orgs = (List<ASObject>) body.getValue();
        Log.println("成功");
        for (ASObject org0: orgs) {
            int id =Util.obj2int(org0.get("id"));
            Organism orgg = map.get(id);
            orgg.changeProperty(type, (String)org0.get(type.field), (String)org0.get("fighting"));
            Log.logln("%s %s 变为 %s".formatted(orgg.toShortString(), type.desc, 
            Util.bigInt2String(orgg.getProperty(type))));
        }
        return true;
    }

    /** 返回使用最少传承卷轴的个数，使副植物的剩余属性小于等于阈值
     * -1如果遇到错误：副植物属性大于2倍阈值；
     * 0：副植物属性小于等于阈值
     * 1~5：阈值 < 副植物属性 <= 2*阈值
     */
    public static int computeChuan(BigInteger original, BigInteger threshold){
        /** 100% 90% 80% 70% 60% 50% of original */
        BigInteger[] percents = new BigInteger[6];
        for (int i = 0; i < percents.length; i++) {
            int decimal = 10-i;
            percents[i]=new BigInteger(String.valueOf(decimal))
            .multiply(original)
            .divide(new BigInteger("10"));
        }
        int jz=0;
        for (; jz < percents.length; jz++) {
            if (percents[jz].compareTo(threshold)<=0){
                break;
            }
        }
        if (jz==6) jz=-1;
        return jz;
    }

    public static void test(){
        int fu = 0;
        for(fu=300; fu<=720; fu+=10){
            BigInteger fus = new BigInteger(String.valueOf(fu));
            BigInteger thresh = new BigInteger("360");
            System.out.println("fu=%d: %d".formatted(fu,computeChuan(fus, thresh)));
        }
    }


    public static void main(String[] args) {
        if (args.length==4 && args[0].equals("gun")) {
            PropType type = PropType.get(args[1]);
            if (type!=null) {
                Integer ini = Integer.parseInt(args[2]);
                List<Integer> foods = Util.readIntegersFromFile(args[3]);
                gunXueQiu(ini, foods, type, getChuancheng());
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
        else if(args.length==2 && args[0].equals("chuancheng")){
            if (args[1].equals("on")) {
                setChuancheng(1);
                return;
            }else if (args[1].equals("off")) {
                setChuancheng(0);
                return;
            }else if (args[1].equals("auto")) {
                setChuancheng(2);
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
        System.out.println("or  : chuancheng on|off|auto");
        System.out.println("or  : gunth hp|gj|ot <value>");
        System.out.println("value: float, unit=10^12亿(except 攻击) or 10^11亿(攻击)");
        System.out.println("or  : fnevo 0|1|2|3|4");
        System.out.println("fnevo: 0：不进化 1：绿叶飞飞 2：火龙果美女 3：旋风火龙果 4：周瑜蕉弩 ");
    }

}
