package src;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;

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

    @Override
    public String toString() {
        return "%s(%d, lv%d)".formatted(this.name, this.id_pvz, this.grade);
    }

    private static String localFriendFile = null;

    private static LinkedHashMap<Integer, Friend> friendMap;

    /** 懒加载 */
    public static LinkedHashMap<Integer, Friend> getFriendMap(){
        if (friendMap==null) {
            loadFriends();
        }
        return friendMap;
    }

    public static byte[] getMyFriends(){
        String path = "/pvz/index.php/user/friends/page/1/sig/807a46a7269ca3d84a1b980f9d47265a?4068335279559=";
        Log.println("远程获取好友列表...");
        return Request.sendGetRequest(path);
    }

    public static boolean saveFriendsToFile(String filename){
        byte[] response = getMyFriends();
        Path filePath = Path.of(filename);
        Path dirPath = filePath.getParent();
        if (dirPath!=null){
            dirPath.toFile().mkdirs();
        }
        File oFile = filePath.toFile();
        try {
            Log.println("保存好友信息到文件...");
            oFile.createNewFile();
            FileOutputStream fStream = new FileOutputStream(oFile);
            fStream.write(response);
            fStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    public static boolean loadFriends(){
        friendMap = new LinkedHashMap<>();
        Document document=null;
        if (localFriendFile!=null) {
            Log.logln("本地加载好友文件...");
            document = Util.parseXml(localFriendFile);
        }
        if (document==null) {
            byte[] response = getMyFriends();
            document = Util.parseXml(response);
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
            if (args[0].equals("save")){
                saveFriendsToFile(args[1]);
                return;
            }
            if (args[0].equals("loadby")){
                if (args[1].equals("remote")) localFriendFile = null;
                else localFriendFile = args[1];
                return;
            }

        }
        System.out.println("args: save <filename>");
        System.out.println("or  : loadby remote|<filename>");
    }
}
