package openerp.openerpresourceserver.wms.vrp.cvrp;

public class CVRPGreedySolver implements CVRPSolver{
    private CVRPInput input;
    @Override
    public CVRPSolution solve(CVRPInput input) {
        this.input = input;
        return null;
    }
}
