package openerp.openerpresourceserver.wms.dto.saleOrder;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.OrderItemReq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateSaleOrderReq {
    private String id;
    @NotBlank
    private String facilityId;
    @NotBlank
    private String customerId;
    @NotBlank
    private String userCreatedId;
    private String saleOrderName;
    private Integer numberOfInvoices;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryBeforeDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryAfterDate;
    private String discountType;
    private BigDecimal discountValue;
    private String deliveryAddress;
    private String deliveryPhone;
    private String purpose;
    private String note;
    @NotBlank
    private String saleChannel;
    private String deliveryMethod;
    private String shippingCarrier;
    private Boolean isExportedInvoice;
    @Size(min = 1, message = "At least one product is required")
    private List<OrderItemReq> orderItems;
}
