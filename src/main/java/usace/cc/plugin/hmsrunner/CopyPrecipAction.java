package usace.cc.plugin.hmsrunner;

import java.util.Optional;

import usace.cc.plugin.Action;
import usace.cc.plugin.DataSource;

public class CopyPrecipAction {
    private Action action;
    public CopyPrecipAction(Action a) {
        action = a;
    }
    public void computeAction(){
        //find source
        Optional<DataSource> opSource = action.getInputDataSource("source");
        if(!opSource.isPresent()){
            System.out.println("could not find datasource named source");
            return;
        }
        DataSource source = opSource.get();
        String sourceDataPath = source.getDataPaths().get("default");
        //create hdf connection
        H5Connection connection = new H5Connection(source.getPaths().get("default"));//assumes one path and assumes it is hdf.
        try {
            connection.open();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //find destination 
        Optional<DataSource> opDestination = action.getOutputDataSource("destination");
        if(!opDestination.isPresent()){
            System.out.println("could not find output datasource destination");
        }
        DataSource destination = opDestination.get();
        String destDataPath = destination.getDataPaths().get("default");//output destination hdf table name 
        for(String destFilePath : destination.getPaths().values()){//assumes datapaths are all hdf files.
            //copy from source to destination
            try {
                connection.copyTo(sourceDataPath, destDataPath,destFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
