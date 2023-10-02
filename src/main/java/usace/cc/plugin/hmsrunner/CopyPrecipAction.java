package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.Action;
import usace.cc.plugin.DataSource;

public class CopyPrecipAction {
    private Action action;
    public CopyPrecipAction(Action a) {
        action = a;
    }
    public void computeAction(){
        //find source
        DataSource source = action.getParameters().get("source");
        String sourceDataPath = source.getDataPaths()[0];
        //create hdf connection
        H5Connection connection = new H5Connection(source.getPaths()[0]);//assumes one path and assumes it is hdf.
        try {
            connection.open();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //find destination 
        DataSource destination = action.getParameters().get("destination");
        String destDataPath = destination.getDataPaths()[0];
        for(String destFilePath : destination.getPaths()){//assumes datapaths are all hdf files.
            //copy from source to destination
            try {
                connection.copyTo(sourceDataPath, destDataPath,destFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
