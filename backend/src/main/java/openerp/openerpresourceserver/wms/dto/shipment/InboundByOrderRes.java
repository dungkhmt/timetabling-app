package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class InboundByOrderRes {
    private String id;
    private String shipmentTypeId;
    private String shipmentName;
    private String fromSupplierName;
    private BigDecimal totalWeight;
    private Integer totalQuantity;
    private String statusId;
    private LocalDate expectedDeliveryDate;
}
