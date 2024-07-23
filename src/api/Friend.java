package src.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Friend {
    public final int grade;
    public final String name;
    public final int id_pvz;
    public final int id_platform;

    public Friend(Element friEle){
        id_pvz = Integer.parseInt(friEle.getAttribute("id"));
        id_platform = Integer.parseInt(friEle.getAttribute("platform_user_id"));
        name = friEle.getAttribute("name");
        grade = Integer.parseInt(friEle.getAttribute("grade"));
    }

    public Friend(User user){
        id_pvz = user.id;
        id_platform = user.user_id;
        name = user.name;
        grade = user.grade;
    }

    public Friend(int id, int id_plat, int grad, String nm){
        id_pvz=id;
        id_platform=id_plat;
        name=nm;
        grade=grad;
    }

    public static Friend fFriend(int id){
        return new Friend(id, 0, 0, "<unknown>");
    }

    @Override
    public String toString() {
        return "%s(%d, lv%d)".formatted(this.name, this.id_pvz, this.grade);
    }

    private static LinkedHashMap<Integer, Friend> friendMap;

    /** 懒加载，包括自己 */
    public static LinkedHashMap<Integer, Friend> getFriendMap(){
        if (friendMap==null) {
            loadFriends();
        }
        return friendMap;
    }

    public static List<Friend> getFriendList(){
        LinkedHashMap<Integer, Friend> map = getFriendMap();
        return new ArrayList<>(map.values());
    }

    public static Friend getFriend(int id){
        Friend f = getFriendMap().get(id);
        if (f==null) {
            if (id==User.getUser().id){
                return new Friend(User.getUser());
            }
            return fFriend(id);
        }
        return f;
    }

    /** 请求函数 */
    public static byte[] getMyFriends(int index){
        String path = "/pvz/index.php/user/friends/page/%d/sig/807a46a7269ca3d84a1b980f9d47265a?4068335279559=".formatted(index);
        Log.logln("远程获取好友列表...");
        return Request.sendGetRequest(path);
    }

    /** 保存到文件 */
    public static boolean saveFriendsToFile(String filename){
        byte[] response = getMyFriends(1);
        if (!save(response, filename+"_%d".formatted(1))) return false;
        int pages = getFriendPages(response);
        for (int i = 2; i <= pages; i++) {
            response = getMyFriends(i);
            if (!save(response, filename+"_%d".formatted(i))) return false;
        }
        return true;
    }

    private static boolean save(byte[] response, String filename){
        Log.println("保存好友信息到文件%s...".formatted(filename));
        return Util.saveBytesToFile(response, filename);
    }

    // 工具函数
    private static int getFriendPages(Document document){
        if (document != null) {
            Element friendsEle =(Element) document.getElementsByTagName("friends").item(0);
            String countStr = friendsEle.getAttribute("page_count");
            return Integer.parseInt(countStr);
        }
        return 0;
    }

    private static int getFriendPages(byte[] response){
        Document document=Util.parseXml(response);
        return getFriendPages(document);
    }

    /** 默认存取路径，index默认1 */
    private static String getDefaultFilename(int index){
        return "userdata/%d/friend_%d.xml".formatted(User.getUser().id,index);
    }

    // 加载好友
    /** 自动从userdata/id/friend_x.xml 中加载好友信息，若无则请求并存到本地 */
    public static boolean loadFriends(){
        Document document=Util.parseXml(getDefaultFilename(1));
        // 本地文件不存在
        if (document==null) {
            byte[] response = getMyFriends(1);
            if (!save(response, getDefaultFilename(1))) return false;
            document=Util.parseXml(response);
            resolveFriends(document,true);
            int pages = getFriendPages(response);
            for (int i = 2; i <= pages; i++) {
                response = getMyFriends(i);
                if (!save(response, getDefaultFilename(i))) return false;
                document=Util.parseXml(response);
                resolveFriends(document,false);
            }
        }
        // 本地加载
        else{
            resolveFriends(document,true);
            int pages = getFriendPages(document);
            for (int i = 2; i<=pages; i++) {
                document=Util.parseXml(getDefaultFilename(i));
                if (document==null) {
                    break;
                }
                resolveFriends(document,false);
            }

        }
        return true;
    }

    /** 将某页好友信息加载到friendMap中。reload==true时，清空并自动加自己 */
    private static boolean resolveFriends(Document document, boolean reload){
        if (reload) {
            friendMap.clear();
            User me = User.getUser();
            friendMap.put(me.id, new Friend(me.id,me.user_id,me.grade,me.name));
        }
        if (document==null){
            Log.logln("加载好友信息失败！");
            return false;
        }
        Element friendsEle =(Element) document.getElementsByTagName("friends").item(0);
        NodeList fList = friendsEle.getChildNodes();
        for (int i = 0; i < fList.getLength(); i++) {
            Node node = fList.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE){
                Friend friend = new Friend((Element) node);
                friendMap.put(friend.id_pvz, friend);
            }
        }
        return true;
    }

    public static void main(String[] args){
        if (args.length == 2) {
            if (args[0].equals("saveto")){
                saveFriendsToFile(args[1]);
                return;
            }
            // else if (args[0].equals("reload")){
            //     return;
            // }

        }
        System.out.println("args: saveto <filename>");
        // System.out.println("or  : reload");
    }
}
