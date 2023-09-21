package usace.cc.plugin.hmsrunner;

import java.util.HashSet;
import java.util.Set;

import hms.model.Project;
import hms.model.data.SpatialVariableType;
import hms.model.project.ComputeSpecification;
import usace.cc.plugin.Action;

public class computeSimulationAction {
    private Action action;
    private String simulationName;
    public computeSimulationAction(Action a, String simname) {
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
