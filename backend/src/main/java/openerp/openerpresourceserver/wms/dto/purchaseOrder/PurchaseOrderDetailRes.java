package openerp.openerpresourceserver.wms.dto.purchaseOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderProductRes;

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
    private Integer numberOfInvoices;
    private String status;
    private String facilityName;
    private String supplierName;
    private String createdByUser;
    private LocalDateTime createdStamp;
    private LocalDate deliveryAfterDate;
    private String note;
    private List<OrderProductRes> orderItems;
}
