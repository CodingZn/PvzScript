package src.api;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

@SuppressWarnings({"resource"})
public class Log {

    private static String addLogInfo(String content){
        String timeStr = Util.dateFormatNow("HH:mm:ss.SSS");
        // String meStr = User.getMeStr();
        String meStr = "";
        return "%s %s%s".formatted(timeStr,meStr,content);
    }

    public static void flog(String str){
        fileStream.print(Util.dateFormatNow("HH:mm:ss.SSS "));
        fileStream.print(str);
    }

    public static void fprint(String str){
        fileStream.print(str);
    }

    public static void fprintln(String str){
        fileStream.println(str);
    }

    public static void logln(String str){
        str = addLogInfo(str);
        fileStream.println(str);
        System.out.println(str);
    }

    public static void log(String str){
        str = addLogInfo(str);
        fileStream.print(str);
        System.out.print(str);
    }

    public static void print(String str){
        fileStream.print(str);
        System.out.print(str);
    }

    public static void println(String str){
        fileStream.println(str);
        System.out.println(str);
    }

    public static void println(){
        fileStream.println();
        System.out.println();
    }


    private static final PrintStream nullStream;
    private static final PrintStream fStream;
    private static PrintStream fileStream;

    static{
        Path filePath = Path.of("log/%s/%s_%05d.log".formatted(
            Util.dateFormatNow("yyyyMM"),
            Util.dateFormatNow("dd_HHmmss"),
            Util.getPid()
        ));
        
        filePath.getParent().toFile().mkdirs();
        nullStream = new PrintStream(PrintStream.nullOutputStream());
        PrintStream tmpfStream;
        try {
            tmpfStream = new PrintStream(filePath.toFile());
        } catch (IOException e) {
            System.out.println("创建日志文件失败！");
            e.printStackTrace();
            tmpfStream = nullStream;
        }
        fStream = tmpfStream;
        fileStream = fStream;
        System.setErr(new PrintStream(new MyTeeOutput(fStream, System.out)));
    }

    public static void main(String[] args) {
        if (args.length==1 && args[0].equals("off")) {
            fileStream = nullStream;
        }else if (args.length==1 && args[0].equals("on")) {
            fileStream = fStream;
        }else {
            System.out.println("args: on|off");
        }
    }
}
