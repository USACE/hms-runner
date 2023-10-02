package usace.cc.plugin.hmsrunner;

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
        String hmsFilePath = action.getParameters().get("project_file").getPaths()[0];
        System.out.println("opening project " + hmsFilePath);
        Project project = Project.open(hmsFilePath);
        System.out.println("preparing to run Simulation " + simulationName);
        project.computeRun(simulationName);
        System.out.println("Simulation run completed for " + hmsFilePath);
        project.close();
    }
}
