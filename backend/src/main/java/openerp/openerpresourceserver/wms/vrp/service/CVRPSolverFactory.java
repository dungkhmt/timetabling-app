package openerp.openerpresourceserver.wms.vrp.service;

import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPCWSSolver;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPGreedySolver;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPSolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CVRPSolverFactory {
    
    private final Map<String, CVRPSolver> solvers = new HashMap<>();
    
    public CVRPSolverFactory() {
        // Register available solvers
        solvers.put("GREEDY", new CVRPGreedySolver());
        solvers.put("CWS", new CVRPCWSSolver());
        solvers.put("CLARKE-WRIGHT", new CVRPCWSSolver()); // Alias
    }
    
    public CVRPSolver getSolver(String solverName) {
        CVRPSolver solver = solvers.get(solverName.toUpperCase());
        if (solver == null) {
            // Default to greedy if solver not found
            return solvers.get("GREEDY");
        }
        return solver;
    }
    
    public Map<String, String> getAvailableSolvers() {
        Map<String, String> availableSolvers = new HashMap<>();
        for (Map.Entry<String, CVRPSolver> entry : solvers.entrySet()) {
            availableSolvers.put(entry.getKey(), entry.getValue().getName());
        }
        return availableSolvers;
    }
}