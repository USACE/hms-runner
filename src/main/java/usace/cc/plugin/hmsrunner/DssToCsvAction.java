package usace.cc.plugin.hmsrunner;
import java.util.Map;
import java.util.Optional;

import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import usace.cc.plugin.api.Action;
import usace.cc.plugin.api.DataSource;

public class DssToCsvAction {
    private Action action;
    public DssToCsvAction(Action a) {
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
            System.out.println("could not find otput datasource named destination");
            return;
        }
        //DataSource destination = opDestination.get();
        //read time series from source
        for(Map.Entry<String,String> es : source.getDataPaths().get().entrySet()){//assumes datapaths for source and dest are ordered the same.
            Optional<Double> hasMultiplier = action.getAttributes().get(es.getValue() + "- multiplier");
            Double multiplier = 1.0d;
            if (hasMultiplier.isPresent()){
                Double mult = (hasMultiplier.get());
                multiplier = mult;
            }
            StringBuilder flows = new StringBuilder();
            TimeSeriesContainer tsc = new TimeSeriesContainer();
                tsc.fullName = es.getValue();

                status = reader.read(tsc,true);
                if (status <0){
                    //panic?
                    DSSErrorMessage error = reader.getLastError();
                    error.printMessage();
                    return;
                }
                double[] values = tsc.values;
                flows = flows.append(tsc.fullName + System.lineSeparator());
                double delta = 1.0/24.0;//test with other datasets - probably need to make it dependent on d part.
                double timestep = 0;
                for(double f : values){
                    f = f*multiplier;
                    flows = flows.append(timestep)
                                .append(",")
                                .append(f)
                                .append(System.lineSeparator());
                    timestep += delta;
                }
                //write time series to destination csv file based on the datasource path.
                byte[] flowdata = flows.toString().getBytes();
                    try {
                        action.put(flowdata, "destination", "default", es.getKey());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        return;
                    }                                    
        }
        //close reader
        reader.close();
    }
}
