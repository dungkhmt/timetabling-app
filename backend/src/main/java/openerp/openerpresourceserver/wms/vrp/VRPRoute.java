package openerp.openerpresourceserver.wms.vrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VRPRoute {
    private int vehicleId;
    private List<Integer> nodeSequence;
    private double distance;
    private double duration;
    private double load;
    
    // Full path for visualization
    private List<GeoPoint> pathPoints;
    
    /**
     * Create an empty route for a vehicle
     */
    public static VRPRoute createEmpty(int vehicleId) {
        return VRPRoute.builder()
            .vehicleId(vehicleId)
            .nodeSequence(new ArrayList<>())
            .distance(0.0)
            .duration(0.0)
            .load(0.0)
            .pathPoints(new ArrayList<>())
            .build();
    }
}
