package usace.wat.plugin.hmsrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class h5Connection {
    private String _path;
    private int file_id = HDF5Constants.H5I_INVALID_HID;
    public h5Connection(String path){
        this._path = path;
    }
    public void open() throws Exception{
        File f = new File(_path.toString());
        if(f.exists()){
            this.file_id = H5.H5Fopen(_path.toString(),
                    HDF5Constants.H5F_ACC_RDWR,
                    HDF5Constants.H5P_DEFAULT);

        } else {
            throw new HDF5Exception(String.format("Unable to open file: %s",_path.toString()));
        }
    }
    public void copyTo(String srcdatasetName, String destDatasetName, String destFilePath) throws Exception{
        int destId = HDF5Constants.H5I_INVALID_HID;
        File f = new File(destFilePath);
        if(f.exists()){
            this.file_id = H5.H5Fopen(destFilePath,
                    HDF5Constants.H5F_ACC_RDWR,
                    HDF5Constants.H5P_DEFAULT);

        } else {
            throw new HDF5Exception(String.format("Unable to open file for copyto: %s",destFilePath));
        }
        H5.H5Ocopy(file_id,srcdatasetName,destId,destDatasetName,HDF5Constants.H5P_DEFAULT,HDF5Constants.H5P_DEFAULT);
        if(destId!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Fclose(destId);
        }
    }
    public void write(double[] flows, double[] times, String datasetName) throws Exception{
        int dataset_id = openDataset(datasetName);
        int totlength = flows.length + times.length;
        double[] data = new double[totlength];
        int dataIndex = 0;
        for(int i=0;i<flows.length;i++){
            data[dataIndex] = times[i];
            dataIndex++;
            data[dataIndex] = flows[i];
            dataIndex ++;
        }
        //check if data fits in the table?
        //dataset exists so we need to write to the dataset.
        H5.H5Dwrite(dataset_id, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);
        H5.H5Dclose(dataset_id);
    }
    private int openDataset(String dataset_name) throws Exception{
        int dataset_id = -1;
        if (exists(dataset_name)){
            //logger.info(String.format("Found existing dataset: %s in file.  Opening dataset for write.",dataset_name));
            dataset_id = H5.H5Dopen(this.file_id, dataset_name, HDF5Constants.H5P_DEFAULT);
        } else {
            throw new HDF5Exception(String.format("Unable to open dataset: %s",dataset_name));
        }
        return dataset_id;
    }
            //enumerates all parts in a path to determine if they exist in the hdf5 file
    private boolean exists(String path) throws Exception {
        String root="";
        List<String> pathparts = new ArrayList<String>(Arrays.asList(path.split("/")));
        return datasetExists(pathparts,root);
    }
    
    private boolean datasetExists(List<String> pathparts, String root) throws Exception{
        for (String pathpart:pathparts){
            String pathtest=root+"/"+pathpart;
            if (H5.H5Lexists(this.file_id,pathtest,HDF5Constants.H5P_LINK_ACCESS_DEFAULT)){
                pathparts.remove(0);
                if (pathparts.size()==0){
                    return true;
                } else {
                    return datasetExists(pathparts,pathtest);
                }
            } else {
                return false;
            }
        }
        return false;
    }
    public void close() throws HDF5LibraryException{
        if(this.file_id!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Fclose(this.file_id);
        }
        
    }
}
