package usace.cc.plugin.hmsrunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import hms.model.Project;
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
        
        Optional<String> modelName = action.getAttributes().get("model-name");
        if(!modelName.isPresent()){
            System.out.println("could not find action attribute named model-name");
            return;
        }
        Optional<String> simulationName =  action.getAttributes().get("simulation");
        if(!simulationName.isPresent()){
            System.out.println("could not find action attribute named simulation");
            return;
        }
        //get the storm dss file
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
        //how do i plan on figuring out the hms project files? one datasource with many paths
        Optional<DataSource> opHmsDataSource = action.getInputDataSource("hms");
        if(!opHmsDataSource.isPresent()){
            System.out.println("could not find action input datasource named hms");
        }
        //placeholder for hms project file.
        String hmsProjectFile = modelOutputDestination;
        DataSource hmsDataSource = opHmsDataSource.get();
        for(Map.Entry<String, String> keyvalue : hmsDataSource.getPaths().entrySet()){
            if(!keyvalue.getKey().contains("grid-file")&!keyvalue.getKey().contains("met-file")){//skip grid and met
                String[] fileparts = keyvalue.getValue().split("/");
                if(keyvalue.getKey().contains("hms-project-file")){
                    //keep track of this modified path
                    hmsProjectFile += fileparts[fileparts.length-1];
                }
                //download the file locally.
                String outdest = modelOutputDestination + fileparts[fileparts.length-1];
                InputStream is = action.getInputStream(hmsDataSource,keyvalue.getKey());
                FileOutputStream fs = new FileOutputStream(outdest,false);
                is.transferTo(fs);//igorance is bliss
                fs.close();
            }
        }
        //get the grid file
        byte[] gfdata = action.get("hms","grid-file","");
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
        //get the basinfile prefix
        Optional<DataSource> opBasinFiles = action.getInputDataSource("basinfiles");
        if(!opBasinFiles.isPresent()){
            System.out.println("could not find action input datasource basinfiles");
            return;
        }
        DataSource basinFiles = opBasinFiles.get();
        //get the controlfile prefix
        Optional<DataSource> opControlFiles = action.getInputDataSource("controlfiles");
        if(!opControlFiles.isPresent()){
            System.out.println("could not find action input datasource controlfiles");
            return;
        }
        DataSource controlFiles = opControlFiles.get();
        //loop over filtered events
        for(Event e : events){
            //update met file
            mflines = mfm.write(e.X,e.Y);
            // TODO write lines to disk.
            //write metfile locally
            String basinPostfix = e.BasinPath;
            //update control file. - do we plan on having control files with the basin files?
            String[] basinparts = basinPostfix.split("/");
            String basinfilename = basinparts[basinparts.length-1];//should get me the last part.
            String base = basinfilename.split("_")[0];//should be all but the last part.
            String controlPostfix = base + ".control";
            controlFiles.getPaths().put("default",controlFiles.getPaths().get("control-prefix") + "/" + controlPostfix);
            InputStream cis = action.getInputStream(controlFiles, "default");
            FileOutputStream cfs = new FileOutputStream(modelOutputDestination + controlPostfix,false);
            cis.transferTo(cfs);//igorance is bliss
            cfs.close();
            //get the basin file for this storm. 
            basinFiles.getPaths().put("default",basinFiles.getPaths().get("basin-prefix") + "/" + basinPostfix);
            InputStream is = action.getInputStream(basinFiles, "default");
            FileOutputStream fs = new FileOutputStream(modelOutputDestination + basinPostfix,false);//check this may need to drop in a slightly different place.
            is.transferTo(fs);//igorance is bliss
            fs.close();

            //open hms project.
            System.out.println("opening project " + hmsProjectFile);
            Project project = Project.open(hmsProjectFile);
            
            //compute
            System.out.println("preparing to run Simulation " + simulationName.get());
            project.computeRun(simulationName.get());
            
            //export excess precip
            //close hms
            project.close();
            //post peak results to tiledb
            //post excess precip
            //post simulation dss (for updating hdf files later) - alternatively write time series to tiledb
        }//next event
    }
}
