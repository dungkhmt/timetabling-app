package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    private LocalDate expectedDeliveryDate;

    private String note;

    private String statusId;
}
