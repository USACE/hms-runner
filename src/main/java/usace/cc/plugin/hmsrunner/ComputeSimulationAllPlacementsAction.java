package usace.cc.plugin.hmsrunner;

import java.io.IOException;
import java.util.Optional;

import usace.cc.plugin.DataSource;
import usace.cc.plugin.Action;

public class ComputeSimulationAllPlacementsAction {
    //compute all placement/basin file locations per storm name
    private Action action;
    public ComputeSimulationAllPlacementsAction(Action action){
        this.action = action;
    }
    public void compute() throws Exception, IOException{
        //get the storm name
        Optional<String> opStormName = action.getAttributes().get("storm-name");
        if(!opStormName.isPresent()){
            System.out.println("could not find action attribute named storm-name");
            return;
        }
        //get the base directory for the storm catalog
        Optional<DataSource> opStormCatalog = action.getInputDataSource("storm-catalog");//should be relative to the S3Datastore instance
        if(!opStormName.isPresent()){
            System.out.println("could not find action input datasource named storm-catalog");
            return;
        }
        //get the storm dss file
        Optional<String> modelName = action.getAttributes().get("model-name");
        if(!modelName.isPresent()){
            System.out.println("could not find action attribute named model-name");
            return;
        }
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        DataSource stormCatalog = opStormCatalog.get();
        stormCatalog.getPaths().put("default", stormCatalog.getPaths().get("storm-catalog-prefix") + "/" + opStormName.get() + ".dss");//not sure if .dss is needed 
        action.copyFileToLocal(stormCatalog.getName(), "default", modelOutputDestination + "/data/" + opStormName.get() + ".dss");
        //get the storms table.
        Optional<DataSource> opStormsTable = action.getInputDataSource("storms");
        if(!opStormsTable.isPresent()){
            System.out.println("could not find action input datasource named storms");
            return;
        }
        byte[] data = action.get(opStormsTable.get().getName(),"default","");
        String stringData = new String(data);
        String[] lines = stringData.split("\n");
        SSTTable table = new SSTTable(lines);
        //filter storms table based on storm name
        Event[] events = table.getEventsByName(opStormName.get());
        //get the hms project files.
        //get the grid file
        byte[] gfdata = action.get("grid-file","default","");
        String gfstringData = new String(gfdata);
        String[] gflines = gfstringData.split("\n");
        GridFileManager gfm = new GridFileManager(gflines);
        //get the met file.
        byte[] mfdata = action.get("met-file","default","");
        String mfstringData = new String(mfdata);
        String[] mflines = mfstringData.split("\n");
        MetFileManager mfm = new MetFileManager(mflines);
        //update grid file based on storm name.
        gflines = gfm.write(opStormName.get(), opStormName.get());//assumes the temp grid and precip grid have the same name - not a safe assumption.
        // TODO write the updated gflines to disk. 
        //loop over filtered events
        for(Event e : events){
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
        }//next event
            
        

    }
}
