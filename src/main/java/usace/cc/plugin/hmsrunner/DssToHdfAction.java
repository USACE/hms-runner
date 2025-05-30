package usace.cc.plugin.hmsrunner;

import java.util.Map;
import java.util.Optional;

import hdf.hdf5lib.exceptions.HDF5LibraryException;
import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import usace.cc.plugin.api.Action;
import usace.cc.plugin.api.DataSource;

public class DssToHdfAction {
    private Action action;
    public DssToHdfAction(Action a) {
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
        Optional<DataSource> opDestination = action.getInputDataSource("destination");
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
            Optional<Double> hasMultiplier = action.getAttributes().get(es.getValue() + "- multiplier");
            Double multiplier = 1.0d;
            if (hasMultiplier.isPresent()){
                Double mult = (hasMultiplier.get());
                multiplier = mult;
            }
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = es.getValue();
            status = reader.read(tsc,true);
            if (status <0){
                //panic?
                DSSErrorMessage error = reader.getLastError();
                error.printMessage();
                break;
            }
            double[] values = tsc.values;
            double[] times = new double[values.length];
            double delta = 1.0/24.0;//test with other datasets - probably need to make it dependent on d part.
            double timestep = 0;
            int i = 0;
            for(double f : values){
                values[i] = f*multiplier;
                times[i] = timestep;
                timestep += delta;
                i++;
            }
            //write time series to destination
            try {
                writer.write(values,times,destination.getDataPaths().get().get(es.getKey()));
            } catch (Exception e) {
                //e.printStackTrace();
                return;
            }
                                        
        }
        //close reader
        reader.close();
        //close writer
        try {
            writer.close();
        } catch (HDF5LibraryException e) {
            return;
        }
    }
}
