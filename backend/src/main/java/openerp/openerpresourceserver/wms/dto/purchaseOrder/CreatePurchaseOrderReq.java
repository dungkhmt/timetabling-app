package openerp.openerpresourceserver.wms.dto.purchaseOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.OrderItemReq;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePurchaseOrderReq {
    @NotBlank
    private String supplierId;
    @NotBlank
    private String facilityId;
    private BigDecimal deliveryCost;
    private String note;
    private String orderName;
    private String tax;
    private String amount;
    private Integer numberOfInvoices;
    @NotNull
    private LocalDate deliveryAfterDate;
    private LocalDate deliveryBeforeDate;

    @Size(min = 1, message = "At least one product is required")
    List<OrderItemReq> orderItems;
}
