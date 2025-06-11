package openerp.openerpresourceserver.wms.dto.purchaseOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.OrderProductRes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PurchaseOrderDetailRes {
    private String id;
    private String orderName;
    private String orderTypeId;
    private String statusId;
    private String fromSupplierName;
    private String createdByUserName;
    private LocalDateTime createdStamp;
    private LocalDate deliveryAfterDate;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private String note;
    private List<OrderProductRes> orderItems;
}
