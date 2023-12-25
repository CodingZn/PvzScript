    // Source code is decompiled from a .class file using FernFlower decompiler.
    package lib;

    import com.exadel.flamingo.flex.amf.AMF0Body;
    import com.exadel.flamingo.flex.amf.AMF0Header;
    import com.exadel.flamingo.flex.amf.AMF0Message;
    import com.exadel.flamingo.flex.amf.AMF3Object;
    import com.exadel.flamingo.flex.messaging.amf.io.AMF3Serializer;

    import flex.messaging.io.ASObject;
    import flex.messaging.io.ASRecordSet;
    import java.beans.PropertyDescriptor;
    import java.io.ByteArrayOutputStream;
    import java.io.DataOutputStream;
    import java.io.IOException;
    import java.io.ObjectOutput;
    import java.lang.reflect.Method;
    import java.sql.ResultSet;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.Date;
    import java.util.IdentityHashMap;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Map;
    import java.util.TimeZone;
    import org.apache.commons.beanutils.PropertyUtils;
    import org.apache.commons.logging.Log;
    import org.apache.commons.logging.LogFactory;
    import org.w3c.dom.Document;
    import org.w3c.dom.Element;
    import org.w3c.dom.NamedNodeMap;
    import org.w3c.dom.Node;
    import org.w3c.dom.NodeList;

    public class MyAMF0Serializer {
    private static final Log log = LogFactory.getLog(MyAMF0Serializer.class);
    private static final int MILLS_PER_HOUR = 60000;
    private static final String NULL_MESSAGE = "null";
    protected DataOutputStream outputStream;
    private Map<Object, Integer> storedObjects = new IdentityHashMap();
    private int storedObjectCount = 0;

    public MyAMF0Serializer(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void serializeMessage(AMF0Message message) throws IOException {
        this.clearStoredObjects();
        this.outputStream.writeShort(message.getVersion());
        this.outputStream.writeShort(message.getHeaderCount());
        Iterator<AMF0Header> headers = message.getHeaders().iterator();

        while(headers.hasNext()) {
            AMF0Header header = (AMF0Header)headers.next();
            this.writeHeader(header);
        }

        this.outputStream.writeShort(message.getBodyCount());
        Iterator<AMF0Body> bodies = message.getBodies();

        while(bodies.hasNext()) {
            AMF0Body body = (AMF0Body)bodies.next();
            this.writeBody(body);
        }

    }

    protected void writeHeader(AMF0Header header) throws IOException {
        this.outputStream.writeUTF(header.getKey());
        this.outputStream.writeBoolean(header.isRequired());
        this.outputStream.writeInt(-1);
        this.writeData(header.getValue());
    }
    protected void writeBody(AMF0Body body) throws IOException {
        if (body.getTarget() == null) {
            this.outputStream.writeUTF("null");
        } else {
            this.outputStream.writeUTF(body.getTarget());
        }
    
        if (body.getResponse() == null) {
            this.outputStream.writeUTF("null");
        } else {
            this.outputStream.writeUTF(body.getResponse());
        }
        // switch to tmpstream
        ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = this.outputStream;
        this.outputStream = new DataOutputStream(tmpStream);
        // measure byte len
        this.writeData(body.getValue());
        int bytelen = tmpStream.size();
        tmpStream.close();
        // switch back
        this.outputStream = outputStream;
        if (this.storedObjects.containsKey(body.getValue())) {
            this.storedObjectCount--;
            this.storedObjects.remove(body.getValue());
        }
        // write as normal
        this.outputStream.writeInt(bytelen);
        this.writeData(body.getValue());
    }
    
    protected void writeData(Object value) throws IOException {
        if (value == null) {
            this.outputStream.writeByte(5);
        } else if (value instanceof AMF3Object) {
            this.writeAMF3Data((AMF3Object)value);
        } else if (this.isPrimitiveArray(value)) {
            this.writePrimitiveArray(value);
        } else if (value instanceof Number) {
            this.outputStream.writeByte(0);
            this.outputStream.writeDouble(((Number)value).doubleValue());
        } else if (value instanceof String) {
            this.writeString((String)value);
        } else if (value instanceof Character) {
            this.outputStream.writeByte(2);
            this.outputStream.writeUTF(value.toString());
        } else if (value instanceof Boolean) {
            this.outputStream.writeByte(1);
            this.outputStream.writeBoolean((Boolean)value);
        } else if (value instanceof Date) {
            this.outputStream.writeByte(11);
            this.outputStream.writeDouble((double)((Date)value).getTime());
            int offset = TimeZone.getDefault().getRawOffset();
            this.outputStream.writeShort(offset / '\uea60');
        } else {
            if (this.storedObjects.containsKey(value)) {
                this.writeStoredObject(value);
                return;
            }

            this.storeObject(value);
            if (value instanceof Object[]) {
                this.writeArray((Object[])((Object[])value));
            } else if (value instanceof Iterator) {
                this.write((Iterator)value);
            } else if (value instanceof Collection) {
                this.write((Collection)value);
            } else if (value instanceof Map) {
                this.writeMap((Map)value);
            } else if (value instanceof ResultSet) {
                ASRecordSet asRecordSet = new ASRecordSet();
                asRecordSet.populate((ResultSet)value);
                this.writeData(asRecordSet);
            } else if (value instanceof Document) {
                this.write((Document)value);
            } else {
                this.writeObject(value);
            }
        }

    }

    protected void writeObject(Object object) throws IOException {
        if (log.isDebugEnabled()) {
            if (object == null) {
                log.debug("Writing object, object param == null");
            } else {
                log.debug("Writing object, class = " + object.getClass());
            }
        }

        this.outputStream.writeByte(3);

        try {
            PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(object);

            for(int i = 0; i < properties.length; ++i) {
                if (!properties[i].getName().equals("class")) {
                String propertyName = properties[i].getName();
                Method readMethod = properties[i].getReadMethod();
                Object propertyValue = null;
                if (readMethod == null) {
                    log.error("unable to find readMethod for : " + propertyName + " writing null!");
                } else {
                    log.debug("invoking readMethod " + readMethod);
                    propertyValue = readMethod.invoke(object);
                }

                log.debug(propertyName + " = " + propertyValue);
                this.outputStream.writeUTF(propertyName);
                this.writeData(propertyValue);
                }
            }

            this.outputStream.writeShort(0);
            this.outputStream.writeByte(9);
        } catch (RuntimeException var7) {
            throw var7;
        } catch (Exception var8) {
            log.error(var8, var8);
            throw new IOException(var8.getMessage());
        }
    }

    protected void writeArray(Object[] array) throws IOException {
        this.outputStream.writeByte(10);
        this.outputStream.writeInt(array.length);

        for(int i = 0; i < array.length; ++i) {
            this.writeData(array[i]);
        }

    }

    protected void writePrimitiveArray(Object array) throws IOException {
        this.writeArray(this.convertPrimitiveArrayToObjectArray(array));
    }

    protected Object[] convertPrimitiveArrayToObjectArray(Object array) {
        Class<?> componentType = array.getClass().getComponentType();
        Object[] result = null;
        if (componentType == null) {
            throw new NullPointerException("componentType is null");
        } else {
            int i;
            if (componentType == Character.TYPE) {
                char[] carray = (char[])((char[])array);
                result = new Object[carray.length];

                for(i = 0; i < carray.length; ++i) {
                result[i] = new Character(carray[i]);
                }
            } else if (componentType == Byte.TYPE) {
                byte[] barray = (byte[])((byte[])array);
                result = new Object[barray.length];

                for(i = 0; i < barray.length; ++i) {
                result[i] = new Byte(barray[i]);
                }
            } else if (componentType == Short.TYPE) {
                short[] sarray = (short[])((short[])array);
                result = new Object[sarray.length];

                for(i = 0; i < sarray.length; ++i) {
                result[i] = new Short(sarray[i]);
                }
            } else if (componentType == Integer.TYPE) {
                int[] iarray = (int[])((int[])array);
                result = new Object[iarray.length];

                for(i = 0; i < iarray.length; ++i) {
                result[i] = iarray[i];
                }
            } else if (componentType == Long.TYPE) {
                long[] larray = (long[])((long[])array);
                result = new Object[larray.length];

                for(i = 0; i < larray.length; ++i) {
                result[i] = new Long(larray[i]);
                }
            } else if (componentType == Double.TYPE) {
                double[] darray = (double[])((double[])array);
                result = new Object[darray.length];

                for(i = 0; i < darray.length; ++i) {
                result[i] = new Double(darray[i]);
                }
            } else if (componentType == Float.TYPE) {
                float[] farray = (float[])((float[])array);
                result = new Object[farray.length];

                for(i = 0; i < farray.length; ++i) {
                result[i] = new Float(farray[i]);
                }
            } else {
                if (componentType != Boolean.TYPE) {
                throw new IllegalArgumentException("unexpected component type: " + componentType.getClass().getName());
                }

                boolean[] barray = (boolean[])((boolean[])array);
                result = new Object[barray.length];

                for(i = 0; i < barray.length; ++i) {
                result[i] = new Boolean(barray[i]);
                }
            }

            return result;
        }
    }

    protected void write(Iterator<?> iterator) throws IOException {
        List<Object> list = new ArrayList();

        while(iterator.hasNext()) {
            list.add(iterator.next());
        }

        this.write((Collection)list);
    }

    protected void write(Collection<?> collection) throws IOException {
        this.outputStream.writeByte(10);
        this.outputStream.writeInt(collection.size());
        Iterator<?> objects = collection.iterator();

        while(objects.hasNext()) {
            Object object = objects.next();
            this.writeData(object);
        }

    }

    protected void writeMap(Map<?, ?> map) throws IOException {
        if (map instanceof ASObject && ((ASObject)map).getType() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Writing Custom Class: " + ((ASObject)map).getType());
            }

            this.outputStream.writeByte(16);
            this.outputStream.writeUTF(((ASObject)map).getType());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Writing Map");
            }

            this.outputStream.writeByte(8);
            this.outputStream.writeInt(0);
        }

        Iterator<?> entrys = map.entrySet().iterator();

        while(entrys.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry)entrys.next();
            log.debug(entry.getKey() + ": " + entry.getValue());
            this.outputStream.writeUTF(entry.getKey().toString());
            this.writeData(entry.getValue());
        }

        this.outputStream.writeShort(0);
        this.outputStream.writeByte(9);
    }

    protected void write(Document document) throws IOException {
        this.outputStream.writeByte(15);
        Element docElement = document.getDocumentElement();
        String xmlData = convertDOMToString(docElement);
        if (log.isDebugEnabled()) {
            log.debug("Writing xmlData: \n" + xmlData);
        }

        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        baOutputStream.write(xmlData.getBytes("UTF-8"));
        this.outputStream.writeInt(baOutputStream.size());
        baOutputStream.writeTo(this.outputStream);
    }

    protected int writeString(String str) throws IOException {
        int strlen = str.length();
        int utflen = 0;
        char[] charr = new char[strlen];
        int count = 0;
        str.getChars(0, strlen, charr, 0);

        char c;
        for(int i = 0; i < strlen; ++i) {
            c = charr[i];
            if (c >= 1 && c <= 127) {
                ++utflen;
            } else if (c > 2047) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        byte[] bytearr;
        if (utflen <= 65535) {
            this.outputStream.writeByte(2);
            bytearr = new byte[utflen + 2];
        } else {
            this.outputStream.writeByte(12);
            bytearr = new byte[utflen + 4];
            bytearr[count++] = (byte)(utflen >>> 24 & 255);
            bytearr[count++] = (byte)(utflen >>> 16 & 255);
        }

        bytearr[count++] = (byte)(utflen >>> 8 & 255);
        bytearr[count++] = (byte)(utflen >>> 0 & 255);

        for(int i = 0; i < strlen; ++i) {
            c = charr[i];
            if (c >= 1 && c <= 127) {
                bytearr[count++] = (byte)c;
            } else if (c > 2047) {
                bytearr[count++] = (byte)(224 | c >> 12 & 15);
                bytearr[count++] = (byte)(128 | c >> 6 & 63);
                bytearr[count++] = (byte)(128 | c >> 0 & 63);
            } else {
                bytearr[count++] = (byte)(192 | c >> 6 & 31);
                bytearr[count++] = (byte)(128 | c >> 0 & 63);
            }
        }

        this.outputStream.write(bytearr);
        return utflen + 2;
    }

    private void writeStoredObject(Object obj) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Writing object reference for " + obj);
        }

        this.outputStream.write(7);
        this.outputStream.writeShort((Integer)this.storedObjects.get(obj));
    }

    private void storeObject(Object obj) {
        this.storedObjects.put(obj, this.storedObjectCount++);
    }

    private void clearStoredObjects() {
        this.storedObjects = new IdentityHashMap();
        this.storedObjectCount = 0;
    }

    protected boolean isPrimitiveArray(Object obj) {
        if (obj == null) {
            return false;
        } else {
            return obj.getClass().isArray() && obj.getClass().getComponentType().isPrimitive();
        }
    }

    private void writeAMF3Data(AMF3Object data) throws IOException {
        this.outputStream.writeByte(17);
        ObjectOutput amf3 = new AMF3Serializer(this.outputStream);
        amf3.writeObject(data.getValue());
    }

    public static String convertDOMToString(Node node) {
        StringBuffer sb = new StringBuffer();
        if (node.getNodeType() == 3) {
            sb.append(node.getNodeValue());
        } else {
            String currentTag = node.getNodeName();
            sb.append('<');
            sb.append(currentTag);
            appendAttributes(node, sb);
            sb.append('>');
            if (node.getNodeValue() != null) {
                sb.append(node.getNodeValue());
            }

            appendChildren(node, sb);
            appendEndTag(sb, currentTag);
        }

        return sb.toString();
    }

    private static void appendAttributes(Node node, StringBuffer sb) {
        if (node instanceof Element) {
            NamedNodeMap nodeMap = node.getAttributes();

            for(int i = 0; i < nodeMap.getLength(); ++i) {
                sb.append(' ');
                sb.append(nodeMap.item(i).getNodeName());
                sb.append('=');
                sb.append('"');
                sb.append(nodeMap.item(i).getNodeValue());
                sb.append('"');
            }
        }

    }

    private static void appendChildren(Node node, StringBuffer sb) {
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();

            for(int i = 0; i < children.getLength(); ++i) {
                sb.append(convertDOMToString(children.item(i)));
            }
        }

    }

    private static void appendEndTag(StringBuffer sb, String currentTag) {
        sb.append('<');
        sb.append('/');
        sb.append(currentTag);
        sb.append('>');
    }
}
