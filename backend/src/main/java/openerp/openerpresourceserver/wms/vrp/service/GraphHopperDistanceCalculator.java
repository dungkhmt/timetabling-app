package openerp.openerpresourceserver.wms.vrp.service;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PointList;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.vrp.GeoPoint;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Distance calculator using GraphHopper for real-world routing
 */
@Service
@Slf4j
public class GraphHopperDistanceCalculator implements DistanceCalculator {

    private final DistanceCalculator fallbackCalculator = new HaversineDistanceCalculator();
    @Value("${graphhopper.osm.file:/home/ubuntu/Hust/timetabling-app/backend/src/main/resources/vietnam-latest.osm.pbf}")
    private String osmFile;
    @Value("${graphhopper.graph.location:target/graph-cache}")
    private String graphLocation;
    private GraphHopper graphHopper;

    @PostConstruct
    public void init() {
        try {
            // Validate OSM file existence
            File osmFileObj = new File(osmFile);
            if (!osmFileObj.exists() || !osmFileObj.canRead()) {
                throw new IllegalStateException("OSM file not found or unreadable: " + osmFile);
            }

            // Initialize GraphHopper
            graphHopper = new GraphHopper();
            graphHopper.setOSMFile(osmFile);
            graphHopper.setGraphHopperLocation(graphLocation);

            graphHopper.setEncodedValuesString("car_access, car_average_speed, road_access");
            // see docs/core/profiles.md to learn more about profiles
            graphHopper.setProfiles(new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")));

            // this enables speed mode for the profile we called car
            graphHopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
            // Import and process OSM data
            graphHopper.importOrLoad();

            log.info("GraphHopper 9.1 initialized successfully with OSM file: {}", osmFile);
        } catch (Exception e) {
            log.error("Failed to initialize GraphHopper - using fallback distance calculator. Cause: {}", e.getMessage(), e);
            graphHopper = null;
        }
    }

    @Override
    public TimeDistance calculateDistance(Node from, Node to) {
        try {
            if (graphHopper == null) {
                return fallbackCalculator.calculateDistance(from, to);
            }

            // Create routing request
            GHRequest request = new GHRequest(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            );
            request.setProfile("car");

            // Execute routing request
            GHResponse response = graphHopper.route(request);

            if (response.hasErrors()) {
                log.error("Error calculating route: {}", response.getErrors());
                return fallbackCalculator.calculateDistance(from, to);
            } else {
                double distance = response.getBest().getDistance();
                double travelTime = response.getBest().getTime() / 1000.0; // ms to seconds

                List<GeoPoint> path = new ArrayList<>();
                PointList points = response.getBest().getPoints();
                for (int i = 0; i < points.size(); i++) {
                    path.add(new GeoPoint(points.getLat(i), points.getLon(i)));
                }

                return TimeDistance.builder()
                        .fromNode(from)
                        .toNode(to)
                        .distance(distance)
                        .travelTime(travelTime)
                        .path(path)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in routing calculation", e);
            return fallbackCalculator.calculateDistance(from, to);
        }
    }

    @Override
    public List<GeoPoint> getRoutePath(Node from, Node to) {
        List<GeoPoint> coordinates = new ArrayList<>();

        try {
            if (graphHopper == null) {
                return fallbackCalculator.getRoutePath(from, to);
            }

            GHRequest request = new GHRequest(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            );
            request.setProfile("car");

            GHResponse response = graphHopper.route(request);

            if (response.hasErrors()) {
                log.error("Error calculating route path: {}", response.getErrors());
                return fallbackCalculator.getRoutePath(from, to);
            } else {
                PointList points = response.getBest().getPoints();
                for (int i = 0; i < points.size(); i++) {
                    coordinates.add(new GeoPoint(
                            points.getLat(i),
                            points.getLon(i)
                    ));
                }
                return coordinates;
            }
        } catch (Exception e) {
            log.error("Error in route path calculation", e);
            return fallbackCalculator.getRoutePath(from, to);
        }
    }

    @Override
    public List<TimeDistance> calculateDistanceMatrix(List<Node> nodes) {
        List<TimeDistance> matrix = new ArrayList<>();

        for (Node from : nodes) {
            for (Node to : nodes) {
                if (from.getId() != to.getId()) {
                    matrix.add(calculateDistance(from, to));
                }
            }
        }

        return matrix;
    }
}