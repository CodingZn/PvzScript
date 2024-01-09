package lib;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import flex.messaging.io.ASObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class MyAMF0Deserializer {
   private static Log log = LogFactory.getLog(MyAMF0Deserializer.class);
   private List<Object> storedObjects = null;
   protected DataInputStream inputStream;
   protected int headerCount;
   protected List<?> headers = new ArrayList();
   protected int bodyCount;
   protected List<?> bodies = new ArrayList();
   protected AMF0Message message = new AMF0Message();
   private static final String data1 = "00 03 00 00 00 01 00 04 6E 75 6C 6C 00 02 2F 33 00 00 00 92 0A 00 00 00 01 11 0A 81 13 4F 66 6C 65 78 2E 6D 65 73 73 61 67 69 6E 67 2E 6D 65 73 73 61 67 65 73 2E 52 65 6D 6F 74 69 6E 67 4D 65 73 73 61 67 65 0D 73 6F 75 72 63 65 13 6F 70 65 72 61 74 69 6F 6E 09 62 6F 64 79 17 64 65 73 74 69 6E 61 74 69 6F 6E 11 63 6C 69 65 6E 74 49 64 15 74 69 6D 65 54 6F 4C 69 76 65 13 6D 65 73 73 61 67 65 49 64 0F 68 65 61 64 65 72 73 13 74 69 6D 65 73 74 61 6D 70 01 06 0D 75 70 64 61 74 65 09 03 01 0A 73 39 66 6C 65 78 2E 74 65 73 74 64 72 69 76 65 2E 73 74 6F 72 65 2E 50 72 6F 64 75 63 74 09 6E 61 6D 65 0B 70 72 69 63 65 0B 69 6D 61 67 65 11 63 61 74 65 67 6F 72 79 07 75 69 64 13 70 72 6F 64 75 63 74 49 64 17 64 65 73 63 72 69 70 74 69 6F 6E 06 0D 70 72 6F 64 20 33 05 40 12 3D 70 A3 D7 0A 3D 06 13 70 72 6F 64 33 2E 70 6E 67 06 17 70 72 6F 64 75 63 74 20 63 61 74 06 49 33 41 44 38 35 46 34 37 2D 31 35 33 36 2D 42 38 34 46 2D 36 36 34 42 2D 30 31 41 30 37 45 30 32 38 34 39 31 04 03 06 25 70 72 6F 64 20 64 65 73 63 72 69 70 74 69 6F 6E 20 33 06 0F 70 72 6F 64 75 63 74 01 04 00 06 49 37 32 31 32 39 46 38 37 2D 45 41 45 46 2D 35 46 37 35 2D 41 37 37 30 2D 30 31 41 30 41 35 45 43 42 37 34 32 0A 0B 01 15 44 53 45 6E 64 70 6F 69 6E 74 06 0D 6D 79 2D 61 6D 66 01 04 00";

   public MyAMF0Deserializer(DataInputStream inputStream) throws IOException {
      this.inputStream = inputStream;
      this.readHeaders();
       if (log.isDebugEnabled()) {
         log.debug("readHeader");
      }

      this.readBodies();
       if (log.isDebugEnabled()) {
         log.debug("readBody");
      }

   }

   public AMF0Message getAMFMessage() {
      return this.message;
   }

   protected void readHeaders() throws IOException {
      this.message.setVersion(this.inputStream.readUnsignedShort());
      this.headerCount = this.inputStream.readUnsignedShort();
       if (log.isDebugEnabled()) {
         log.debug("headerCount = " + this.headerCount);
      }

      for(int i = 0; i < this.headerCount; ++i) {
         this.storedObjects = new ArrayList();
         String key = this.inputStream.readUTF();
         boolean required = this.inputStream.readBoolean();
         this.inputStream.readInt();
         byte type = this.inputStream.readByte();
         Object value = this.readData(type);
         this.message.addHeader(key, required, value);
      }

   }

   protected void readBodies() throws IOException {
      this.bodyCount = this.inputStream.readUnsignedShort();
       if (log.isDebugEnabled()) {
         log.debug("bodyCount = " + this.bodyCount);
      }

      for(int i = 0; i < this.bodyCount; ++i) {
         this.storedObjects = new ArrayList();
         String method = this.inputStream.readUTF();
         String target = this.inputStream.readUTF();
         this.inputStream.readInt();
         byte type = this.inputStream.readByte();
          if (log.isDebugEnabled()) {
            log.debug("type = " + type);
         }

         Object data = this.readData(type);
         this.message.addBody(method, target, data, type);
      }

   }

   protected Object readCustomClass() throws IOException {
      String type = this.inputStream.readUTF();
       if (log.isDebugEnabled()) {
         log.debug("Reading Custom Class: " + type);
      }

      ASObject aso = new ASObject(type);
      return this.readObject(aso);
   }

   protected ASObject readObject() throws IOException {
      ASObject aso = new ASObject();
      return this.readObject(aso);
   }

   protected ASObject readObject(ASObject aso) throws IOException {
      this.storeObject(aso);
       if (log.isDebugEnabled()) {
         log.debug("reading object");
      }

      String key = this.inputStream.readUTF();

      for(byte type = this.inputStream.readByte(); type != 9; type = this.inputStream.readByte()) {
         Object value = this.readData(type);
         if (value == null) {
            log.info("Skipping NULL value for :" + key);
         } else {
            aso.put(key, value);
             if (log.isDebugEnabled()) {
               log.debug(" adding {key=" + key + ", value=" + value + ", type=" + type + "}");
            }
         }

         key = this.inputStream.readUTF();
      }

       if (log.isDebugEnabled()) {
         log.debug("finished reading object");
      }

      return aso;
   }

   protected List<?> readArray() throws IOException {
      List<Object> array = new ArrayList();
      this.storeObject(array);
       if (log.isDebugEnabled()) {
         log.debug("Reading array");
      }

      long length = (long)this.inputStream.readInt();
       if (log.isDebugEnabled()) {
         log.debug("array length = " + length);
      }

      for(long i = 0L; i < length; ++i) {
         byte type = this.inputStream.readByte();
         Object data = this.readData(type);
         array.add(data);
      }

      return array;
   }

   private void storeObject(Object o) {
      this.storedObjects.add(o);
       if (log.isDebugEnabled()) {
         log.debug("storedObjects.size: " + this.storedObjects.size());
      }

   }

   protected Date readDate() throws IOException {
      long ms = (long)this.inputStream.readDouble();
      int timeoffset = this.inputStream.readShort() * '\uea60' * -1;
      TimeZone serverTimeZone = TimeZone.getDefault();
      Calendar sent = new GregorianCalendar();
      sent.setTime(new Date(ms - (long)serverTimeZone.getRawOffset() + (long)timeoffset));
      TimeZone sentTimeZone = sent.getTimeZone();
      if (sentTimeZone.inDaylightTime(sent.getTime())) {
         sent.setTime(new Date(sent.getTime().getTime() - 3600000L));
      }

      return sent.getTime();
   }

   protected Object readFlushedSO() throws IOException {
      int index = this.inputStream.readUnsignedShort();
       if (log.isDebugEnabled()) {
         log.debug("Object Index: " + index);
      }

      return this.storedObjects.get(index);
   }

   protected Object readASObject() {
      return null;
   }

   protected Object readAMF3Data() throws IOException {
      ObjectInput amf3 = new MyAMF3Deserializer(this.inputStream);

      try {
         return amf3.readObject();
      } catch (ClassNotFoundException var3) {
         throw new RuntimeException(var3);
      }
   }

   protected Object readData(byte type) throws IOException {
       if (log.isDebugEnabled()) {
         log.debug("Reading data of type " + AMF0Body.getObjectTypeDescription(type));
      }

      switch (type) {
         case 0:
            return new Double(this.inputStream.readDouble());
         case 1:
            return this.inputStream.readBoolean();
         case 2:
            return this.inputStream.readUTF();
         case 3:
            return this.readObject();
         case 4:
            throw new IOException("Unknown/unsupported object type " + AMF0Body.getObjectTypeDescription(type));
         case 5:
         case 6:
            return null;
         case 7:
            return this.readFlushedSO();
         case 8:
            this.inputStream.readInt();
            return this.readObject();
         case 9:
            return null;
         case 10:
            return this.readArray();
         case 11:
            return this.readDate();
         case 12:
            return this.readLongUTF(this.inputStream);
         case 13:
            return this.readASObject();
         case 14:
            return null;
         case 15:
            return convertToDOM(this.inputStream);
         case 16:
            return this.readCustomClass();
         case 17:
            return this.readAMF3Data();
         default:
            throw new IOException("Unknown/unsupported object type " + AMF0Body.getObjectTypeDescription(type));
      }
   }

   private Object readLongUTF(DataInputStream in) throws IOException {
      int utflen = in.readInt();
      StringBuffer str = new StringBuffer(utflen);
      byte[] bytearr = new byte[utflen];
      int count = 0;
      in.readFully(bytearr, 0, utflen);

      while(count < utflen) {
         int c = bytearr[count] & 255;
         byte char2;
         switch (c >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
               ++count;
               str.append((char)c);
               break;
            case 8:
            case 9:
            case 10:
            case 11:
            default:
               throw new UTFDataFormatException();
            case 12:
            case 13:
               count += 2;
               if (count > utflen) {
                  throw new UTFDataFormatException();
               }

               char2 = bytearr[count - 1];
               if ((char2 & 192) != 128) {
                  throw new UTFDataFormatException();
               }

               str.append((char)((c & 31) << 6 | char2 & 63));
               break;
            case 14:
               count += 3;
               if (count > utflen) {
                  throw new UTFDataFormatException();
               }

               char2 = bytearr[count - 2];
               int char3 = bytearr[count - 1];
               if ((char2 & 192) != 128 || (char3 & 192) != 128) {
                  throw new UTFDataFormatException();
               }

               str.append((char)((c & 15) << 12 | (char2 & 63) << 6 | (char3 & 63) << 0));
         }
      }

      return new String(str);
   }

   public static Document convertToDOM(InputStream is) throws IOException {
      Document document = null;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      is.skip(4L);

      try {
         DocumentBuilder builder = factory.newDocumentBuilder();
         document = builder.parse(new InputSource(is));
         return document;
      } catch (Exception var4) {
         log.error(var4, var4);
         throw new IOException("Error while parsing xml: " + var4.getMessage());
      }
   }

   public static void main(String[] args) throws Exception {
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(toByteArray("00 00 00 01 00 12 41 70 70 65 6e 64 54 6f 47 61 74 65 77 61 79 55 72 6c 00 00 00 00 28 02 00 25 3f 50 48 50 53 45 53 53 49 44 3d 73 39 63 68 75 68 38 37 36 66 71 35 6c 64 6f 31 39 68 6e 67 75 66 6e 61 68 32 00 01 00 0b 2f 31 2f 6f 6e 52 65 73 75 6c 74 00 04 6e 75 6c 6c 00 00 06 23 11 0a 0b 5f 64 61 74 61 2e 77 65 62 2e 70 76 7a 5f 73 31 2e 61 70 70 2e 73 65 72 76 69 63 65 73 2e 63 6c 69 65 6e 74 2e 66 69 67 68 74 72 65 73 75 6c 74 15 61 73 73 61 69 6c 61 6e 74 73 09 0d 01 0a 0b 01 05 69 64 06 0f 39 31 33 37 37 34 37 05 68 70 06 11 32 39 36 38 31 33 38 36 0d 68 70 5f 6d 61 78 06 0a 09 6f 72 69 64 04 86 7f 0b 67 72 61 64 65 06 07 34 33 37 15 71 75 61 6c 69 74 79 5f 69 64 04 08 01 0a 0b 01 04 06 0f 38 37 32 37 38 39 32 08 06 15 36 33 34 38 39 33 31 30 30 33 0c 06 15 37 35 33 38 35 36 38 39 36 31 0e 04 85 3f 10 06 07 34 33 39 14 04 0a 01 0a 0b 01 04 06 11 31 30 30 39 33 37 36 36 08 06 09 36 31 39 35 0c 06 0b 31 30 35 31 35 0e 04 89 3a 10 06 05 33 39 14 04 06 01 0a 0b 01 04 06 11 31 30 30 39 33 35 35 39 08 06 0b 33 36 34 35 33 0c 06 0b 37 32 34 38 38 0e 04 87 2f 10 06 05 34 30 14 04 0c 01 0a 0b 01 04 06 0f 38 38 33 32 35 30 36 08 06 13 31 31 39 31 32 36 30 36 32 0c 06 30 0e 04 86 02 10 06 07 34 33 36 14 04 08 01 0a 0b 01 04 04 82 b4 86 04 08 06 05 36 37 0c 06 07 35 31 33 0e 04 49 10 06 03 38 14 04 01 01 13 64 65 66 65 6e 64 65 72 73 09 07 01 0a 0b 01 04 06 07 34 32 34 08 06 0f 39 39 36 36 35 30 37 0c 06 3e 0e 06 09 32 30 32 38 10 06 07 33 30 30 14 06 03 31 01 0a 0b 01 04 06 07 34 32 37 08 06 0f 39 39 36 36 33 33 31 0c 06 48 0e 06 40 10 06 42 14 06 44 01 0a 0b 01 04 06 07 34 32 38 08 06 48 0c 06 48 0e 06 40 10 06 42 14 06 44 01 15 61 77 61 72 64 73 5f 6b 65 79 06 41 63 65 38 62 33 61 66 63 63 33 32 35 62 33 30 37 36 36 37 64 63 37 39 63 65 62 66 30 35 66 65 36 15 64 69 65 5f 73 74 61 74 75 73 04 00 15 69 73 5f 77 69 6e 6e 69 6e 67 03 11 70 72 6f 63 65 73 65 73 09 11 01 0a 0b 01 13 61 73 73 61 69 6c 61 6e 74 0a 0b 01 09 74 79 70 65 06 56 04 06 2e 01 3a 09 03 01 0a 0b 01 04 06 4a 0d 6f 6c 64 5f 68 70 06 48 11 69 73 5f 64 6f 64 67 65 04 01 08 06 48 0d 61 74 74 61 63 6b 04 00 0f 69 73 5f 66 65 61 72 04 00 1b 6e 6f 72 6d 61 6c 5f 61 74 74 61 63 6b 04 00 13 62 6f 75 74 43 6f 75 6e 74 04 01 01 0d 73 6b 69 6c 6c 73 09 07 01 0a 0b 01 09 75 73 65 72 06 56 04 06 07 39 37 37 09 6e 61 6d 65 06 0d e5 bc ba e8 a2 ad 0d 62 61 74 74 65 72 04 00 10 06 05 31 37 1b 6f 72 67 61 6e 69 73 6d 5f 61 74 74 72 06 03 30 15 61 74 74 61 63 6b 5f 6e 75 6d 06 44 01 0a 0b 01 68 06 56 04 06 09 31 31 32 37 6c 06 0d e5 bc ba e6 94 bb 70 04 00 10 06 03 37 74 06 76 78 06 44 01 0a 0b 01 68 06 56 04 06 09 31 31 34 33 6c 06 0d e7 b2 be e7 a1 ae 70 04 00 10 06 03 33 74 06 76 78 06 44 01 1f 65 78 63 6c 75 73 69 76 65 53 6b 69 6c 6c 73 09 01 01 15 73 70 65 63 5f 62 75 66 66 73 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 56 04 06 06 01 3a 09 03 01 0a 0b 01 04 06 3c 5a 06 3e 5c 04 00 08 06 76 5e 06 3e 60 04 00 62 06 3e 64 04 01 01 66 09 03 01 0a 0b 01 68 06 56 04 06 07 39 37 36 6c 06 6e 70 04 00 10 06 05 31 36 74 06 76 78 06 44 01 81 06 09 01 01 81 08 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 56 04 06 16 01 3a 09 03 01 0a 0b 01 04 06 4a 5a 06 48 5c 04 00 08 06 76 5e 06 48 60 04 00 62 06 48 64 04 01 01 66 09 09 01 0a 0b 01 68 06 56 04 06 07 39 39 33 6c 06 6e 70 04 00 10 06 05 33 33 74 06 76 78 06 44 01 0a 0b 01 68 06 56 04 06 09 31 31 33 34 6c 06 7c 70 04 00 10 06 05 31 34 74 06 76 78 06 44 01 0a 0b 01 68 06 56 04 06 09 31 31 35 32 6c 06 81 02 70 04 00 10 06 05 31 32 74 06 76 78 06 44 01 0a 0b 01 68 06 56 04 06 07 37 31 31 6c 06 0d e7 83 88 e7 81 ab 70 04 00 10 06 05 33 31 74 06 81 18 78 06 44 01 81 06 09 01 01 81 08 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 56 04 06 26 01 3a 09 03 01 0a 0b 01 04 06 46 5a 06 48 5c 04 00 08 06 0f 39 39 35 36 39 31 39 5e 06 09 39 34 31 32 60 04 00 62 06 81 22 64 04 01 01 66 09 01 01 81 06 09 01 01 81 08 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 56 04 06 1e 01 3a 09 03 01 0a 0b 01 04 06 46 5a 06 81 20 5c 04 00 08 06 0f 39 39 35 31 31 30 38 5e 06 09 35 38 31 31 60 04 00 62 06 81 26 64 04 01 01 66 09 01 01 81 06 09 01 01 81 08 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 56 04 04 82 b4 86 04 01 3a 09 03 01 0a 0b 01 04 06 46 5a 06 81 24 5c 04 00 08 06 0f 39 39 35 31 30 36 38 5e 06 2c 60 04 ff ff ff ff 62 06 2c 64 04 01 01 66 09 01 01 81 06 09 01 01 81 08 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 11 64 65 66 65 6e 64 65 72 04 06 46 01 3a 09 03 01 0a 0b 01 04 04 82 b4 86 04 5a 06 34 5c 04 00 08 06 76 5e 06 34 60 04 01 62 06 34 64 04 01 01 66 09 01 01 81 06 09 01 01 81 08 09 01 01 01 0a 0b 01 56 0a 0b 01 58 06 56 04 06 2e 01 3a 09 03 01 0a 0b 01 04 06 46 5a 06 81 28 5c 04 00 08 06 76 5e 06 81 28 60 04 01 62 06 81 28 64 04 02 01 66 09 07 01 0a 0b 01 68 06 56 04 06 6a 6c 06 6e 70 04 00 10 06 72 74 06 76 78 06 44 01 0a 0b 01 68 06 56 04 06 7a 6c 06 7c 70 04 00 10 06 7e 74 06 76 78 06 44 01 0a 0b 01 68 06 56 04 06 81 00 6c 06 81 02 70 04 00 10 06 81 04 74 06 76 78 06 44 01 81 06 09 01 01 81 08 09 01 01 01 09 72 61 6e 6b 04 00 05 73 63 01 07 75 70 69 01 01")));
      MyAMF0Deserializer des = new MyAMF0Deserializer(dis);
      System.out.println(des.getAMFMessage().toString());
      FightObject fo = new FightObject(des.getAMFMessage().getBody(0).getValue());
      System.out.println(fo);
   }

   private static byte[] toByteArray(String hex) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      String[] toks = hex.split(" ");

      for(int i = 0; i < toks.length; ++i) {
         baos.write(Integer.parseInt(toks[i], 16));
      }

      return baos.toByteArray();
   }
}
