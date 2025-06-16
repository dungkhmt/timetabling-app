package openerp.openerpresourceserver.wms.dto.invoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private String id;
    private String invoiceName;
    private String invoiceTypeId;
    private String statusId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdStamp;
    
    // Customer Info
    private CustomerInfoDTO customerInfo;
    
    // Supplier Info (for purchase invoices)
    private SupplierInfoDTO supplierInfo;
    
    // Invoice Items
    private List<InvoiceItemDTO> items;
    
    // Totals
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private Integer totalQuantity;

    private String note;
    private String createdByUserName;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfoDTO {
        private String id;
        private String name;
        private String phone;
        private String email;
        private String fullAddress;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierInfoDTO {
        private String id;
        private String name;
        private String phone;
        private String email;
        private String fullAddress;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemDTO {
        private String id;
        private Integer seqId;
        private String productId;
        private String productName;
        private String unit;
        private Integer quantity;
        private BigDecimal discount;
        private BigDecimal tax;
        private BigDecimal amount;
        private BigDecimal price;
        private String note;
    }
}