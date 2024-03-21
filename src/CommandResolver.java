package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;

public class CommandResolver {
    public static void resolve(String cmd){
        String[] strs = cmd.toLowerCase().split(" ", 2);
        if (!isValid(cmd)){
            return;
        }
        String[] args;
        if (strs.length == 2) {
            args = strs[1].split(" ");
        }
        else {
            args = new String[]{};
        }
        switch (strs[0]) {
            case "cookie" ->{
                String[] cookieArgs = strs[1].split(" ", 2);
                Cookie.resolver(cookieArgs);
                return;
            }
            case "log" ->{
                Log.main(args);
                return;
            }
            case "request" ->{
                Request.resolve(args);
                return;
            }
            case "ctrl" ->{
                Control.main(args);
                return;
            }
            case "organism"->{
                Organism.main(args);
                return;
            }
            case "mytool"->{
                MyTool.main(args);
                return;
            }
            case "orid"->{
                Orid.main(args);
                return;
            }
            case "tool"->{
                Tool.main(args);
                return;
            }
            case "skill"->{
                Skill.main(args);
                return;
            }
            case "warehouse"->{
                Warehouse.main(args);
                return;
            }
            case "friend"->{
                Friend.main(args);
                return;
            }
            case "cave"->{
                Cave.main(args);
                return;
            }
            case "evolution"->{
                Evolution.main(args);
                return;
            }
            case "route"->{
                EvolRoute.main(args);
                return;
            }
            case "quality"->{
                Quality.main(args);
                return;
            }
            case "skillup"->{
                Warehouse.skillUp(args);
                return;
            }
            case "buxie"->{
                BuXie.main(args);
                return;
            }
            case "battle"->{
                Battle.main(args);
                return;
            }
            case "stonebattle"->{
                StoneBattle.main(args);
                return;
            }
            case "fubenbattle"->{
                FubenBattle.main(args);
                return;
            }
            case "serverbattle"->{
                ServerBattle.main(args);
                return;
            }
            case "garden"->{
                Garden.main(args);
                return;
            }
            case "execfile"->{
                resolveFile(strs[1]);
                return;
            }
            case "dailyreward"->{
                DailyReward.main(args);
                return;
            }
            case "territory"->{
                Territory.main(args);
                return;
            }
            case "shop"->{
                Shop.main(args);
                return;
            }
            default->{
                Log.logln("Error format!");
                return;
            }
        }
    }

    private static boolean isValid(String cmd){
        if (cmd.trim().length()==0 || cmd.trim().charAt(0)=='#'){
            return false;
        }
        else return true;
    }

    private static Stack<File> fileStack = new Stack<>(); 

    public static void resolveFile(String filename){
        List<String> cmds;
        File scriptFile = new File(filename);
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile, Charset.forName("UTF-8")))) {
            cmds = reader.lines().toList();
        } catch (FileNotFoundException e) {
            Log.logln("文件%s不存在！".formatted(filename));
            return;
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        if (fileStack.contains(scriptFile)){
            Log.logln("recursion call is forbidden!");
            return;
        }
        fileStack.push(scriptFile);
        cmds.forEach(c->{
            if (isValid(c)){
                printPrompt(c);
                resolve(c);
                Log.println();
            }
        });
        fileStack.pop();
    }

    private static void printPrompt(String cmd){
        int depth = fileStack.size();
        Log.log("%s %s %s\n".formatted(">>>".repeat(depth), cmd, "<<<"));
        return;
    }

}
