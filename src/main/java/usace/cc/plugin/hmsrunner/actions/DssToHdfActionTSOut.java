package usace.cc.plugin.hmsrunner.actions;

import java.util.Map;
import java.util.Optional;

import hdf.hdf5lib.exceptions.HDF5LibraryException;
import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import usace.cc.plugin.api.Action;
import usace.cc.plugin.api.DataSource;
import usace.cc.plugin.hmsrunner.utils.H5Connection;

public class DssToHdfActionTSOut {
    private Action action;
    public DssToHdfActionTSOut(Action a) {
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
        int status = reader.setDSSFileName(source.getPaths().get("default"));//assumes one path and assumes it is dss.
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
            return;
        }
        DataSource destination = opDestination.get();
        //create hdf writer
        H5Connection writer = new H5Connection(destination.getPaths().get("default"));//assumes one path and assumes it is hdf.
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
            double[] values = new double[tsc.values.length+1];
            double[] times = new double[tsc.values.length+1];
            double delta = 1.0/24.0;//test with other datasets - probably need to make it dependent on d part.
            double timestep = -0.5/24.0;
            int i = 0;
            double lastval = 0.0;
            for(double f : tsc.values){
                values[i] = f;
                times[i] = timestep;
                timestep += delta;
                i++;
                lastval = f;
            }
            times[i] = timestep;
            values[i] = lastval;
            //write time series to destination
            try {
                writer.write(values,times,destination.getDataPaths().get().get(es.getKey()));
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
