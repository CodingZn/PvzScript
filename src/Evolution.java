package src;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Evolution {
    private static String host;
    private static final String realhost = "pvz-s1.youkia.com";
    private static final String testhost = "pvzol.org";
    private static final String cookie;

    private static List<Integer[]> EVOLUTION_PATHS = new ArrayList<>();

    private static String getPath(String plantId, String routeId){
            String time = Long.toString(new Date().getTime());
            return "/pvz/index.php/organism/evolution/id/" + plantId + "/route/" + routeId + 
            "/shortcut/2/sig/520a1059b91423de592b8283e11d251b?" + time;
        }

    static {
        String readcookie = "";
        try (FileInputStream reader = new FileInputStream("data/cookie")) {
            readcookie = new String(reader.readAllBytes());
        } catch (Exception e) {
            System.out.println("读取data/cookie文件出错！");
            System.exit(1);
        }
        cookie = readcookie;
        host = realhost;
        EVOLUTION_PATHS.add(new Integer[]{1,178,179,180,181,197,6,7,266,267,268,269,270,271,272,273,274});
    }

    private static boolean evolve(String plantId, int pathno){
        return evolve(plantId, pathno, 0, -1);
    }

    private static boolean evolve(String plantId, int pathno, int start, int end){
        Integer[] thispath = EVOLUTION_PATHS.get(pathno);
        if (end == -1){
            end = thispath.length;
        }
        for (int i = start; i < end; i++) {
            String uri = "http://"+host+getPath(plantId, thispath[i].toString());
            System.out.printf("evolue: id=%s, route=%d ---> ", plantId, thispath[i]);
            int retlen = sendRequest(uri);
            System.out.printf("length: %d\n",retlen);
            if (retlen == -1 || retlen == 191){
                return false;
            }
            int sleepTime = 1500;
            if (retlen == 2441){
                sleepTime = 15000;
                i--;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            
        }
        return true;
    }

    private static int sendRequest(String uri){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.GET().version(Version.HTTP_1_1).uri(URI.create(uri))
        .header("Accept", "*/*")
        .header("Accept-Language", "zh-CN")
        .header("Referer","http://pvz-s1.youkia.com/youkia/main.swf?1192980633")
        .header("x-flash-version","34,0,0,305")
        .header("Accept-Encoding","gzip, deflate")
        .header("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/7.0; .NET4.0C; .NET4.0E;")
        // .header("Host","pvz-s1.youkia.com")
        .header("Cookie", cookie)
        // .header("Connection","keep-alive")
        .timeout(Duration.ofMillis(20000));
        HttpRequest request = builder.build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body().length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        
    }
    
    public static void main(String[] args) throws IOException, InterruptedException{
        System.out.println(cookie);
        if (args.length < 2 || args.length > 4){
            System.out.println("need argument: plantId pathno [start [end] ]");
            assert false;
        }

        String plantId = args[0];
        int pathno = Integer.parseInt(args[1]);
        switch (args.length) {
            case 2 ->{
                evolve(plantId, pathno);
                break;
            }
            case 3 ->{
                int start = Integer.parseInt(args[2]);
                evolve(plantId, pathno, start, -1);
                break;
            }
            case 4 ->{
                int start = Integer.parseInt(args[2]);
                int end = Integer.parseInt(args[3]);
                evolve(plantId, pathno, start, end);
                break;
            }
        }
        
    }
    
}
