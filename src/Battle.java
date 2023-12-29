package src;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;


public class Battle {
    
    public static byte[] shuaDongAmf(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        Object[] value = new Object[3];
        value[0] = caveid;
        Set<Integer> participants = new HashSet<>(paohui);
        participants.addAll(zhuli);
        
        value[1] = Util.integerArr2int(participants.toArray());
        value[2] = hard_level;
        return Util.encodeAMF("api.cave.challenge", "/1", value);

    }

    // private static void chuli22SunShang(ASObject obj){
    //     ASObject[] processes = (ASObject[]) obj.get("proceses");
    //     for (ASObject asObject : processes) {
    //         System.out.println(asObject);
    //     }
    // }

    /** second arg: 13, 14, 15 */
    public static boolean buXie(int plantId, int xiepingId){
        int[] value = new int[]{plantId, xiepingId};
        byte[] reqAmf = Util.encodeAMF("api.apiorganism.refreshHp", "/1", value);
        byte[] response = Request.sendPostAmf(reqAmf, false);

        System.out.printf("plant %d use %d\n", plantId, xiepingId);
        return Response.isOnStatusException(Util.decodeAMF(response).getBody(0), false);

    }

    public static boolean shuaDongDaiji(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        byte[] reqAmf = shuaDongAmf(caveid, hard_level, zhuli, paohui);
        byte[] response;
        do {
            System.out.printf("刷洞%d开始",caveid);
            response = Request.sendPostAmf(reqAmf, true);
            AMF0Message msg = Util.decodeAMF(response);
            if (msg == null){
                System.out.printf("cave %d success\n", caveid);
                return true;
            }
            AMF0Body body= msg.getBody(0);
            if(Response.isOnStatusException(body, true)){
                String exc = Response.getExceptionDescription(body);
                if (exc.equals("Exception:参与战斗的生物HP不能小于1")){
                    zhuli.forEach(i->{
                        buXie(i, 13);
                    });
                    paohui.forEach(i->{
                        buXie(i, 13);
                    });
                    continue;
                }
                else{
                    System.out.printf("cave %d fail\n", caveid);
                    return false;
                }
            }
            else{
                System.out.printf("cave %d success\n", caveid);
                return true;
            }
        } while (true);
    }

    public static void main(String[] args) {
        List<Integer> zhuli = Arrays.asList(new Integer[]{9137747, 8727892, 8832506});
        List<Integer> paohui = Arrays.asList(new Integer[]{10093766, 10093559, 10094084});
        List<Integer> caves = Util.readIntegersFromFile("data/cave.txt");
        caves.forEach(c->{
            shuaDongDaiji(c, 3, zhuli, paohui);
        });
        
    }
}
