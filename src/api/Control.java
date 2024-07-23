package src.api;

import static src.api.Util.delay;
import static src.api.Util.parseDate;

import java.util.Date;

public class Control {
    public static void main(String[] args) {
        if (args.length==2 && args[0].equals("wait")) {
            int seconds = Integer.parseInt(args[1]);
            Log.logln("waiting...");
            delay(seconds*1000);
            return;
        }else if (args.length==2 && args[0].equals("setserver")) {
            int server = Integer.parseInt(args[1]);
            Request.setServer(server);
            Log.logln("now server: s%d".formatted(Request.getServer()));
            Log.logln("please update your cookie!");
            return;
        }else if (args.length==2 && args[0].equals("waitto")) {
            
            if (args[1].matches("^((0?|1)[0-9]|2[0-3]):[0-5][0-9]$")){
                String targetStr = Util.dateFormatNow("yyyy-MM-dd") + " "+args[1]+":00";
                Date target = parseDate(targetStr);
                if (target!=null){
                    long waitMs = Math.max(target.getTime()-new Date().getTime(), 0L);
                    Log.logln("waiting...");
                    delay(waitMs);
                }
                return;
            }else if (args[1].matches("^((0?|1)[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")){
                String targetStr = Util.dateFormatNow("yyyy-MM-dd") + " "+args[1];
                Date target = parseDate(targetStr);
                if (target!=null){
                    long waitMs = Math.max(target.getTime()-new Date().getTime(), 0L);
                    Log.logln("waiting...");
                    delay(waitMs);
                }
                return;
            }
        }
        
        System.out.println("args: wait <seconds>");
        System.out.println("or  : setserver <index>");
        System.out.println("or  : waitto <time>");
        System.out.println("<time>: format like HH:mm or HH:mm:ss");
        
    }
}
