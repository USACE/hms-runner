package usace.cc.plugin.hmsrunner;

import java.util.ArrayList;

public class MetFileManager {
//read in and parse a met file
//validate Normalization technique for storm start date
//add ability to update x and y location
//add ability to write out updated met file as bytes or bytestream
public Block[] EverythingElse;
public Block[] PrecipGrids;
public Block[] TemperatureGrids;
public Boolean IsValid = false;
//potentially change to a streamreader of some sort. 
public MetFileManager(String[] lines){
    ArrayList<String> blines = new ArrayList<String>();
    ArrayList<Block> blocks = new ArrayList<Block>();
    ArrayList<Block> precip = new ArrayList<Block>();
    ArrayList<Block> temp = new ArrayList<Block>();
    Boolean isTemp = false;
    Boolean isPrecip = false;
    Boolean isValid = false;
    for(String line :lines){
        blines.add(line);
        if (line.contains("Precip Method Parameters: Gridded Precipitation")){
            isPrecip = true;
            //System.out.println("is precip");
        }else if(line.contains("Air Temperature Method Parameters: Grid")){
            isTemp = true;
            //System.out.println("is temp");
        }
        if (line.contains("Time Shift Method:")){
            if (isPrecip){
                if(line.contains("NORMALIZE")){
                    isValid = true;
                }
            }else if (isTemp){
                if(line.contains("NONE")){
                    isValid = true;
                }
                if(line.contains("NORMALIZE")){
                    isValid = true;
                }
            }
        }
        if( line.contains("End:")){
            //System.out.println("Found End:");
            Object[] oblines = blines.toArray();
            String[] ablines = new String[oblines.length];
            for(int i = 0; i<oblines.length;i++){
                ablines[i] = (String)oblines[i];
            }
            
            Block b = new Block(ablines);
                if(isPrecip){
                    if (isValid){
                        precip.add(b);
                    }
                }else if(isTemp){
                    if(isValid){
                        temp.add(b);
                    }
                    
                }else{
                    blocks.add(b);
                }         
            blines = new ArrayList<String>();
            isTemp = false;
            isPrecip = false;
            isValid = false;
        }
    }
    //System.out.println(temp.size());
    //System.out.println(precip.size());
    //System.out.println(blocks.size());
    if(temp.size()==1){
        if (precip.size()==1){
            if(blocks.size()>=1){
                Object[] eteo = blocks.toArray();
                EverythingElse = new Block[eteo.length];
                Object[] tgo = temp.toArray();
                TemperatureGrids = new Block[tgo.length];
                Object[] pgo = precip.toArray();
                PrecipGrids = new Block[pgo.length];
                for(int i = 0; i<eteo.length;i++){
                    EverythingElse[i] = (Block)eteo[i];
                }
                for(int i = 0; i<tgo.length;i++){
                    TemperatureGrids[i] = (Block)tgo[i];
                }
                for(int i = 0; i<pgo.length;i++){
                    PrecipGrids[i] = (Block)pgo[i];
                }
                IsValid = true;
            }
        }
    }
}
public String[] write(Double x, Double y, String stormName) {
    ArrayList<String> lines = new ArrayList<String>();
    for(Block b : EverythingElse){
        for(String l : b.Lines){
            lines.add(l);
        }
    }
    for(Block b : PrecipGrids){
        for(String l: b.Lines){
            if(l.contains("Precip Grid Name:")){
                lines.add("     Precip Grid Name: " + stormName + "\n");
                continue;
            }
            if (l.contains("Storm Center X-coordinate:")){
                //fix the line to contain the new coordinate.
                String[] parts = l.split(" ");
                parts[parts.length-1] = x.toString();
                String newl = "     ";
                for(String part : parts){
                    newl += part;
                }
                lines.add(newl);
                continue;
            }
            if (l.contains("Storm Center Y-coordinate:")){
                //fix the line to contain the new coordinate.
                String[] parts = l.split(" ");
                parts[parts.length-1] = y.toString();
                String newl = "     ";
                for(String part : parts){
                    newl += part;
                }
                lines.add(newl);
                continue;
            }

                lines.add(l);
        }
    }
    for(Block b : TemperatureGrids){
        for(String l : b.Lines){
            String line = l;
            if(l.contains("Temperature Grid Name:")){
                line = "     Temperature Grid Name: " + stormName;
            }
            if(l.contains("Time Shift Method:")){
                line = "     Time Shift Method: NORMALIZE";
            }
            lines.add(line + "\n");
        }
    }
    Object[] oblines = lines.toArray();
    String[] ablines = new String[oblines.length];
    for(int i = 0; i<oblines.length;i++){
        ablines[i] = (String)oblines[i];
    }
    return ablines;
}
}
