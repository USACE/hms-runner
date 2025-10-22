package usace.cc.plugin.hmsrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class PeakMerger {
        @Test
    public static void main(String[] args){
        System.out.println("merging peak files");
        String duration = "72h_peaks";
        File folder = new File("/workspaces/hms-runner/testdata/trinity/conformance/simulations/summary-data/hydrology/");
        String[] lines = new String[20000];
        String mainheader = "";
        for(File f: folder.listFiles()){
            if(!f.isDirectory()){
                if(!f.getName().contains(duration + "_")) continue;
                byte[] data = null;
                try {
                    data = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String stringData = new String(data);
                String[] rows = stringData.split("\n");
                String header = "";
                for(String row : rows){
                    if (header.equals("")){
                        header = row;
                        mainheader = row;
                    }else{
                        String[] parts = row.split(",", 2); // Split the first field from the rest
                        if (parts.length == 0) continue; // Skip empty lines
                        int recordIndex = Integer.parseInt(parts[0]);
                        lines[recordIndex-1] = row;                             
                    }
   
                }
            }
        }
        String s = mainheader + "\n";
        for(String e : lines){
            s += e + "\n";
        }
        FileOutputStream output;
        try {
            output = new FileOutputStream("/workspaces/hms-runner/testdata/trinity/conformance/simulations/summary-data/hydrology_iteration2_" + duration + ".csv");
            output.write(s.getBytes());
            output.close();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //System.out.println(s);
    }
}
