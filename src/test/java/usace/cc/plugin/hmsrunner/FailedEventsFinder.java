package usace.cc.plugin.hmsrunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class FailedEventsFinder {
        @Test
    public static void main(String[] args){
        System.out.println("testing read failed events");
        File folder = new File("/workspaces/hms-runner/testdata/trinity/conformance/simulations/logs/hydrology/");
        Map<String,ArrayList<Integer>> failures = new HashMap<String,ArrayList<Integer>>();
        for(File f: folder.listFiles()){
            if(!f.isDirectory()){
                String filename = f.getName();
                byte[] data = null;
                try {
                    data = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String stringData = new String(data);
                String[] parsed = stringData.split(": ");
                String[] events = parsed[1].split(", ");
                ArrayList<Integer> ints = new ArrayList<Integer>();
                for(String e : events){
                    if(!e.equals(" ")){
                        Integer i = Integer.parseInt(e);
                        ints.add(i);
                    }

                }
                failures.put(filename, ints);
            }
        }
        String s = "";
        for(Map.Entry<String,ArrayList<Integer>> e : failures.entrySet()){
            s += e.getKey();
            for(Integer i : e.getValue()){
                s += "," + Integer.toString(i);
            }
            s += "\n";
        }
        System.out.println(s);
        s = "";
        for(Map.Entry<String,ArrayList<Integer>> e : failures.entrySet()){
            //s += e.getKey();
            for(Integer i : e.getValue()){
                s += "," + Integer.toString(i);
            }
            //s += "\n";
        }
        System.out.println(s);
    }
}
