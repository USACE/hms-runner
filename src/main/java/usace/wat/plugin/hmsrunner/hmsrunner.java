package usace.wat.plugin.hmsrunner;

import usace.wat.plugin.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.python.antlr.base.mod;

import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;
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
        PluginManager pm = new PluginManager();
        //load payload. 
        Payload mp = pm.getPayload();
        //get Alternative name
        String model_name = (String) mp.getAttributes().get("model_name");
        //get simulation name?
        String simulation_name = (String) mp.getAttributes().get("simulation");
        //copy the model to local if not local
        //hard coded outputdestination is fine in a container
        String modelOutputDestination = "/model/"+model_name+"/";
        
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
        System.out.println("preparing to run " + hmsFilePath);
        Project project = Project.open(hmsFilePath);
        project.computeRun(simulation_name);
        System.out.println("run completed for " + hmsFilePath);
        //push results to s3.
        for (DataSource output : mp.getOutputs()) { 
            Path path = Paths.get(modelOutputDestination + output.getName());
            if(output.getName().contains(".dss")){
                //Path dest = Paths.get(output.getPaths()[0]);//this is the dss file destination... change extension to csv (what if there are many outputs)?
                int i = 0;
                StringBuilder flows = new StringBuilder();
                for(String p : output.getPaths()){
                    if (i==0){
                        i++;//skip the actual dss file.
                    }else{
                        //open up the dss file. reference: https://www.hec.usace.army.mil/confluence/display/dssJavaprogrammer/General+Example
                        TimeSeriesContainer tsc = new TimeSeriesContainer();
                        tsc.fullName = p;
                        HecTimeSeries reader = new HecTimeSeries();
                        int status = reader.setDSSFileName(modelOutputDestination + output.getName());
                        if (status <0){
                            //panic?
                            DSSErrorMessage error = reader.getLastError();
                            error.printMessage();
                            //return;
                        }
                        status = reader.read(tsc,true);
                        if (status <0){
                            //panic?
                            DSSErrorMessage error = reader.getLastError();
                            error.printMessage();
                        // return;
                        }
                        double[] values = tsc.values;
                        flows = flows.append(tsc.fullName + "\r\n");
                        for(double f : values){
                            flows = flows.append(f);
                            flows = flows.append("\r\n");
                        }
                        i++;                        
                    }
                }
                //write times and values to csv.
                byte[] flowdata = flows.toString().getBytes();
                pm.putFile(flowdata, output, 0);
            }
            /*
            byte[] data;
            try {
                data = Files.readAllBytes(path);
                pm.putFile(data, output,0);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  
             */

        }
        Hms.shutdownEngine();
    }

}