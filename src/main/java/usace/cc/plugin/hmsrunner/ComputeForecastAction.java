package usace.cc.plugin.hmsrunner;

import java.util.Optional;

import hms.model.Project;
import usace.cc.plugin.Action;

public class ComputeForecastAction {
    private Action action;
    private String simulationName;
    private String variantName;
    public ComputeForecastAction(Action a, String simName, String variantName) {
        this.action = a;
        this.simulationName = simName;
        this.variantName = variantName;
    }
    public void computeAction(){
        Optional<String> hmsFilePathResult = action.getAttributes().get("project_file");
        if(hmsFilePathResult.isPresent()){
            String hmsFilePath = hmsFilePathResult.get();
            System.out.println("opening project " + hmsFilePath);
            Project project = Project.open(hmsFilePath);
            System.out.println("preparing to run Forecast " + simulationName + ":" + variantName);
            project.computeForecast(simulationName, variantName);
            System.out.println("Forecast run completed for " + hmsFilePath);
            project.close();            
        }else{
            System.out.println("could not get string at attribute named project_file");
        }

    }
}
