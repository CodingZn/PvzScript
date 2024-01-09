package lib;

import java.util.HashMap;

public class FightProcess {
    public final HashMap<String, Object> assailant;
    public final FightingDefender[] defenders;
    public final FightingSkill[] skills;

    public final String ASS_TYPE = "type";
    public final String ASS_ID = "id";
    
    @SuppressWarnings({"unchecked"})
    public FightProcess(HashMap<String, Object> map){
        assailant = (HashMap<String, Object>) map.getOrDefault("assailant", new HashMap<>());
        Object[] defenderObjArr = (Object[]) map.get("defenders");
        FightingDefender[] defenderArr = new FightingDefender[defenderObjArr.length];
        for (int i=0; i<defenderObjArr.length; i++) {
            FightingDefender dfd = new FightingDefender((HashMap<String, Object>)defenderObjArr[i]);
            defenderArr[i] = dfd;
        }
        defenders = defenderArr;

        Object[] skillObjArr = (Object[]) map.get("skills");
        FightingSkill[] skillArr = new FightingSkill[skillObjArr.length];
        for (int i=0; i<skillObjArr.length; i++) {
            FightingSkill skl = new FightingSkill((HashMap<String, Object>)skillObjArr[i]);
            skillArr[i] = skl;
        }
        skills = skillArr;

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FightProcess: {%s(%s) -attack-> ".formatted(assailant.getOrDefault(ASS_ID, "???"),
            assailant.getOrDefault(ASS_TYPE, "???")));
        for (FightingDefender df : defenders) {
            sb.append("%d(%d->%d) ".formatted(df.id, df.old_hp, df.hp));
        }
        sb.append("} ");
        return sb.toString();
    }
}
