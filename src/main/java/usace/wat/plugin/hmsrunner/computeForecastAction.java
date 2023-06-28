package usace.wat.plugin.hmsrunner;

import hms.model.Project;
import hms.model.project.ComputeSpecification;
import usace.cc.plugin.Action;

public class computeForecastAction {
    private Action action;
    private String simulation_name;
    private String variant_name;
    public computeForecastAction(Action a, String simulation_name, String variant_name) {
        this.action = a;
        this.simulation_name = simulation_name;
        this.variant_name = variant_name;
    }
    public void ComputeAction(){
        String hmsFilePath = action.getParameters().get("project_file").getPaths()[0];
        System.out.println("opening project " + hmsFilePath);
        Project project = Project.open(hmsFilePath);
        System.out.println("preparing to run Forecast " + simulation_name + ":" + variant_name);
        project.computeForecast(simulation_name, variant_name);
        System.out.println("Forecast run completed for " + hmsFilePath);
        project.close();
    }
}
