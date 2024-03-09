package src;

import static src.Util.delay;
import static src.Util.parseDate;

import java.util.Date;

public class Control {
    public static void main(String[] args) {
        if (args.length==2 && args[0].equals("wait")) {
            int seconds = Integer.parseInt(args[1]);
            delay(seconds*1000);
            return;
        }else if (args.length==2 && args[0].equals("waitto")) {
            
            if (args[1].matches("^((0?|1)[0-9]|2[0-3]):[0-5][0-9]$")){
                String targetStr = Util.dateFormatNow("yyyy-MM-dd") + " "+args[1]+":00";
                Date target = parseDate(targetStr);
                if (target!=null){
                    long waitMs = Math.max(target.getTime()-new Date().getTime(), 0L);
                    delay(waitMs);
                }
                return;
            }else if (args[1].matches("^((0?|1)[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")){
                String targetStr = Util.dateFormatNow("yyyy-MM-dd") + " "+args[1];
                Date target = parseDate(targetStr);
                if (target!=null){
                    long waitMs = Math.max(target.getTime()-new Date().getTime(), 0L);
                    delay(waitMs);
                }
                return;
            }
        }
        
        System.out.println("args: wait <seconds>");
        System.out.println("or  : waitto <time>");
        System.out.println("<time>: format like HH:mm or HH:mm:ss");
        
    }
}
