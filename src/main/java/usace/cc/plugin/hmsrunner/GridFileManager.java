package usace.cc.plugin.hmsrunner;

import java.util.ArrayList;

public class GridFileManager {
//read in a grid file - parse it into the precipitation and temperature grids
//provide ability to export a grid file (as bytes or a bytestream) with a single precip and temperature grid
public Block[] EverythingElse;
public Block[] PrecipGrids;
public Block[] TemperatureGrids;
//potentially change to a streamreader of some sort. 
public GridFileManager(String[] lines){
    ArrayList<String> blines = new ArrayList<String>();
    ArrayList<Block> blocks = new ArrayList<Block>();
    ArrayList<Block> precip = new ArrayList<Block>();
    ArrayList<Block> temp = new ArrayList<Block>();
    Boolean isTemp = false;
    Boolean isPrecip = false;
    for(String line :lines){
        blines.add(line);
        if (line.contains("Grid Type: ")){
            if (line.contains("Precipitation")){
                isPrecip = true;
            }else if(line.contains("Temperature")){
                isTemp = true;
            }
        }
        if( line.contains("End:")){
            Object[] oblines = blines.toArray();
            String[] ablines = new String[oblines.length];
            for(int i = 0; i<oblines.length;i++){
                ablines[i] = (String)oblines[i];
            }
            Block b = new Block(ablines);
                if(isPrecip){
                    precip.add(b);
                }else if(isTemp){
                    temp.add(b);
                }else{
                    blocks.add(b);
                }         
            blines = new ArrayList<String>();
            isTemp = false;
            isPrecip = false;
        }
    }
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
}
public String[] write(String precipGridName, String tempGridName) {
    ArrayList<String> lines = new ArrayList<String>();
    for(Block b : EverythingElse){
        for(String l : b.Lines){
            lines.add(l + "\n");
        }
    }
    for(Block b : PrecipGrids){
        boolean isRightGrid = false;
        for(String l: b.Lines){
            if (l.contains("Grid: ")){
                if (l.contains(precipGridName)){
                    isRightGrid = true;
                    break;
                }
            }
        }
        if(isRightGrid){
            for(String l : b.Lines){
                lines.add(l + "\n");
            }
        }
    }
    for(Block b : TemperatureGrids){
        boolean isRightGrid = false;
        for(String l: b.Lines){
            if (l.contains("Grid: ")){
                if (l.contains(tempGridName)){
                    isRightGrid = true;
                    break;
                }
            }
        }
        if(isRightGrid){
            for(String l : b.Lines){
                lines.add(l + "\n");
            }
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
