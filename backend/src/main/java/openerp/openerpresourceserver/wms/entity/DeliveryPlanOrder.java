package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "wms2_delivery_plan_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPlanOrder {
    @Id
    private String id;

    private String deliveryPlanId;

    private String deliveryBillId;

    @Column(name = "delivery_plan_order_seq_id")
    private String deliveryPlanOrderSeqId;
}
