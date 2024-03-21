package src;

import static src.Util.obj2bigint;
import static src.Util.obj2int;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
        List<Friend> tmpList = new ArrayList<>();
        for (int i = 0; i < fList.getLength(); i++) {
            tmpList.add(new Friend((Element) fList.item(i)));
        }
        friends = tmpList;

    }

    public BigInteger getMoney(){ return money; }
    public void changeMoney(long amount){ money.add(new BigInteger(Long.toString(amount))); }

    public int getRmbMoney(){ return rmb_money; }
    public void changeRmbMoney(int amount){ rmb_money+=amount; }

    public int getStoneCha(){ return stone_cha_count; }
    public void changeStoneCha(int amount){ stone_cha_count+=amount; }

    public int getTreeHeight(){ return tree_height; }
    public void changeTreeHeight(int amount){ tree_height+=amount; }

    public boolean getTreeDone(){ return tree_done; }
    public void changeTreeDone(boolean new_done){ tree_done=new_done; }

    public int getCaveCha(){ return cave_cha_amount; }
    public void changeCaveCha(int amount){ cave_cha_amount+=amount; }

    public int getHonor(){ return honor; }
    public void changeHonor(int amount){ honor+=amount; }

    public int getFubenCha(){ return fuben_cha; }
    public void changeFubenCha(int amount){ fuben_cha+=amount; }

    private static User me;

    /** 加载最新用户信息 */
    public static User loadUser(){
        String url = "/pvz/index.php/default/user/sig/e5bf533f2151a47642b38ba33ae21953?"+Long.toString(new Date().getTime());
        byte[] response = Request.sendGetRequest(url);
        Document document = Util.parseXml(response);
        me = new User(document);
        return me;
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

}
