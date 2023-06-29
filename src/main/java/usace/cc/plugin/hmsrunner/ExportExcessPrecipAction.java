package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.Action;
import usace.cc.plugin.DataSource;

import java.util.HashSet;
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
        DataSource destination = action.getParameters().get("destination");
        Set<SpatialVariableType> variables = new HashSet<>();
        variables.add(SpatialVariableType.INC_EXCESS);
        specification.exportSpatialResults(destination.getPaths()[0], variables);
    }
}
