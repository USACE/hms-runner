package usace.cc.plugin.hmsrunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import usace.cc.plugin.DataSource;
import usace.cc.plugin.DataStore;
import usace.cc.plugin.FileDataStoreS3;
import usace.cc.plugin.IOManager;
import usace.cc.plugin.Action;

public class ComputeSimulationAllPlacementsAction {
    //compute all placement/basin file locations per storm name
    private Action action;
    public ComputeSimulationAllPlacementsAction(Action action){
        this.action = action;
    }
    public void computeAction() throws Exception, IOException{
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
        Optional<String[]> opPathNames = action.getAttributes().get("exported-peak-paths");
        if(!opPathNames.isPresent()){
            System.out.println("could not find action attribute named exported-peak-paths");
            return;
        }
        String[] pathNames = opPathNames.get();
        Optional<int[]> opDurations = action.getAttributes().get("exported-peak-durations");
        if(!opDurations.isPresent()){
            System.out.println("could not find action attribute named exported-peak-durations");
            return;
        }
        int[] durations = opDurations.get();
        //get the storm dss file //assumes precip and temp in the same location.
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        DataSource stormCatalog = opStormCatalog.get();
        stormCatalog.getPaths().put("default", stormCatalog.getPaths().get("storm-catalog-prefix") + "/" + opStormName.get() + ".dss");//not sure if .dss is needed 
        action.copyFileToLocal(stormCatalog.getName(), "default", modelOutputDestination + "/data/" + opStormName.get() + ".dss");
        
        //get the storms table. // TODO update logic to pull from tiledb
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
                InputStream is = action.getInputStream(hmsDataSource,keyvalue.getKey());
                FileOutputStream fs = new FileOutputStream(outdest,false);
                is.transferTo(fs);//igorance is bliss
                fs.close();
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
            InputStream is = action.getInputStream(hmsDataSource,keyvalue.getKey());
            FileOutputStream fs = new FileOutputStream(outdest,false);
            is.transferTo(fs);//igorance is bliss
            fs.close();
        }
        //get the grid file
        byte[] gfdata = action.get("hms","grid-file","");
        String gfstringData = new String(gfdata);
        String[] gflines = gfstringData.split("\n");
        GridFileManager gfm = new GridFileManager(gflines);
        //get the met file.
        byte[] mfdata = action.get("hms","met-file","");
        String mfstringData = new String(mfdata);
        String[] mflines = mfstringData.split("\n");
        MetFileManager mfm = new MetFileManager(mflines);
        //update grid file based on storm name.
        gflines = gfm.write(opStormName.get(), opStormName.get());//assumes the temp grid and precip grid have the same name - not a safe assumption.
        //write the updated gflines to disk.
        linesToDisk(gflines, modelOutputDestination + simulationName.get() + ".grid");
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
            mflines = mfm.write(e.X,e.Y);
            //write lines to disk.
            linesToDisk(mflines, modelOutputDestination + metName + ".met");
            //write metfile locally
            String basinPostfix = e.BasinPath;
            //update control file. - do we plan on having control files with the basin files?
            String[] basinparts = basinPostfix.split("/");
            String basinfilename = basinparts[basinparts.length-1];//should get me the last part.
            String base = basinfilename.split("_")[0];//should be all but the last part.
            String controlPostfix = base + ".control";
            controlFiles.getPaths().put("default",controlFiles.getPaths().get("control-prefix") + "/" + controlPostfix);
            InputStream cis = action.getInputStream(controlFiles, "default");
            FileOutputStream cfs = new FileOutputStream(modelOutputDestination + controlName + ".control",false);
            cis.transferTo(cfs);//igorance is bliss
            cfs.close();
            //get the basin file for this storm. 
            basinFiles.getPaths().put("default",basinFiles.getPaths().get("basin-prefix") + "/" + basinPostfix);
            InputStream is = action.getInputStream(basinFiles, "default");
            FileOutputStream fs = new FileOutputStream(modelOutputDestination + basinName + ".basin",false);//check this may need to drop in a slightly different place.
            is.transferTo(fs);//igorance is bliss
            fs.close();

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

    private double[][] extractPeaksFromDSS(String path, int[] timesteps, String[] datapaths){
        HecTimeSeries reader = new HecTimeSeries();
        int status = reader.setDSSFileName(path);
        double[][] result = new double[timesteps.length][];
        if (status <0){
            //panic?
            DSSErrorMessage error = reader.getLastError();
            error.printMessage();
            return result;
        }

        for (int timestep = 0; timestep < timesteps.length; timestep++){
            result[timestep] = new double[datapaths.length];
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
        FileDataStoreS3 s3store = (FileDataStoreS3)store.getSession();
        if(s3store == null){
            System.out.println("could not cast store named " + ds.getStoreName() + " to FileDataStoreS3");
            System.exit(-1);
            return;
        }
        
        //modify default
        String path = ds.getPaths().get("default").replace("eventnumber", Integer.toString(eventNumber));
        s3store.put(is, path);
    }
}
