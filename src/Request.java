package src;

import static src.Util.delay;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

public class Request {
    private static int timeout = 40000;
    private static final String cookie;
    private static String host;
    private static final String realhost = "pvz-s1.youkia.com";
    private static final String http = "http://";
    private static final String amfPath = "/pvz/amf/";
    // private static final String testhost = "pvzol.org";

    private static final int leastInterval = 1000;
    private static Long lastSentTime = 0L;

    private static final int wait2441Time = 15000;
    private static final int waitAmfTime = 3000;
    
    static {
        host = realhost;
        String readcookie = "";
        try (FileInputStream reader = new FileInputStream("data/cookie")) {
            readcookie = new String(reader.readAllBytes());
        } catch (Exception e) {
            System.out.println("读取data/cookie文件出错！");
            System.exit(1);
        }
        cookie = readcookie;

    }

    private static void addHeaders(HttpRequest.Builder builder){
        builder.version(Version.HTTP_1_1)
        .header("Accept", "*/*")
        .header("Accept-Language", "zh-CN")
        .header("Referer","http://pvz-s1.youkia.com/youkia/main.swf?1192980633")
        .header("x-flash-version","34,0,0,305")
        .header("Accept-Encoding","gzip, deflate")
        .header("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/7.0; .NET4.0C; .NET4.0E;")
        // .header("Host","pvz-s1.youkia.com")
        .header("Cookie", cookie)
        // .header("Connection","keep-alive")
        ;
    }

    private static synchronized void sendIntervalBlock(){
        long interval = System.currentTimeMillis() - lastSentTime;
        if (interval < leastInterval){
            delay(leastInterval-interval);
        }
        lastSentTime = System.currentTimeMillis();
    }

    /** @return valid body of response, null if exceptio */
    public static byte[] sendGetRequest(String path, boolean handleAmfBlock){
        byte[] response;
        do {
            response = sendOneGet(path);
            if (response==null) return null;
            if (!is2441Block(response)){
                if(!handleAmfBlock || !isAmfBlock(response)){
                    break;
                }
                else{
                    System.out.print("拦");
                    delay(waitAmfTime);
                    System.out.print("\b\b");
                }
            }
            else{
                System.out.print("拦");
                delay(wait2441Time);
                System.out.print("\b\b");
                break;
            }
        } while (true);
        return response;
    }
   
    /** @return body of response, null if exception */
    private static byte[] sendOneGet(String path){
        String uri = http + host + path;
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.GET().uri(URI.create(uri)).timeout(Duration.ofMillis(timeout));
        addHeaders(builder);
        HttpRequest request = builder.build();
        try {
            sendIntervalBlock();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }

    /** @return amf body of response, null if exception */
    private static byte[] sendOnePostAmf(byte[] body){
        String uri = http + host + amfPath;
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.POST(BodyPublishers.ofByteArray(body))
        .uri(URI.create(uri))
        .timeout(Duration.ofMillis(timeout));
        addHeaders(builder);
        HttpRequest request = builder.build();
        try {
            sendIntervalBlock();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** @return valid amf body of response, 2441 block handled */
    public static byte[] sendPostAmf(byte[] body, boolean handleAmfBlock){
        byte[] response;
        do {
            response = sendOnePostAmf(body);
            if (response==null) return null;
            if (!is2441Block(response)){
                if(!handleAmfBlock || !isAmfBlock(response)){
                    break;
                }
                else{
                    System.out.print("拦");
                    delay(waitAmfTime);
                    System.out.print("\b\b");
                }
            }
            else{
                System.out.print("拦");
                delay(wait2441Time);
                System.out.print("\b\b");
                break;
            }
        } while (true);
        return response;
    }

    /** to check if there is a rechapter block */
    private static boolean is2441Block(byte[] response){
        assert(response != null);
        if (response.length == 2441){
            return true;
        }
        return false;
    }

    /** to check if amf block is traggered */
    private static boolean isAmfBlock(byte[] response){
        AMF0Message msg = Util.decodeAMF(response);
        if (msg == null) return false;
        AMF0Body body = msg.getBody(0);
        if(Response.isOnStatusException(body, false)
        && Response.getExceptionDescription(body).equals("Exception:请不要操作过于频繁。"))
        {
            return true;
        }
        return false;
    }
    
    public static void main(String[] args) {
    }
}
