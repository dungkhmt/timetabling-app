package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRouteResponseDTO {
    private List<ShipperRouteDTO> shipperRoutes;
    private double totalDistance;
    private int totalDeliveries;
    private int unassignedDeliveries;
}