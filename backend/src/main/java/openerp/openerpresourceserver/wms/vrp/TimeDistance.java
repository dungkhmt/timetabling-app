package openerp.openerpresourceserver.wms.vrp;

import lombok.Getter;
import lombok.Setter;
import openerp.openerpresourceserver.wms.vrp.Node;

@Getter
@Setter
public class TimeDistance {
    private Node fromNode;
    private Node toNode;
    private double travelTime;
    private double distance;
}
