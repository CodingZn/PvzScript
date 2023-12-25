package src;

import java.io.PrintStream;

public class Util {
    public static void printBytes(byte[] bytes, PrintStream out){
        for (int i = 0; i < bytes.length; i++) {
            if (i%16==0){
                out.println();
            }
            out.printf("%02x ", bytes[i]);
        }
    }

    public static boolean delay(int ms){
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
