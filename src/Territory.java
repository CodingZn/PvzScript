package src;

import static src.Util.obj2int;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Body;

import flex.messaging.io.ASObject;

public class Territory {
    
    public static boolean getAward(int id, String nickname){
        byte[] req = Util.encodeAMF("api.territory.getAward", "/1", new Object[]{id});
        Log.log("领取 %s 领地收益 ".formatted(nickname));
        byte[] res = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(res).getBody(0);
        if (Response.isOnStatusException(body, false)){
            return false;
        }
        ASObject resobj = (ASObject) body.getValue();
        Log.print("%d金币 ".formatted(obj2int(resobj.get("money"))));
        int honor = obj2int(resobj.get("honor"));
        if (honor!=0) {
            Log.print("%d荣誉".formatted(honor));
        }
        Log.println();
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public static List<ASObject> getTerritoryInfo(int id, String nickname){
        byte[] req = Util.encodeAMF("api.territory.getTerritory", "/1", new Object[]{id});
        Log.log("查看 %s 领地 ".formatted(nickname));
        byte[] res = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(res).getBody(0);
        if (Response.isOnStatusException(body, false)){
            return null;
        }
        Log.println();
        ASObject resobj = (ASObject) body.getValue();
        return (List<ASObject>) resobj.get("teritory");
        
    }

    public static boolean getAllAward(){
        User user = User.getUser();
        List<ASObject> territoryList = getTerritoryInfo(user.id, user.name);
        for (ASObject terrObj : territoryList) {
            int uid = obj2int(terrObj.get("user_id"));
            getAward(uid, terrObj.get("user_nickname").toString());
        }
        return true;
    }

    public static boolean battle(int id, int plantid1, Integer plantid2){
        int[] plantid;
        String plaString;
        if (plantid2==null){
            plantid=new int[]{plantid1};
            plaString = Organism.getOrganism(plantid1).toShortString();
        }else{
            plantid=new int[]{plantid1, plantid2};
            plaString = Organism.getOrganism(plantid1).toShortString() + " "+
                Organism.getOrganism(plantid2).toShortString() ;
        }
        Object[] value = new Object[]{id, plantid, 1,0};
        byte[] req = Util.encodeAMF("api.territory.challenge", "/1", value);
        Log.log("使用植物[%s]占领 %d 领地 ".formatted(plaString,id));
        byte[] res = Request.sendPostAmf(req, true);
        AMF0Body body = Util.decodeAMF(res).getBody(0);
        if (Response.isOnStatusException(body, true)){
            return false;
        }
        ASObject resobj = (ASObject) body.getValue();
        boolean is_fight = obj2int(resobj.get("is_fight"))==1;
        if (is_fight){
            Log.print("战斗");
            ASObject fightObj = (ASObject) resobj.get("fight");
            Log.print((Boolean)fightObj.get("is_winning")?"成功 ":"失败 ");
        }
        else{
            Log.print("完成 ");
        }
        
        int cost = obj2int(resobj.get("cost_money"));
        int honor = obj2int(resobj.get("honor"));
        Log.println("花费%d金币 当前荣誉%d".formatted(cost,honor));
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public static boolean keep(List<Integer> userids, List<Integer> plants){
        User user = User.getUser();
        List<ASObject> territoryList = getTerritoryInfo(user.id, user.name);
        Set<Integer> occupiedUser = new HashSet<>();
        Set<Integer> occupiedOrg = new HashSet<>();
        for (ASObject asObject : territoryList) {
            int user_id = obj2int(asObject.get("user_id"));
            occupiedUser.add(user_id);
            List<ASObject> orgs = (List<ASObject>) asObject.get("organisms");
            for (ASObject org : orgs) {
                int orgid = obj2int(org.get("id"));
                occupiedOrg.add(orgid);
            }
        }
        plants.removeIf(p->occupiedOrg.contains(p));
        for (Integer uid : userids) {
            if (!occupiedUser.contains(uid)){
                if (plants.size()==0) {
                    Log.logln("尝试占领%d的领地无可用植物！".formatted(uid));
                    return false;
                }
                int plt = plants.get(0);
                battle(uid, plt, null);
                plants.remove(0);
                occupiedOrg.add(plt);
                occupiedUser.add(uid);
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public static boolean save(String userFile, String plantFile){
        User user = User.getUser();
        List<ASObject> territoryList = getTerritoryInfo(user.id, user.name);
        List<Integer> occupiedUser = new ArrayList<>();
        List<Integer> occupiedOrg = new ArrayList<>();
        for (ASObject asObject : territoryList) {
            int user_id = obj2int(asObject.get("user_id"));
            occupiedUser.add(user_id);
            List<ASObject> orgs = (List<ASObject>) asObject.get("organisms");
            for (ASObject org : orgs) {
                int orgid = obj2int(org.get("id"));
                occupiedOrg.add(orgid);
            }
        }
        Util.saveIntegersToFile(occupiedOrg, plantFile);
        Util.saveIntegersToFile(occupiedUser, userFile);
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("award")){
            getAllAward();
            return;
        }
        else if ((args.length == 3 || args.length == 4) && args[0].equals("battle")){
            int tid = Integer.parseInt(args[1]);
            int plant1 = Integer.parseInt(args[2]);
            Integer pl2 = null;
            if (args.length==4){
                pl2 = Integer.parseInt(args[3]);
            }
            battle(tid, plant1, pl2);
            return;
        }
        else if (args.length == 3 && args[0].equals("keep")){
            List<Integer> userids = Util.readIntegersFromFile(args[1]);
            List<Integer> plants = Util.readIntegersFromFile(args[2]);
            keep(userids, plants);
            return;
        }
        else if (args.length == 3 && args[0].equals("save")){
            save(args[1],args[2]);
            return;
        }

        System.out.println("args: award");
        System.out.println("or  : battle <userid> <plant1> [<plant2>]");
        System.out.println("or  : keep <userid_file> <plant_file>");
        System.out.println("or  : save <userid_file> <plant_file>");
    }
}
