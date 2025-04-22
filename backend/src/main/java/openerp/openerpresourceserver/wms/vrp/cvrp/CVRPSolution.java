package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.Getter;
import lombok.Setter;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.VRPRoute;

import java.util.List;

@Getter
@Setter
public class CVRPSolution {
    private List<VRPRoute> routes;
    private List<Node> unScheduledNodes;
}
