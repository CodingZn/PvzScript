package src.cmdApp;

import java.util.Scanner;

import src.api.Log;
import src.api.Util;

public class App {

    private static final String RELEASE_TYPE = "public";

    private static final String APP_VERSION = "1.5.0";
    private static final String RELEASE_DATE = "2024.8.12";

    /**
     * mode 0: command mode
     * mode 1: file batch mode
     */
    private static int mode = 0;
    public static void main(String[] args) {
        if (args.length == 0){
            interact();
        }
        else if (args.length == 1){
            CommandResolver.resolveFile(args[0]);
        }
        else {
            System.out.println("args: (NO ARGS) --for-interact");
            System.out.println("or  : <filename> --for-execute-one-file");
        }
    }

    private static void interact(){
        printInfo();
        System.out.println("Command Mode:");
        Scanner scanner = Util.scanner;
        while (true){
            printPrompt();
            String cmd = scanner.nextLine();
            if (cmd.equals("info")){
                printInfo();
            }
            else if (cmd.equals("exit")){
                break;
            }
            else if (cmd.equals("file")){
                mode = 1;
                System.out.println("FileBatch Mode:");
            }
            else if (cmd.equals("cmd")){
                mode = 0;
                System.out.println("Command Mode:");
            }
            else{
                if (mode == 0) {
                    Log.flog(">>> ");
                    Log.fprintln(cmd);
                    CommandResolver.resolve(cmd);
                }
                else if (mode == 1) {
                    Log.flog("batch filename: ");
                    Log.fprintln(cmd);
                    CommandResolver.resolveFile(cmd);
                }
                else {
                    assert false;
                }
            }
        }
        scanner.close();
        System.out.println("Bye!");
    }
    
    private static void printPrompt(){
        if (mode == 0) {
            System.out.print(">>> ");
        }
        else if (mode == 1) {
            System.out.print("batch filename: ");
        }
        else {
            assert false;
        }
    }

    private static void printInfo(){
        System.out.println("Welcome to PvzScript!");
        System.out.print("Version: %s %s. ".formatted(APP_VERSION, RELEASE_TYPE));
        System.out.println("Released at %s".formatted(RELEASE_DATE));
        System.out.println("Powered by CodingZn@GitHub, ID: 陈年老榴莲@pvz-s1");
    }
}