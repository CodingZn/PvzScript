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

    private static boolean toFile;

    static{
        Date now = new Date();
        Calendar noe = Calendar.getInstance();
        noe.setTime(now);
        Path filePath = Path.of("log/%4d%02d/%02d_%02d%02d%02d.log".formatted(
            noe.get(Calendar.YEAR),
            noe.get(Calendar.MONTH)+1,
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

        OutputStream o2 = new MyTeeOutput(stdStream,fileStream);
        bothStream = new PrintStream(o2, true);

    }

    public static PrintStream bothOut(){
        if (toFile) return bothStream;
        else return stdStream;
    }

    public static PrintStream stdOut(){
        return stdStream;
    }

    public static PrintStream fileOut(){
        if (toFile) return fileStream;
        else return nullStream;
    }
   
    public static void printHelp(){
        System.out.println("args: on|off");
        System.out.println("on|off: close or enable file log");
    }

    public static void main(String[] args) {
        if (args.length==1 && args[0].equals("off")) {
            toFile = false;
        }else if (args.length==1 && args[0].equals("on")) {
            toFile = true;
        }else {
            printHelp();
        }
    }
}
