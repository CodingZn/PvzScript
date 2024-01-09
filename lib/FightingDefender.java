package lib;

import java.util.HashMap;

/**
 * a wrapped class representing fighting defenders
 */
public class FightingDefender {
    public final Integer id;
    public final Long old_hp;
    public final Integer is_dodge;
    public final Long hp;
    public final Long attack;
    public final Integer is_fear;
    public final Long normal_attack;
    public final Integer boutCount;
    public FightingDefender(HashMap<String, Object> map){
        this.id = Integer.parseInt(map.get("id").toString());
        this.old_hp = Long.parseLong(map.get("old_hp").toString());
        this.is_dodge = (Integer) map.get("is_dodge");
        this.hp = Long.parseLong(map.get("hp").toString());
        this.attack = Long.parseLong(map.get("attack").toString());
        this.is_fear = (Integer) map.get("is_fear");
        this.normal_attack = Long.parseLong(map.get("normal_attack").toString());
        this.boutCount = (Integer) map.get("boutCount");
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FightingDefender {");
        sb.append("id=%d, ".formatted(id));
        sb.append("old_hp=%d, ".formatted(old_hp));
        sb.append("is_dodge=%d, ".formatted(is_dodge));
        sb.append("hp=%d, ".formatted(hp));
        sb.append("attack=%d, ".formatted(attack));
        sb.append("is_fear=%d, ".formatted(is_fear));
        sb.append("normal_attack=%d, ".formatted(normal_attack));
        sb.append("boutCount=%d} ".formatted(boutCount));
        return sb.toString();
    }
}
