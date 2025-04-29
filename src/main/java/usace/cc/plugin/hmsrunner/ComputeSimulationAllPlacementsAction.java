package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.Action;

public class ComputeSimulationAllPlacementsAction {
    //compute all placement/basin file locations per storm name
public Action action;
    public ComputeSimulationAllPlacementsAction(Action action){
        this.action = action;
    }
    public void compute(){
        //get the storm name
        //String stormName = action.getStringOrFail("storm-name");
        //get the storm dss file
        //get the storms table.
        //filter storms table based on storm name
        //get the hms project files.
        //get the grid file
        //get the met file.
        //update grid file based on storm name.
        //loop over filtered events
            //update met file
            //write metfile locally
            //update control file.
            //get the basin file for this storm.
            //open hms project.
            //compute
            //export excess precip
            //close hms
            //post peak results to tiledb
            //post excess precip
            //post simulation dss (for updating hdf files later) - alternatively write time series to tiledb
        //next event
        

    }
}
