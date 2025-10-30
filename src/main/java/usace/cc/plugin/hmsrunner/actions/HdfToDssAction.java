/*package usace.cc.plugin.hmsrunner.actions;

import hdf.hdf5lib.exceptions.HDF5LibraryException;
import hec.heclib.dss.DSSErrorMessage;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesContainer;
import usace.cc.plugin.Action;
import usace.cc.plugin.DataSource;
import usace.cc.plugin.Payload;
import usace.cc.plugin.PluginManager;

public class HdfToDssAction {
    private Action action;
    public HdfToDssAction(Action a) {
        action = a;
    }
    public void computeAction(){

        //************* this initial implementation is for reference lines ****************
        PluginManager pm = PluginManager.getInstance();
        //find source
        DataSource source = action.getParameters().get("hdf_source_template");//assumes path =0 is the template, datapath = 0 is the start index, datapath 1 is the end index, datapath 2 is string lenght of the names table.
        String startidxstring = source.getDataPaths()[0];
        String endidxstring = source.getDataPaths()[1];
        String namesTableDataTypeLengthstring = source.getDataPaths()[2];
        int startIndex = Integer.parseInt(startidxstring);
        int endIndex = Integer.parseInt(endidxstring);
        int namesTableDataTypeLength = Integer.parseInt(namesTableDataTypeLengthstring);
        DataSource destination = action.getParameters().get("dss_destination");
        
        
        //create dss writer
        //open up the dss file. reference: https://www.hec.usace.army.mil/confluence/display/dssJavaprogrammer/General+Example
        HecTimeSeries writer = new HecTimeSeries();
        int status = writer.open(destination.getPaths()[0],false,7);//assumes one path and assumes it is dss.
        if (status <0){
            //panic?
            DSSErrorMessage error = writer.getLastError();
            error.printMessage();
            return;
        }
        //get the refline names from the first file
        String hdfPath = source.getPaths()[0].replace("{ATTR::CC_EVENT_NUMBER}", startidxstring);//assumes one path and assumes it is hdf.
        //create hdf reader
        H5Connection reader = new H5Connection(hdfPath);
        try {
            reader.open();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //open the names table and retrieve the names
        String[] names = reader.readReflinesNamesColumn(namesTableDataTypeLength);

        for(int i = startIndex; i<endIndex; i++){
        //get the refline names from the first file
        String hdfPath = source.getPaths()[0].replace("{ATTR::CC_EVENT_NUMBER}", startidxstring);//assumes one path and assumes it is hdf.
        //create hdf reader
        H5Connection reader = new H5Connection(hdfPath);
        try {
            reader.open();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        }

 
        //read time series from source
        int datasetPathIndex = 0;
        for(String p : source.getDataPaths()){//assumes datapaths for source and dest are ordered the same.
            boolean hasMultiplier = payload.getAttributes().containsKey(p + "- multiplier");
            float multiplier = 1.0f;
            if (hasMultiplier){
                float mult = Float.parseFloat((String) payload.getAttributes().get(p + " - multiplier"));
                multiplier = mult;
            }
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = p;
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
                writer.write(values,times,destination.getDataPaths()[datasetPathIndex]);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            datasetPathIndex++;
                                        
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
}*/