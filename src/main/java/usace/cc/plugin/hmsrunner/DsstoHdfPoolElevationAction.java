package usace.cc.plugin.hmsrunner;

import java.util.Map;
import java.util.Optional;

import hdf.hdf5lib.exceptions.HDF5LibraryException;
import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import usace.cc.plugin.api.Action;
import usace.cc.plugin.api.DataSource;

public class DsstoHdfPoolElevationAction {
        private Action action;
    public DsstoHdfPoolElevationAction(Action a) {
        action = a;
    }
    public void computeAction(){
        //find source 
        Optional<DataSource> opSource = action.getInputDataSource("source");
        if(!opSource.isPresent()){
            System.out.println("could not find input datasource named source");
            return;
        }
        DataSource source = opSource.get();
        //create dss reader
        //open up the dss file. reference: https://www.hec.usace.army.mil/confluence/display/dssJavaprogrammer/General+Example
        HecTimeSeries reader = new HecTimeSeries();
        int status = reader.setDSSFileName(source.getPaths().get().get("default"));//assumes one path and assumes it is dss.
        if (status <0){
            //panic?
            DSSErrorMessage error = reader.getLastError();
            error.printMessage();
            return;
        }
        //find destination parameter
        Optional<DataSource> opDestination = action.getOutputDataSource("destination");
        if(!opDestination.isPresent()){
            System.out.println("could not find output datasource named destination");
        }
        DataSource destination = opDestination.get();
        //create hdf writer
        H5Connection writer = new H5Connection(destination.getPaths().get().get("default"));//assumes one path and assumes it is hdf.
        try {
            writer.open();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //read time series from source
        for(Map.Entry<String,String> es : source.getDataPaths().get().entrySet()){//assumes datapaths for source and dest are ordered the same.
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = es.getValue();

            status = reader.read(tsc,true);
            if (status <0){
                //panic?
                DSSErrorMessage error = reader.getLastError();
                error.printMessage();
                break;
            }
            double value = tsc.values[0];
            //write first value of timeseries to destination
            try {
                writer.writePoolElevation(value,destination.getDataPaths().get().get(es.getKey()), destination.getDataPaths().get().get("TwoDFlowAreaName"), destination.getDataPaths().get().get("CellsCSV"));//expected datasetPathIndex will be IC Point Name
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
                                       
        }
        //close reader
        reader.close();
        //close writer
        try {
            writer.close();
        } catch (HDF5LibraryException e) {
            e.printStackTrace();
        }
    }
}
