package usace.cc.plugin.hmsrunner;
import hec.map.WorldPt;
import hms.gis.raster.GridDefinition;
import hms.gis.referencing.Reprojector;
import hms.model.basin.BasinManager;
import hms.model.basin.Subbasin;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import hms.gis.raster.GridDefinition;
import hms.model.basin.BasinManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class RelativeWorldPointMapper {
    BasinManager basinManager;
    WorldPt[] worldPts;
    GridDefinition gridDefinition;
    private final Map<String, Geometry> subbasinGeometries = new HashMap<>();
    private final Map<WorldPt, String> worldPtStringMap = new HashMap<>();

    RelativeWorldPointMapper(BasinManager basinManager, GridDefinition gridDefinition) {
        this.basinManager = basinManager;
        this.gridDefinition = gridDefinition;
    }

    WorldPt[] getWorldPoints() {
        WorldPt[] result = worldPts;

        // First check (no locking)
        if (result != null)
            return result;

        synchronized (this) {
            // Second check (with locking)
            if (worldPts == null) {

                String discretizationCrs = gridDefinition.getCrs();
                String basinCrs = basinManager.getBasinSpatialProperties().getCoordinateSystem();

                try (Reprojector reprojector = Reprojector.builder()
                        .from(discretizationCrs)
                        .to(basinCrs)
                        .build()) {

                    // these coordinates will be in the discretization's CRS
                    Coordinate[] coordinates = gridDefinition.getGridCellCentroidCoords();

                    // build and populate an array of WorldPt with as many entries as coordinates
                    worldPts = new WorldPt[coordinates.length];

                    for (int i = 0; i < coordinates.length; i++) {
                        Coordinate coordinate = coordinates[i];

                        // WorldPt needs to be in the basin model's CRS to get the correct data
                        // reproject to the basin model's CRS to get the correct data
                        Coordinate reprojectedCoordinate = reprojector.reproject(coordinate);
                        worldPts[i] = new WorldPt(reprojectedCoordinate.getX(), reprojectedCoordinate.getY());
                    }
                }
            }

            return worldPts;
        }
    }

    int[] getIndices() {
        // these coordinates will be in the discretization's CRS
        Coordinate[] coordinates = gridDefinition.getGridCellCentroidCoords();

        // build and populate arrays of WorldPt and indices with as many entries as coordinates
        int[] indices = new int[coordinates.length];

        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coordinate = coordinates[i];
            indices[i] = gridDefinition.getIndex(coordinate);
        }

        return indices;
    }

    String getSubbasinName(WorldPt worldPt) {
        String subbasinName = null;

        if (worldPtStringMap.containsKey(worldPt)) {
            subbasinName = worldPtStringMap.get(worldPt);
        } else {
            GeometryFactory geometryFactory = new GeometryFactory();
            Point point = geometryFactory.createPoint(new Coordinate(worldPt.e, worldPt.n));

            Map<String, Geometry> geometries = getSubbasinGeometries();
            for (Map.Entry<String, Geometry> entry : geometries.entrySet()) {
                Geometry subbasinGeometry = entry.getValue();

                if (subbasinGeometry.contains(point)) {
                    subbasinName = entry.getKey();
                    worldPtStringMap.putIfAbsent(worldPt, subbasinName);
                } else {
                    worldPtStringMap.putIfAbsent(worldPt, null);
                }
            }
        }

        return subbasinName;
    }

    private Map<String, Geometry> getSubbasinGeometries() {
        Map<String, Geometry> result = subbasinGeometries;

        // First check (no locking)
        if (!result.isEmpty())
            return result;

        synchronized (this) {
            // Second check (with locking)
            if (subbasinGeometries.isEmpty()) {

                List<Subbasin> subbasinList = basinManager.getSubbasins();
                for (Subbasin subbasin : subbasinList) {
                    subbasinGeometries.putIfAbsent(subbasin.name(), subbasin.getGeometry());
                }
            }

            return subbasinGeometries;
        }
    }

}
