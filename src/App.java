package src;

import java.util.Scanner;

public class App {
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
            Log.set(true, false);
            CommandResolver.resolveFile(args[0]);
        }
        else {
            System.out.println("args: NO ARGS --for-interact");
            System.out.println("or  : filename --for-execute-one-file");
        }
    }

    private static void interact(){
        System.out.println("Welcome to PvzScript!");
        Log.set(true, true);
        System.out.println("Command Mode:");
        Scanner scanner = new Scanner(System.in);
        while (true){
            printPrompt();
            String cmd = scanner.nextLine();
            if (cmd.equals("exit")){
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
                    CommandResolver.resolve(cmd);
                }
                else if (mode == 1) {
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
}