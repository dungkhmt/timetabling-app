package openerp.openerpresourceserver.wms.vrp.service;

import jakarta.annotation.PostConstruct;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPGreedySolver;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPSolver;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and configuring VRP solvers
 */
@Service
public class CVRPSolverFactory {
    private final Map<String, CVRPSolver> solvers = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Register available solvers
        registerSolver(new CVRPGreedySolver());
        // Register more solvers as they are implemented
    }
    
    private void registerSolver(CVRPSolver solver) {
        solvers.put(solver.getName().toLowerCase(), solver);
    }
    
    /**
     * Get a solver by name
     */
    public CVRPSolver getSolver(String name) {
        CVRPSolver solver = solvers.get(name.toLowerCase());
        if (solver == null) {
            // Default to greedy solver if requested solver not found
            solver = solvers.get("greedy best insertion");
        }
        return solver;
    }
    
    /**
     * Get all available solvers
     */
    public Map<String, CVRPSolver> getAllSolvers() {
        return new HashMap<>(solvers);
    }
}