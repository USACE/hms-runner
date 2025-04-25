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
    for(String line :lines){
        blines.add(line);
        if( line.equals("END:")){
            String[] ablines = (String[])blines.toArray();
            Block b = new Block(ablines);
            if (ablines.length>2){
                if(ablines[1].contains("Precipitation")){//what if there are newlines? i think this could be 1 or 2
                    precip.add(b);
                }else if(ablines[1].contains("Temperature")){
                    temp.add(b);
                }else{
                    blocks.add(b);
                }
            }else{
                blocks.add(b);
            }            
            blines = new ArrayList<String>();
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
        if(b.Lines[0].contains(gridName)){//what if it is a new line?
            for(String l : b.Lines){
                lines.add(l);
            }
        }
    }
    for(Block b : TemperatureGrids){
        if(b.Lines[0].contains(gridName)){//what if it is a new line?
            for(String l : b.Lines){
                lines.add(l);
            }
        }
    }
    return (String[])lines.toArray();
}
}
