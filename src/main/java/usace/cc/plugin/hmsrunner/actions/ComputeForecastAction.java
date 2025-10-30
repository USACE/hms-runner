package usace.cc.plugin.hmsrunner.actions;

import java.util.Optional;

import hms.model.Project;
import usace.cc.plugin.api.Action;

public class ComputeForecastAction {
    private Action action;
    public ComputeForecastAction(Action a) {
        this.action = a;
    }
    public void computeAction(){
        Optional<String> hmsFilePathResult = action.getAttributes().get("project_file");
        Optional<String> simulationName =  action.getAttributes().get("simulation");
        Optional<String> variantName = action.getAttributes().get("variant");
        if(hmsFilePathResult.isPresent()){
            String hmsFilePath = hmsFilePathResult.get();
            System.out.println("opening project " + hmsFilePath);
            Project project = Project.open(hmsFilePath);
            System.out.println("preparing to run Forecast " + simulationName + ":" + variantName);
            project.computeForecast(simulationName.get(), variantName.get());
            System.out.println("Forecast run completed for " + hmsFilePath);
            project.close();            
        }else{
            System.out.println("could not get string at attribute named project_file");
        }

    }
}
