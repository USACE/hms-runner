package usace.cc.plugin.hmsrunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;



public class GridReaderTest {
        @Test
    public void TestOpen(){
        System.out.println("testing open");
        Path p = Paths.get("/workspaces/hms-runner/testdata/duwamish/duwamish_cutdown.grid");
        try {
            byte[] data = Files.readAllBytes(p);
            String stringData = new String(data);
            //System.out.println(stringData);
            String[] lines = stringData.split("\n");
            //System.out.println(lines.length);
            GridFileManager g = new GridFileManager(lines);
            String name = "PORAORC";
            String tempname = "AORC_TEMP";
            System.out.println(name);
            String[] newlines = g.write(name, tempname);
            for(String l : newlines){
                System.out.print(l);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
