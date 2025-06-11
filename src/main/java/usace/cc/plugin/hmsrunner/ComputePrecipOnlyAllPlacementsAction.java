package usace.cc.plugin.hmsrunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.formula.functions.Even;
import org.jcodec.common.DictionaryCompressor.Int;

import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;
import hms.Hms;
import hms.gui.g.tm;
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

public class ComputePrecipOnlyAllPlacementsAction {
    //compute all placement/basin file locations per storm name
    private Action action;
    public ComputePrecipOnlyAllPlacementsAction(Action action){
        this.action = action;
    }
    public void computeAction(){
        //get the storm name
        String stormName = System.getenv("CC_EVENT_IDENTIFIER");
        /*Optional<String> opStormName = action.getAttributes().get("storm-name");
        if(!opStormName.isPresent()){
            System.out.println("could not find action attribute named storm-name");
            return;
        }*/
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


        Optional<ArrayList<String>> opPathNames = action.getAttributes().get("exported-cumulative-paths");
        if(!opPathNames.isPresent()){
            System.out.println("could not find action attribute named exported-cumulative-paths");
            return;
        }
        ArrayList<String> pathNames = opPathNames.get();
        Optional<ArrayList<Integer>> opDurations = action.getAttributes().get("exported-cumulative-durations");
        if(!opDurations.isPresent()){
            System.out.println("could not find action attribute named exported-cumulative-durations");
            return;
        }
        ArrayList<Integer> durations = opDurations.get();
        Optional<String> opPeakDataSourceName = action.getAttributes().get("cumulative-datasource-name");
        if(!opPeakDataSourceName.isPresent()){
            System.out.println("could not find action attribute named cumulative-datasource-name");
            return;
        }
        //get the storm dss file //assumes precip and temp in the same location.
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        //Files.createDirectories(Paths.get(modelOutputDestination));
        DataSource stormCatalog = opStormCatalog.get();
        //stormCatalog.getPaths().put("default", stormCatalog.getPaths().get("storm-catalog-prefix") + "/" + opStormName.get() + ".dss");//not sure if .dss is needed 
        String modifiedStormName = stormName;//opStormName.get();
        modifiedStormName = modifiedStormName.replace("st","ST");
        try {
            //@ TODO fix this to not have to lowercase the st. 

            action.copyFileToLocal(stormCatalog.getName(), "default", modelOutputDestination + "data/" + modifiedStormName + ".dss");
        } catch (InvalidDataSourceException | IOException | DataStoreException e) {

            System.out.println(modelOutputDestination + "data/" + modifiedStormName + ".dss not found");
            //e.printStackTrace();
            System.exit(-1);
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
        Event[] events = table.getEventsByName(stormName);//opStormName.get());

        //add additional optional filter on event number:
        Optional<Integer> opEventNumber = action.getAttributes().get("event-number");
        Boolean run_event = false;
        if(opEventNumber.isPresent()){
            run_event = true;
            Boolean found = false;
            Event[] tmpevents = new Event[1];
            Integer eventnumber = opEventNumber.get();
            for(Event e : events){
                if (e.EventNumber.intValue()==opEventNumber.get().intValue()){
                    tmpevents[0] = e;
                    found = true;
                }
            }
            events = tmpevents;
            if(!found){
                System.out.println("could not find event number " + Integer.toString(eventnumber));
                System.exit(-1);
            }
        }
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
        //String gmstormName = opStormName.get().replace("st", "ST");
        gflines = gfm.write(modifiedStormName,modifiedStormName);//assumes the temp grid and precip grid have the same name - not a safe assumption.
        //write the updated gflines to disk.
        try {
            linesToDisk(gflines, modelOutputDestination + modelName.get() + ".grid");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String failedEvents = "";
        Integer EventCount = 0;
        HashMap<Integer, double[][]> peakdata = new HashMap<Integer, double[][]>();
        //loop over filtered events
        for(Event e : events){
            //update met file
            mflines = mfm.write(e.X,e.Y, stormName);
            //write lines to disk.
            try {
                //write metfile locally
                linesToDisk(mflines, modelOutputDestination + metName.get() + ".met");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            //open hms project.
            System.out.println("opening project " + hmsProjectFile);
            Project project = Project.open(hmsProjectFile);
            
            //compute

            try{
                System.out.println("preparing to run Simulation " + simulationName.get() + " for event number " + Integer.toString(e.EventNumber));
                System.out.println(java.time.LocalDateTime.now());
                long start = System.currentTimeMillis();
                project.computeRun(simulationName.get());
                checkLogFile(modelOutputDestination + simulationName.get() + ".log");
                long end = System.currentTimeMillis();
                System.out.println("took " + (end-start)/1000 + " seconds");

                System.out.println("processing peaks " + simulationName.get() + " for event number " + Integer.toString(e.EventNumber));
                //post peak results to tiledb
                //should i have a list of durations? should i have a list of dss pathnames?
                double [][] durationPeaks = extractDurationCumulativesFromDSS(modelOutputDestination + simulationName.get() + ".dss", durations, pathNames);//need to provide event number in the path
                start = System.currentTimeMillis();
                System.out.println("took " + (start-end)/1000 + " seconds");
                peakdata.put(e.EventNumber, durationPeaks);
                end = System.currentTimeMillis();
                System.out.println("posting to aws took " + (end-start)/1000 + " seconds");
                project.close();

            }catch (Exception ex){
                System.out.println(e.EventNumber + " failed to compute");
                failedEvents += Integer.toString(e.EventNumber) + ", ";
                project.close();
            }
            EventCount ++;//makes sure 0 is never the value.
            //overwrite the simulation dss file because it needs to get "cleaned" after each run.
            String olddssfile = modelOutputDestination + simulationName.get() + ".dss";
            File myf = new File(olddssfile);
            myf.delete();

        }//next event
        if(run_event){
            //TODO read the csv if it is there - checkif the event row is there if it is update it. if it isnt append it.
            System.out.println("failed events: " + failedEvents);
            Hms.shutdownEngine();
            return;
        }
        //create header row //
        String header = "eventNumber,";
        for(String name : pathNames){
            header += name + ",";
        }
        header = header.substring(0,header.length()-1);
        header += "\n";
        for(Integer duration : durations){
            //create a path//
            //add header row.
            StringBuilder sb = new StringBuilder();
            sb.append(header);
            for(Map.Entry<Integer,double[][]> en : peakdata.entrySet()){
                double[] vals = en.getValue()[durations.indexOf(duration)];
                sb.append(Integer.toString(en.getKey()) + ",");
                for(double value : vals){
                    sb.append(Double.toString(value) + ",");
                }
                sb.append("\n");
            }
            try{
                action.put(sb.toString().getBytes(), opPeakDataSourceName.get(), Integer.toString(duration), ""); 
            }catch(Exception ex){
                System.out.println("failed writing duration " + Integer.toString(duration));
            }
                      
        }
        System.out.println("failed events: " + failedEvents);
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
    private void checkLogFile(String path) throws Exception{
        FileInputStream fs = new FileInputStream(path);
        byte[] data = fs.readAllBytes();
        fs.close();
        String sdata = new String(data);
        String[] lines = sdata.split("\n");
        for(String line : lines){
            if (line.contains("ERROR ")){
                throw new Exception("hms did not indeed run.");
            }
        }
    }
    private double[][] extractDurationCumulativesFromDSS(String path, ArrayList<Integer> timesteps, ArrayList<String> datapaths){
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
                //find duration cumulative.
                double runningVal = 0.0;
                for (int timestep = 0; timestep < duration; timestep++)
                {
                    runningVal += data[timestep];
                }
                result[durationIndex][datapathindex] = runningVal;
                durationIndex ++;
            }
            datapathindex ++;
        }  
        reader.closeAndClear(); //why so many close options? seems like close should do what it needs to do.
        return result;
    }
}
