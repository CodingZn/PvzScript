// Source code is decompiled from a .class file using FernFlower decompiler.
package lib;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

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
            log.debug("Skipping NULL value for :" + key);
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
      ObjectInput amf3 = new AMF3Deserializer(this.inputStream);

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
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(toByteArray("00 03 00 00 00 01 00 04 6E 75 6C 6C 00 02 2F 33 00 00 00 92 0A 00 00 00 01 11 0A 81 13 4F 66 6C 65 78 2E 6D 65 73 73 61 67 69 6E 67 2E 6D 65 73 73 61 67 65 73 2E 52 65 6D 6F 74 69 6E 67 4D 65 73 73 61 67 65 0D 73 6F 75 72 63 65 13 6F 70 65 72 61 74 69 6F 6E 09 62 6F 64 79 17 64 65 73 74 69 6E 61 74 69 6F 6E 11 63 6C 69 65 6E 74 49 64 15 74 69 6D 65 54 6F 4C 69 76 65 13 6D 65 73 73 61 67 65 49 64 0F 68 65 61 64 65 72 73 13 74 69 6D 65 73 74 61 6D 70 01 06 0D 75 70 64 61 74 65 09 03 01 0A 73 39 66 6C 65 78 2E 74 65 73 74 64 72 69 76 65 2E 73 74 6F 72 65 2E 50 72 6F 64 75 63 74 09 6E 61 6D 65 0B 70 72 69 63 65 0B 69 6D 61 67 65 11 63 61 74 65 67 6F 72 79 07 75 69 64 13 70 72 6F 64 75 63 74 49 64 17 64 65 73 63 72 69 70 74 69 6F 6E 06 0D 70 72 6F 64 20 33 05 40 12 3D 70 A3 D7 0A 3D 06 13 70 72 6F 64 33 2E 70 6E 67 06 17 70 72 6F 64 75 63 74 20 63 61 74 06 49 33 41 44 38 35 46 34 37 2D 31 35 33 36 2D 42 38 34 46 2D 36 36 34 42 2D 30 31 41 30 37 45 30 32 38 34 39 31 04 03 06 25 70 72 6F 64 20 64 65 73 63 72 69 70 74 69 6F 6E 20 33 06 0F 70 72 6F 64 75 63 74 01 04 00 06 49 37 32 31 32 39 46 38 37 2D 45 41 45 46 2D 35 46 37 35 2D 41 37 37 30 2D 30 31 41 30 41 35 45 43 42 37 34 32 0A 0B 01 15 44 53 45 6E 64 70 6F 69 6E 74 06 0D 6D 79 2D 61 6D 66 01 04 00")));
      MyAMF0Deserializer des = new MyAMF0Deserializer(dis);
      System.out.println(des.getAMFMessage().toString());
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
