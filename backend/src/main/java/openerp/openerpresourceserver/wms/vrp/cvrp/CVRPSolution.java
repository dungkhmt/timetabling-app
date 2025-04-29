package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.VRPRoute;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRPSolution {
    private List<VRPRoute> routes;
    private List<Node> unscheduledNodes;
    private double totalDistance;
    private double totalDuration;
    private double totalCost;
    private int solverIterations;
    private double solverTime;
    
    /**
     * Create an empty solution
     */
    public static CVRPSolution createEmpty() {
        return CVRPSolution.builder()
            .routes(new ArrayList<>())
            .unscheduledNodes(new ArrayList<>())
            .totalDistance(0.0)
            .totalDuration(0.0)
            .totalCost(0.0)
            .solverIterations(0)
            .solverTime(0.0)
            .build();
    }
    
    /**
     * Calculate solution metrics
     */
    public void calculateMetrics(CVRPInput input) {
        totalDistance = 0.0;
        totalDuration = 0.0;
        totalCost = 0.0;
        
        for (VRPRoute route : routes) {
            totalDistance += route.getDistance();
            totalDuration += route.getDuration();
            
            if (input.getVehicles() != null && route.getVehicleId() < input.getVehicles().size()) {
                openerp.openerpresourceserver.wms.vrp.Vehicle vehicle = input.getVehicles().get(route.getVehicleId());
                // Calculate cost based on fixed cost plus per-km cost
                double fixedCost = 0.0; // Default if not specified
                double costPerKm = 1.0; // Default cost per km
                
                totalCost += fixedCost + costPerKm * route.getDistance() / 1000.0;
            }
        }
    }
}
