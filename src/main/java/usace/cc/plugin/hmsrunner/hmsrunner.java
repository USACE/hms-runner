package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        String modelName = (String) mp.getAttributes().get("model_name");
        //get simulation name?
        String simulationName = (String) mp.getAttributes().get("simulation");
        //get variant if it exists
        String variantName = (String) mp.getAttributes().get("variant");
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/model/"+modelName+"/";
        File dest = new File(modelOutputDestination);
        deleteDirectory(dest);
        //download the payload to list all input files
        String hmsFilePath = "";

        //perform all actions
        for (Action a : mp.getActions()){
            pm.LogMessage(new Message(a.getDescription()));
            switch(a.getName()){
                case "download_inputs":
                    downloadInputsAction dia = new downloadInputsAction(a, mp, pm, modelOutputDestination);
                    dia.computeAction();
                    hmsFilePath = dia.getHMSFilePath();
                    break;
                case "push_outputs":
                    pushOutputsAction poa = new pushOutputsAction(a, mp, pm, modelOutputDestination);
                    poa.computeAction();
                    break;
                case "compute_forecast":
                    computeForecastAction cfa = new computeForecastAction(a, simulationName, variantName);
                    cfa.computeAction();
                    break;
                case "compute_simulation":
                    computeSimulationAction csa = new computeSimulationAction(a, simulationName);
                    csa.computeAction();
                    break;
                case "dss_to_hdf":
                    dsstoHdfAction da = new dsstoHdfAction(a);
                    da.computeAction();
                    break;
                case "copy_precip_table":
                    copyPrecipAction ca = new copyPrecipAction(a);
                    ca.computeAction();
                    break;
                case "export_excess_precip":
                    Project project = Project.open(hmsFilePath);
                    ComputeSpecification spec = project.getComputeSpecification(simulationName);//move to export precip action eventually
                    exportExcessPrecipAction ea = new exportExcessPrecipAction(a, spec);
                    ea.computeAction();
                    project.close();
                    break;
                case "dss_to_csv":
                    dsstoCsvAction dca = new dsstoCsvAction(a);
                    dca.computeAction();
                    break;
                default:
                break;
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