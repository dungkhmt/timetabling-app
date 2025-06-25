package openerp.openerpresourceserver.wms.dto.delivery;

import java.util.List;

public class DeliveryRouteDetailRes {
    private List<CoordinateDTO> path;        // Full detailed path for drawing on map
    private List<DeliveryPointDTO> deliveryPoints;   // Delivery waypoints with details
    private double totalDistance;            // Total route distance in meters
    private double totalLoad;
}
