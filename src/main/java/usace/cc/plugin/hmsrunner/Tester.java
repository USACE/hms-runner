package usace.cc.plugin.hmsrunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class Tester {
    public static void main(String[] args) {
        System.out.println("testing open");
        Path p = Paths.get("/workspaces/hms-runner/testdata/duwamish/POR___pt_only.met");
        byte[] data;
        try {
            data = Files.readAllBytes(p);
            String stringData = data.toString();
            String[] lines = stringData.split("\n");
            MetFileManager m = new MetFileManager(lines);

            if (m.IsValid){
                System.out.println("valid");
            }else{
                //System.out.println(m.PrecipGrids[0].Lines);
                System.out.println("invalid");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
