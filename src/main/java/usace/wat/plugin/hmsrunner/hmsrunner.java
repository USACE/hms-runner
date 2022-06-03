package usace.wat.plugin.hmsrunner;
import usace.wat.plugin.*;
import usace.wat.plugin.utils.Loader;

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
        //copy payload to local or read it from S3.
        //this should come from the payload - 
        String bucket = filepath.split("/")[0];
        String key = filepath.replaceAll(bucket, "");
        Loader loader = new Loader();
        String outputDestination = "/workspaces/hms-runner/run/payload.yml";
        loader.DownloadFromS3(bucket, key, outputDestination);      
        //deseralize to objects (looks like payload format has shifted since the objects were made.)
        File f = new File(outputDestination);
        ModelPayload payload = ModelPayload.readYaml(f);
        //check that the plugin name is correct.
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/workspaces/hms-runner/run/model/tenk/";
        //manually putting this in until we improve the payload to list all files
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.hms",modelOutputDestination + "tenk.hms");
        loader.DownloadFromS3(bucket, "/models/tenk/hrap.dss",modelOutputDestination + "hrap.dss");
        loader.DownloadFromS3(bucket, "/models/tenk/hrapcells",modelOutputDestination + "hrapcells");
        loader.DownloadFromS3(bucket, "/models/tenk/regions/hrapcells",modelOutputDestination + "/regions/hrapcells");
        loader.DownloadFromS3(bucket, "/models/tenk/Jan_96.control",modelOutputDestination + "Jan_96.control");
        loader.DownloadFromS3(bucket, "/models/tenk/Stage3_HRAP.met",modelOutputDestination + "Stage3_HRAP.met");
        loader.DownloadFromS3(bucket, "/models/tenk/Tenk_1.basin",modelOutputDestination + "Tenk_1.basin");
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.dss",modelOutputDestination + "tenk.dss");
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.gage",modelOutputDestination + "tenk.gage");
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.grid",modelOutputDestination + "tenk.grid");
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.pdata",modelOutputDestination + "tenk.pdata");
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.regn",modelOutputDestination + "tenk.regn");
        loader.DownloadFromS3(bucket, "/models/tenk/tenk.run",modelOutputDestination + "tenk.run");

        //compute passing in the event config portion of the model payload
        String hmsFile = modelOutputDestination + "tenk.hms";
        System.out.println("preparing to run " + hmsFile);
        Project project = Project.open(hmsFile);
        project.computeRun(payload.ModelName());
        Hms.shutdownEngine();
    }
}