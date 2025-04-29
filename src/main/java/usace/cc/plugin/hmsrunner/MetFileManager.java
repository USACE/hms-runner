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
        if (line.contains("Precip Method Parameters: Grid")){
            isPrecip = true;
        }else if(line.contains("Air Temperature method Parameters: Grid")){
            isTemp = true;
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
            }
        }
        if( line.contains("END:")){
            String[] ablines = (String[])blines.toArray();
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
    System.out.println(temp.size());
    System.out.println(precip.size());
    System.out.println(blocks.size());
    if(temp.size()==1){
        if (precip.size()==1){
            if(blocks.size()>=1){
                EverythingElse = (Block[])blocks.toArray();
                TemperatureGrids = (Block[])temp.toArray();
                PrecipGrids = (Block[])precip.toArray();
                IsValid = true;
            }
        }
    }
}
public String[] Write(Double x, Double y) {
    ArrayList<String> lines = new ArrayList<String>();
    for(Block b : EverythingElse){
        for(String l : b.Lines){
            lines.add(l);
        }
    }
    for(Block b : PrecipGrids){
        for(String l: b.Lines){
                if (l.contains("Storm Center X-coordinate:")){
                    //fix the line to contain the new coordinate.
                    String[] parts = l.split(" ");
                    parts[parts.length-1] = x.toString();
                    String newl = "";
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
                    String newl = "";
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
            lines.add(l);
        }
    }
    return (String[])lines.toArray();
}
}
