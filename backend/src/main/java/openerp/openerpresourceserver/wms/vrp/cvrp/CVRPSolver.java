package openerp.openerpresourceserver.wms.vrp.cvrp;

/**
 * Interface for all CVRP solvers
 */
public interface CVRPSolver {
    /**
     * Solve the CVRP problem
     * @param input The CVRP input data
     * @param params Parameters for the solving algorithm
     * @return The solution, or null if no solution found
     */
    CVRPSolution solve(CVRPInput input, CVRPParams params);
    
    /**
     * Get the name of the solver
     */
    String getName();
}
