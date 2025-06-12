package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentProductForDeliveryRes {
    private String id;

    private String facilityId;

    private String facilityName;

    private String productId;

    private String productName;

    private BigDecimal weight;

    private Integer quantity;

    private String unit;

    private BigDecimal price;
}
