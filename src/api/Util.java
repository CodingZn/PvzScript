package src.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import lib.MyAMF0Serializer;
import lib.MyAMF0Deserializer;

public class Util {

    public static final Scanner scanner;

    static{
        scanner=new Scanner(System.in);
    }

    public static void printBytes(byte[] bytes){
        printBytes(bytes, System.out);
    }
    public static void printBytes(byte[] bytes, PrintStream out){
        for (int i = 0; i < bytes.length; i++) {
            if (i%16==0){
                out.println();
            }
            out.printf("%02x ", bytes[i]);
        }
    }

    // public static boolean delay(int ms){
    //     return delay(ms);
    // }

    public static boolean delay(long ms){
        if (ms <= 0) {
            return true;
        }
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            // e.printStackTrace();
            return false;
        }
    }

    /** @return nullable */
    public static AMF0Message decodeAMF(String filename){
        try (FileInputStream fi = new FileInputStream(filename)) {
            return decodeAMF(fi.readAllBytes());
        } catch (FileNotFoundException e){
            Log.println("文件%s不存在！".formatted(filename));
            return null;
        }
         catch (IOException e) {
            Log.println("Error Decoding AMF file!");
            e.printStackTrace();
            return null;
        }
    }

    /** @return nullable */
    public static AMF0Message decodeAMF(byte[] bytes){
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream di = new DataInputStream(bArrayInputStream);
        try {
            MyAMF0Deserializer deserializer = new MyAMF0Deserializer(di);
            return deserializer.getAMFMessage();
        } catch (IOException e) {
            Log.println("Error Decoding AMF message!");
            Util.printBytes(bytes, System.out);
            e.printStackTrace();
            return null;
        }
        
    }

    /** @return nullable */
    public static AMF0Message tryDecodeAMF(byte[] bytes){
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream di = new DataInputStream(bArrayInputStream);
        try {
            MyAMF0Deserializer deserializer = new MyAMF0Deserializer(di);
            return deserializer.getAMFMessage();
        } catch (IOException e) {
            return null;
        }
        
    }

    public static byte[] encodeAMF(String target, String response, Object value){
        return encodeAMF(target, response, value, AMF0Body.DATA_TYPE_ARRAY);
    }

    /** @return nullable */
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

    public static int[] integerArr2int(Object[] arr){
        int[] res = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = (Integer)arr[i];
        }
        return res;
    }

    /** @return nullable */
    public static List<Integer> readIntegersFromFile(String filename){
        try (BufferedReader bStream = new BufferedReader(new FileReader(filename))) {
            List<Integer> res = new ArrayList<>();
            bStream.lines().forEach(l->{
                if (l.length()!=0 && l.charAt(0)!='#'){
                    String[] strs = l.split("[\t ]");
                    for (int i = 0; i < strs.length; i++) {
                        res.add(Integer.parseInt(strs[i]));
                    }
                }
            });
            return res;
        } catch (FileNotFoundException e) {
            Log.println("文件%s不存在！".formatted(filename));
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    
    public static boolean saveIntegersToFile(List<Integer> integers, String filename){
        try (BufferedWriter bStream = new BufferedWriter(new FileWriter(filename))) {
            for (Integer e : integers) {
                bStream.write(String.valueOf(e));
                bStream.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** @return nullable */
    public static Document parseXml(String filename){
        try {
            //创建DOM解析器的工厂实例
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //从DOM工厂中获取解析器
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fInputStream = new FileInputStream(filename);
            //使用解析器生成Document实例
            return documentBuilder.parse(fInputStream);
        } catch (FileNotFoundException e) {
            Log.println("文件%s不存在！".formatted(filename));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** @return nullable */
    public static Document parseXml(File file){
        try {
            //创建DOM解析器的工厂实例
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //从DOM工厂中获取解析器
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fInputStream = new FileInputStream(file);
            //使用解析器生成Document实例
            return documentBuilder.parse(fInputStream);
        } catch (FileNotFoundException e) {
            Log.println("文件%s不存在！".formatted(file.getName()));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** @return nullable */
    public static Document parseXml(byte[] byteArray){
        try {
            //创建DOM解析器的工厂实例
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //从DOM工厂中获取解析器
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream bInputStream = new ByteArrayInputStream(byteArray);
            //使用解析器生成Document实例
            return documentBuilder.parse(bInputStream);
        } catch (Exception e) {
            // Util.printBytes(byteArray, System.out);
            // System.out.println(byteArray);
            // e.printStackTrace();
            return null;
        }
    }

    /** if xml failure, return message; else return null */
    public static String getXmlMessage(Document document){
        Node statusNode = document.getElementsByTagName("status").item(0);
        if (statusNode.getNodeType()==Node.ELEMENT_NODE && 
        ((Element) statusNode).getTextContent().equals("failure")){
            Node errorNode = document.getElementsByTagName("error").item(0);
            String msg = ((Element) errorNode).getAttribute("message");
            return msg;
        }
        else{
            return null;
        }
    }

    public static byte[] toByteArray(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String[] toks = hex.split("([ \n\t]+)|(\r\n)+");
    
        for(int i = 0; i < toks.length; ++i) {
            baos.write(Integer.parseInt(toks[i], 16));
        }
 
        return baos.toByteArray();
    }

    /** @return nullable */
    public static byte[] readBytesFromFile(String filename) {
        try (FileInputStream fi = new FileInputStream(filename)) {
            String str = new String(fi.readAllBytes());
            return toByteArray(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public static boolean saveBytesToFile(byte[] response, String filename){
        Path filePath = Path.of(filename);
        Path dirPath = filePath.getParent();
        if (dirPath!=null){
            dirPath.toFile().mkdirs();
        }
        File oFile = filePath.toFile();
        try {
            oFile.createNewFile();
            FileOutputStream fStream = new FileOutputStream(oFile);
            fStream.write(response);
            fStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Integer obj2int(Object number){
        Integer res;
        if (number instanceof String){
            res = Integer.parseInt((String)number);
        }
        else if (number instanceof Float){
            res = ((Float)number).intValue();
        }
        else if (number instanceof Double){
            res = ((Double)number).intValue();
        }
        else if (number instanceof Short){
            res = ((Short) number).intValue();
        }
        else if (number instanceof Integer){
            res = (Integer) number;
        }
        else if (number instanceof Long){
            res = ((Long) number).intValue();
        }
        else{
            Log.println(number.getClass().toString());
            return null;
        }
        return res;
    }

    public static Long obj2long(Object number){
        Long res;
        if (number instanceof String){
            res = Long.parseLong((String)number);
        }
        else if (number instanceof Float){
            res = ((Float)number).longValue();
        }
        else if (number instanceof Double){
            res = ((Double)number).longValue();
        }
        else if (number instanceof Short){
            res = ((Short) number).longValue();
        }
        else if (number instanceof Integer){
            res = ((Integer) number).longValue();
        }
        else if (number instanceof Long){
            res = ((Long) number);
        }
        else{
            Log.println(number.getClass().toString());
            return null;
        }
        return res;
    }

    public static BigInteger obj2bigint(Object number){
        BigInteger res;
        if (number instanceof String){
            res = new BigInteger((String)number);
        }
        else if (number instanceof Float){
            Long tmp = ((Float)number).longValue();
            res = new BigInteger(tmp.toString());
        }
        else if (number instanceof Double){
            Long tmp = ((Double)number).longValue();
            res = new BigInteger(tmp.toString());
        }
        else if (number instanceof Short){
            Long tmp = ((Short)number).longValue();
            res = new BigInteger(tmp.toString());
        }
        else if (number instanceof Integer){
            Long tmp = ((Integer)number).longValue();
            res = new BigInteger(tmp.toString());
        }
        else if (number instanceof Long){
            Long tmp = ((Long)number).longValue();
            res = new BigInteger(tmp.toString());
        }
        else{
            Log.println(number.getClass().toString());
            return null;
        }
        return res;
    }

    /** format as yyyy-MM-dd HH:mm:ss.SSS */
    public static String dateFormat(Date date, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /** format as yyyy-MM-dd HH:mm:ss.SSS */
    public static String dateFormatNow(String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    /** yyyy-MM-dd HH:mm:ss.SSS */
    public static Date parseDate(String date){
        return parseDate("yyyy-MM-dd HH:mm:ss.SSS", date);
    }

    public static Date parseDate(String format, String date){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(date);
        } catch (Exception e) {
            return null;
        }

    }

    public static Object loadObject(String filename){
        return loadObject(filename, true);
    }

    public static Object loadObject(String filename, boolean notify){
        try {
            FileInputStream fInputStream = new FileInputStream(filename);
            ObjectInputStream oInputStream = new ObjectInputStream(fInputStream);
            Object readobj = oInputStream.readObject();
            oInputStream.close();
            return readobj;
        } catch (InvalidClassException e){
            if (notify) Log.println("类信息不一致。");
            return null;
        } catch (FileNotFoundException e) {
            if (notify) Log.println("文件%s不存在！".formatted(filename));
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveObject(Object obj, String filename){
        return saveObject(obj, filename, true);
    }

    public static boolean saveObject(Object obj, String filename, boolean notify){
        try {
            Path fileDir = Path.of(filename);
            if (fileDir.getParent()!=null) fileDir.getParent().toFile().mkdirs();
            FileOutputStream fOutputStream = new FileOutputStream(fileDir.toFile());
            ObjectOutputStream outputStream = new ObjectOutputStream(fOutputStream);
            outputStream.writeObject(obj);
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            if (notify) Log.println("文件%s不存在！".formatted(filename));
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getPid(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        return Integer.parseInt(pid);
    }

    public static String readText(String filename, boolean notify){
        try (FileInputStream reader = new FileInputStream(filename)) {
            return new String(reader.readAllBytes());
        } catch (FileNotFoundException e){
            if (notify) Log.logln("文件%s不存在！".formatted(filename));
            return null;
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    public static String readText(String filename, boolean notify, String charset){
        try (FileInputStream reader = new FileInputStream(filename)) {
            return new String(reader.readAllBytes(), charset);
        } catch (FileNotFoundException e){
            if (notify) Log.logln("文件%s不存在！".formatted(filename));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeText(String filename, String content, boolean notify){
        try (FileOutputStream printer = new FileOutputStream(filename,false)) {
            printer.write(content.getBytes());
        } catch (Exception e) {
            if (notify) Log.logln("写入到%s失败！".formatted(filename));
            return;
        }
    }
    
    public static String bigInt2String(BigInteger value){
        BigInteger yi_min = new BigInteger("100000000");
        BigInteger cifang_min = new BigInteger("1000000000000");
        if (value.compareTo(yi_min) <0) {
            return value.toString();
        }
        else if (value.compareTo(yi_min)>=0 && value.compareTo(cifang_min)<0) {
            double vl = value.doubleValue();
            return "%.2f亿".formatted(vl/100000000);
        }
        else {
            int len = value.toString().length();
            return "%s.%s×10^%d亿".formatted(value.toString().substring(0,1)
            ,value.toString().substring(1,3),len-9);
        }

    }
    /** 获取账户文件夹名，从api模块里获取信息，返回可能为null */ 
    public static final String getAccountDirName(){
        try {
            int server=Request.getServer();
            int id = User.getUser().id;
            return getAccountDirName(server, id);
        } catch (Exception e) {
            return null;
        }
    }
    /** 获取账户文件夹名，允许null输入，返回可能为null */
    public static final String getAccountDirName(Integer server, Integer id){
        try {
            return "%02d_%d".formatted(server,id);
        } catch (Exception e) {
            return null;
        }
    }
    /** 获取所有子文件夹名 */
    public static List<String> getSubDirs(String dir){
        List<String> dirs = new ArrayList<>();
        File folder = new File(dir); // 指定当前文件夹路径
        File[] files = folder.listFiles(); // 获取当前文件夹下的所有文件和文件夹
        for (File file : files) {
            if (file.isDirectory()) { // 判断是否为文件夹
                String fileName = file.getName(); 
                dirs.add(fileName);
            }
        }
        return dirs;
    }
    /** 在一大串字符串中截取两个字符串中间的字符串。 */
    public static String findStrBetween(String str, String bg, String ed){
        int begin = str.indexOf(bg);
        int end = str.indexOf(ed);
        if (begin==-1 || end==-1){
            return null;
        }
        return str.substring(begin+bg.length(), end);
    }

    /** 回车确认 */
    public static boolean confirm(){
        if (!scanner.nextLine().equals("")){
            return false;
        }
        return true;
    }

}
