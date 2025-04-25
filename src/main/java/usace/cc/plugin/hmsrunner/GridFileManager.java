package usace.cc.plugin.hmsrunner;

import java.util.ArrayList;

import org.python.antlr.PythonParser.continue_stmt_return;

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
        if (line.substring(0,10).contains("Grid Type: ")){
            if (line.contains("Precipitation")){
                isPrecip = true;
            }else if(line.contains("Temperature")){
                isTemp = true;
            }
        }
        if( line.equals("END:")){
            String[] ablines = (String[])blines.toArray();
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
}
public String[] Write(String gridName) {
    ArrayList<String> lines = new ArrayList<String>();
    for(Block b : EverythingElse){
        for(String l : b.Lines){
            lines.add(l);
        }
    }
    for(Block b : PrecipGrids){
        boolean isRightGrid = false;
        for(String l: b.Lines){
            if (l.substring(0,5).equals("Grid: ")){
                if (l.contains(gridName)){
                    isRightGrid = true;
                    break;
                }
            }
        }
        if(isRightGrid){
            for(String l : b.Lines){
                lines.add(l);
            }
        }
    }
    for(Block b : TemperatureGrids){
        boolean isRightGrid = false;
        for(String l: b.Lines){
            if (l.substring(0,5).equals("Grid: ")){
                if (l.contains(gridName)){
                    isRightGrid = true;
                    break;
                }
            }
        }
        if(isRightGrid){
            for(String l : b.Lines){
                lines.add(l);
            }
        }
    }
    return (String[])lines.toArray();
}
}
