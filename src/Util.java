package src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;
import com.exadel.flamingo.flex.messaging.amf.io.AMF0Deserializer;

import lib.MyAMF0Serializer;

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

    public static byte[] encodeAMF(String target, String response, Object value, byte type){
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        MyAMF0Serializer serializer = new MyAMF0Serializer(new DataOutputStream(bStream));
        AMF0Message message = new AMF0Message();
        message.setVersion(3);
        message.addBody(target, response, value, AMF0Body.DATA_TYPE_ARRAY);
        try {
            serializer.serializeMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        return bStream.toByteArray();
    }
}
