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
            //return;
        }
        //first arg should be a modelpayload check to see it is
        //copy payload to local or read it from S3.      
        //deseralize to objects (looks like payload format has shifted since the objects were made.)
        File f = new File("/workspaces/hms-runner/example_data/payload.yml");
        ModelPayload payload = ModelPayload.readYaml(f);
        //check that the plugin name is correct.
        //copy the model to local if not local
        //compute passing in the event config portion of the model payload
        System.out.println("preparing to run " + payload.ModelFilePath()[0].getFilePath());
        Project project = Project.open(payload.ModelFilePath()[0].getFilePath());
        project.computeRun(payload.ModelName());
        Hms.shutdownEngine();
    }
}