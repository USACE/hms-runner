package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.Action;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import hms.model.Project;
import hms.model.data.SpatialVariableType;
import hms.model.project.ComputeSpecification;
public class ExportExcessPrecipAction {
    private Action action;
    public ExportExcessPrecipAction(Action a) {
        action = a;
    }
    public void computeAction(){
        //find destination
        Optional<String> opHmsFilePath = action.getAttributes().get("project_file");
        if(!opHmsFilePath.isPresent()){
            System.out.println("could not find the action attribute project_file");
        }
        Optional<String> opSimulationName = action.getAttributes().get("simulation_name");
        if(!opSimulationName.isPresent()){
            System.out.println("could not find the action attribute simulation_name");
        }
        String hmsFilePath = opHmsFilePath.get();
        Project project = Project.open(hmsFilePath);//convert to a payloadattribute on the action
        ComputeSpecification spec = project.getComputeSpecification(opSimulationName.get());//move to export precip action eventually
        Optional<String> destination = action.getAttributes().get("destination_path");
        Set<SpatialVariableType> variables = new HashSet<>();
        variables.add(SpatialVariableType.INC_EXCESS);//why not allow for this to be parameterized too?
        spec.exportSpatialResults(destination.get(), variables);
        project.close();
    }
}
