package lib;

import java.util.HashMap;

/**
 * a wrapped class representing fighting skills
 */
public class FightingSkill {
    public final String id;
    public final String name;
    public final Integer batter;
    public final String grade;
    public final String organism_attr;
    public final String attack_num;

    public FightingSkill(HashMap<String, Object> map){
        this.id = map.getOrDefault("id", "undefined").toString();
        this.name = map.getOrDefault("name", "undefined").toString();
        this.batter = (Integer) map.getOrDefault("batter", -9999);
        this.grade = map.getOrDefault("grade", "undefined").toString();
        this.organism_attr = map.getOrDefault("organism_attr", "undefined").toString();
        this.attack_num = map.getOrDefault("attack_num", "undefined").toString();
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FighterInfo {");
        sb.append("id=%s, ".formatted(id));
        sb.append("name=%s, ".formatted(name));
        sb.append("batter=%d, ".formatted(batter));
        sb.append("grade=%s, ".formatted(grade));
        sb.append("organism_attr=%s, ".formatted(organism_attr));
        sb.append("attack_num=%s} ".formatted(attack_num));
        return sb.toString();
    }
}
