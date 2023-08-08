package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;

public class pushOutputsAction {
    private Action action;
    private Payload mp;
    private PluginManager pm;
    private String modelOutputDestination;

    public pushOutputsAction(Action a, Payload mp, PluginManager pm, String modelOutputDestination) {
        this.action = a;
        this.mp = mp;
        this.pm = pm;
        this.modelOutputDestination = modelOutputDestination;
    }
    public void computeAction(){
        for (DataSource output : mp.getOutputs()) {
            Path path = Paths.get(modelOutputDestination + output.getName());
            byte[] data;
            try {
                data = Files.readAllBytes(path);
                pm.putFile(data, output,0);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
