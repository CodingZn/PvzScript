package src.api;

import java.util.Random;

public class Paohui {
    public final int plant_id;
    public final int orid_id;
    public final String orid_name;
    
    public final int grade_ini;
    public final int occupy;

    private int grade_predicate;
    private int decimal_predicate;

    public int getGradePredicate(){
        return grade_predicate;
    }

    /** 一次带级后预测升级，约2.2级 */
    public int upgrade(){
        decimal_predicate += 22;
        grade_predicate += (decimal_predicate/10);
        decimal_predicate = decimal_predicate % 10;
        return grade_predicate;
    }

    public Paohui(Organism organism){
        plant_id = organism.id;
        orid_id = organism.pid;
        Orid plantOrid = Orid.getOridMap().get(orid_id);
        orid_name = plantOrid.name;
        grade_ini = organism.grade;
        occupy = plantOrid.occupy;
        grade_predicate = grade_ini;
        decimal_predicate = new Random().nextInt(10);
    }

}
