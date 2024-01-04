package src;

import static src.Util.delay;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

public class Request {
    private static int timeout = 40000;
    private static String host;
    private static final String realhost = "pvz-s1.youkia.com";
    private static final String http = "http://";
    private static final String amfPath = "/pvz/amf/";
    // private static final String testhost = "pvzol.org";

    private static final int leastInterval = 100;
    private static int reqInterval = 1000;
    private static Long lastSentTime = 0L;

    private static final int wait2441Time = 15000;
    private static final int waitAmfTime = 10000;
    private static final int retryInterval = 3000;

    private static final HttpClient directClient;
    private static final HttpClient proxyClient;
    private static HttpClient httpClient;
    private static int proxyPort;
    
    static {
        host = realhost;
        if (Cookie.getCookie() == null){
            System.out.println("读取data/cookie文件出错！");
            System.out.println("请在运行前设置cookie！");
        }
        proxyPort = 8887;
        proxyClient = HttpClient.newBuilder()
        .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", proxyPort)))
        .build();
        directClient = HttpClient.newHttpClient();

        httpClient = proxyClient;
    }

    public static boolean changeProxy(boolean on){
        if (on){
            httpClient = proxyClient;
        }
        else{
            httpClient = directClient;
        }
        return httpClient!=null;
    }

    public static int setInterval(int interval){
        if (interval < leastInterval){
            Request.reqInterval = leastInterval;
        }
        else{
            Request.reqInterval = interval;
        }
        return Request.reqInterval;
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
        .header("Cookie", Cookie.getCookie())
        // .header("Connection","keep-alive")
        ;
    }

    private static synchronized void sendIntervalBlock(){
        long interval = System.currentTimeMillis() - lastSentTime;
        if (interval < reqInterval){
            delay(reqInterval-interval);
        }
    }

    private static synchronized void updateLastSend(){
        lastSentTime = System.currentTimeMillis();
    }

    /** @return valid body of response, null if exception */
    public static byte[] sendGetRequest(String path){
        byte[] response;
        int retryCount = 5;
        do {
            response = sendOneGet(path);
            if (response==null){
                if (retryCount == 0){
                    System.out.println("请求失败！请检查网络设置。");
                    return null;
                }
                else{
                    System.out.printf("请求失败，将在%2d秒后重试最多%2d次", retryInterval/1000, retryCount);
                    delay(retryInterval);
                    System.out.print(String.join("", Collections.nCopies(32,"\b")));
                    retryCount--;
                    continue;
                }
            }
            else if (is2441Block(response, true)){
                System.out.print("拦");
                delay(wait2441Time);
                System.out.print("\b\b");
                continue;
            }
            else{
                break;
            }
        } while (true);
        return response;
    }

    /** @return body of response, null if exception. 
     * @apiNote support gzip compress
     */
    private static byte[] sendOneGet(String path){
        String uri = http + host + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.GET().uri(URI.create(uri)).timeout(Duration.ofMillis(timeout));
        addHeaders(builder);
        HttpRequest request = builder.build();
        try {
            sendIntervalBlock();
            HttpResponse.BodyHandler<InputStream> bh = HttpResponse.BodyHandlers.ofInputStream();
            HttpResponse<InputStream> response = httpClient.send(request, bh);
            updateLastSend();
            Optional<String> s = response.headers().firstValue("Content-Encoding");
            if (s.isPresent() && s.get().toLowerCase().equals("gzip")) {
                GZIPInputStream gi = new GZIPInputStream(response.body());
                return gi.readAllBytes();
            }
            return response.body().readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }

    /** @return amf body of response, null if exception */
    private static byte[] sendOnePostAmf(byte[] body){
        String uri = http + host + amfPath;
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.POST(BodyPublishers.ofByteArray(body))
        .uri(URI.create(uri))
        .timeout(Duration.ofMillis(timeout));
        addHeaders(builder);
        HttpRequest request = builder.build();
        try {
            sendIntervalBlock();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            updateLastSend();
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** @return valid amf body of response, 2441 block handled */
    public static byte[] sendPostAmf(byte[] body, boolean handleAmfBlock){
        byte[] response;
        int retryCount = 5;
        do {
            response = sendOnePostAmf(body);
            if (response==null){
                if (retryCount == 0){
                    System.out.println("请求失败！请检查网络设置。");
                    return null;
                }
                else{
                    System.out.printf("请求失败，将在%2d秒后重试最多%2d次", retryInterval/1000, retryCount);
                    delay(retryInterval);
                    System.out.print(String.join("", Collections.nCopies(32,"\b")));
                    retryCount--;
                    continue;
                }
            }
            else if (is2441Block(response, false)){
                System.out.print("拦");
                delay(wait2441Time);
                System.out.print("\b\b");
            }
            else if(handleAmfBlock && isAmfBlock(response)){
                System.out.print("频繁");
                delay(waitAmfTime);
                System.out.print("\b\b\b\b");
            }
            else{
                break;
            }
        } while (true);
        return response;
    }

    /** to check if there is a rechapter block */
    private static boolean is2441Block(byte[] response, boolean decoded){
        assert(response != null);
        if (!decoded && response.length == 2441){
            return true;
        }
        else if (decoded){
            String body = new String(response);
            if (body.indexOf("<script type=\"text/javascript\">") != -1){
                return true;
            }
            if (body.indexOf("Your requests are too frequent!") != -1){
                return true;
            }
        }
        return false;
    }

    /** to check if amf block is traggered */
    private static boolean isAmfBlock(byte[] response){
        AMF0Message msg = Util.decodeAMF(response);
        if (msg == null) return false;
        AMF0Body body = msg.getBody(0);
        if(Response.isOnStatusException(body, false)
        && (Response.getExceptionDescription(body).equals("Exception:请不要操作过于频繁。")
        || Response.getExceptionDescription(body).equals("请不要操作过于频繁。")))
        {
            return true;
        }
        return false;
    }

    public static void resolve(String[] args) {
        if (args.length == 2 && args[0].equals("proxy")) {
            if (args[1].equals("on")){
                changeProxy(true);
                return;
            }else if (args[1].equals("off")){
                changeProxy(false);
                return;
            }
        }
        else if (args.length == 2 && args[0].equals("interval")) {
            try {
                int value = Integer.parseInt(args[1]);
                int actual = setInterval(value);
                System.out.printf("new interval: %d\n", actual);
                return;
            } catch (Exception e) {
            }
        }

        System.out.println("args: proxy on|off");
        System.out.println("or  : interval n(ms)");
    }
    
    public static void main(String[] args) {
    }
}
