/*package usace.cc.plugin.hmsrunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import hec.heclib.grid.GridData;
import hec.heclib.grid.GridInfo;
import hec.heclib.grid.GriddedData;

public class DssReadGridCoordinate {
        @Test
    void test() {
        int hdfVal = 6474;
        try {
              
            String filename = "/workspaces/hms-runner/Jan_1996___Calibration_ExcessPrecip.dss";
            String path = "//KanawhaCWMS - 1996/PRECIPITATION/17JAN1996:0000/17JAN1996:0100/RUN:Jan 1996 - Calibration/";

            GriddedData gd = new GriddedData();
            GridData data1=new GridData();
             
            int[] status = new int[1];
 
            gd.setDSSFileName(filename);
            gd.setPathname(path);
            GridInfo info  = gd.retrieveGriddedData(); // read grid info without only
            if( info == null)
            {
             assertNotNull(info,"\"Could not find path: \"+path+\" in file :\"+filename");
            }
             
            // print some specific info
            System.out.println("GridInfoSize = "+info.getGridInfoSize());
            System.out.println("Units = "+info.getDataUnits());
            System.out.println("NumberOfCellsX = "+info.getNumberOfCellsX());
            System.out.println("NumberOfCellsY = "+info.getNumberOfCellsY());
             
             
            System.out.println(info.toString()); // print all info
             
             
            info = gd.retrieveGriddedData(true, data1, status); // read info and the data.
            int originX = info.getLowerLeftCellX();
            int originY = info.getLowerLeftCellY(); 
            int cellsX = info.getNumberOfCellsX();
            int queryx = hdfVal%cellsX;
            int queryy = (hdfVal - queryx)/cellsX;
            
            // print out grid values.
            double val = data1.getValueAt(originX +queryx, originY+ queryy);
            System.out.println("HDF Value " + hdfVal + "(column " +queryx+", row "+queryy+") = "+val);
             
            GriddedData.closeAllFiles();  
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
*/