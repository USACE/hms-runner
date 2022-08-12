package usace.wat.plugin.hmsrunner;

import usace.wat.plugin.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import hms.model.Project;
import hms.Hms;

public class hmsrunner  {
    public static final String PluginName = "hmsrunner";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(PluginName + " says hello.");
        //check the args are greater than 1
        Utilities.InitalizeFromEnv();
        if(args.length!=1){
            System.out.println("Did not detect only one argument");
            return;
        }else{
            System.out.println(args[0]);
        }
        //first arg should be a modelpayload check to see it is
        String filepath = args[0].split("=")[1];
        //load payload. 
        ModelPayload mp = Utilities.LoadPayload(filepath);
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/workspaces/hms-runner/run/model/"+mp.getModel().getName()+"/";
        //download the payload to list all input files
        Utilities.CopyPayloadInputsLocally(mp, modelOutputDestination);    
        //compute passing in the event config portion of the model payload
        String hmsFile = modelOutputDestination + mp.getModel().getName() + ".hms";
        System.out.println("preparing to run " + hmsFile);
        Project project = Project.open(hmsFile);
        project.computeRun(mp.getModel().getAlternative());
        System.out.println("run completed for " + hmsFile);
        //push results to s3.
        for (ResourcedFileData output : mp.getOutputs()) {
            //ResourceInfo ri = new ResourceInfo();
            //need to set the resource info
            //Utilities.DownloadObject(info)  
            Path path = Paths.get(modelOutputDestination + output.getFileName());
            byte[] data;
            try {
                data = Files.readAllBytes(path);
                Utilities.UploadFile(output.getResourceInfo(), data);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  
        }
        Hms.shutdownEngine();
    }
}