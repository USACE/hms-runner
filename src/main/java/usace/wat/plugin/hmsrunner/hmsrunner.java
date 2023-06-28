package usace.wat.plugin.hmsrunner;

import usace.cc.plugin.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import hms.model.Project;
import hms.model.project.ComputeSpecification;
import hms.model.data.*;

import hms.Hms;

public class hmsrunner  {
    public static final String PluginName = "hmsrunner";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(PluginName + " says hello.");
        //check the args are greater than 1
        PluginManager pm = PluginManager.getInstance();
        //load payload. 
        Payload mp = pm.getPayload();
        //get Alternative name
        String model_name = (String) mp.getAttributes().get("model_name");
        //get simulation name?
        String simulation_name = (String) mp.getAttributes().get("simulation");
        //get variant if it exists
        String variant_name = (String) mp.getAttributes().get("variant");
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/model/"+model_name+"/";
        File dest = new File(modelOutputDestination);
        deleteDirectory(dest);
        //download the payload to list all input files
        String hmsFilePath = "";
        
        for(DataSource i : mp.getInputs()){
            if (i.getName().contains(".hms")){
                //compute passing in the event config portion of the model payload
                hmsFilePath = modelOutputDestination + i.getName();
                //break;
            }
            byte[] bytes = pm.getFile(i, 0);
            //write bytes locally.
            File f = new File(modelOutputDestination, i.getName());
            try {
                if (!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                if (!f.createNewFile()){
                    f.delete();
                    if(!f.createNewFile()){
                        System.out.println(f.getPath() + " cant create or delete this location");
                        return;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try(FileOutputStream outputStream = new FileOutputStream(f)){
                outputStream.write(bytes);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
        }    
 
        //perform all actions
        for (Action a : mp.getActions()){
            pm.LogMessage(new Message(a.getDescription()));
            switch(a.getName()){
                case "compute_forecast":
                    computeForecastAction cfa = new computeForecastAction(a, simulation_name, variant_name);
                    cfa.ComputeAction();
                    break;
                case "compute_simulation":
                    computeSimulationAction csa = new computeSimulationAction(a, simulation_name);
                    csa.ComputeAction();
                    break;
                case "dss_to_hdf": 
                    dsstoHdfAction da = new dsstoHdfAction(a);
                    da.ComputeAction();
                    break;
                case "copy_precip_table":
                    CopyPrecipAction ca = new CopyPrecipAction(a);
                    ca.ComputeAction();
                    break;
                case "export_excess_precip":
                    Project project = Project.open(hmsFilePath);
                    ComputeSpecification spec = project.getComputeSpecification(simulation_name);//move to export precip action eventually
                    ExportExcessPrecipAction ea = new ExportExcessPrecipAction(a, spec);
                    ea.ComputeAction();
                    break;
                case "dss_to_csv":
                    dsstoCsvAction dca = new dsstoCsvAction(a);
                    dca.ComputeAction();
                    break;
                default:
                break;
            }

        }
        //push results to s3.

        for (DataSource output : mp.getOutputs()) { 
            Path path = Paths.get(modelOutputDestination + output.getName());
            byte[] data;
            try {
                data = Files.readAllBytes(path);
                pm.putFile(data, output,0);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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