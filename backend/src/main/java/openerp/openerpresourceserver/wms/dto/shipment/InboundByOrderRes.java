package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class InboundByOrderRes {
    private String id;
    private String shipmentType;
    private String shipmentName;
    private String supplierName;
    private String statusId;
    private LocalDate expectedDeliveryDate;
}
