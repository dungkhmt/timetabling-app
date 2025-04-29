package openerp.openerpresourceserver.wms.vrp;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeDistance {
    private Node fromNode;
    private Node toNode;
    private double travelTime;
    private double distance;
    
    // Path coordinates for visualization
    private List<GeoPoint> path;
    
    /**
     * Create a TimeDistance object with direct path
     */
    public static TimeDistance createDirect(Node from, Node to) {
        double distance = calculateHaversineDistance(
            from.getLatitude(), from.getLongitude(),
            to.getLatitude(), to.getLongitude()
        );
        double travelTime = distance / 50.0 * 3600; // Assuming 50 km/h
        
        List<GeoPoint> path = new ArrayList<>();
        path.add(new GeoPoint(from.getLatitude(), from.getLongitude()));
        path.add(new GeoPoint(to.getLatitude(), to.getLongitude()));
        
        return TimeDistance.builder()
            .fromNode(from)
            .toNode(to)
            .distance(distance)
            .travelTime(travelTime)
            .path(path)
            .build();
    }
    
    /**
     * Calculate direct distance using Haversine formula
     */
    public static double calculateHaversineDistance(double fromLat, double fromLng, double toLat, double toLng) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(toLat - fromLat);
        double lngDistance = Math.toRadians(toLng - fromLng);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Convert to meters
    }
}
