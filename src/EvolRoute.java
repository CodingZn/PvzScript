package src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

public class EvolRoute implements Serializable{
    public final int number;
    public final List<Evolution> routeList; 
    public final List<Orid> oridList; 
    public final Orid begin;
    public final Orid end;

    public final long cost;

    private EvolRoute(Stack<Orid> stack){
        List<Orid> tmpOridList = new LinkedList<>(stack);
        end = tmpOridList.get(0);
        Collections.reverse(tmpOridList);
        oridList = tmpOridList;
        begin = oridList.get(0);
        List<Evolution> tmp = new ArrayList<>();
        long tmpCost=0;
        for (int i = 0; i < oridList.size()-1; i++) {
            int targetId = oridList.get(i+1).id;
            Orid curr = oridList.get(i);
            if (curr.evolution1.target == targetId){
                tmp.add(curr.evolution1);
                tmpCost+=curr.evolution1.money;
            }else if (curr.evolution2.target == targetId){
                tmp.add(curr.evolution2);
                tmpCost+=curr.evolution2.money;
            }else assert false;
            
        }
        routeList = tmp;
        cost = tmpCost;
        number = allGenerated.size();
        allGenerated.add(this);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("路线%d: 花费%d\n".formatted(this.number, this.cost));
        sb.append(this.begin.name);
        for (Evolution evol : this.routeList) {
            sb.append(evol.toShortString());
        }
        sb.append("\n");
        return sb.toString();
    }

    public List<Integer> toIntList(){
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < res.size(); i++) {
            res.add(routeList.get(i).id);
        }
        return res;
    }

    public static final String defaultPath = "data/route/";

    public String defaultFileName(){
        StringBuffer sb = new StringBuffer();
        sb.append(begin.name.replace('★', 'x'))
        .append("_")
        .append(end.name.replace('★', 'x'))
        .append("_")
        .append(cost)
        .append("_")
        .append("%04d".formatted(new Date().getTime()%10000));
        return sb.toString();
    }

    private static volatile List<EvolRoute> allGenerated = new ArrayList<>();
    private static final TreeMap<Integer, Orid> oridMap = Orid.getOridMap();

    private static void visit(Orid curr, Orid begin, Stack<Orid> stack, List<EvolRoute> res){
        stack.push(curr);
        if (curr==begin){
            res.add(new EvolRoute(stack));
        }else{
            for (Orid pred : curr.predecessors) {
                visit(pred, begin, stack, res);
            }
        }
        stack.pop();
    }

    public static List<EvolRoute> getEvolRoutes(int begin_id, int end_id){
        Orid begin = oridMap.get(begin_id);
        Orid end = oridMap.get(end_id);
        Stack<Orid> oridStack = new Stack<>();
        List<EvolRoute> res = new ArrayList<>();
        visit(end, begin, oridStack, res);
        Collections.sort(res, new Comparator<EvolRoute>() {
            @Override
            public int compare(EvolRoute o1, EvolRoute o2) {
                if (o1.cost < o2.cost) return -1;
                if (o1.cost > o2.cost) return 1;
                return 0;
            }
        });
        return res;
    }

    public static void showEvolRoutes(int begin_id, int end_id){
        List<EvolRoute> res = getEvolRoutes(begin_id, end_id);
        for (EvolRoute evolRoute : res) {
            System.out.println(evolRoute);
        }
    }

    public static EvolRoute getRoute(int route_number){
        if (allGenerated.size() <= route_number){return null;}
        return allGenerated.get(route_number);
    }

    public static boolean saveRoute(int route_no){
        if (route_no >= allGenerated.size()) {
            return false;
        }
        EvolRoute route = allGenerated.get(route_no);
        return saveRoute(route_no, defaultPath+route.defaultFileName());
    }

    public static boolean saveRoute(int route_no, String filename){
        if (route_no >= allGenerated.size()) {
            return false;
        }
        EvolRoute route = allGenerated.get(route_no);
        return Util.saveIntegersToFile(route.toIntList(), filename);
    }

    public static void main(String[] args) {
        if (args.length == 3 && args[0].equals("search")){
            int begin = Integer.parseInt(args[1]);
            int end = Integer.parseInt(args[2]);
            showEvolRoutes(begin, end);
            return;
        }
        else if (args.length == 3 && args[0].equals("save")){
            int route_number = Integer.parseInt(args[1]);
            saveRoute(route_number, args[2]);
            return;
        }
        else if (args.length == 2 && args[0].equals("save")){
            int route_number = Integer.parseInt(args[1]);
            saveRoute(route_number);
            return;
        }
        System.out.println("args: search <begin_id> <end_id>");
        System.out.println("or  : save <route_number> <filename>");

    }
}
