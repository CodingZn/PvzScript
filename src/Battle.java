package src;


import static src.Util.delay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

import lib.FightObject;


public class Battle {
    
    private static byte[] shuaDongAmf(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        Object[] value = new Object[3];
        value[0] = caveid;
        Set<Integer> participants = new HashSet<>(paohui);
        participants.addAll(zhuli);
        
        value[1] = Util.integerArr2int(participants.toArray());
        value[2] = hard_level;
        return Util.encodeAMF("api.cave.challenge", "/1", value);

    }

    private static boolean getAward(String award_key){
        byte[] reqAmf = Util.encodeAMF("api.reward.lottery", "/1", new Object[]{award_key});
        byte[] response = Request.sendPostAmf(reqAmf, true);
        AMF0Message msg = Util.decodeAMF(response);
        System.out.printf(" award: ");
        if (Response.isOnStatusException(msg.getBody(0), true)){
            System.out.print("x\n");
            return false;
        }else{
            System.out.print("√\n");
            return true;
        }
    }

    /** 仅在有植物死亡后触发补血 */
    public static boolean shuaDongDaiji(int caveid, int hard_level, List<Integer> zhuli, List<Integer> paohui){
        byte[] reqAmf = shuaDongAmf(caveid, hard_level, zhuli, paohui);
        byte[] response;
        do {
            System.out.printf("刷洞%d开始",caveid);
            response = Request.sendPostAmf(reqAmf, true);
            AMF0Message msg = Util.decodeAMF(response);
            AMF0Body body= msg.getBody(0);
            if(Response.isOnStatusException(body, true)){
                String exc = Response.getExceptionDescription(body);
                if (exc.equals("Exception:参与战斗的生物HP不能小于1")){
                    boolean res = BuXie.buxie(zhuli, paohui);
                    if (!res){
                        System.out.printf("血瓶不足！\n");
                        return false;
                    }
                    continue;
                }
                // else if (exc.equals("Exception:请不要操作过于频繁。")){
                //     delay(10000);
                //     continue;
                // }
                else{
                    System.out.printf(" fail\n", caveid);
                    return false;
                }
            }
            else{
                System.out.printf(" success.", caveid);
                FightObject fo = new FightObject(body.getValue());
                boolean res = getAward(fo.award_key);
                return res;
            }
        } while (true);
    }

    public static void main(String[] args) {
        if (args.length == 4) {
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = Util.readIntegersFromFile(args[3]);
                caves.forEach(c->{
                    shuaDongDaiji(c, hard_level, zhuli, paohui);
                    delay(2000);
                });
                return;
            } catch (NumberFormatException e) {
            }
        }
        else if (args.length == 3){
            try {
                int hard_level = Integer.parseInt(args[1]);
                List<Integer> caves = Util.readIntegersFromFile(args[0]);
                List<Integer> zhuli = Util.readIntegersFromFile(args[2]);
                List<Integer> paohui = new ArrayList<>();
                caves.forEach(c->{
                    shuaDongDaiji(c, hard_level, zhuli, paohui);
                    delay(2000);
                });
                return;
            } catch (NumberFormatException e) {
            }
        }

        System.out.println("args: cave_file hard_level zhuli_file [ paohui_file ]");
        System.out.println("hard_level: 1 or 2 or 3");
        System.out.println("file format: id seperated by \\n");
    }
}
