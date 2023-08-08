package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class downloadInputsAction {
    private Action action;
    private Payload mp;
    private PluginManager pm;
    private String modelOutputDestination;
    private String hms_fp;

    public downloadInputsAction(Action a, Payload mp, PluginManager pm, String modelOutputDestination) {
        this.action = a;
        this.mp = mp;
        this.pm = pm;
        this.modelOutputDestination = modelOutputDestination;
        this.hms_fp = ""; // for storing the hms file path
    }
    public void computeAction(){
        String hmsFilePath = "";
        for(DataSource i : mp.getInputs()){
            if (i.getName().contains(".hms")){
                //compute passing in the event config portion of the model payload
                hmsFilePath = modelOutputDestination + i.getName();
                this.hms_fp = hmsFilePath;
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
                e.printStackTrace();
                return;
            }
            try(FileOutputStream outputStream = new FileOutputStream(f)){
                outputStream.write(bytes);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
        }
    }

    public String getHMSFilePath() {
        return this.hms_fp;
    }
}
