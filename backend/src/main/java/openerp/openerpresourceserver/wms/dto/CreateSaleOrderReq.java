package openerp.openerpresourceserver.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateSaleOrderReq {
    private String inventoryId;
    private String customerId;
    private String saleOrderName;
    private Integer numberOfInvoice;
    private LocalDateTime deliveryBeforeDate;
    private LocalDateTime deliveryAfterDate;
    private String discountType;
    private BigDecimal discountValue;
    private String deliveryAddress;
    private String deliveryPhone;
    private String purpose;
    private String note;
    private String saleChannel;
    private String deliveryMethod;
    private String shippingCarrier;
    private Boolean isExportedInvoice;
    private List<OrderItemReq> orderItems;
}
