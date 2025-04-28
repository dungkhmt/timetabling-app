package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "wms2_delivery_bill")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryBill extends BaseEntity {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    private String deliveryBillName;

    private Integer priority;

    private String sequenceId;

    private BigDecimal totalWeight;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    private LocalDate expectedDeliveryDate;

    private String note;

    private String statusId;
}
