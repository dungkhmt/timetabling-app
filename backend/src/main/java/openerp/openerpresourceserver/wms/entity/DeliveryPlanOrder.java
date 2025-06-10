package openerp.openerpresourceserver.wms.entity;

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

    private Integer deliveryPlanOrderSeqId;
}
