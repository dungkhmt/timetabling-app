package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipperRouteDTO {
    private String shipperId;
    private String shipperName;
    private List<CoordinateDTO> path;        // Full detailed path for drawing on map
    private List<DeliveryPointDTO> deliveryPoints;   // Delivery waypoints with details
    private double totalDistance;            // Total route distance in meters
    private double totalLoad;                // Total load for this route
}