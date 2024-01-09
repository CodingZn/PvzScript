package lib;

import java.util.HashMap;

public class FightObject {
    private static final String ASSAILANTS = "assailants";
    private static final String DEFENDERS = "defenders";
    private static final String PROCESS = "proceses";
    private static final String AWARD_KEY = "awards_key";
    private static final String DIE_STATUS = "die_status";
    private static final String IS_WINNING = "is_winning";
    private static final String RANK = "rank";
    // private static final String SC = "sc";
    // private static final String UPI = "upi";

    public final FighterInfo[] assailants;
    public final FighterInfo[] defenders;
    public final FightProcess[] processes;
    public final String award_key;
    public final Integer die_status;
    public final Boolean is_winning;
    public final Integer rank;

    @SuppressWarnings({"unchecked"})
    public FightObject(HashMap<Object, Object> fightMap){
        Object[] assObjArr = (Object[]) fightMap.get(ASSAILANTS);
        int assSize = assObjArr.length;
        FighterInfo[] res = new FighterInfo[assSize];
        for (int i = 0; i < assSize; i++) {
            HashMap<String, Object> map = (HashMap<String, Object>)assObjArr[i];
            FighterInfo fi = new FighterInfo(map);
            res[i] = fi;
        }
        assailants = res;

        Object[] defObjArr = (Object[]) fightMap.get(DEFENDERS);
        int defsize = defObjArr.length;
        FighterInfo[] res2 = new FighterInfo[defsize];
        for (int i = 0; i < defsize; i++) {
            
            HashMap<String, Object> map = (HashMap<String, Object>)defObjArr[i];
            FighterInfo fi = new FighterInfo(map);
            res2[i] = fi;
        }
        defenders = res2;

        Object[] objArr = (Object[]) fightMap.get(PROCESS);
        int size = objArr.length;
        FightProcess[] res3 = new FightProcess[size];
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> map = (HashMap<String, Object>)objArr[i];
            FightProcess fi = new FightProcess(map);
            res3[i] = fi;
        }
        processes = res3;

        award_key = (String) fightMap.get(AWARD_KEY);
        die_status = (Integer) fightMap.get(DIE_STATUS);
        is_winning = (Boolean) fightMap.get(IS_WINNING);
        rank = (Integer) fightMap.get(RANK);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("{");
        sb.append("awards_key=%s, ".formatted(award_key));
        sb.append("die_status=%d, ".formatted(die_status));
        sb.append("is_winning=%s, ".formatted(is_winning));
        sb.append("rank=%d, ".formatted(rank));
        
        sb.append("assailants=[\n");
        for (FighterInfo fi : assailants) {
            sb.append(fi.toString());
            sb.append(",");
            sb.append("\n");
        }
        sb.append("], ");

        sb.append("defenders=[\n");
        for (FighterInfo fi : defenders) {
            sb.append(fi.toString());
            sb.append(",");
            sb.append("\n");
        }
        sb.append("], ");

        sb.append("processes=[\n");
        for (FightProcess fp: processes) {
            sb.append(fp.toString());
            sb.append(",");
            sb.append("\n");
        }
        sb.append("], ");
        
        return sb.toString();
    }

}

