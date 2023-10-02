package usace.cc.plugin.hmsrunner;

import org.junit.jupiter.api.Test;
import org.python.icu.impl.Assert;

import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class H5ConnectionTest {
    @Test
    public void TestOpen(){
        System.out.println("testing open");
        H5Connection connection = new H5Connection("/workspaces/hms-runner/testdata/ElkMiddle.p01.hdf");
        try {
            connection.open();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (HDF5LibraryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void TestWrite(){
        System.out.println("testing write");
        H5Connection connection = new H5Connection("/workspaces/hms-runner/testdata/ElkMiddle.p01.hdf");
        try {
            connection.open();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        double[] flows = new double[577];
        double[] times = new double[577];
        try{
            connection.write(flows,times, "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: ElkMiddle BCLine: Frametown Local Baseflow");
        }catch(Exception e){
            Assert.fail(e);
        }
        
        try {
            connection.close();
        } catch (HDF5LibraryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void TestCopyTo(){
        System.out.println("testing copy to");
        H5Connection connection = new H5Connection("/workspaces/hms-runner/testdata/ExportedPrecip.p01.hdf");
        try {
            connection.open();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String datasetName = "Event Conditions/Meteorology/Precipitation/Values";
        try{
            connection.copyTo(datasetName, datasetName,"/workspaces/hms-runner/testdata/ElkMiddle.p01.hdf");
        }catch(Exception e){
            Assert.fail(e);
        }
        
        try {
            connection.close();
        } catch (HDF5LibraryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
