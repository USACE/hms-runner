package usace.cc.plugin.hmsrunner;

import java.util.Optional;

import hms.model.Project;
import usace.cc.plugin.Action;

public class ComputeSimulationAction {
    private Action action;
    private String simulationName;
    public ComputeSimulationAction(Action a, String simname) {
        this.action = a;
        this.simulationName = simname;
    }
    public void computeAction(){
        Optional<String> hmsFilePathResult = action.getAttributes().get("project_file");
        if(hmsFilePathResult.isPresent()){
            String hmsFilePath = hmsFilePathResult.get();
            System.out.println("opening project " + hmsFilePath);
            Project project = Project.open(hmsFilePath);
            System.out.println("preparing to run Simulation " + simulationName);
            project.computeRun(simulationName);
            System.out.println("Simulation run completed for " + hmsFilePath);
            project.close();
        }else{
            System.out.println("could not get string at attribute named project_file");
        }
    }
}
