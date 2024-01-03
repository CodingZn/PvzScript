package src;

import java.io.FileInputStream;

public class Cookie {
    private static String currCookie;

    static {
        loadCookie();
    }

    public static boolean loadCookie(String filename){
        try (FileInputStream reader = new FileInputStream(filename)) {
            currCookie = new String(reader.readAllBytes());
            return true;
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
}
