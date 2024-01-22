package src;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

public class Log {

    private static PrintStream bothStream;
    private static PrintStream nullStream;

    private static PrintStream fileStream;
    private static PrintStream stdStream;

    private static boolean toStd;
    private static boolean toFile;

    static{
        Date now = new Date();
        Calendar noe = Calendar.getInstance();
        noe.setTime(now);
        Path filePath = Path.of("log/%d%d/%d_%d%d%d.log".formatted(
            noe.get(Calendar.YEAR),
            noe.get(Calendar.MONTH),
            noe.get(Calendar.DATE),
            noe.get(Calendar.HOUR_OF_DAY),
            noe.get(Calendar.MINUTE),
            noe.get(Calendar.SECOND)));
        
        filePath.getParent().toFile().mkdirs();
        stdStream = System.out;
        nullStream = new PrintStream(PrintStream.nullOutputStream());
        try {
            fileStream = new PrintStream(filePath.toFile());
        } catch (IOException e) {
            System.out.println("创建日志文件失败！");
            e.printStackTrace();
            fileStream = null;
        }

        OutputStream o2 = new MyTeeOutput(fileStream, bothStream);
        bothStream = new PrintStream(o2);
        

    }

    private static void updateOutput(){
        if (toFile && toStd){
            System.setOut(bothStream);
        } else if (!toFile && toStd){
            System.setOut(stdStream);
        } else if (toFile && !toStd){
            System.setOut(fileStream);
        } else if (!toFile && !toStd){
            System.setOut(nullStream);
        } else assert false;
    }

    public static void set(boolean useFile, boolean useStd){
        toFile = useFile;
        toStd = useStd;
        updateOutput();
    }
   
    public static void printHelp(){
        System.out.println("args: on|off|show|hide");
        System.out.println("on|off: close or enable file log");
        System.out.println("show|hide: close or enable stdout");
    }

    public static void main(String[] args) {
        if (args.length==1 && args[0].equals("off")) {
            toFile = false;
            updateOutput();
        }else if (args.length==1 && args[0].equals("on")) {
            toFile = false;
            updateOutput();
        }else if (args.length==1 && args[0].equals("show")) {
            toStd = false;
            updateOutput();
        }else if (args.length==1 && args[0].equals("hide")) {
            toStd = true;
            updateOutput();
        }else {
            printHelp();
        }
    }
}
