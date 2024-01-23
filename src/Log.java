package src;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public class Log {

    public static void flogln(String str){
        fileStream.print(Util.dateFormatNow("HH:mm:ss "));
        fileStream.println(str);
    }

    public static void flog(String str){
        fileStream.print(Util.dateFormatNow("HH:mm:ss "));
        fileStream.print(str);
    }

    public static void flog(){
        fileStream.print(Util.dateFormatNow("HH:mm:ss "));
    }

    public static void fprint(String str){
        fileStream.print(str);
    }

    public static void fprintln(String str){
        fileStream.println(str);
    }

    public static void logln(String str){
        fileStream.print(Util.dateFormatNow("HH:mm:ss "));
        fileStream.println(str);
        System.out.println(str);
    }

    public static void logln(){
        fileStream.println();
        System.out.println();
    }

    public static void log(String str){
        fileStream.print(Util.dateFormatNow("HH:mm:ss "));
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
        Path filePath = Path.of("log/%s/%s.log".formatted(
            Util.dateFormatNow("yyyyMM"),
            Util.dateFormatNow("dd_HHmmss")
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
            System.out.println("on|off: close or enable file log");
        }
    }
}
