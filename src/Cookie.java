package src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Cookie {
    private static String currCookie;

    static {
        loadCookie();
    }

    public static boolean loadCookie(String filename){
        currCookie = readCookie(filename);
        User.clear();
        return currCookie!=null;
    }

    public static boolean loadCookie(){
        return loadCookie("data/cookie");
    }

    public static String getCookie(){
        return currCookie;
    }

    public static String readCookie(String filename){
        try (FileInputStream reader = new FileInputStream(filename)) {
            return new String(reader.readAllBytes());
        } catch (FileNotFoundException e){
            Log.logln("文件%s不存在！".formatted(filename));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setCookie(String newCookie){
        if (!currCookie.equals(newCookie)) {
            currCookie = newCookie;
            User.clear();
            return true;
        }
        return false;
    }

    public static void resolver(String[] args) {
        if (args.length == 2) {
            if (args[0].equals("load")){
                Cookie.loadCookie(args[1]);
                return;
            }
            else if (args[0].equals("set")){
                Cookie.setCookie(args[1]);
                return;
            }
            
        }
        System.out.println("args: load filename");
        System.out.println("or  : set cookieString");
    }
}
