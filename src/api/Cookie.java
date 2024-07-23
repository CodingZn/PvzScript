package src.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Cookie {
    private static String currCookie;

    static {
        loadCookie();
    }

    public static boolean isValidCookie(String str){
        if (str==null) return false;
        return str.matches("^[A-Za-z0-9_;= %/\\.]+$");
    }

    public static boolean loadCookie(String filename){
        setCookieValue(readCookie(filename));
        return currCookie!=null;
    }

    public static boolean loadCookie(){
        return loadCookie("data/cookie");
    }

    public static String getCookie(){
        return currCookie;
    }

    public static void saveCookie(String filename){
        try (FileOutputStream printer = new FileOutputStream(filename,false)) {
            printer.write(currCookie.getBytes());
        } catch (Exception e) {
            Log.logln("写入到%s失败！".formatted(filename));
            return;
        }
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
        if (!newCookie.equals(currCookie)) {
            setCookieValue(newCookie);
            return true;
        }
        return false;
    }

    private static void setCookieValue(String str){
        if (isValidCookie(str)){
            if (!str.equals(currCookie)) {
                User.clear();
            }
            currCookie = str;
        }
        else{
            if (!isValidCookie(currCookie)){
                currCookie=null;
                User.clear();
            }
            Log.logln("cookie格式不正确！");
        }
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
        System.out.println("args: load <filename>");
        System.out.println("or  : set <cookieString>");
    }
}
