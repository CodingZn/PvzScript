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
        String[] strs = cmd.split(" ", 2);
        if (!isValid(cmd)){
            return;
        }
        else if (strs.length == 2) {
            String[] args = strs[1].split(" ");
            switch (strs[0].toLowerCase()) {
                case "cookie" ->{
                    String[] cookieArgs = strs[1].split(" ", 2);
                    Cookie.resolver(cookieArgs);
                    return;
                }
                case "request" ->{
                    Request.resolve(args);
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
                case "quality"->{
                    Quality.main(args);
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
                case "execfile"->{
                    resolveFile(strs[1]);
                    return;
                }
            
                default->{
                    break;
                }
            }
        }
        System.out.println("Error format!");
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
            System.out.printf("file %s not found!\n", filename);
            return;
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        if (fileStack.contains(scriptFile)){
            System.out.println("recursion call is forbidden!");
            return;
        }
        fileStack.push(scriptFile);
        cmds.forEach(c->{
            if (isValid(c)){
                printPrompt(c);
                resolve(c);
                System.out.println();
            }
        });
        fileStack.pop();
    }

    private static void printPrompt(String cmd){
        int depth = fileStack.size();
        System.out.printf("%s %s %s\n",">>>".repeat(depth), cmd, "<<<");
        return;
    }

}
