package usace.cc.plugin.hmsrunner;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;
import jakarta.ws.rs.NotFoundException;

public class H5Connection {
    private String path;
    private int fileId = HDF5Constants.H5I_INVALID_HID;
    public H5Connection(String path){
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
    /* 
    public void saveAs(String destFilePath, String[] datasetNames) throws Exception{
        File f = new File(destFilePath);
        int destId = HDF5Constants.H5I_INVALID_HID;
        if(f.exists()){
            destId = H5.H5Fopen(destFilePath,
                    HDF5Constants.H5F_ACC_RDWR,
                    HDF5Constants.H5P_DEFAULT);

        } else {
            throw new HDF5Exception(String.format("Unable to open file for copyto: %s",destFilePath));
        }
        for(String dataset: datasetNames){
            H5.H5Ocopy(this.fileId, dataset, destId, dataset, destId, destId); //@TODO: update me.
        }
        
    }*/
    public String[] readReflinesNamesColumn(int length) throws Exception{
        String ReflinesNameTable = "/Results/Unsteady/Output/Output Blocks/Base Output/Unsteady Time Series/Reference Lines/Name";
        //open names table, find index of point name
        //open source dataset
        int sourceId = openDataset(ReflinesNameTable, this.fileId);
        //find dimensions to build an array to read.
        long[] dims = new long[2];
        long[] maxdims = new long[2];
        int spaceId = H5.H5Dget_space(sourceId);
        H5.H5Sget_simple_extent_dims(spaceId, dims, maxdims);
        long totdems = dims[0];
        int memId = H5.H5Tcreate(HDF5Constants.H5T_STRING, length);
  
        byte[] dset = new byte[(int)totdems*length];//names is a string
        //read dataset to an array
        H5.H5Dread(sourceId,memId,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,dset);
        H5.H5Dclose(sourceId);
        ByteBuffer buf = ByteBuffer.wrap(dset);
        String[] Names = new String[(int)totdems];
        int startPosition = 0;
        for(int i=0;i<totdems;i++){
            buf.position(startPosition);
            startPosition += length;
            buf.limit(startPosition);
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            String Name = new String(bytes, Charset.forName("UTF-8")).trim();
            Names[i] = Name;
        }
        return Names;
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

    private class ObservedFlowData {
        String date;
        double timestep;
        float flowVal;
        ObservedFlowData(ByteBuffer databuf, int dbposition){
            readBuffer(databuf,dbposition);
        }
        void readBuffer(ByteBuffer databuf, int dbposition) {
            ByteBuffer stringbuf = databuf.duplicate();
            stringbuf.position(dbposition);
            stringbuf.limit(dbposition + 36);//18 
            byte[] bytearr = new byte[stringbuf.remaining()];
            stringbuf.get(bytearr);
            this.date = new String(bytearr, Charset.forName("UTF-8")).trim();
            this.timestep = databuf.getDouble(dbposition + 36);
            this.flowVal = databuf.getFloat(dbposition + 44);
        }
        void writeBuffer(ByteBuffer databuf, int dbposition) {
            byte[] temp_str = this.date.getBytes(Charset.forName("UTF-8"));
            int arraylen = (temp_str.length > 36) ? 36 : temp_str.length;
            for (int ndx = 0; ndx < arraylen; ndx++)
                databuf.put(dbposition +ndx, temp_str[ndx]);
            for (int ndx = arraylen; ndx < 36; ndx++)
                databuf.put(dbposition + arraylen, (byte) 0);//pad whitespace
            databuf.putDouble(dbposition +36, this.timestep);
            databuf.putFloat(dbposition + 44, this.flowVal);
        }
    }

    public void writeResSimReleases(double[] flows, String datasetName) throws Exception{
        int datasetId = openDataset(datasetName, this.fileId);
        //find dimensions to build an array to read.
        long[] dims = new long[2];
        long[] maxdims = new long[2];
        int spaceId = H5.H5Dget_space(datasetId);
        H5.H5Sget_simple_extent_dims(spaceId, dims, maxdims);
        long totdems = dims[0];//*3;//dims[1];
        int strtype_id = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
        H5.H5Tset_size(strtype_id, 36);

        int memId = H5.H5Tcreate(HDF5Constants.H5T_COMPOUND, 48);
        H5.H5Tinsert(memId, "Date", 0, strtype_id);
        H5.H5Tinsert(memId, "Simulation Time", 36, HDF5Constants.H5T_IEEE_F64LE);
        H5.H5Tinsert(memId, "Value", 44, HDF5Constants.H5T_IEEE_F32LE);
        byte[] dset = new byte[(int)totdems*48];
        //read dataset to an array
        H5.H5Dread(datasetId,memId,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,dset);
        ByteBuffer inbuf = ByteBuffer.wrap(dset);
        inbuf.order(ByteOrder.nativeOrder());
        ObservedFlowData[] inObsFlow = new ObservedFlowData[(int)dims[0]];
        for (int i=0;i<dims[0];i++){
            inObsFlow[i] = new ObservedFlowData(inbuf, i*48);
        }
        inObsFlow[0].flowVal = (float)flows[0];//repeat first?
        for(int i=0;i<flows.length;i++){
            inObsFlow[i+1].flowVal = (float)flows[i];
        }
        inObsFlow[inObsFlow.length-1].flowVal = (float)flows[flows.length-1];//repeat last?
        //check if data fits in the table?
        //dataset exists so we need to write to the dataset.
        //try catch finally, with a try catch around close
        byte[] dset_data = new byte[(int)dims[0]*48];
        ByteBuffer outbuf = ByteBuffer.wrap(dset_data);
        outbuf.order(ByteOrder.nativeOrder());
        for (int i=0;i<dims[0];i++){
            inObsFlow[i].writeBuffer(outbuf, i*48);
        }
        H5.H5Dwrite(datasetId, memId, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, dset_data);
        H5.H5Dclose(datasetId);
    }
    public void writePoolElevation(double stageValue, String icPointName, String TwoDFlowAreaName, String csvCells) throws Exception{
        String ICPointNamesTable = "Event Conditions/Unsteady/Initial Conditions/IC Point Names";
        String ICPointElevationsTable = "Event Conditions/Unsteady/Initial Conditions/IC Point Elevations";
        String StartingPoolCellTable = "Event Conditions/Unsteady/Initial Conditions/2D Flow Areas";
        String[] stringcells = csvCells.split(",");
        //open names table, find index of point name
        //open source dataset
        int sourceId = openDataset(ICPointNamesTable, this.fileId);
        //find dimensions to build an array to read.
        long[] dims = new long[2];
        long[] maxdims = new long[2];
        int spaceId = H5.H5Dget_space(sourceId);
        H5.H5Sget_simple_extent_dims(spaceId, dims, maxdims);
        long totdems = dims[0];
        int memId = H5.H5Tcreate(HDF5Constants.H5T_STRING, 32);
  
        byte[] dset = new byte[(int)totdems*32];//names is a string
        //read dataset to an array
        H5.H5Dread(sourceId,memId,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,dset);
        H5.H5Dclose(sourceId);
        //find index of provided name
        int index = -1;
        ByteBuffer buf = ByteBuffer.wrap(dset);
        int startPosition = 0;
        for(int i=0;i<totdems;i++){
            buf.position(startPosition);
            startPosition += 32;
            buf.limit(startPosition);
            byte[] pointbytes = new byte[buf.remaining()];
            buf.get(pointbytes);
            String pointName = new String(pointbytes, Charset.forName("UTF-8")).trim();
            if(pointName.equals(icPointName)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new NotFoundException("IC Point Name not found");
        }

        //open elevations table and read elevation values
        int elesourceId = openDataset(ICPointElevationsTable, this.fileId);
        //find dimensions to build an array to read.
        long[] eledims = new long[2];
        long[] elemaxdims = new long[2];
        int elespaceId = H5.H5Dget_space(elesourceId);
        H5.H5Sget_simple_extent_dims(elespaceId, eledims, elemaxdims);
        long eletotdems = eledims[0];
        float[] eledset = new float[(int)eletotdems];//elevations is a string
        //read dataset to an array
        H5.H5Dread(elesourceId,HDF5Constants.H5T_NATIVE_FLOAT,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,eledset);
        //change elevation value at given name's index to the stageValue
        eledset[index] = (float)stageValue;
        //write
        H5.H5Dwrite(elesourceId, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, eledset);
        H5.H5Dclose(elesourceId);

        //open 2d flow area table
        StartingPoolCellTable += "/" + TwoDFlowAreaName;
        int poolsourceId = openDataset(StartingPoolCellTable, this.fileId);
        //find dimensions to build an array to read.
        long[] pooldims = new long[2];
        long[] poolmaxdims = new long[2];
        int poolspaceId = H5.H5Dget_space(poolsourceId);
        H5.H5Sget_simple_extent_dims(poolspaceId, pooldims, poolmaxdims);
        long pooltotdems = pooldims[0];

        float[] pooldset = new float[(int)pooltotdems];//elevations is a string
        //read dataset to an array
        H5.H5Dread(poolsourceId,HDF5Constants.H5T_NATIVE_FLOAT,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,pooldset);
        //change elevation value at given cell locations index to the stageValue
        for(String sc : stringcells){
            int cellLoc = Integer.parseInt(sc);
            pooldset[cellLoc] = (float)stageValue;
        }
        
        //write
        H5.H5Dwrite(poolsourceId, HDF5Constants.H5T_IEEE_F32LE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, pooldset);
        H5.H5Dclose(poolsourceId);
    }
    public void write(double[] flows, double[] times, String datasetName) throws Exception{
        int datasetId = openDataset(datasetName, this.fileId);
        int totlength = flows.length + times.length;
        float[] data = new float[totlength];
        int dataIndex = 0;
        for(int i=0;i<flows.length;i++){
            data[dataIndex] = (float)times[i];
            dataIndex++;
            data[dataIndex] = (float)flows[i];
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
