package src.api;

public class Cookie {
    private static String currCookie;

    public static boolean isValidCookie(String str){
        if (str==null) return false;
        return str.matches("^[A-Za-z0-9_;= %/,\\+\\*\\.\\w\\n]+$");
    }

    public static boolean loadCookie(String filename){
        setCookieValue(Util.readText(filename,true));
        return currCookie!=null;
    }

    public static String getCookie(){
        return currCookie;
    }

    public static void saveCookie(String filename){
        Util.writeText(filename, currCookie, true);
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
            str=str.replaceAll("\\n", "");
            if (!str.equals(currCookie)) {
                User.clear();
                currCookie = str;
                User.getUser();
            }
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
