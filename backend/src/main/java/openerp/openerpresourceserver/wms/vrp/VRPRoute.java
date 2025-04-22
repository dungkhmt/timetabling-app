package openerp.openerpresourceserver.wms.vrp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VRPRoute {
    private List<Node> nodes;
    private Vehicle vehicle;
    private double distance;
}
