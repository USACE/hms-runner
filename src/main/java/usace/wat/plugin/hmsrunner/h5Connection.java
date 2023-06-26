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
        File f = new File(destFilePath);
        int destId = HDF5Constants.H5I_INVALID_HID;
        if(f.exists()){
            destId = H5.H5Fopen(destFilePath,
                    HDF5Constants.H5F_ACC_RDWR,
                    HDF5Constants.H5P_DEFAULT);

        } else {
            throw new HDF5Exception(String.format("Unable to open file for copyto: %s",destFilePath));
        }
        //open source dataset
        int source_id = openDataset(srcdatasetName, this.file_id);
        //find dimensions to build an array to read.
        long[] dims = new long[2];
        long[] maxdims = new long[2];
        int space_id = H5.H5Dget_space(source_id);
        H5.H5Sget_simple_extent_dims(space_id, dims, maxdims);
        long totdems = dims[0]*dims[1];
        float[] dset = new float[(int)totdems];
        //int mem_space_id = H5.H5Screate_simple(2, dims, maxdims);
        //read dataset to an array
        H5.H5Dread(source_id,HDF5Constants.H5T_NATIVE_FLOAT,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,dset);
        //open destination dataset
        int dest_id = openDataset(destDatasetName, destId);
        //write array bytes to destination.
        H5.H5Dwrite(dest_id, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, dset);
        //close resources
        if(dest_id!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Dclose(dest_id);
        }
        if(source_id!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Dclose(source_id);
        }
        if(destId!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Fclose(destId);
        }
    }
    public void write(double[] flows, double[] times, String datasetName) throws Exception{
        int dataset_id = openDataset(datasetName, this.file_id);
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
        //try catch finally, with a try catch around close
        H5.H5Dwrite(dataset_id, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);
        H5.H5Dclose(dataset_id);
    }
    private int openDataset(String dataset_name, int fid) throws Exception{
        int dataset_id = -1;
        if (exists(dataset_name)){
            //logger.info(String.format("Found existing dataset: %s in file.  Opening dataset for write.",dataset_name));
            dataset_id = H5.H5Dopen(fid, dataset_name, HDF5Constants.H5P_DEFAULT);
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
