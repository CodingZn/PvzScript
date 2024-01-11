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

    private static LinkedHashMap<Integer, Friend> friendMap = new LinkedHashMap<>();

    public static LinkedHashMap<Integer, Friend> getFriendMap(){
        return friendMap;
    }

    public static boolean saveFriendsToFile(String filename){
        String path = "/pvz/index.php/user/friends/page/1/sig/807a46a7269ca3d84a1b980f9d47265a?4068335279559=";
        byte[] response = Request.sendGetRequest(path);
        Path filePath = Path.of(filename);
        Path dirPath = filePath.getParent();
        if (dirPath!=null){
            dirPath.toFile().mkdirs();
        }
        File oFile = filePath.toFile();
        try {
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

    public static boolean loadFriends(String filename){
        friendMap.clear();
        Document document = Util.parseXml(filename);
        if (document==null) return false;
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
            if (args[0].equals("load")){
                loadFriends(args[1]);
                return;
            }

        }
        System.out.println("args: save|load filename");
    }
}
