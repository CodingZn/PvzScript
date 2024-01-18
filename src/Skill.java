package src;

import static src.Util.obj2int;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.amf.AMF0Message;

public class Skill {
    public final int id;
    public final String name;
    public final int grade;
    public final int next_grade_id;
    public final int learn_grade;
    public final int learn_tool_id;
    public final Tool learn_tool;
    public final int learn_probability;

    public final String describe;

    public Skill(Map<String, String> asObj){
        id = obj2int(asObj.get("id"));
        name = (String) asObj.get("name");
        grade = obj2int(asObj.get("grade"));
        next_grade_id = obj2int(asObj.get("next_grade_id"));
        learn_grade = obj2int(asObj.get("learn_grade"));
        learn_tool_id = obj2int(asObj.get("learn_tool"));
        learn_probability = obj2int(asObj.get("learn_probability"));
        describe = (String) asObj.get("describe");
        learn_tool = Tool.getTool(learn_tool_id);
    }

    @Override
    public String toString() {
        Skill next = getSkill(this.next_grade_id);
        StringBuffer sb = new StringBuffer();
        sb.append("lv.%d %s(%d): %s\n".formatted(grade, name, id, describe));
        sb.append("植物等级 %d 级, 可使用[%s]学习; 成功率%d%% \n".formatted(learn_grade,learn_tool.name, learn_probability));
        if (next!=null){
            sb.append("{下一级: %s}\n".formatted(next.toShortString()));
        }
        
        return sb.toString();
    }

    public String toShortString() {
        StringBuffer sb = new StringBuffer();
        sb.append("lv.%d %s(%d)".formatted(grade, name, id, describe,learn_grade));
        return sb.toString();
    }

    private static final String SKILL_FILENAME = "static/skill";

    private static TreeMap<Integer, Skill> skillMap = new TreeMap<>();

    public static TreeMap<Integer, Skill> getSkillMap(){
        return Skill.skillMap;
    }

    public static Skill getSkill(int id){
        return skillMap.get(id);
    }

    public static Skill getSkill(String name, int level){
        Optional<Skill> opsk= skillMap.values().stream().filter(sk->(
            sk.name.equals(name) && sk.grade == level
        )).findFirst();
        return opsk.isPresent()?opsk.get():null;
    }

    static {
        if (!loadSkill()){
            assert false;
        }
    }

    public static boolean loadSkill(){
        if (!loadSkill(false)){
            return loadSkill(true);
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public static boolean loadSkill(boolean remote){
        AMF0Message msg;
        if (remote) {
            byte[] req = Util.encodeAMF("api.apiskill.getAllSkills", "/1", new Object[]{});
            byte[] resp = Request.sendPostAmf(req, true);
            msg = Util.decodeAMF(resp);
        } else{
            msg = Util.decodeAMF(SKILL_FILENAME);
        }
        
        if (msg==null){
            return false;
        }
        AMF0Body body = msg.getBody(0);
        if (!body.getTarget().equals("/1/onResult")){
            return false;
        }
        Object[] skillArr = (Object[])body.getValue();
        for (int i = 0; i < skillArr.length; i++) {
            Skill skill = new Skill((Map<String, String>) skillArr[i]);
            skillMap.put(skill.id, skill);
        }
        
        return true;
    }


    public static void main(String[] args){
        if (args.length == 2 && args[0].equals("show")) {
            int id = Integer.parseInt(args[1]);
            System.out.println(getSkill(id));
            return;
        }
        else if (args.length == 2 && args[0].equals("search")) {
            getSkillMap().values().stream().forEach(sk->{
                if (sk.name.equals(args[1])){
                    System.out.println(sk);
                } 
            });
            return;
        }
        else {
            System.out.println("args: skill show <id>");
            System.out.println("or  : skill search <name>");
        }

    }
}
