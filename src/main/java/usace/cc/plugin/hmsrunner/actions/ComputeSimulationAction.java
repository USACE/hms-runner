package usace.cc.plugin.hmsrunner.actions;

import java.util.Optional;

import hms.model.Project;
import usace.cc.plugin.api.Action;

public class ComputeSimulationAction {
    private Action action;
    public ComputeSimulationAction(Action a) {
        this.action = a;
    }
    public void computeAction(){
        Optional<String> hmsFilePathResult = action.getAttributes().get("project_file");
        if(!hmsFilePathResult.isPresent()){
            System.out.println("could not get string at attribute named project_file");
            return;
        }
        Optional<String> simulationName =  action.getAttributes().get("simulation");
        if(!simulationName.isPresent()){
            System.out.println("could not get string at attribute named simulation");
            return;
        }
        String hmsFilePath = hmsFilePathResult.get();
        System.out.println("opening project " + hmsFilePath);
        Project project = Project.open(hmsFilePath);
        System.out.println("preparing to run Simulation " + simulationName.get());
        project.computeRun(simulationName.get());
        System.out.println("Simulation run completed for " + hmsFilePath);
        project.close();
    }
}
