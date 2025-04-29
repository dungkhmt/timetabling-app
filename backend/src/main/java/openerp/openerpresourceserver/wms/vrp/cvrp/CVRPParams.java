package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRPParams {
    // General parameters
    private int maxIterations = 1000;
    private int timeLimit = 30; // seconds
    
    // Greedy algorithm parameters
    private boolean useCapacityConstraints = true;
    private boolean useBestFit = true;
    
    // Set default parameters
    public static CVRPParams getDefaultParams() {
        return CVRPParams.builder()
            .maxIterations(1000)
            .timeLimit(30)
            .useCapacityConstraints(true)
            .useBestFit(true)
            .build();
    }
}
