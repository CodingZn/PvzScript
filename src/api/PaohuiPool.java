package src.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class PaohuiPool {
    private List<Paohui> pool;
    /** 允许参与战斗的最大炮灰格数 */
    public final int space;
    public final int maxLevel;
    public final boolean keepFull;
    /** 当前炮灰池所有炮灰所占用的格数 */
    private int now_occupy = 0;
    /** 当前等级低于阈值的炮灰个数 */
    private int validPaohuiCount = 0;

    private List<Integer> paohuiIdList;
    
    /** 去掉超过等级的炮灰 */
    private void removeHigherLevel(){
        // 尽可能保持每次战斗植物满格（仅炮灰过多才会考虑移除）
        if (keepFull){
            if (now_occupy <= space) return;
            this.pool.removeIf(p->{
                if (p==null) return true;
                else if (p.getGradePredicate() > maxLevel && now_occupy > space) {
                    now_occupy -= p.occupy;
                    return true;
                }
                return false;
            });
        }
        // 只要超过等级就移除
        else{
            this.pool.removeIf(p->{
                if (p==null) return true;
                else if (p.getGradePredicate() > maxLevel) {
                    now_occupy -= p.occupy;
                    return true;
                }
                return false;
            });
        }
        
    }

    /** 排序，等级从低到高，双格优先 */
    private void sort(){
        this.pool.sort(new Comparator<Paohui>() {
            @Override
            public int compare(Paohui o1, Paohui o2) {
                if (o1.getGradePredicate() < o2.getGradePredicate()){
                    return -1;
                }
                else if (o1.getGradePredicate() > o2.getGradePredicate()){
                    return 1;
                }
                else if (o1.occupy > o2.occupy){
                    return -1;
                }
                else if (o1.occupy < o2.occupy){
                    return 1;
                }
                else return 0;
            }
        });
    }

    private List<Integer> getAll(){
        return new ArrayList<>(this.pool.stream().map(p->{
            return Integer.valueOf(p.plant_id);
        }).toList());
    }

    /** 按排好的顺序选择不超过数量的炮灰 */
    private List<Integer> getChosen(){
        List<Integer> paohui_actual = new ArrayList<>();
        int remainRoom = space;
        for (Paohui pao : this.pool) {
            if (remainRoom >= 1){
                if (remainRoom-pao.occupy>=0){
                    paohui_actual.add(pao.plant_id);
                    remainRoom = remainRoom-pao.occupy;
                }else{
                    continue;
                }
            }
            else{
                break;
            }
        }
        return paohui_actual;
    }
    
    /** 构建炮灰池，支持-1 */
    public PaohuiPool(Collection<Integer> zhuli, List<Integer> paohui, int max_level, boolean kpFull){
        int level_limit = max_level;
        keepFull = kpFull;
        if (level_limit==-1){
            space=999;
            paohuiIdList = paohui;
            maxLevel = level_limit;
            return;
        }else if (level_limit==0){
            level_limit = Integer.MAX_VALUE;
            validPaohuiCount = 99999;
        }
        maxLevel = level_limit;
        LinkedHashMap<Integer, Organism> organisms = Organism.getOrganisms();
        int zhuli_occupy = 0;
        for (Integer id : zhuli) {
            zhuli_occupy += new Paohui(organisms.get(id)).occupy;
        }
        space = 10 - zhuli_occupy;
        this.pool = new ArrayList<>( paohui.stream().map(id->{
            Paohui ph = new Paohui(organisms.get(id));
            now_occupy += ph.occupy;
            if (ph.grade_ini <= maxLevel){
                validPaohuiCount++;
            }
            return ph;
        }).toList());
        this.removeHigherLevel();
    }

    /** 获取当前最低等级、最大占用格数的炮灰 */
    public List<Integer> getChosenPaohuis(){
        if (maxLevel==-1){
            return paohuiIdList;
        }
        this.removeHigherLevel();
        if (now_occupy > space){
            this.sort();
            return this.getChosen();
        }
        // 优化，仅炮灰过多时做选择
        else{
            return this.getAll();
        }

    }

    /** 对参展炮灰中除特定集合外的所有炮灰进行一次战后升级（预测） */
    public void updateExcept(Collection<Integer> fighters, Collection<Integer> died){
        if (maxLevel==-1){
            return ;
        }
        for (Paohui pao : this.pool) {
            if (fighters.contains(pao.plant_id) && !died.contains(pao.plant_id) && pao.getGradePredicate() <= maxLevel){
                int newGrade = pao.upgrade();
                Organism.setGrade(pao.plant_id, newGrade);
                if (newGrade > maxLevel){
                    validPaohuiCount--;
                }
            }
        }
    }

    public boolean hasValidPaohui(){
        if (maxLevel==-1){
            return true;
        }
        return validPaohuiCount>0;
    }
}
