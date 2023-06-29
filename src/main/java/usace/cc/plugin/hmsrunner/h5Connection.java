package usace.cc.plugin.hmsrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class h5Connection {
    private String path;
    private int fileId = HDF5Constants.H5I_INVALID_HID;
    public h5Connection(String path){
        this.path = path;
    }
    public void open() throws Exception{
        File f = new File(path);
        if(f.exists()){
            this.fileId = H5.H5Fopen(path,
                    HDF5Constants.H5F_ACC_RDWR,
                    HDF5Constants.H5P_DEFAULT);

        } else {
            throw new HDF5Exception(String.format("Unable to open file: %s",path));
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
        int sourceId = openDataset(srcdatasetName, this.fileId);
        //find dimensions to build an array to read.
        long[] dims = new long[2];
        long[] maxdims = new long[2];
        int spaceId = H5.H5Dget_space(sourceId);
        H5.H5Sget_simple_extent_dims(spaceId, dims, maxdims);
        long totdems = dims[0]*dims[1];
        float[] dset = new float[(int)totdems];
        //read dataset to an array
        H5.H5Dread(sourceId,HDF5Constants.H5T_NATIVE_FLOAT,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,dset);
        //open destination dataset
        int destDSId = openDataset(destDatasetName, destId);
        //write array bytes to destination.
        H5.H5Dwrite(destDSId, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, dset);
        //close resources
        if(destDSId!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Dclose(destDSId);
        }
        if(sourceId!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Dclose(sourceId);
        }
        if(destId!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Fclose(destId);
        }
    }
    public void write(double[] flows, double[] times, String datasetName) throws Exception{
        int datasetId = openDataset(datasetName, this.fileId);
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
        H5.H5Dwrite(datasetId, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);
        H5.H5Dclose(datasetId);
    }
    private int openDataset(String datasetName, int fid) throws Exception{
        int datasetId = -1;
        if (exists(datasetName)){
            datasetId = H5.H5Dopen(fid, datasetName, HDF5Constants.H5P_DEFAULT);
        } else {
            throw new HDF5Exception(String.format("Unable to open dataset: %s",datasetName));
        }
        return datasetId;
    }
            //enumerates all parts in a path to determine if they exist in the hdf5 file
    private boolean exists(String path) throws Exception {
        String root="";
        List<String> pathparts = new ArrayList<>(Arrays.asList(path.split("/")));
        return datasetExists(pathparts,root);
    }
    
    private boolean datasetExists(List<String> pathparts, String root) throws Exception{
        for (String pathpart:pathparts){
            String pathtest=root+File.separator+pathpart;
            if (H5.H5Lexists(this.fileId,pathtest,HDF5Constants.H5P_LINK_ACCESS_DEFAULT)){
                pathparts.remove(0);
                if (pathparts.isEmpty()){
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
        if(this.fileId!=HDF5Constants.H5I_INVALID_HID){
            H5.H5Fclose(this.fileId);
        }
        
    }
}
