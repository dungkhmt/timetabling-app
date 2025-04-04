package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InboundDetailRes {
    private String id;
    private String shipmentType;
    private String shipmentName;
    private String supplierName;
    private String statusId;
    private LocalDateTime createdStamp;
    private LocalDate expectedDeliveryDate;
    private List<InboundDetailProductRes> products;
}
