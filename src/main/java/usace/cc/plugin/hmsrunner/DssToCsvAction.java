package usace.cc.plugin.hmsrunner;
import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import usace.cc.plugin.Action;
import usace.cc.plugin.DataSource;
import usace.cc.plugin.Payload;
import usace.cc.plugin.PluginManager;

public class DssToCsvAction {
    private Action action;
    public DssToCsvAction(Action a) {
        action = a;
    }
    public void computeAction(){
        //get instance of plugin manager
        PluginManager pm = PluginManager.getInstance();
        //find source 
        DataSource source = action.getParameters().get("source");
        //create dss reader
        //open up the dss file. reference: https://www.hec.usace.army.mil/confluence/display/dssJavaprogrammer/General+Example
        HecTimeSeries reader = new HecTimeSeries();
        int status = reader.setDSSFileName(source.getPaths()[0]);//assumes one path and assumes it is dss.
        if (status <0){
            //panic?
            DSSErrorMessage error = reader.getLastError();
            error.printMessage();
            return;
        }
        //find destination parameter
        DataSource destination = action.getParameters().get("destination");
        Payload payload = pm.getPayload();
        //read time series from source
        int pathIndex = 0;
        for(String p : source.getDataPaths()){//assumes datapaths for source and dest are ordered the same.
            boolean hasMultiplier = payload.getAttributes().containsKey(p + "- multiplier");
            float multiplier = 1.0f;
            if (hasMultiplier){
                float mult = Float.parseFloat((String) payload.getAttributes().get(p + " - multiplier"));
                multiplier = mult;
            }
            StringBuilder flows = new StringBuilder();
            TimeSeriesContainer tsc = new TimeSeriesContainer();
                tsc.fullName = p;

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
                pm.putFile(flowdata, destination, pathIndex);
                pathIndex++;
                                        
        }
        //close reader
        reader.close();
    }
}
