package src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Cookie {
    private static String currCookie;

    static {
        loadCookie();
    }

    public static boolean loadCookie(String filename){
        try (FileInputStream reader = new FileInputStream(filename)) {
            currCookie = new String(reader.readAllBytes());
            return true;
        } catch (FileNotFoundException e){
            System.out.println("文件%s不存在！".formatted(filename));
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadCookie(){
        return loadCookie("data/cookie");
    }

    public static String getCookie(){
        return currCookie;
    }

    public static boolean setCookie(String newCookie){
        currCookie = newCookie;
        return true;
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
