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
        @Test
    public void TestRename(){
        System.out.println("repairing catalog.");
        Path p = Paths.get("/workspaces/hms-runner/testdata/trinity/catalog.grid");
        try {
            byte[] data = Files.readAllBytes(p);
            String stringData = new String(data);
            //System.out.println(stringData);
            String[] lines = stringData.split("\n");
            //System.out.println(lines.length);
            GridFileManager g = new GridFileManager(lines);
            for(Block precip : g.PrecipGrids){
                //System.out.print(precip.Lines[0]);
                String precipName = precip.Lines[1].split(": ")[1];
                String yyyymmdd = precipName.split("_")[0];
                //split name based on the date to construct a new date.
                String tempName_old = "AORC " + yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6, 8);
                Boolean found_replacement = false;
                for(Block t: g.TemperatureGrids){
                    if(t.Lines[1].contains(tempName_old)){
                        found_replacement = true;
                        t.Lines[1] = t.Lines[1].replace(tempName_old, precipName);
                        t.Lines[2] = t.Lines[2].replace(tempName_old, precipName);
                        String dssName = tempName_old.replace(" ", "_");
                        dssName = dssName.replace("-", "_");
                        t.Lines[7] = t.Lines[7].replace(dssName, precipName);
                    }
                }
                if(!found_replacement){
                    System.out.println("could not find replacement for " + precipName);
                }
            }
            String[] newlines = g.writeAll();
            for(String l : newlines){
                System.out.print(l);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
