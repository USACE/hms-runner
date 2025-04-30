package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import hms.model.Project;
import hms.model.project.ComputeSpecification;
import hms.Hms;

public class hmsrunner  {
    public static final String PLUGINNAME = "hmsrunner";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(PLUGINNAME + " says hello.");
        //check the args are greater than 1
        PluginManager pm = PluginManager.getInstance();
        //load payload. 
        Payload mp = pm.getPayload();
        //get Alternative name
        Optional<String> modelName = mp.getAttributes().get("model_name");
        //get simulation name?
        Optional<String> simulationName =  mp.getAttributes().get("simulation");
        //get variant if it exists
        Optional<String> variantName = mp.getAttributes().get("variant");
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        File dest = new File(modelOutputDestination);
        deleteDirectory(dest);
        //download the payload to list all input files
        String hmsFilePath = "";
        
        for(DataSource i : mp.getInputs()){
            try {
                if (i.getName().contains(".hms")){
                    //compute passing in the event config portion of the model payload
                    hmsFilePath = modelOutputDestination + i.getName();
                }
                //byte[] bytes = pm.getFile(i, 0);//convert to file stream
                File f = new File(modelOutputDestination,i.getName());
                mp.copyFileToLocal(i.getName(), "default", f.getAbsolutePath());
                /*              
                
                if (!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                if (!f.createNewFile()){
                    System.out.println(f.getPath() + " cant create or delete this location");
                    System.exit(1);
                }
                try(FileOutputStream output = new FileOutputStream(f)){
                    if(f.canWrite()){
                        System.out.println(f.getPath() + " can write");
                    }else{
                        System.out.println(f.getPath() + " cannot write");
                    }
                    fs.transferTo(output);
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println(f.getPath() + " had issues transferring byte stream");
                    System.exit(1);
                }  */
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
                //return;
            }

        }    
 
        //perform all actions
        for (Action a : mp.getActions()){
            pm.logMessage(new Message(a.getDescription()));
            switch(a.getType()){
                case "compute_forecast":
                    ComputeForecastAction cfa = new ComputeForecastAction(a, simulationName.get(), variantName.get());
                    cfa.computeAction();
                    break;
                case "compute_simulation":
                    ComputeSimulationAction csa = new ComputeSimulationAction(a, simulationName.get());
                    csa.computeAction();
                    break;
                case "dss_to_hdf": 
                    DssToHdfAction da = new DssToHdfAction(a);
                    da.computeAction();
                    break;
                case "dss_to_hdf_tsout": 
                    DssToHdfActionTSOut dat = new DssToHdfActionTSOut(a);
                    dat.computeAction();
                    break;
                case "dss_to_hdf_observations": 
                    DssToHdfObservedFlowsAction dofa = new DssToHdfObservedFlowsAction(a);
                    dofa.computeAction();
                    break;
                case "dss_to_hdf_pool_elevations":
                    DsstoHdfPoolElevationAction dpea = new DsstoHdfPoolElevationAction(a);
                    dpea.computeAction();
                    break;
                case "copy_precip_table":
                    CopyPrecipAction ca = new CopyPrecipAction(a);
                    ca.computeAction();
                    break;
                case "export_excess_precip":
                    Project project = Project.open(hmsFilePath);
                    ComputeSpecification spec = project.getComputeSpecification(simulationName.get());//move to export precip action eventually
                    ExportExcessPrecipAction ea = new ExportExcessPrecipAction(a, spec);
                    ea.computeAction();
                    project.close();
                    break;
                case "dss_to_csv":
                    DssToCsvAction dca = new DssToCsvAction(a);
                    dca.computeAction();
                    break;
                default:
                break;
            }

        }
        //push results to s3.

        for (DataSource output : mp.getOutputs()) { 
            Path path = Paths.get(modelOutputDestination + output.getName());
            //byte[] data;
            try {
                //data = Files.readAllBytes(path);//convert to filestream
                //InputStream fs = Files.newInputStream(path);
                mp.copyFileToRemote(output.getName(), "default", path.toString());
                //pm.fileWriter(fs, output, 0);
                //pm.putFile(data, output,0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
                return;
            } 
        }
        Hms.shutdownEngine();
    }
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
