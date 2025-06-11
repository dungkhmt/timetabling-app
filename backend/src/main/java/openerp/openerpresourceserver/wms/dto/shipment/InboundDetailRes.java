package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.math.BigDecimal;
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
    private String shipmentTypeId;
    private String shipmentName;
    private String fromSupplierName;
    private String statusId;
    private LocalDateTime createdStamp;
    private LocalDate expectedDeliveryDate;
    private BigDecimal totalWeight;
    private Integer totalQuantity;
    private String note;
    private List<InboundDetailProductRes> products;
}
