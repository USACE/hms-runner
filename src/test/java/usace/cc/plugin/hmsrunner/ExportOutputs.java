/*package usace.cc.plugin.hmsrunner;

import org.junit.jupiter.api.Test;
import org.python.icu.impl.Assert;

import hdf.hdf5lib.exceptions.HDF5LibraryException;
import java.util.HashSet;
import java.util.Set;

import hms.model.data.SpatialVariableType;
import hms.model.project.ComputeSpecification;
import hms.model.Project;
import hms.Hms;
public class ExportOutputs {
    @Test
    public void TestExport(){
        String hmsFilePath = "/workspaces/hms-runner/testdata/duwamish/duwamish_cutdown.hms";
        Project project = Project.open(hmsFilePath);
        ComputeSpecification spec = project.getComputeSpecification("POR 19802022");//move to export precip action eventually
        Set<SpatialVariableType> variables = new HashSet<>();
        variables.add(SpatialVariableType.COLD_CONTENT);
        variables.add(SpatialVariableType.COLD_CONTENT_ATI);
        variables.add(SpatialVariableType.SWE);
        variables.add(SpatialVariableType.MELT_RATE_ATI);

        spec.exportSpatialResults("/workspaces/hms-runner/testdata/output.dss", variables);
    }

}*/


