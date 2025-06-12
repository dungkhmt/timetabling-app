package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ShipmentForDeliveryRes {
    private String id;
    private String shipmentName;
    private String statusId;
    private String toCustomerName;
    private String deliveryFullAddress;
    private String deliveryAddressId;
    private BigDecimal totalWeight;
    private Integer totalQuantity;
    private String expectedDeliveryDate;
    private List<ShipmentProductForDeliveryRes> shipmentItems;
}
