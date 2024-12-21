package src.api;

import java.io.Serializable;

public class Seed implements Serializable{
    public int server;
    public String id;
    public String level;
    public String nickname;
    public String cookie;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Seed){
            Seed other = (Seed) obj;
            return this.server==other.server && this.id==other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return nickname.hashCode() ^ Integer.hashCode(server);
    }
    
    @Override
    public String toString() {
        return "s%d-%s-lv.%s-%s".formatted(server,id,level,nickname);
    }

    private Seed(){}

    public boolean isValid(){
        return true;
    }

    public static Seed readFromXmlSeed(String filename){
        String txt = src.api.Util.readText(filename, false, "UTF-8");
        String uid =src.api.Util.findStrBetween(txt, "<UserID>", "</UserID>");
        if (uid==null) return null;
        String nkname =src.api.Util.findStrBetween(txt, "<UserName>", "</UserName>");
        if (nkname==null) return null;
        String level0str =src.api.Util.findStrBetween(txt, "</UserName>", "<UserRandomString>");
        if (level0str==null) return null;
        String level =src.api.Util.findStrBetween(level0str, "<UserLevel>", "</UserLevel>");
        if (level==null) return null;
        String domain =src.api.Util.findStrBetween(txt, "<UserDomain>", "</UserDomain>");
        if (domain==null) return null;
        String server = src.api.Util.findStrBetween(domain,"s",".youkia");
        if (server==null) return null;
        String cookie =src.api.Util.findStrBetween(txt, "<UserCookies>", "</UserCookies>");
        if (cookie==null) return null;
        Seed acc = new Seed();
        acc.cookie=cookie;
        acc.id=uid;
        acc.nickname=nkname;
        acc.server=Integer.parseInt(server);
        acc.level=level;
        return acc;
    }

    public boolean activate(){
        Request.setServer(server);
        Cookie.applyCookie(cookie);
        User me = User.getMe();
        return me != null;
    }

    public static boolean activate(String filename){
        Seed tmp = Seed.readFromXmlSeed(filename);
        if (tmp == null) {
            System.out.println("请检查种子格式！");
            return false;
        }
        if (tmp.activate() == false){
            System.out.println("用户登录失败！请检查种子是否有效！");
            return false;
        }
        return true;
    }

    public static void resolve(String[] args) {
        if (args.length == 2) {
            if (args[0].equals("load")){
                activate(args[1]);
                return;
            }
            
        }
        System.out.println("args: load <filename>");
    }

}
