package usace.wat.plugin.hmsrunner;

import hms.model.Project;
import hms.model.project.ComputeSpecification;
import usace.cc.plugin.Action;

public class computeSimulationAction {
    private Action action;
    private String simulation_name;
    public computeSimulationAction(Action a, String simulation_name) {
        this.action = a;
        this.simulation_name = simulation_name;
    }
    public void ComputeAction(){
        String hmsFilePath = action.getParameters().get("project_file").getPaths()[0];
        System.out.println("opening project " + hmsFilePath);
        Project project = Project.open(hmsFilePath);
        System.out.println("preparing to run Simulation " + simulation_name);
        project.computeRun(simulation_name);
        System.out.println("Simulation run completed for " + hmsFilePath);
    }
}
