package src;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Request {
    private static final String cookie;
    private static String host;
    private static final String realhost = "pvz-s1.youkia.com";
    private static final String http = "http://";
    private static final String amfPath = "/pvz/amf/";
    // private static final String testhost = "pvzol.org";
    
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

    /** return the len of response body */
    public static int sendGetRequest(String path){
        String uri = http + host + path;
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.GET().uri(URI.create(uri)).timeout(Duration.ofMillis(20000));
        addHeaders(builder);
        HttpRequest request = builder.build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body().length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        
    }

    /** @return amf body of response, null if exception */
    public static byte[] sendPostAmf(byte[] body){
        String uri = http + host + amfPath;
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.POST(BodyPublishers.ofByteArray(body))
        .uri(URI.create(uri))
        .timeout(Duration.ofMillis(20000));
        addHeaders(builder);
        HttpRequest request = builder.build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args) {
    }
}
