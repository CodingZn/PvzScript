package src;

import static src.Util.decodeAMF;
import static src.Util.obj2int;
import static src.Util.obj2long;

import java.io.File;
import java.util.List;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;

public class Shop {
    public static final String path = "static/shop";
    public static ASObject loadShop(int no){
        String filename = "%s%d".formatted(path,no);
        File file = new File(filename);
        if (file.isFile()){
            AMF0Message msg = decodeAMF(filename);
            return (ASObject) msg.getBody(0).getValue();
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static boolean buy(int buy_id, int amount){
        byte[] req = Util.encodeAMF("api.shop.buy", "/1", new Object[]{buy_id, amount});
        Log.log("兑换或购买项%d，%d个 ".formatted(buy_id, amount));
        byte[] res = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(res).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        boolean status = (Boolean)((ASObject) body.getValue()).get("status");
        Log.print(status?"成功 ":"失败 ");
        ASObject toolObj = (ASObject)((ASObject) body.getValue()).get("tool");
        // 购买的是道具
        if (toolObj!=null) {
            int id_now = obj2int(toolObj.get("id"));
            String toolname = Tool.getTool(id_now).name;
            long amount_now = obj2long(toolObj.get("amount"));
            Log.println("当前有%d个%s".formatted(amount_now,toolname));
        }
        List<ASObject> plantObj = (List<ASObject>)((ASObject) body.getValue()).get("organisms");
        // 购买的是植物
        if (plantObj!=null && plantObj.size()>0) {
            ASObject first = plantObj.get(0);
            int id_first = obj2int(first.get("id"));
            int pid = obj2int(first.get("prototype_id"));
            Log.println("%s的起始id为%d".formatted(Orid.getOrid(pid).name,id_first));
        }
        return true;
    }

    public static void main(String[] args) {
        // if ((args.length==2||args.length==3)&&args[1].equals("show")) {
            
        // }else if (args.length==3 && args[1].equals("search")) {
            
        // }else 
        if (args.length==3 && args[0].equals("buy")) {
            int buy_id = Integer.parseInt(args[1]);
            int amount = Integer.parseInt(args[2]);
            buy(buy_id, amount);
            return;
        }
        
        // System.out.println("args: <shop_no> show [<id>]");
        // System.out.println("or  : <shop_no> search <tool_name>");
        System.out.println("args: buy <buy_id> <amount>");
        System.out.println("礼券购买: 1006.挑战书 1018.时之沙 1015.高级血瓶 1014.中级血瓶 1013.低级血瓶");
        System.out.println("金币购买: 218.蓝叶草 219.双叶草");
        System.out.println("荣誉购买: 4450.增强卷轴");
    }
}
