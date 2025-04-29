package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;
import openerp.openerpresourceserver.wms.vrp.Vehicle;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRPInput {
    private List<Node> nodes;
    private List<Vehicle> vehicles;
    private List<TimeDistance> distances;
    
    // For storing auxiliary data needed by solvers
    private Object data;
    
    /**
     * Find a TimeDistance object for a specific pair of nodes
     */
    public TimeDistance findTimeDistance(int fromNodeId, int toNodeId) {
        for (TimeDistance td : distances) {
            if (td.getFromNode().getId() == fromNodeId && 
                td.getToNode().getId() == toNodeId) {
                return td;
            }
        }
        return null;
    }
    
    /**
     * Get distance between two nodes
     */
    public double getDistance(int fromNodeId, int toNodeId) {
        TimeDistance td = findTimeDistance(fromNodeId, toNodeId);
        return td != null ? td.getDistance() : 0.0;
    }
    
    /**
     * Get travel time between two nodes
     */
    public double getTravelTime(int fromNodeId, int toNodeId) {
        TimeDistance td = findTimeDistance(fromNodeId, toNodeId);
        return td != null ? td.getTravelTime() : 0.0;
    }
}
