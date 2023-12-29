package src;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BatchEvolution {
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException{
        File file = new File("data/evolution.txt");
        FileReader fr = new FileReader(file);
        BufferedReader bReader = new BufferedReader(fr);
        String line = bReader.readLine();
        while (line != null){
            String[] ags = line.split(" ");
            System.out.printf("processing %s\n", ags[0]);
            Evolution.main(ags);
            line = bReader.readLine();
        }
        bReader.close();
    }
}
