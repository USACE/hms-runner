package usace.cc.plugin.hmsrunner.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
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
import usace.cc.plugin.hmsrunner.utils.Event;
import usace.cc.plugin.hmsrunner.model.SSTTable;
import usace.cc.plugin.hmsrunner.model.GridFileManager;
import usace.cc.plugin.hmsrunner.model.MetFileManager;

public class ComputeSimulationAllPlacementsAction {
    //compute all placement/basin file locations per storm name
    private Action action;
    public ComputeSimulationAllPlacementsAction(Action action){
        this.action = action;
    }
    public void computeAction(){
        String eventId = System.getenv("CC_EVENT_IDENTIFIER");
        Integer eventNumber = -1;
        Boolean computeByEventNumber = false;
        try{
            eventNumber = Integer.parseInt(eventId);
            computeByEventNumber = true;
        }catch(NumberFormatException ex){
            //System.exit(-1);
        }
        
        //get the storm name
        String stormName = eventId;//placeholder
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
        Optional<String> opPeakDataSourceName = action.getAttributes().get("peak-datasource-name");
        if(!opPeakDataSourceName.isPresent()){
            System.out.println("could not find action attribute named peak-datasource-name");
            return;
        }
        Optional<String> opLogDataSourceName = action.getAttributes().get("log-datasource-name");
        if(!opLogDataSourceName.isPresent()){
            System.out.println("could not find action attribute named log-datasource-name");
            return;
        }
        //get the storm dss file //assumes precip and temp in the same location.
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        //Files.createDirectories(Paths.get(modelOutputDestination));
        DataSource stormCatalog = opStormCatalog.get();
        //stormCatalog.getPaths().put("default", stormCatalog.getPaths().get("storm-catalog-prefix") + "/" + opStormName.get() + ".dss");//not sure if .dss is needed 

        
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
            System.exit(-1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(-1);
        } catch (DataStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(-1);
        }
        String stringData = new String(data);
        String[] lines = stringData.split("\n");
        SSTTable table = new SSTTable(lines);
        //filter storms table based on storm name
        if(computeByEventNumber){
            for(Event e : table.getEvents()){
                if (e.EventNumber.intValue()==eventNumber){
                    stormName = e.StormPath.substring(0,e.StormPath.length()-4);//remove.dss
                }
            }
            String[] prefixparts = stormCatalog.getPaths().get("default").split("/");
            prefixparts[prefixparts.length-1] = stormName + ".dss";
            String path = "";
            for(String p : prefixparts){
                path += p + "/";
            }
            path = path.substring(0,path.length()-1);
            stormCatalog.getPaths().put("default", path);      
        }
        Event[] events = table.getEventsByName(stormName);//opStormName.get());

        //add additional optional filter on event number://TODO convert this to an array instead of a single int
        //Optional<Integer> opEventNumber = action.getAttributes().get("event-number");
        //Boolean run_event = false;
        if(computeByEventNumber){
            //run_event = true;
            Boolean found = false;
            Event[] tmpevents = new Event[1];
            for(Event e : events){
                if (e.EventNumber.intValue()==eventNumber){
                    tmpevents[0] = e;
                    found = true;
                }
            }
            events = tmpevents;
            if(!found){
                System.out.println("could not find event number " + Integer.toString(eventNumber));
                System.exit(-1);
            }
        }

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
            System.exit(-1);
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
            System.exit(-1);
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
            System.exit(-1);
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
        String failedEvents = "";
        Integer EventCount = 0;
        LinkedHashMap<Integer, double[][]> peakdata = new LinkedHashMap<Integer, double[][]>();
        LinkedHashMap<Integer,LinkedHashMap<String,Long>> timelog = new LinkedHashMap<Integer,LinkedHashMap<String,Long>>();
        //loop over filtered events
        for(Event e : events){
            //update met file
            mflines = mfm.write(e.X,e.Y, stormName);
            LinkedHashMap<String, Long> eventtimes = new LinkedHashMap<String,Long>();
            //write lines to disk.
            try {
                linesToDisk(mflines, modelOutputDestination + metName.get() + ".met");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                System.exit(-1);
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
                
                byte[] bdata = action.get(basinFiles.getName(), "default", "");
                String bdatastring = new String(bdata);
                String sqliteName = basinfilename.replace(".basin", ".sqlite");
                System.out.println("replacing " + sqliteName + " with " + basinName.get());
                bdatastring = bdatastring.replace(sqliteName, basinName.get());
                String[] blines = bdatastring.split("\n");
                blines[0] = "Basin: " + basinName.get();
                linesToDisk(blines, modelOutputDestination + basinName.get() + ".basin");
                //InputStream is = action.getInputStream(basinFiles, "default");
                //FileOutputStream fs = new FileOutputStream(modelOutputDestination + basinName.get() + ".basin",false);//check this may need to drop in a slightly different place.
                //is.transferTo(fs);//igorance is bliss
                //fs.close();
            } catch (IOException| InvalidDataSourceException | DataStoreException e1) {
                // TODO Auto-generated catch block
                System.println(basinFiles.getPaths().get("default") + " not found.");
                // e1.printStackTrace();
                System.exit(-1);
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
                eventtimes.put("computing simulation", (end-start)/1000);
                System.out.println("took " + (end-start)/1000 + " seconds");
                System.out.println("posting simulation dss to aws " + simulationName.get() + " for event number " + Integer.toString(e.EventNumber));

                //post simulation dss (for updating hdf files later) - alternatively write time series to tiledb
                copyOutputFileToRemoteWithEventSubstitution(action, simulationDss, e.EventNumber, modelOutputDestination + simulationName.get() + ".dss");//need to provide event number in the path
                start = System.currentTimeMillis();
                eventtimes.put("posting simulation dss to aws", (start-end)/1000);
                System.out.println("took " + (start-end)/1000 + " seconds");
                System.out.println("processing peaks " + simulationName.get() + " for event number " + Integer.toString(e.EventNumber));
                //post peak results to tiledb
                //should i have a list of durations? should i have a list of dss pathnames?
                double [][] durationPeaks = extractPeaksFromDSS(modelOutputDestination + simulationName.get() + ".dss", durations, pathNames);//need to provide event number in the path
                end = System.currentTimeMillis();
                eventtimes.put("processing peaks", (end-start)/1000);
                System.out.println("took " + (end-start)/1000 + " seconds");
                peakdata.put(e.EventNumber, durationPeaks);
                start = System.currentTimeMillis();
                eventtimes.put("posting peaks to map", (start-end)/1000);
                System.out.println("posting peaks to map took " + (start-end)/1000 + " seconds");
                project.close();
                try {
                    //read control file, update it to be 72 horus. 
                    byte[] cdata = action.get(controlFiles.getName(), "default", "");
                    String datastring = new String(cdata);
                    String[] clines = datastring.split("\n");
                    clines[0] = "Control: " + controlName.get();
                    String st = clines[6].split(": ")[1];
                    HecTime endtime = new HecTime(st,"00:01");
                    endtime.addHours(72);
                    clines[8] = "     End Date: " + endtime.date(8);
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

                System.out.println("reopening project " + hmsProjectFile);
                project = Project.open(hmsProjectFile);
                //export excess precip
                //end = System.currentTimeMillis();
                System.out.println("exporting excess precip " + simulationName.get() + " for event number " + Integer.toString(e.EventNumber));
                ComputeSpecification spec = project.getComputeSpecification(simulationName.get());
                Set<SpatialVariableType> variables = new HashSet<>();
                variables.add(SpatialVariableType.INC_EXCESS);//why not allow for this to be parameterized too?
                spec.exportSpatialResults(modelOutputDestination + exportedPrecipName.get(), variables);
                end = System.currentTimeMillis();
                eventtimes.put("exporting spatial results", (end-start)/1000);
                System.out.println("exporting spatial excess precip took " + (end-start)/1000 + " seconds");
                //write exported excess precip to the cloud.
                
                copyOutputFileToRemoteWithEventSubstitution(action, excessPrecipOutput, e.EventNumber, modelOutputDestination + exportedPrecipName.get());
                start = System.currentTimeMillis();
                eventtimes.put("posting spatial results to aws", (start-end)/1000);
                System.out.println("posting to aws took " + (start-end)/1000 + " seconds");
                //close hms (again)
                project.close();

            }catch (Exception ex){
                System.out.println(e.EventNumber + " failed to compute");
                failedEvents += Integer.toString(e.EventNumber) + ", ";
                project.close();
                timelog.put(e.EventNumber, eventtimes);
            }
            EventCount ++;//makes sure 0 is never the value.
                //overwrite the simulation dss file because it needs to get "cleaned" after each run.
                String olddssfile = modelOutputDestination + simulationName.get() + ".dss";
                File myf = new File(olddssfile);
                myf.delete();
                //overwrite the exported precip file because it needs to get "cleaned" after each run. //incidentally this works for both hdf or dss depending on the action prarmeter
                String oldprecipfile = modelOutputDestination + exportedPrecipName.get();
                File myprecipfile = new File(oldprecipfile);
                myprecipfile.delete();
                timelog.put(e.EventNumber, eventtimes);

        }//next event
        //if(failedEvents)
        if(computeByEventNumber){
            if(!failedEvents.equals("")){
                System.out.println("event failed: " +failedEvents);
                System.exit(-1);
            }
            
            System.out.println("event " + eventNumber + " updating peak files.");
            for(Integer duration : durations){
                //get the old data
                System.out.println("processing duration " + Integer.toString(duration));
                byte[] olddata = null;
                try {
                    Optional<DataSource> opDest = action.getOutputDataSource(opPeakDataSourceName.get());
                    DataSource dest = opDest.get();
                    dest.getPaths().put(Integer.toString(duration), dest.getPaths().get(Integer.toString(duration)).replace(Integer.toString(eventNumber),stormName));
                    InputStream is = action.getInputStream(dest, Integer.toString(duration));
                    olddata = is.readAllBytes();
                    //olddata = action.get(opPeakDataSourceName.get(),Integer.toString(duration), ""); get only uses input data sources.
                } catch (InvalidDataSourceException e1) {
                    
                    System.out.println("invalid datasource");
                    System.exit(-1);
                } catch (IOException e1) {
                    
                    System.out.println("io exception");
                    System.exit(-1);
                } catch (DataStoreException e1) {
                    
                    System.out.println("datastre exception");
                    System.exit(-1);
                }
                String olddatastring = new String(olddata);
                String[] oldRows = olddatastring.split("\n");
                Integer oldrowindex = -1;
                Integer tmpIndex = 0;
                for(String row : oldRows){
                    String[] items = row.split(",");
                    if(items[0].equals(Integer.toString(eventNumber))){
                        oldrowindex = tmpIndex;
                    }
                    tmpIndex ++;
                }
                StringBuilder row = new StringBuilder();
                for(Map.Entry<Integer,double[][]> en : peakdata.entrySet()){
                    double[] vals = en.getValue()[durations.indexOf(duration)];
                    row.append(Integer.toString(en.getKey()) + ",");
                    for(double value : vals){
                        row.append(Double.toString(value) + ",");
                    }
                    row.append("\n");
                }

                StringBuilder newData = new StringBuilder();
                tmpIndex = 0;
                for(String oldRow : oldRows){
                    if(tmpIndex.intValue()==oldrowindex.intValue()){
                        newData.append(row.toString());
                        System.out.println("upserting " + row.toString());
                    }else{
                        newData.append(oldRow + "\n");
                    }
                    tmpIndex +=1;
                }
                if (oldrowindex.intValue()==-1){
                    newData.append(row.toString());
                    System.out.println("appending " + row.toString());
                }
                try{
                    action.put(newData.toString().getBytes(), opPeakDataSourceName.get(), Integer.toString(duration), ""); 
                }catch(Exception ex){
                    System.out.println("failed writing duration " + Integer.toString(duration));
                }
                          
            }
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
        
        if( failedEvents.equals("")){
            System.out.println("No events failed");
        }else{
            String f = "failed events: " + failedEvents;
            System.out.println(f);
            
            try {
                action.put(f.toString().getBytes(), opLogDataSourceName.get(), "failed_events", "");
            } catch (Exception e1) {
                System.out.println("failed writing failed events");
            }
        }
        
        String timelogstring ="event_number,";
        LinkedHashMap<String,Long> first = ((Map.Entry<Integer, LinkedHashMap<String, Long>>)timelog.entrySet().toArray()[0]).getValue();
        for(Map.Entry<String, Long> r : first.entrySet()){
            timelogstring += r.getKey() + ",";
        }
        timelogstring += "\n";
        for(Map.Entry<Integer, LinkedHashMap<String, Long>> e : timelog.entrySet()){
            timelogstring += Integer.toString(e.getKey()) + ",";
            for(Map.Entry<String, Long> r : e.getValue().entrySet()){
                timelogstring += Long.toString(r.getValue()) + ",";
            }
            timelogstring += "\n";
        }
        System.out.println(timelogstring);
        try {
            action.put(timelogstring.toString().getBytes(), opLogDataSourceName.get(), "time_log", "");
        } catch (Exception e1) {
            System.out.println("failed writing timelogs");
        }
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
    private void copyOutputFileToRemoteWithEventSubstitution(IOManager iomanager, DataSource ds, int eventNumber, String localPath){
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
        String path = ds.getPaths().get("default").replace("$<eventnumber>", Integer.toString(eventNumber));
        try {
            s3store.put(is, path);
        } catch (DataStoreException e) {
            System.out.println("could not write data to path " + path);
        }
    }
}
