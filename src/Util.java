package src;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.exadel.flamingo.flex.amf.AMF0Message;
import com.exadel.flamingo.flex.messaging.amf.io.AMF0Deserializer;

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

    public static AMF0Message decodeAMF(byte[] bytes){
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream di = new DataInputStream(bArrayInputStream);
        try {
            AMF0Deserializer deserializer = new AMF0Deserializer(di);
            return deserializer.getAMFMessage();
        } catch (IOException e) {
            System.out.println("Error Decoding AMF message!");
            return null;
        }
        
    }

}
