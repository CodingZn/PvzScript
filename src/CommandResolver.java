package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

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

    public static void resolveFile(String filename){
        List<String> cmds;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename, Charset.forName("UTF-8")))) {
            cmds = reader.lines().toList();
        } catch (FileNotFoundException e) {
            System.out.printf("file %s not found!\n", filename);
            return;
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        cmds.forEach(c->{
            if (isValid(c)){
                System.out.printf(">>> %s <<<\n", c);
                resolve(c);
                System.out.println();
            }
        });
    }

}
