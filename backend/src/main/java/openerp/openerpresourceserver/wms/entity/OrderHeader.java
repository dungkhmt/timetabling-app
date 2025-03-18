package openerp.openerpresourceserver.wms.entity;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleOrderStatus;
import openerp.openerpresourceserver.wms.entity.sequence.StringPrefixSequenceGenerator;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Entity
@Builder
@Table(name = "wms2_order_header")
@NoArgsConstructor
@AllArgsConstructor
public class OrderHeader extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wms2_order_header_sequences")
    @GenericGenerator(
            name = "wms2_order_header_sequences",
            strategy = "openerp.openerpresourceserver.wms.entity.sequence.StringPrefixSequenceGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.VALUE_PREFIX_PARAMETER, value = "ORD"),
                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d"),
                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.SEQUENCE_TABLE_PARAMETER, value = "wms2_order_header_sequences"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1") // Fix lỗi
            })
    private String id;

    @Column(name = "order_type_id", length = 40)
    private String orderTypeId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    private String status;

    @Column(name = "order_name")
    private String orderName;

    @Column(name = "delivery_before_date")
    private LocalDateTime deliveryBeforeDate;

    @Column(name = "delivery_after_date")
    private LocalDateTime deliveryAfterDate;

    @Column(name = "note")
    private String note;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

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