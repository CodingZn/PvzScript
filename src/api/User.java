package src.api;

import static src.api.Util.obj2bigint;
import static src.api.Util.obj2int;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import src.interf.UserChangeCallback;

public class User {
    /** 平台id */
    public final int user_id;
    /** pvz游戏id */
    public final int id;
    public final String name;

    public final int grade;
    /** 游戏币 */
    private BigInteger money;
    private int rmb_money;

    private int stone_cha_count;
    private int tree_height;
    private boolean tree_done;
    public final int cave_cha_max;
    private int cave_cha_amount;
    private int honor;
    private int fuben_cha;
    /** 不全，仅有第一页！ */
    public final List<Friend> friends;

    public User(Document document){
        Element userEle = (Element) document.getElementsByTagName("user").item(0);
        user_id = obj2int(userEle.getAttribute("user_id"));
        id = obj2int(userEle.getAttribute("id"));
        name = userEle.getAttribute("name");
        // grade = Integer.parseInt(userEle.getAttribute("name"));
        money = obj2bigint(userEle.getAttribute("money"));
        rmb_money = Integer.parseInt(userEle.getAttribute("rmb_money"));
        stone_cha_count = Integer.parseInt(userEle.getAttribute("stone_cha_count"));

        Element treeEle = (Element) userEle.getElementsByTagName("tree").item(0);
        tree_height = Integer.parseInt(treeEle.getAttribute("height"));
        tree_done = (treeEle.getAttribute("today").equals("1"));

        Element gradeEle = (Element) userEle.getElementsByTagName("grade").item(0);
        grade = Integer.parseInt(gradeEle.getAttribute("id"));

        Element caveEle = (Element) userEle.getElementsByTagName("cave").item(0);
        cave_cha_amount = Integer.parseInt(caveEle.getAttribute("amount"));
        cave_cha_max = Integer.parseInt(caveEle.getAttribute("max_amount"));

        Element territoryEle = (Element) userEle.getElementsByTagName("territory").item(0);
        honor = Integer.parseInt(territoryEle.getAttribute("honor"));

        Element fubenEle = (Element) userEle.getElementsByTagName("fuben").item(0);
        fuben_cha = Integer.parseInt(fubenEle.getAttribute("fuben_lcc"));

        Element friendsEle = (Element) userEle.getElementsByTagName("friends").item(0);
        NodeList fList = friendsEle.getElementsByTagName("item");
        friends = new ArrayList<>();
        friends.add(new Friend(this.id,this.user_id,this.grade,this.name));
        for (int i = 0; i < fList.getLength(); i++) {
            friends.add(new Friend((Element) fList.item(i)));
        }

    }

    @Override
    public String toString() {
        return "s%d-%s".formatted(Request.getServer(),this.name);
    }

    public BigInteger getMoney(){ return money; }
    public void changeMoney(long amount){ 
        money.add(new BigInteger(Long.toString(amount)));
        doCallback(); 
    }

    public int getRmbMoney(){ return rmb_money; }
    public void changeRmbMoney(int amount){ rmb_money+=amount;doCallback(); }

    public int getStoneCha(){ return stone_cha_count; }
    public void changeStoneCha(int amount){ stone_cha_count+=amount;doCallback(); }

    public int getTreeHeight(){ return tree_height; }
    public void changeTreeHeight(int amount){ tree_height+=amount;doCallback(); }

    public boolean getTreeDone(){ return tree_done; }
    public void changeTreeDone(boolean new_done){ tree_done=new_done;doCallback(); }

    public int getCaveCha(){ return cave_cha_amount; }
    public synchronized void changeCaveCha(int amount){ 
        cave_cha_amount+=amount; 
        doCallback();
    }

    public int getHonor(){ return honor; }
    public void changeHonor(int amount){ honor+=amount;doCallback(); }

    public int getFubenCha(){ return fuben_cha; }
    public void changeFubenCha(int amount){ fuben_cha+=amount;doCallback(); }

    private static User me;

    /** 加载最新用户信息 */
    public static User loadUser(){
        String url = "/pvz/index.php/default/user/sig/e5bf533f2151a47642b38ba33ae21953?"+Long.toString(new Date().getTime());
        Log.log("加载用户信息...");
        byte[] response = Request.sendGetRequest(url);
        Document document = Util.parseXml(response);
        if (document == null) {
            return null;
        }
        try {
            me = new User(document);
            Log.println("成功！");
        } catch (Exception e) {
            // e.printStackTrace();
            Log.println("失败！");
            me=null;
        }
        return me;
    }

    /** 不加载，可能为null */
    public static User getMe(){
        return me;
    }

    /** 不加载，可能为空字符串 */
    public static String getMeStr(){
        return me==null?"":"<%s>".formatted(me.toString());
    }

    /** 懒加载 */
    public static User getUser(){
        if (me==null){
            return loadUser();
        }
        else return me;
    }


    public static void clear(){
        me=null;
        Warehouse.clear();
    }

    public static boolean savePic(int plat_id,String filename){
        byte[] response = Request.sendGetWWWRequest("/attach/picture/%d/%d/face64.jpg".formatted(
            plat_id/10000+1,plat_id
        ));
        return Util.saveBytesToFile(response, filename);
    }

    public static boolean savePic(String filename){
        if (User.getUser()!=null) {
            return savePic(User.getUser().user_id,filename);
        }
        return false;
    }

    /** 信息展示回调 */
    private UserChangeCallback callback;
    private void doCallback(){
        if (callback!=null) {
            callback.onCallback(User.getUser());
        }
    }
    public void setCallback(UserChangeCallback cbk){
        this.callback=cbk;
    }

}
