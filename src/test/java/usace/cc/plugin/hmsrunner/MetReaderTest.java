package usace.cc.plugin.hmsrunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;



public class MetReaderTest {
        @Test
    public void TestOpen(){
        System.out.println("testing open");
        Path p = Paths.get("/workspaces/hms-runner/testdata/duwamish/POR___pt_only.met");
        try {
            byte[] data = Files.readAllBytes(p);
            String stringData = new String(data);
            //System.out.println(stringData);
            String[] lines = stringData.split("\n");
            //System.out.println(lines.length);
            MetFileManager m = new MetFileManager(lines);
            if (m.IsValid){
                System.out.println("valid");
                String[]newlines = m.write(1.0,2.0,"stormname");
                for(String line : newlines){
                    System.out.print(line);
                }
            }else{
                System.out.println("invalid");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
