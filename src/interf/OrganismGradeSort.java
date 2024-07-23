package src.interf;

import java.util.Comparator;

import src.api.Organism;

public class OrganismGradeSort implements Comparator<Organism> {
    @Override
    public int compare(Organism o1, Organism o2) {
        if (o1.grade > o2.grade){
            return -1;
        }else if (o1.grade == o2.grade && o1.id < o2.id){
            return -1;
        }else return 1;
    }

}
