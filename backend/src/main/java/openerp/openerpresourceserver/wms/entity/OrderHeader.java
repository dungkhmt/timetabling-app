package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static openerp.openerpresourceserver.wms.constant.Constants.ORDER_ITEM_ID_PREFIX;
import static org.apache.commons.lang3.StringUtils.isBlank;

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

    private LocalDateTime orderDate;

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


    @Override
    public void customPrePersist() {
        if(isBlank(id)) {
            id = SnowFlakeIdGenerator.getInstance().nextId(ORDER_ITEM_ID_PREFIX);
        }
    }
}