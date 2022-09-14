package usace.wat.plugin.hmsrunner;

import usace.wat.plugin.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        if(args.length!=2){
            for(String s : args){
                System.out.println("arg " + s);
            }
            System.out.println("Did not detect only payload `pathtopayload` argument");
            return;
        }else{
            for(String s : args){
                System.out.println("arg " + s);
            }
        }
        //first arg should be a modelpayload check to see it is
        String filepath = args[1];
        //load payload. 
        ModelPayload mp = Utilities.LoadPayload(filepath);
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/model/"+mp.getModel().getName()+"/";
        //download the payload to list all input files
        Utilities.CopyPayloadInputsLocally(mp, modelOutputDestination);
        for(ResourcedFileData i : mp.getInputs()){
            if (i.getFileName().contains(mp.getModel().getName() + ".hms")){
                //compute passing in the event config portion of the model payload
                String hmsFile = modelOutputDestination + i.getFileName();
                System.out.println("preparing to run " + hmsFile);
                Project project = Project.open(hmsFile);
                project.computeRun(mp.getModel().getAlternative());
                System.out.println("run completed for " + hmsFile);
                break;
            }
        }
        walk("/model/");
        walk("/model/data/");    
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
    private static void walk(String dir){
        try (Stream<Path> walk = Files.walk(Paths.get(dir))) {
            // We want to find only regular files
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            result.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}