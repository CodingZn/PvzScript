package src.api;

public enum PropType{
    HP(445,"HP","hp",1131), 
    ATTACK(446,"攻击","attack",1132), 
    HUJIA(447,"护甲","miss",1133),
    CHUANTOU(448,"穿透","precision",1134),
    SPEED(449,"速度","speed",1137), 
    SHANBI(1093,"闪避","new_miss",1135),
    MINGZHONG(1094,"命中","new_precision",1136);

    public final int value;
    public final String desc;
    public final String field;
    public final int chuanId;
    private PropType(int v, String s, String f, int c){
        value=v;
        desc=s;
        field=f;
        chuanId=c;
    }
    public static final PropType get(String nm){
        switch (nm.toLowerCase()) {
            case "hp"->{return HP;}
            case "gj"->{return ATTACK;}
            case "hj"->{return HUJIA;}
            case "ct"->{return CHUANTOU;}
            case "sb"->{return SHANBI;}
            case "mz"->{return MINGZHONG;}
            case "sd"->{return SPEED;}
            default->{return null;}
        }
    }
}
