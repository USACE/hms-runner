package usace.cc.plugin.hmsrunner;
import java.time.ZoneId;
import hms.model.data.SpatialVariableType;
import hms.model.project.ComputeSpecification;
import hms.model.Project;
import hms.util.TimeConverter;
import hec.heclib.util.Heclib;
import hec.map.WorldPt;
import hec.map.WorldRect;
import hms.gis.raster.GridDefinition;
import hms.gis.referencing.ReferenceUtils;
import hms.map.layer.spatial.SpatialResultsDraw;
import hms.map.layer.spatial.imagetiles.BasinElementIdentifierMapper;
import hms.map.layer.spatial.imagetiles.TileCellMapperManager;
import hms.map.layer.spatial.imagetiles.TiledImageCellMapping;
import hms.model.basin.BasinManager;
import hms.model.basin.BasinManagerProxy;
import hms.model.basin.Subbasin;
import hms.model.data.DataStore;
import hms.model.data.SpatialVariable;
import hms.reports.cfconventions.hdf.SpatialResultsHDFIO;
import mil.army.usace.hec.vortex.VortexData;
import mil.army.usace.hec.vortex.VortexGrid;
import mil.army.usace.hec.vortex.io.DataWriter;
import org.locationtech.jts.geom.*;
import java.time.LocalDateTime;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportInitialStatesAction {
    public static void main(String[] args){
        //speculating that time range for the export is based on the *.control file, however, possibly finding that all grids are iterated over even if they dont meet the time range. so it is slower than anticipated.
        
        String hmsFilePath = "/workspaces/hms-runner/testdata/duwamish/duwamish_cutdown.hms";
        Project project = Project.open(hmsFilePath);
        ComputeSpecification spec = project.getComputeSpecification("POR 19802022");//move to export precip action eventually
        String destination = "/workspaces/hms-runner/testdata/gridsavestates.dss";
        
        Set<SpatialVariableType> variables = new HashSet<>();
        variables.add(SpatialVariableType.COLD_CONTENT);
        variables.add(SpatialVariableType.COLD_CONTENT_ATI);
        variables.add(SpatialVariableType.SWE);
        variables.add(SpatialVariableType.MELT_RATE_ATI);
        variables.add(SpatialVariableType.LIQUID_WATER_CONTENT);

        spec.exportSpatialResults(destination, variables);
        project.close();
        /* 
        int timeStep = spec.getSpatialResultsWriteInterval();

		List<ZonedDateTime> times = new ArrayList<>();

		LocalDateTime startTime = TimeConverter.toLocalDateTime(spec.getSimulationTimeSpec().getStartTime());
		LocalDateTime endTime = TimeConverter.toLocalDateTime(spec.getSimulationTimeSpec().getEndTime());

		// start time can never be the same as the end time when it is coming from the simulation
		// get the size from the duration between the start and end times
		int size = (int) (Duration.between(startTime, endTime).toMinutes() / timeStep);

		for (int i = 0; i < size; i++) {
			times.add(ZonedDateTime.of(startTime.plusMinutes((long) (i + 1) * timeStep), ZoneId.of("Z")));
		}
        Map<String, Point> subbasinCentroids;
        SpatialVariable spatialVariable = new SpatialVariable(SpatialVariableType.COLD_CONTENT);
        float DSS_NO_DATA_VALUE = Heclib.UNDEFINED_FLOAT;
        //discretizationDef = getDiscretizationDef();//used for crs and cell size.
        String discretzationCrs = "";//albers
        double cellSizeX = 1000;
        double cellSizeY = 1000;
        GridDefinition gridDefinition;
        String basinName = spec.basinName();
        BasinManagerProxy basinManagerProxy = DataStore.project().getBasinProxy(basinName);
        BasinManager basinManager = basinManagerProxy.getBasin();
        String basinCrs = basinManager.getBasinSpatialProperties().getCoordinateSystem();
        discretzationCrs = basinCrs;
        ZonedDateTime[] endTimes = times.toArray(ZonedDateTime[]::new);
        SpatialResultsDraw spatialResultsDrawLayer;

        BasinElementIdentifierMapper basinElementIdMapper;
        try {
            subbasinCentroids = initSubbasinCentroids(basinManager);

            // initialize tile mapping
            String cacheFile = TiledImageCellMapping.getTileCacheFile(basinManager);
            TiledImageCellMapping tiledImageCellMapping = new TiledImageCellMapping(cacheFile);
            TileCellMapperManager tileCellMapperManager = new TileCellMapperManager(basinManager);

            
            WorldRect worldRect = tileCellMapperManager.getWorldRect(basinCrs);
            tiledImageCellMapping.initializeTiles(tileCellMapperManager, worldRect);

            tiledImageCellMapping.load();

            spatialResultsDrawLayer = new SpatialResultsDraw(tiledImageCellMapping);

            basinElementIdMapper = tileCellMapperManager.getIdMapper();

            // TileCellMapperManager will be in the basin manager's unit system
            Envelope discretizationCrsEnvelope = tileCellMapperManager.getEnvelope(discretzationCrs);

            double originX = Math.floor(discretizationCrsEnvelope.getMinX() / cellSizeX) * cellSizeX;
            double originY = Math.floor(discretizationCrsEnvelope.getMinY() / cellSizeY) * cellSizeY;
            double terminusX = Math.ceil(discretizationCrsEnvelope.getMaxX() / cellSizeX) * cellSizeX;
            double terminusY = Math.ceil(discretizationCrsEnvelope.getMaxY() / cellSizeY) * cellSizeY;

            int numCol = (int) ((terminusX - originX) / cellSizeX);
            int numRow = (int) ((terminusY - originY) / cellSizeY);

        gridDefinition = GridDefinition.builder()
                    .originX(originX)
                    .originY(originY)
                    .dx(cellSizeX)
                    .dy(cellSizeY)
                    .nx(numCol)
                    .ny(numRow)
                    .crs(discretzationCrs)
                    .build();
        } finally {
            basinManagerProxy.close();
        }








        AtomicInteger processed = new AtomicInteger();

        //String basinName = spec.basinName();
        //BasinManagerProxy basinManagerProxy = DataStore.project().getBasinProxy(basinName);
        //BasinManager basinManager = basinManagerProxy.getBasin();

        spatialVariable.setOutputUnitSystem(basinManager.unitSystem());

        try {
            //String basinCrs = basinManager.getBasinSpatialProperties().getCoordinateSystem();

            RelativeWorldPointMapper worldPointMapper = new RelativeWorldPointMapper(basinManager, gridDefinition);//absolute would be easier/better.

            WorldPt[] worldPts = worldPointMapper.getWorldPoints();
            int[] indices = worldPointMapper.getIndices();

            // next, query the results using the arrays of world points and indices
            //long timeStep = spec.getSpatialResultsWriteInterval();
            Duration interval = Duration.ofMinutes(timeStep);

            String units = spatialVariable.getOutputUnits();
            spatialVariable.setWriteInterval((int) timeStep);

            String variable = spatialVariable.getDisplayString();

            Map<String, String> writeOptions = new HashMap<>();
            writeOptions.put("partB", spec.basinName());
            writeOptions.put("partF", spec.fPart());
            writeOptions.put("expandName", String.valueOf(endTimes.length > 1));

            // get the correct no data value; differentiate between TIFF, ASCII, and DSS based upon the extension
            float noDataValue = DSS_NO_DATA_VALUE;


            // build a buffer of data to be written to disk
            int maxBufferLength = DataStore.getSettings().arrayBufferLength();
            int bufferLength = Math.max(1, maxBufferLength / gridDefinition.getSize());
            List<VortexData> buffer = new ArrayList<>();
            Map<String, float[]> elementDataCache = new HashMap<>();

            // currentTime is the end time; startTime occurs one timeStep before
            for (ZonedDateTime et : endTimes) {
                SpatialResultsHDFIO reader = new SpatialResultsHDFIO(
                        spec.getSpatialResults(null).getFileName(true), true);

                float[] data = new float[gridDefinition.getSize()];
                Arrays.fill(data, noDataValue);

                // this logic will loop over all grid cell geometries, find the value for the variable of interest
                // and grid cell geometry, and assign it to the corresponding discretization grid cell (i.e., index).
                // subsequent grid cell geometries (for the same discretization grid cell) will overwrite any existing
                // data for the discretization grid cell.
                for (int i = 0; i < worldPts.length; i++) {
                    WorldPt worldPt = worldPts[i];

                    int index = indices[i];

                    String subbasinName = worldPointMapper.getSubbasinName(worldPt);

                    if (subbasinName == null) continue;

                    if (!elementDataCache.containsKey(subbasinName)) {
                        SpatialVariable sv = new SpatialVariable(spatialVariable);
                        sv.setFunctionString(spatialVariable.getFunctionString());
                        sv.setElementName(subbasinName);
                        float[] subbasinValues = reader.readValuesForSpatialVariable(sv, et, et);
                        elementDataCache.put(subbasinName, subbasinValues);
                    }

                    int startIndex = basinElementIdMapper.getStartIndex(subbasinName);
                    int elementIndex = spatialResultsDrawLayer.getElementForPoint(worldPt);
                    int basinElementColumn = elementIndex - startIndex;

                    float[] subbasinValues = elementDataCache.get(subbasinName);
                    int numValues = subbasinValues.length;

                    if (isValidPt(elementIndex, basinElementColumn, numValues)) {
                        data[index] = subbasinValues[basinElementColumn];
                    } else {
                        // The world point is near a subbasin boundary and retrieved an element index from an adjacent
                        // subbasin which can lead to holes in the export. Try again after shifting the world point
                        // closer to the subbasin centroid.
                        WorldPt shiftedPt = shiftWorldPt(worldPt, subbasinName, basinCrs, numValues, startIndex,discretzationCrs,spatialResultsDrawLayer,subbasinCentroids);
                        if (shiftedPt != null) {
                            int shiftedElementIndex = spatialResultsDrawLayer.getElementForPoint(shiftedPt);
                            int shiftedBasinElementColumn = shiftedElementIndex - startIndex;
                            data[index] = subbasinValues[shiftedBasinElementColumn];
                        }
                    }
                }

                elementDataCache.clear();

                ZonedDateTime st = et.minusMinutes(timeStep);

                VortexGrid vortexGrid = VortexGrid.builder()
                        .dx(gridDefinition.getDx())
                        .dy(gridDefinition.getDy())
                        .nx(gridDefinition.getNx())
                        .ny(gridDefinition.getNy())
                        .originX(gridDefinition.getOriginX())
                        .originY(gridDefinition.getOriginY())
                        .wkt(gridDefinition.getCrs())
                        .data(data)
                        .noDataValue(noDataValue)
                        .units(units)
                        .shortName(variable)
                        .startTime(st)
                        .endTime(et)
                        .interval(interval)
                        .build();

                buffer.add(vortexGrid);

                if (buffer.size() >= bufferLength) {
                    write(buffer, writeOptions, destination);
                    buffer.clear();
                }

                //int newValue = (int) (((float) processed.incrementAndGet() / endTimes.length) * 100);
                //support.firePropertyChange("progress", null, newValue);
                reader.close();
            }

            write(buffer, writeOptions, destination);
        } catch (Exception e) {
            //LOGGER.log(Level.SEVERE, e.getMessage());
            int test = 5;
        } finally {
            basinManagerProxy.close();
        }
        //

*/
    }
    private static void write(List<VortexData> data, Map<String, String> options, String destination) {
        DataWriter writer = DataWriter.builder()
                .data(data)
                .destination(destination)
                .options(options)
                .build();

        writer.write();
    }
    private static boolean isValidPt(int elementIndex, int basinElementColumn, int numValues) {
        return elementIndex != -1 && basinElementColumn >= 0 && basinElementColumn < numValues;
    }
    private static WorldPt shiftWorldPt(WorldPt worldPt, String subbasinName, String basinCrs, int numValues, int startIndex, String crs, SpatialResultsDraw spatialResultsDrawLayer, Map<String,Point> subbasinCentroids) {
        Point subbasinCentroid = subbasinCentroids.get(subbasinName);

        // convert x, y resolution from discretization CRS to basin CRS
        Unit<Length> discretizationUnits = ReferenceUtils.getMapUnits(crs);
        Unit<Length> basinUnits = ReferenceUtils.getMapUnits(basinCrs);
        double factor = discretizationUnits.getConverterTo(basinUnits).convert(1.0);
        double xShift = 1000 * factor;
        double yShift = 1000 * factor;

        // attempt to move the world pt 1 cell length towards the centroid
        double centroidX = subbasinCentroid.getX();
        double centroidY = subbasinCentroid.getY();

        double angleRad = Math.atan2(centroidY - worldPt.n, centroidX - worldPt.e);
        double cos = Math.cos(angleRad) * xShift;
        double sin = Math.sin(angleRad) * yShift;

        WorldPt shiftedWorldPt = new WorldPt(worldPt);
        shiftedWorldPt.e = worldPt.e + cos;
        shiftedWorldPt.n = worldPt.n + sin;
        int shiftedElementIndex = spatialResultsDrawLayer.getElementForPoint(shiftedWorldPt);
        int shiftedBasinElementColumn = shiftedElementIndex - startIndex;
        if (isValidPt(shiftedElementIndex, shiftedBasinElementColumn, numValues))
            return shiftedWorldPt;

        // attempt to move the world pt 1 cell length towards a neighbor cell; start in a direction nearest the centroid
        double angleDeg = angleRad * (180 / Math.PI);
        if (angleDeg < 0) {
            angleDeg = angleDeg + 360;
        }

        int direction;
        if (angleDeg >=  337.5 || angleDeg <= 22.5) {
            direction = 0;
        } else if (angleDeg >= 22.5 && angleDeg < 67.5) {
            direction = 1;
        } else if (angleDeg >= 67.5 && angleDeg < 112.5) {
            direction = 2;
        } else if (angleDeg >= 112.5 && angleDeg < 157.5) {
            direction = 3;
        } else if (angleDeg >= 157.5 && angleDeg < 202.5) {
            direction = 4;
        } else if (angleDeg >= 202.5 && angleDeg < 247.5) {
            direction = 5;
        } else if (angleDeg >= 247.5 && angleDeg < 292.5) {
            direction = 6;
        } else {
            direction = 7;
        }

        int[][] directions = {{1,0}, {1,1}, {0, 1}, {-1, 1}, {-1,0}, {-1, -1}, {0,-1}, {1, -1}};
        int size = directions.length;
        for (int i = 0; i < size; i++) {
            WorldPt reShiftedWorldPt = new WorldPt(worldPt);
            int index = (i + direction) % size;
            reShiftedWorldPt.e = worldPt.e + directions[index][0] * xShift;
            reShiftedWorldPt.n = worldPt.n + directions[index][1] * yShift;
            int reShiftedElementIndex = spatialResultsDrawLayer.getElementForPoint(reShiftedWorldPt);
            int reShiftedBasinElementColumn = reShiftedElementIndex - startIndex;
            if (isValidPt(reShiftedElementIndex, reShiftedBasinElementColumn, numValues))
                return reShiftedWorldPt;
        }

        return null;
    }
    private static Map<String,Point> initSubbasinCentroids(BasinManager manager) {
        List<Subbasin> subbasins = manager.getSubbasins();
        Map<String, Point> subbasinCentroids = new HashMap<>();
        for (Subbasin subbasin : subbasins) {
            Geometry subbasinGeometry = subbasin.getGeometry();
            if (subbasinGeometry != null) {
                subbasinCentroids.put(subbasin.name(), subbasinGeometry.getCentroid());
            }
        }
        return subbasinCentroids;
    }
}
