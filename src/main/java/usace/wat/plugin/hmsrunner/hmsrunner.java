package usace.wat.plugin.hmsrunner;

import usace.wat.plugin.*;
import java.io.File;
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
        System.out.println("java.library.path=" + System.getProperty("java.library.path"));
        if(args.length!=1){
            System.out.println("Did not detect only one argument");
            return;
        }else{
            System.out.println(args[0]);
        }
        //first arg should be a modelpayload check to see it is
        String filepath = args[0].split("=")[1];
        ModelPayload mp = Utilities.LoadPayload(filepath);
        //copy payload to local or read it from S3.     
        /*
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/workspaces/hms-runner/run/model/"+payload.ModelName()+"/";
        //download the payload to list all input files
        for (LinkedDataDescription input : payload.Inputs()) {
            loader.DownloadFromS3(input.getResourceInfo().getAuthority(), input.getResourceInfo().getFragment(),modelOutputDestination + input.getName());
        }      
        //compute passing in the event config portion of the model payload
        String hmsFile = modelOutputDestination + payload.ModelName() + ".hms";
        System.out.println("preparing to run " + hmsFile);
        Project project = Project.open(hmsFile);
        project.computeRun(payload.ModelAlternative());
        System.out.println("run completed for " + hmsFile);
        //push results to s3.
        for (LinkedDataDescription output : payload.Outputs()) {
            loader.UploadToS3(output.getResourceInfo().getAuthority(), output.getResourceInfo().getFragment(),modelOutputDestination + output.getName());
        }
        Hms.shutdownEngine();
        */

    }
}