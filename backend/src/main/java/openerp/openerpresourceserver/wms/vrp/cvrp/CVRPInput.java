package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.Getter;
import lombok.Setter;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;
import openerp.openerpresourceserver.wms.vrp.Vehicle;

import java.util.List;

@Getter
@Setter

public class CVRPInput {
    private List<Node> nodes;
    private List<Vehicle> vehicles;
    private List<TimeDistance> distances;
    private CVRPParams params;
}
