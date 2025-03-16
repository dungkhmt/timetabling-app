package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OutBoundByOrderRes {
    private String id;
    private String shipmentType;
    private String shipmentName;
    private String customerName;
    private String statusId;
}
