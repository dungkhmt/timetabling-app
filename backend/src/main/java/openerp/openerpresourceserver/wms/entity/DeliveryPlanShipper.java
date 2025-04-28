package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "wms2_delivery_plan_shipper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPlanShipper
{
    @Id
    private String id;

    private String deliveryPlanId;

    private String shipperId;

    private String deliveryPlanShipperSeqId;

    private String driverRoleId;
}
