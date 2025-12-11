package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.api.*;
import usace.cc.plugin.api.IOManager.InvalidDataStoreException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import usace.cc.plugin.hmsrunner.actions.*;

import hms.Hms;

public class HmsRunner  {
    public static final String PLUGINNAME = "hmsrunner";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(PLUGINNAME + " says hello.");
        //check the args are greater than 1
        PluginManager pm;
        try {
            pm = PluginManager.getInstance();
        } catch (InvalidDataStoreException e) {
            e.printStackTrace();
            System.out.println("could not find one of the datastores in payload to register.");
            System.exit(-1);
            return;
        }
        //load payload. 
        Payload mp = pm.getPayload();
        //get Alternative name
        Optional<String> modelName = mp.getAttributes().get("model-name");
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/model/"+modelName.get()+"/";
        File dest = new File(modelOutputDestination);
        deleteDirectory(dest);
        //download the payload to list all input files
        ioManagerToLocal(mp, modelOutputDestination);    
         
        //perform all actions
        for (Action a : mp.getActions()){
            pm.logMessage(new Message(a.getDescription()));
            switch(a.getType()){
                case "compute_forecast":
                    ComputeForecastAction cfa = new ComputeForecastAction(a);
                    cfa.computeAction();
                    break;
                case "compute_simulation":
                    ComputeSimulationAction csa = new ComputeSimulationAction(a);
                    csa.computeAction();
                    break;
                case "compute_simulation_all_placements_given_storm":
                    ComputeSimulationAllPlacementsAction csapa = new ComputeSimulationAllPlacementsAction(a);
                    try {
                        csapa.computeAction();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("generic exception computing all placements");
                        System.exit(-1);
                    }
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
                    ExportExcessPrecipAction ea = new ExportExcessPrecipAction(a);
                    ea.computeAction();
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
        ioManagerToRemote(mp, modelOutputDestination);

        Hms.shutdownEngine();
    }
    public static void ioManagerToLocal(IOManager iomanager, String modelOutputDestination) {
        for(DataSource i : iomanager.getInputs()){
            try {
                File f = new File(modelOutputDestination,i.getName());
                iomanager.copyFileToLocal(i.getName(), "default", f.getAbsolutePath());

            } catch (Exception e) {
                System.out.println("failed copying datasource named " + i.getName() + " to local");
                System.exit(1);
            }
        }
        return;
    }
    public static void ioManagerToRemote(IOManager ioManager, String modelOutputDestination){
        for (DataSource output : ioManager.getOutputs()) { 
            Path path = Paths.get(modelOutputDestination + output.getName());
            try {
                ioManager.copyFileToRemote(output.getName(), "default", path.toString());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
                return;
            } 
        }
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
