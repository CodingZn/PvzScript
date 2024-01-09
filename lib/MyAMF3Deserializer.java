package lib;

import com.exadel.flamingo.flex.amf.AMF3Constants;
import com.exadel.flamingo.flex.messaging.amf.io.util.ActionScriptClassDescriptor;
import com.exadel.flamingo.flex.messaging.amf.io.util.DefaultActionScriptClassDescriptor;
import com.exadel.flamingo.flex.messaging.amf.io.util.externalizer.Externalizer;
import com.exadel.flamingo.flex.messaging.amf.io.util.instanciator.AbstractInstanciator;
import com.exadel.flamingo.flex.messaging.util.StringUtil;
import com.exadel.flamingo.flex.messaging.util.XMLUtil;
import java.io.DataInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class MyAMF3Deserializer extends DataInputStream implements ObjectInput, AMF3Constants {
   protected static final Log log = LogFactory.getLog(MyAMF3Deserializer.class);
   protected static final Log logMore = LogFactory.getLog(MyAMF3Deserializer.class.getName() + ".MORE");
   protected final boolean debug;
   protected final boolean debugMore;
   protected final List<String> storedStrings = new ArrayList();
   protected final List<Object> storedObjects = new ArrayList();
   protected final List<ActionScriptClassDescriptor> storedClassDescriptors = new ArrayList();
   protected final XMLUtil xmlUtil = new XMLUtil();

   public MyAMF3Deserializer(InputStream in) {
      super(in);
      this.debug = log.isDebugEnabled();
    //   this.debug = true;
      this.debugMore = logMore.isDebugEnabled();
    //   this.debugMore = true;
      if (this.debugMore) {
         this.debug("new MyAMF3Deserializer(in=", in, ")");
      }

   }

   public Object readObject() throws IOException {
      if (this.debugMore) {
         this.debug("readObject()...");
      }

      int type = this.readAMF3Integer();
      return this.readObject(type);
   }

   protected Object readObject(int type) throws IOException {
      if (this.debugMore) {
         this.debug("readObject(type=", type, ")");
      }

      switch (type) {
         case 0:
         case 1:
            return null;
         case 2:
            return Boolean.FALSE;
         case 3:
            return Boolean.TRUE;
         case 4:
            return this.readAMF3Integer();
         case 5:
            return this.readAMF3Double();
         case 6:
            return this.readAMF3String();
         case 7:
            return this.readAMF3Xml();
         case 8:
            return this.readAMF3Date();
         case 9:
            return this.readAMF3Array();
         case 10:
            return this.readAMF3Object();
         case 11:
            return this.readAMF3XmlString();
         case 12:
            return this.readAMF3ByteArray();
         default:
            throw new IllegalArgumentException("Unknown type: " + type);
      }
   }

   protected int readAMF3Integer() throws IOException {
      int result = 0;
      int n = 0;

      int b;
      for(b = this.readUnsignedByte(); (b & 128) != 0 && n < 3; ++n) {
         result <<= 7;
         result |= b & 127;
         b = this.readUnsignedByte();
      }

      if (n < 3) {
         result <<= 7;
         result |= b;
      } else {
         result <<= 8;
         result |= b;
         if ((result & 268435456) != 0) {
            result |= -536870912;
         }
      }

      if (this.debugMore) {
         this.debug("readAMF3Integer() -> ", result);
      }

      return result;
   }

   protected Double readAMF3Double() throws IOException {
      double d = this.readDouble();
      Double result = Double.isNaN(d) ? null : d;
      if (this.debugMore) {
         this.debug("readAMF3Double() -> ", result);
      }

      return result;
   }

   protected String readAMF3String() throws IOException {
      String result = null;
      if (this.debugMore) {
         this.debug("readAMF3String()...");
      }

      int type = this.readAMF3Integer();
      if ((type & 1) == 0) {
         result = this.getFromStoredStrings(type >> 1);
      } else {
         int length = type >> 1;
         if (this.debugMore) {
            this.debug("readAMF3String() - length=", String.valueOf(length));
         }

         if (length <= 0) {
            result = "";
         } else {
            byte[] utfBytes = new byte[length];
            char[] utfChars = new char[length];
            this.readFully(utfBytes);
            int iBytes = 0;
            int iChars = 0;

            while(true) {
               while(iBytes < length) {
                  int c = utfBytes[iBytes++] & 255;
                  if (c > 127) {
                     byte c2;
                     switch (c >> 4) {
                        case 12:
                        case 13:
                           c2 = utfBytes[iBytes++];
                           if ((c2 & 192) != 128) {
                              throw new UTFDataFormatException("Malformed input around byte " + (iBytes - 2));
                           }

                           utfChars[iChars++] = (char)((c & 31) << 6 | c2 & 63);
                           break;
                        case 14:
                           c2 = utfBytes[iBytes++];
                           int c3 = utfBytes[iBytes++];
                           if ((c2 & 192) == 128 && (c3 & 192) == 128) {
                              utfChars[iChars++] = (char)((c & 15) << 12 | (c2 & 63) << 6 | (c3 & 63) << 0);
                              break;
                           }

                           throw new UTFDataFormatException("Malformed input around byte " + (iBytes - 3));
                        default:
                           throw new UTFDataFormatException("Malformed input around byte " + (iBytes - 1));
                     }
                  } else {
                     utfChars[iChars++] = (char)c;
                  }
               }

               result = new String(utfChars, 0, iChars);
               if (this.debugMore) {
                  this.debug("readAMF3String() - result=", StringUtil.toString(result));
               }

               this.addToStoredStrings(result);
               break;
            }
         }
      }

      if (this.debugMore) {
         this.debug("readAMF3String() -> ", StringUtil.toString(result));
      }

      return result;
   }

   protected Date readAMF3Date() throws IOException {
      Date result = null;
      int type = this.readAMF3Integer();
      if ((type & 1) == 0) {
         result = (Date)this.getFromStoredObjects(type >> 1);
      } else {
         result = new Date((long)this.readDouble());
         this.addToStoredObjects(result);
      }

      if (this.debugMore) {
         this.debug("readAMF3Date() -> ", result);
      }

      return result;
   }

   protected Object readAMF3Array() throws IOException {
      Object result = null;
      int type = this.readAMF3Integer();
      if ((type & 1) == 0) {
         result = this.getFromStoredObjects(type >> 1);
      } else {
         int size = type >> 1;
         String key = this.readAMF3String();
         int i;
         if (key.length() == 0) {
            Object[] objects = new Object[size];
            this.addToStoredObjects(objects);

            for(i = 0; i < size; ++i) {
               objects[i] = this.readObject();
            }

            result = objects;
         } else {
            Map<Object, Object> map = new HashMap();
            this.addToStoredObjects(map);

            while(key.length() > 0) {
               map.put(key, this.readObject());
               key = this.readAMF3String();
            }

            for(i = 0; i < size; ++i) {
               map.put(i, this.readObject());
            }

            result = map;
         }
      }

      if (this.debugMore) {
         this.debug("readAMF3Array() -> ", result);
      }

      return result;
   }

   protected Object readAMF3Object() throws IOException {
      if (this.debug) {
         this.debug("readAMF3Object()...");
      }

      Object result = null;
      int type = this.readAMF3Integer();
      if (this.debug) {
         this.debug("readAMF3Object() - type=", type); // 11
      }

      if ((type & 1) == 0) {
         result = this.getFromStoredObjects(type >> 1);
      } else {
         boolean inlineClassDef = (type >> 1 & 1) != 0;
         if (this.debug) {
            this.debug("readAMF3Object() - inlineClassDef=", String.valueOf(inlineClassDef));
         }

         ActionScriptClassDescriptor desc = null;
         int i;
         if (inlineClassDef) {
            int propertiesCount = type >> 4;
            if (this.debug) {
               this.debug("readAMF3Object() - propertiesCount=", String.valueOf(propertiesCount));
            }

            byte encoding = (byte)(type >> 2 & 3);
            if (this.debug) {
               this.debug("readAMF3Object() - encoding=", encoding);
            }

            String className = this.readAMF3String();
            if (this.debug) {
               this.debug("readAMF3Object() - className=", StringUtil.toString(className));
            }

            desc = new DefaultActionScriptClassDescriptor(className, encoding);
            this.addToStoredClassDescriptors((ActionScriptClassDescriptor)desc);
            if (this.debug) {
               this.debug("readAMF3Object() - defining ", String.valueOf(propertiesCount), " properties...");
            }

            for(i = 0; i < propertiesCount; ++i) {
               String name = this.readAMF3String();
               if (this.debug) {
                  this.debug("readAMF3Object() - defining property name=", name);
               }

               ((ActionScriptClassDescriptor)desc).defineProperty(name);
            }
         } else {
            desc = this.getFromStoredClassDescriptors(type >> 2);
         }

         if (this.debug) {
            this.debug("readAMF3Object() - actionScriptClassDescriptor=", desc);
         }

         int objectEncoding = ((ActionScriptClassDescriptor)desc).getEncoding();
         Externalizer externalizer = ((ActionScriptClassDescriptor)desc).getExternalizer();
         if (externalizer != null) {
            try {
               result = externalizer.newInstance(((ActionScriptClassDescriptor)desc).getType(), this);
            } catch (Exception var16) {
               throw new RuntimeException("Could not instantiate type: " + ((ActionScriptClassDescriptor)desc).getType(), var16);
            }
         } else {
            
            if (((ActionScriptClassDescriptor)desc).getType().equals("data.web.pvz_s1.app.services.client.fightresult")){
                result = new HashMap<>();
            }
            else{
                result = ((ActionScriptClassDescriptor)desc).newJavaInstance();
            }
            
         }

         int index = this.addToStoredObjects(result);
         if ((objectEncoding & 1) != 0) {
            if (externalizer != null) {
               if (this.debug) {
                  this.debug("readAMF3Object() - using externalizer=", externalizer);
               }

               try {
                  externalizer.readExternal(result, this);
               } catch (IOException var14) {
                  throw var14;
               } catch (Exception var15) {
                  throw new RuntimeException("Could not read externalized object: " + result, var15);
               }
            } else {
               if (this.debug) {
                  this.debug("readAMF3Object() - legacy Externalizable=", result.getClass());
               }

               try {
                  ((Externalizable)result).readExternal(this);
               } catch (IOException var12) {
                  throw var12;
               } catch (Exception var13) {
                  throw new RuntimeException("Could not read externalizable object: " + result, var13);
               }
            }
         } else {
            Object value;
            byte vType;
            if (((ActionScriptClassDescriptor)desc).getPropertiesCount() > 0) {
               if (this.debug) {
                  this.debug("readAMF3Object() - reading defined properties...");
               }

               for(i = 0; i < ((ActionScriptClassDescriptor)desc).getPropertiesCount(); ++i) {
                  vType = this.readByte();
                  value = this.readObject(vType);
                  if (this.debug) {
                     this.debug("readAMF3Object() - setting defined property: ", ((ActionScriptClassDescriptor)desc).getPropertyName(i), "=", StringUtil.toString(value));
                  }

                  ((ActionScriptClassDescriptor)desc).setPropertyValue(i, result, value);
               }
            }

            if (objectEncoding == 2) {
               if (this.debug) {
                  this.debug("readAMF3Object() - reading dynamic properties...");
               }

               while(true) {
                  String name = this.readAMF3String();
                  if (name.length() == 0) {
                     break;
                  }

                  vType = this.readByte();
                  value = this.readObject(vType);
                  if (this.debug) {
                     this.debug("readAMF3Object() - setting dynamic property: ", name, "=", StringUtil.toString(value));
                  }

                  ((ActionScriptClassDescriptor)desc).setPropertyValue(name, result, value);
               }
            }
         }

         if (result instanceof AbstractInstanciator) {
            if (this.debug) {
               this.debug("readAMF3Object() - resolving instanciator...");
            }

            try {
               result = ((AbstractInstanciator)result).resolve();
            } catch (Exception var11) {
               throw new RuntimeException("Could not instantiate object: " + result, var11);
            }

            this.setStoredObject(index, result);
         }
      }

      if (this.debug) {
         this.debug("readAMF3Object() -> ", result);
      }

      return result;
   }

   protected Document readAMF3Xml() throws IOException {
      String xml = this.readAMF3XmlString();
      Document result = this.xmlUtil.buildDocument(xml);
      if (this.debugMore) {
         this.debug("readAMF3Xml() -> ", result);
      }

      return result;
   }

   protected String readAMF3XmlString() throws IOException {
      String result = null;
      int type = this.readAMF3Integer();
      if ((type & 1) == 0) {
         result = this.getFromStoredStrings(type >> 1);
      } else {
         byte[] bytes = this.readBytes(type >> 1);
         result = new String(bytes, "UTF-8");
         this.addToStoredStrings(result);
      }

      if (this.debugMore) {
         this.debug("readAMF3XmlString() -> ", StringUtil.toString(result));
      }

      return result;
   }

   protected byte[] readAMF3ByteArray() throws IOException {
      int type = this.readAMF3Integer();
      byte[] result;
      if ((type & 1) == 0) {
         result = (byte[])((byte[])this.getFromStoredObjects(type >> 1));
      } else {
         result = this.readBytes(type >> 1);
         this.addToStoredObjects(result);
      }

      if (this.debugMore) {
         this.debug("readAMF3ByteArray() -> ", result);
      }

      return result;
   }

   protected void addToStoredStrings(String s) {
      if (this.debug) {
         this.debug("addToStoredStrings(s=", StringUtil.toString(s), ") at index=", String.valueOf(this.storedStrings.size()));
      }

      this.storedStrings.add(s);
   }

   protected String getFromStoredStrings(int index) {
      if (this.debug) {
         this.debug("getFromStoredStrings(index=", String.valueOf(index), ")");
      }

      String s = (String)this.storedStrings.get(index);
      if (this.debug) {
         this.debug("getFromStoredStrings() -> ", StringUtil.toString(s));
      }

      return s;
   }

   protected int addToStoredObjects(Object o) {
      int index = this.storedObjects.size();
      if (this.debug) {
         this.debug("addToStoredObjects(o=", o, ") at index=", String.valueOf(index));
      }

      this.storedObjects.add(o);
      return index;
   }

   protected void setStoredObject(int index, Object o) {
      if (this.debug) {
         this.debug("setStoredObject(index=", String.valueOf(index), ", o=", o, ")");
      }

      this.storedObjects.set(index, o);
   }

   protected Object getFromStoredObjects(int index) {
      if (this.debug) {
         this.debug("getFromStoredObjects(index=", String.valueOf(index), ")");
      }

      Object o = this.storedObjects.get(index);
      if (this.debug) {
         this.debug("getFromStoredObjects() -> ", o);
      }

      return o;
   }

   protected void addToStoredClassDescriptors(ActionScriptClassDescriptor desc) {
      if (this.debug) {
         this.debug("addToStoredClassDescriptors(desc=", desc, ") at index=", String.valueOf(this.storedClassDescriptors.size()));
      }

      this.storedClassDescriptors.add(desc);
   }

   protected ActionScriptClassDescriptor getFromStoredClassDescriptors(int index) {
      if (this.debug) {
         this.debug("getFromStoredClassDescriptors(index=", String.valueOf(index), ")");
      }

      ActionScriptClassDescriptor desc = (ActionScriptClassDescriptor)this.storedClassDescriptors.get(index);
      if (this.debug) {
         this.debug("getFromStoredClassDescriptors() -> ", desc);
      }

      return desc;
   }

   protected byte[] readBytes(int count) throws IOException {
      byte[] bytes = new byte[count];
      this.readFully(bytes);
      return bytes;
   }

   protected void debug(Object... msgs) {
      this.debug((Throwable)null, msgs);
   }

   protected void debug(Throwable t, Object... msgs) {
      String message = "";
      if (msgs != null && msgs.length > 0) {
         if (msgs.length == 1) {
            message = String.valueOf(msgs[0]);
         } else {
            StringBuilder sb = new StringBuilder();
            Object[] arr$ = msgs;
            int len$ = msgs.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Object o = arr$[i$];
               if (o instanceof String) {
                  sb.append(o);
               } else {
                  sb.append(StringUtil.toString(o));
               }
            }

            message = sb.toString();
         }
      }

      if (t != null) {
         System.out.println(t.getMessage());
         System.out.println(message);
      } else {
        System.out.println(message);
      }
    //   if (t != null) {
    //      log.debug(message, t);
    //   } else {
    //      log.debug(message);
    //   }

   }
}

