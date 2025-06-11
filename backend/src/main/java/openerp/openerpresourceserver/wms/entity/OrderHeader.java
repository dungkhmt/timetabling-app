package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.wms.converter.JsonReqListConverter;
import openerp.openerpresourceserver.wms.dto.JsonReq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "wms2_order_header")
@NoArgsConstructor
@AllArgsConstructor
public class OrderHeader extends BaseEntity {
    @Id
    private String id;

    private String orderTypeId;

    private LocalDate orderDate;

    private String statusId;

    private String saleChannelId;

    private String orderName;

    private LocalDate deliveryBeforeDate;

    private LocalDate deliveryAfterDate;

    private String note;

    private Integer priority;

    private String deliveryAddressId;

    private String deliveryFullAddress;

    private String deliveryPhone;

    @Convert(converter = JsonReqListConverter.class)
    private List<JsonReq> costs;

    @ManyToOne
    @JoinColumn(name = "from_supplier_id")
    private Supplier fromSupplier;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_approved_id")
    private UserLogin userApproved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_cancelled_id")
    private UserLogin userCancelled;

    private Integer totalQuantity;

    private BigDecimal totalAmount;
}