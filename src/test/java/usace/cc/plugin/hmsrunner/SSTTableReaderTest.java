package usace.cc.plugin.hmsrunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;



public class SSTTableReaderTest {
        @Test
    public void TestOpen(){
        System.out.println("testing open");
        Path p = Paths.get("/workspaces/hms-runner/testdata/storms.csv");
        try {
            byte[] data = Files.readAllBytes(p);
            String stringData = new String(data);
            //System.out.println(stringData);
            String[] lines = stringData.split("\n");
            //System.out.println(lines.length);
            SSTTable sst = new SSTTable(lines);
            Event[] stormsst = sst.getEventsByName("20081110_72hr_st1_r403");
            for(Event l : stormsst){
                System.out.println(l.BasinPath);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
