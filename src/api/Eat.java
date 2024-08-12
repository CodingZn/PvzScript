package src.api;

import java.util.Collection;
import java.util.LinkedList;
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
            String f = (String) ((ASObject) body.getValue()).get("fight");
            Log.println("%s增加%s".formatted(type.desc,v));
            zhu.increaseProperty(type, v, f);
            map.remove(fuId);
            return true;
        }
        else{
            Log.println(exc);
            return false;
        }
    }

    public static boolean gunXueQiu(int iniPlantId, Collection<Integer> foods_, EatType type){
        LinkedList<Integer> foods=new LinkedList<>(foods_);
        Integer baoId = iniPlantId;
        while (!foods.isEmpty()) {
            Integer id = foods.get(0);
            foods.remove(0);
            if (!eatPlant(id, baoId, type, 10)){
                return false;
            }
            baoId = id;
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length==4 && args[0].equals("gun")) {
            EatType type = EatType.get(args[1]);
            if (type!=null) {
                Integer ini = Integer.parseInt(args[2]);
                List<Integer> foods = Util.readIntegersFromFile(args[3]);
                gunXueQiu(ini, foods, type);
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
        System.out.println("args: gun <type> <ini_id> <plants_filename>");
        System.out.println("type: hp gj hj ct sb mz sd");
        System.out.println("or  : confirm on|off");
    }
}
