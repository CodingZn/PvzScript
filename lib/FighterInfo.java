package lib;

import java.util.HashMap;

/**
 * a wrapped class representing assailants and defenders
 */
public class FighterInfo {
    public final Integer id;
    public final Long hp;
    public final Long hp_max;
    public final Integer orid;
    public final Integer grade;
    public final Integer quality_id;

    public FighterInfo(HashMap<String, Object> map){
        this.id = Integer.parseInt(map.getOrDefault("id", "-1").toString());
        this.hp = Long.parseLong(map.getOrDefault("hp", "-1").toString());
        this.hp_max = Long.parseLong(map.getOrDefault("hp_max", "-1").toString());
        this.orid = Integer.parseInt(map.getOrDefault("orid", "-1").toString());
        this.grade = Integer.parseInt(map.getOrDefault("grade", "-1").toString());
        this.quality_id = Integer.parseInt(map.getOrDefault("quality_id", "-1").toString());
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FighterInfo {");
        sb.append("id=%d, ".formatted(id));
        sb.append("hp=%d, ".formatted(hp));
        sb.append("hp_max=%d, ".formatted(hp_max));
        sb.append("orid=%d, ".formatted(orid));
        sb.append("grade=%d, ".formatted(grade));
        sb.append("quality_id=%d} ".formatted(quality_id));
        return sb.toString();
    }
}
