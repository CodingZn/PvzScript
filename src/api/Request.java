package src.api;

import static src.api.Util.delay;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

public class Request {
    protected static int timeout = 40000;
    private static String host;
    private static final String realhost = "pvz-s1.youkia.com";
    private static final String http = "http://";
    private static final String amfPath = "/pvz/amf/";

    protected static final int leastInterval = 50;
    protected static int reqInterval = 1000;
    private static Long lastSentTime = 0L;

    protected static int wait2441Time = 15000;
    protected static int waitAmfTime = 10000;
    protected static int wait302Time = 2*60*1000;
    protected static final int leastRetryInterval = 1000;
    protected static int retryInterval = 20000;
    protected static int retryMaxCount = 10;

    protected static boolean useProxy;
    protected static int proxyPort;

    private enum RequestType{
        GET, POST_AMF
    }
    
    static {
        host = realhost;
        if (Cookie.getCookie() == null){
            Log.logln("读取data/cookie文件出错！");
            Log.print("请在运行前设置cookie！");
        }
        setProxyPort(8887);
        useProxy = true;
    }

    private static HttpClient getClient(){
        if (useProxy) {
            return getProxyClient();
        }else {
            return getDirectClient();
        }
    }

    private static HttpClient getProxyClient(){
        return HttpClient.newBuilder()
        .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", getProxyPort())))
        .build();
    }

    private static HttpClient getDirectClient(){
        return HttpClient.newHttpClient();
    }

    public static boolean changeProxy(boolean on){
        useProxy = on;
        return true;
    }

    public static void setProxyPort(int port){
        proxyPort = port;
    }

    private static int getProxyPort(){ return proxyPort; }

    public static int setInterval(int interval){
        if (interval < leastInterval){
            Request.reqInterval = leastInterval;
        }
        else{
            Request.reqInterval = interval;
        }
        return Request.reqInterval;
    }

    public static int setBlockTime(int value){
        if (value >= leastRetryInterval){
            wait2441Time = value;
        }
        return wait2441Time;
    }

    public static int setAMFBlockTime(int value){
        if (value >= leastRetryInterval){
            waitAmfTime = value;
        }
        return waitAmfTime;
    }

    public static int setTimeout(int value){
        if (value >= leastRetryInterval){
            timeout = value;
        }
        return timeout;
    }

    public static int setRetry(int max_count, int retry_interval){
        if (retry_interval < leastRetryInterval){
            Request.retryInterval = leastRetryInterval;
        }
        else{
            Request.retryInterval = retry_interval;
        }

        if (max_count >= 1){
            Request.retryMaxCount = max_count;
        }

        return Request.retryInterval;
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
        return sendOneRequest(RequestType.GET, path, null, false);
    }

    /** @return valid amf body of response, 2441 block handled */
    public static byte[] sendPostAmf(byte[] body, boolean handleAmfBlock){
        return sendOneRequest(RequestType.POST_AMF, null, body, handleAmfBlock);
    }

    private static byte[] send(HttpRequest request) throws IOException, InterruptedException{
        sendIntervalBlock();
        HttpResponse.BodyHandler<InputStream> bh = HttpResponse.BodyHandlers.ofInputStream();
        HttpResponse<InputStream> response = getClient().send(request, bh);
        updateLastSend();
        Optional<String> s = response.headers().firstValue("Content-Encoding");
        if (s.isPresent() && s.get().toLowerCase().equals("gzip")) {
            GZIPInputStream gi = new GZIPInputStream(response.body());
            return gi.readAllBytes();
        }
        return response.body().readAllBytes();
    }

    /** type=0: get; type=1: postAmf; return valid response */
    private static byte[] sendOneRequest(RequestType type, String path, byte[] body, boolean handleAmfBlock){
        byte[] response;
        HttpRequest request;
        if (type==RequestType.GET){
            String uri = http + host + path;
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.GET().uri(URI.create(uri)).timeout(Duration.ofMillis(timeout));
            addHeaders(builder);
            request = builder.build();
            
        }else if (type==RequestType.POST_AMF){
            String uri = http + host + amfPath;
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.POST(BodyPublishers.ofByteArray(body))
            .uri(URI.create(uri))
            .timeout(Duration.ofMillis(timeout));
            addHeaders(builder);
            request = builder.build();
            
        }else {
            assert false;
            return null;
        }
        int retryCount = retryMaxCount;
        do {
            // 获得response，已经完成gzip解码
            try {
                response = send(request);
            } catch (ConnectException e){
                Log.logln("错误：连接通道关闭。");
                if (useProxy) {
                    Log.print("可能由于代理未开启导致。");
                    useProxy = false;
                    Log.println("已关闭代理。");
                }
                delay(reqInterval);
                continue;
            } catch (HttpTimeoutException e){
                Log.print("请求超时！");
                response = null;
            } catch (IOException e) {
                e.printStackTrace();
                Log.print("请求失败！");
                response = null;
            } catch (InterruptedException e){
                response = null;
            }
            // 预处理 response
            if (response==null){
                if (retryCount == 0){
                    Log.println("请求失败！请检查网络设置。");
                    return null;
                }
                else{
                    Log.print("将在%2d秒后重试最多%2d次\n".formatted(retryInterval/1000, retryCount));
                    delay(retryInterval);
                    retryCount--;
                    continue;
                }
            }
            else if (is2441Block(response)){
                Log.print("拦");
                delay(wait2441Time);
            }
            else if(handleAmfBlock && isAmfBlock(response)){
                Log.print("繁");
                delay(waitAmfTime);
            }
            else if (is302(response)){
                Log.println("服务器返回302。将在%d分钟后重试".formatted(wait302Time/60000));
                delay(wait302Time);
            }
            else if (isCharlesReport(response)){
                delay(reqInterval);
                continue;
            }
            else{
                break;
            }
        } while (true);
        return response;
    
    }
    
    /** to check if there is a rechapter block */
    private static boolean is2441Block(byte[] response){
        assert(response != null);
        String body = new String(response);
        if (body.indexOf("Your requests are too frequent!") != -1){
            return true;
        }
        return false;
    }

    /** to check if amf block is traggered */
    private static boolean isAmfBlock(byte[] response){
        AMF0Message msg = Util.tryDecodeAMF(response);
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

    /** to check if server return 302 busy */
    private static boolean is302(byte[] response){
        assert(response != null);
        String body = new String(response);
        if (body.indexOf("<head><title>302 Found</title></head>") != -1){
            return true;
        }
        if (body.indexOf("302 Found") != -1){
            return true;
        }
        return false;
    }

    /** to check if there is a Charles error */
    private static boolean isCharlesReport(byte[] response){
        assert(response != null);
        String body = new String(response);
        if (body.indexOf("Charles Error Report") != -1){
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
                setInterval(value);
                Log.log("new interval: %d\n".formatted(Request.reqInterval));
                return;
            } catch (Exception e) {
            }
        }
        else if (args.length == 2 && args[0].equals("setblock")) {
            try {
                int value = Integer.parseInt(args[1]);
                setBlockTime(value);
                Log.log("new wait time: %d\n".formatted(Request.wait2441Time));
                return;
            } catch (Exception e) {
            }
        }
        else if (args.length == 2 && args[0].equals("setamfblock")) {
            try {
                int value = Integer.parseInt(args[1]);
                setAMFBlockTime(value);
                Log.log("new amf wait time: %d\n".formatted(Request.waitAmfTime));
                return;
            } catch (Exception e) {
            }
        }
        else if (args.length == 2 && args[0].equals("timeout")) {
            try {
                int value = Integer.parseInt(args[1]);
                setTimeout(value);
                Log.log("new timeout: %d\n".formatted(Request.timeout));
                return;
            } catch (Exception e) {
            }
        }
        else if (args.length == 2 && args[0].equals("port")) {
            try {
                int value = Integer.parseInt(args[1]);
                setProxyPort(value);
                Log.log("new proxy port: %d\n".formatted(getProxyPort()));
                return;
            } catch (Exception e) {
            }
        }
        else if (args.length == 3 && args[0].equals("retry")){
            try {
                int max_count = Integer.parseInt(args[1]);
                int retry_interval = Integer.parseInt(args[2]);
                setRetry(max_count, retry_interval);
                Log.log("new retry interval: %d, maxCount: %d\n".formatted(Request.retryInterval, Request.retryMaxCount));
                return;
            } catch (Exception e) {
            }
        }

        System.out.println("args: proxy on|off");
        System.out.println("or  : port <number>");
        System.out.println("or  : interval <ms>");
        System.out.println("or  : setblock <ms>");
        System.out.println("or  : setamfblock <ms>");
        System.out.println("or  : retry <max_count> <interval_ms>");
    }
    
}
