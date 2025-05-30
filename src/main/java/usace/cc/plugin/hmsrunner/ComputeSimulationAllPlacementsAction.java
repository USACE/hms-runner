package usace.cc.plugin.hmsrunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import hms.Hms;
import hms.model.Project;
import hms.model.data.SpatialVariableType;
import hms.model.project.ComputeSpecification;
import usace.cc.plugin.api.DataSource;
import usace.cc.plugin.api.DataStore;
import usace.cc.plugin.api.DataStore.DataStoreException;
import usace.cc.plugin.api.cloud.aws.FileStoreS3;
import usace.cc.plugin.api.IOManager;
import usace.cc.plugin.api.IOManager.InvalidDataSourceException;
import usace.cc.plugin.api.Action;

public class ComputeSimulationAllPlacementsAction {
    //compute all placement/basin file locations per storm name
    private Action action;
    public ComputeSimulationAllPlacementsAction(Action action){
        this.action = action;
    }
    public void computeAction(){
        //get the storm name
        Optional<String> opStormName = action.getAttributes().get("storm-name");
        if(!opStormName.isPresent()){
            System.out.println("could not find action attribute named storm-name");
            return;
        }
        //get the base directory for the storm catalog
        Optional<DataSource> opStormCatalog = action.getInputDataSource("storm-catalog");//should be relative to the S3Datastore instance
        if(!opStormCatalog.isPresent()){
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
        Optional<String> metName =  action.getAttributes().get("met-name");
        if(!metName.isPresent()){
            System.out.println("could not find action attribute named met-name");
            return;
        }
        Optional<String> controlName =  action.getAttributes().get("control-name");
        if(!controlName.isPresent()){
            System.out.println("could not find action attribute named control-name");
            return;
        }
        Optional<String> basinName =  action.getAttributes().get("basin-name");
        if(!basinName.isPresent()){
            System.out.println("could not find action attribute named basin-name");
            return;
        }
        Optional<String> exportedPrecipName =  action.getAttributes().get("exported-precip-name");
        if(!exportedPrecipName.isPresent()){
            System.out.println("could not find action attribute named exported-precip-name");
            return;
        }
        Optional<ArrayList<String>> opPathNames = action.getAttributes().get("exported-peak-paths");
        if(!opPathNames.isPresent()){
            System.out.println("could not find action attribute named exported-peak-paths");
            return;
        }
        ArrayList<String> pathNames = opPathNames.get();
        Optional<ArrayList<Integer>> opDurations = action.getAttributes().get("exported-peak-durations");
        if(!opDurations.isPresent()){
            System.out.println("could not find action attribute named exported-peak-durations");
            return;
        }
        ArrayList<Integer> durations = opDurations.get();
        //get the storm dss file //assumes precip and temp in the same location.
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        DataSource stormCatalog = opStormCatalog.get();
        stormCatalog.getPaths().put("default", stormCatalog.getPaths().get("storm-catalog-prefix") + "/" + opStormName.get() + ".dss");//not sure if .dss is needed 
        try {
            //@ TODO fix this to not have to lowercase the st. 
            String modifiedStormName = opStormName.get();
            modifiedStormName = modifiedStormName.replace("st","ST");
            action.copyFileToLocal(stormCatalog.getName(), "default", modelOutputDestination + "/data/" + modifiedStormName + ".dss");
        } catch (InvalidDataSourceException | IOException | DataStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //get the storms table. // TODO update logic to pull from tiledb
        Optional<DataSource> opStormsTable = action.getInputDataSource("storms");
        if(!opStormsTable.isPresent()){
            System.out.println("could not find action input datasource named storms");
            return;
        }
        byte[] data = new byte[0];
        try {
            data = action.get(opStormsTable.get().getName(),"default","");
        } catch (InvalidDataSourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DataStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String stringData = new String(data);
        String[] lines = stringData.split("\n");
        SSTTable table = new SSTTable(lines);
        //filter storms table based on storm name
        Event[] events = table.getEventsByName(opStormName.get());
        //get the hms project files.
        // one datasource for hms base directory with many paths
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
                InputStream is;
                try {
                    System.out.println(keyvalue.getValue());
                    is = action.getInputStream(hmsDataSource,keyvalue.getKey());
                    FileOutputStream fs = new FileOutputStream(outdest,false);
                    is.transferTo(fs);//igorance is bliss
                    fs.close();
                } catch (IOException| InvalidDataSourceException | DataStoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.exit(-1);
                }

            }
        }
        //one datsource to keep relative pathing structure with /data/
        Optional<DataSource> opHmsDataDataSource = action.getInputDataSource("hms-data");
        if(!opHmsDataDataSource.isPresent()){
            System.out.println("could not find action input datasource named hms-data");
        }
        DataSource hmsDataDataSource = opHmsDataDataSource.get();
        for(Map.Entry<String, String> keyvalue : hmsDataDataSource.getPaths().entrySet()){
            String[] fileparts = keyvalue.getValue().split("/");
            //download the file locally.
            String outdest = modelOutputDestination + "data/" + fileparts[fileparts.length-1];
            System.out.println(outdest);
            System.out.println(keyvalue.getValue());
            try{
                InputStream is = action.getInputStream(hmsDataDataSource,keyvalue.getKey());
                FileOutputStream fs = new FileOutputStream(outdest,false);
                is.transferTo(fs);//igorance is bliss
                fs.close();
            } catch (IOException| InvalidDataSourceException | DataStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(-1);
            }
        }
        //get the grid file
        byte[] gfdata = new byte[0];
        try {
            gfdata = action.get("hms","grid-file","");
        } catch (InvalidDataSourceException | IOException | DataStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String gfstringData = new String(gfdata);
        String[] gflines = gfstringData.split("\n");
        gflines[0] = "Grid Manager: " + modelName.get();//@ TODO this should be prepped correctly on model library, this is just a safety net.
        gflines[1] = "     Grid Manager: " + modelName.get();
        GridFileManager gfm = new GridFileManager(gflines);
        //get the met file.
        byte[] mfdata = new byte[0];
        try {
            mfdata = action.get("hms","met-file","");
        } catch (InvalidDataSourceException | IOException | DataStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String mfstringData = new String(mfdata);
        String[] mflines = mfstringData.split("\n");
        MetFileManager mfm = new MetFileManager(mflines);
        //update grid file based on storm name.
        String stormName = opStormName.get().replace("st", "ST");
        gflines = gfm.write(stormName,stormName);//assumes the temp grid and precip grid have the same name - not a safe assumption.
        //write the updated gflines to disk.
        try {
            linesToDisk(gflines, modelOutputDestination + modelName.get() + ".grid");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        Optional<DataSource> opExcessPrecipOutput = action.getOutputDataSource("excess-precip");
        if(!opExcessPrecipOutput.isPresent()){
            System.out.println("could not find action output datasource excess-precip");
            return;
        }
        DataSource excessPrecipOutput = opExcessPrecipOutput.get();
        Optional<DataSource> opSimulationDss = action.getOutputDataSource("simulation-dss");
        if(!opSimulationDss.isPresent()){
            System.out.println("could not find action output datasource simulation-dss");
            return;
        }
        DataSource simulationDss = opSimulationDss.get();
        //loop over filtered events
        for(Event e : events){
            //update met file
            mflines = mfm.write(e.X,e.Y, stormName);
            //write lines to disk.
            try {
                linesToDisk(mflines, modelOutputDestination + metName.get() + ".met");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //write metfile locally
            String basinPostfix = e.BasinPath;
            //update control file. - do we plan on having control files with the basin files?
            String[] basinparts = basinPostfix.split("/");
            String basinfilename = basinparts[basinparts.length-1];//should get me the last part.
            String base = basinfilename.split("_")[0];//should be all but the last part.
            String controlPostfix = base + ".control";
            controlFiles.getPaths().put("default",controlFiles.getPaths().get("control-prefix") + "/" + controlPostfix);
            //InputStream cis;
            try {
                //temporary change due to improper name in the control file @TODO fix this in greg's script that preps controlfiles
                byte[] cdata = action.get(controlFiles.getName(), "default", "");
                String datastring = new String(cdata);
                String[] clines = datastring.split("\n");
                clines[0] = "Control: " + controlName.get();
                linesToDisk(clines, modelOutputDestination + controlName.get() + ".control");
                //cis = action.getInputStream(controlFiles, "default");
                //FileOutputStream cfs = new FileOutputStream(modelOutputDestination + controlName.get() + ".control",false);
                //cis.transferTo(cfs);//igorance is bliss-
                //cfs.close();
            } catch (IOException| InvalidDataSourceException | DataStoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                System.exit(-1);
            }

            //get the basin file for this storm. 
            basinFiles.getPaths().put("default",basinFiles.getPaths().get("basin-prefix") + "/" + basinPostfix + ".basin");
            try {
                //temporary change due to improper name in the basin file @TODO fix this in greg's script that preps basinfiles
                byte[] bdata = action.get(basinFiles.getName(), "default", "");
                String bdatastring = new String(bdata);
                bdatastring = bdatastring.replace("1992-11-29_trinity_nov_dec_2015.sqlite", basinName.get() + ".sqlite");
                String[] blines = bdatastring.split("\n");
                blines[0] = "Basin: " + basinName.get();
                linesToDisk(blines, modelOutputDestination + basinName.get() + ".basin");
                //InputStream is = action.getInputStream(basinFiles, "default");
                //FileOutputStream fs = new FileOutputStream(modelOutputDestination + basinName.get() + ".basin",false);//check this may need to drop in a slightly different place.
                //is.transferTo(fs);//igorance is bliss
                //fs.close();
            } catch (IOException| InvalidDataSourceException | DataStoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                System.exit(-1);
            }

            //open hms project.
            System.out.println("opening project " + hmsProjectFile);
            Project project = Project.open(hmsProjectFile);
            
            //compute
            System.out.println("preparing to run Simulation " + simulationName.get());
            project.computeRun(simulationName.get());
            
            //export excess precip
            ComputeSpecification spec = project.getComputeSpecification(simulationName.get());
            Set<SpatialVariableType> variables = new HashSet<>();
            variables.add(SpatialVariableType.INC_EXCESS);//why not allow for this to be parameterized too?
            spec.exportSpatialResults(modelOutputDestination + exportedPrecipName.get(), variables);
            //close hms
            project.close();
            //write exported excess precip to the cloud.
            
            copyFileToRemote(action, excessPrecipOutput, e.EventNumber, modelOutputDestination + exportedPrecipName.get());
            
            //post peak results to tiledb
            //should i have a list of durations? should i have a list of dss pathnames?
            double [][] durationPeaks = extractPeaksFromDSS(modelOutputDestination + simulationName.get() + ".dss", durations, pathNames);//need to provide event number in the path
            // TODO write peaks to tiledb or an in memory object to flush at the end of the compute of all locations.
            System.out.println(durationPeaks);//ta-da.
            //post simulation dss (for updating hdf files later) - alternatively write time series to tiledb
            copyFileToRemote(action, simulationDss, e.EventNumber, modelOutputDestination + simulationName.get() + ".dss");//need to provide event number in the path
        }//next event
        Hms.shutdownEngine();
        return;
    }

    private void linesToDisk(String[] lines, String path) throws IOException{
        StringBuilder sb = new StringBuilder();
        for(String line : lines){
            sb.append(line + "\n");
        }
        FileOutputStream fs = new FileOutputStream(path,false);
        fs.write(sb.toString().getBytes());
        fs.close();
        return;
    }

    private double[][] extractPeaksFromDSS(String path, ArrayList<Integer> timesteps, ArrayList<String> datapaths){
        HecTimeSeries reader = new HecTimeSeries();
        int status = reader.setDSSFileName(path);
        double[][] result = new double[timesteps.size()][];
        if (status <0){
            //panic?
            DSSErrorMessage error = reader.getLastError();
            error.printMessage();
            return result;
        }

        for (int timestep = 0; timestep < timesteps.size(); timestep++){
            result[timestep] = new double[datapaths.size()];
        }
        int datapathindex = 0;
        for(String datapath : datapaths){
            //get the data.
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = datapath;
            status = reader.read(tsc,true);
            if (status <0){
                //panic?
                DSSErrorMessage error = reader.getLastError();
                error.printMessage();
                reader.closeAndClear();
                return result;
            }
            int durationIndex = 0;
            double[] data = tsc.values;
            for (int duration : timesteps){
                //find duration peak.
                double maxval = 0.0;
                double runningVal = 0.0;
                for (int timestep = 0; timestep < data.length; timestep++)
                {
                    runningVal += data[timestep];
                    if (timestep < duration)
                    {
                        maxval = runningVal;
                    }
                    else
                    {
                        runningVal -= data[timestep - duration];
                        if (runningVal > maxval)
                        {
                            maxval = runningVal;
                        }
                    }
                }
                result[durationIndex][datapathindex] = maxval/(double)duration;
                durationIndex ++;
            }
            datapathindex ++;
        }  
        reader.closeAndClear(); //why so many close options? seems like close should do what it needs to do.
        return result;
    }
    private void copyFileToRemote(IOManager iomanager, DataSource ds, int eventNumber, String localPath){
        InputStream is;
        try {
            is = new FileInputStream(localPath);
        } catch (FileNotFoundException e) {
            System.out.println("could not read from path " + localPath);
            System.exit(-1);
            return;
        }
        Optional<DataStore> opStore = iomanager.getStore(ds.getStoreName());
        
        if(!opStore.isPresent()){
            System.out.println("could not find store named " + ds.getStoreName());
            System.exit(-1);
            return;
        }
        DataStore store = opStore.get();
        FileStoreS3 s3store = (FileStoreS3)store.getSession();
        if(s3store == null){
            System.out.println("could not cast store named " + ds.getStoreName() + " to FileDataStoreS3");
            System.exit(-1);
            return;
        }
        
        //modify default
        String path = ds.getPaths().get("default").replace("eventnumber", Integer.toString(eventNumber));
        try {
            s3store.put(is, path);
        } catch (DataStoreException e) {
            System.out.println("could not write data to path " + path);
        }
    }
}
