package openerp.openerpresourceserver.wms.entity;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleOrderStatus;

@Getter
@Setter
@Entity
@Builder
@Table(name = "wms2_order_header")
@NoArgsConstructor
@AllArgsConstructor
public class OrderHeader extends BaseEntity {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "order_type_id", length = 40)
    private String orderTypeId;

    private LocalDateTime orderDate;

    private String status;

    @ManyToOne
    @JoinColumn(name = "from_supplier_id")
    private Supplier fromSupplier;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_approved_id")
    private UserLogin userApproved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_cancelled_id")
    private UserLogin userCancelled;


}