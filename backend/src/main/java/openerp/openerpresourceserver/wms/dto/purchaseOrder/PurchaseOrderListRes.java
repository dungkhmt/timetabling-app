package openerp.openerpresourceserver.wms.dto.purchaseOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PurchaseOrderListRes {
    private String id;
    private String orderName;
    private String supplierName;
    private String facilityName;
    private String status;
    private LocalDateTime createdStamp;
    private BigDecimal totalAmount;
    private String createdByUserName;
}
