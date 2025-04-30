package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.Action;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import hms.model.data.SpatialVariableType;
import hms.model.project.ComputeSpecification;
public class ExportExcessPrecipAction {
    private Action action;
    private ComputeSpecification specification;
    public ExportExcessPrecipAction(Action a, ComputeSpecification spec ) {
        action = a;
        specification = spec;
    }
    public void computeAction(){
        //find destination 
        Optional<String> destination = action.getAttributes().get("destination_path");
        Set<SpatialVariableType> variables = new HashSet<>();
        variables.add(SpatialVariableType.INC_EXCESS);//why not allow for this to be parameterized too?
        specification.exportSpatialResults(destination.get(), variables);
    }
}
