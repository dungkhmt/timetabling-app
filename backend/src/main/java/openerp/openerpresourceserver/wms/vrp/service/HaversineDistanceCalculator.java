package openerp.openerpresourceserver.wms.vrp.service;

import openerp.openerpresourceserver.wms.vrp.GeoPoint;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple distance calculator using the Haversine formula
 */
@Service
public class HaversineDistanceCalculator implements DistanceCalculator {
    
    @Override
    public TimeDistance calculateDistance(Node from, Node to) {
        double distance = TimeDistance.calculateHaversineDistance(
            from.getLatitude(), from.getLongitude(),
            to.getLatitude(), to.getLongitude()
        );
        double travelTime = distance / 50.0 * 3600.0; // Assuming 50 km/h
        
        return TimeDistance.builder()
            .fromNode(from)
            .toNode(to)
            .distance(distance)
            .travelTime(travelTime)
            .path(getRoutePath(from, to))
            .build();
    }
    
    @Override
    public List<GeoPoint> getRoutePath(Node from, Node to) {
        return Arrays.asList(
            new GeoPoint(from.getLatitude(), from.getLongitude()),
            new GeoPoint(to.getLatitude(), to.getLongitude())
        );
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