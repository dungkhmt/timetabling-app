package openerp.openerpresourceserver.wms.dto.purchaseOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.JsonReq;
import openerp.openerpresourceserver.wms.dto.OrderItemReq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePurchaseOrderReq {
    private String id;
    @NotBlank
    private String supplierId;
    private String note;
    private String orderName;
    private BigDecimal discount;
    private List<JsonReq> costs;
    private LocalDate orderDate;
    @NotNull
    private LocalDate deliveryAfterDate;
    private LocalDate deliveryBeforeDate;

    @Size(min = 1, message = "At least one product is required")
    List<OrderItemReq> orderItems;
}
