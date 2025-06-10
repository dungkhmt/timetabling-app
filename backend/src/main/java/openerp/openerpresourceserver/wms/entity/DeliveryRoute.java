package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery_route")
public class DeliveryRoute {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "delivery_plan_id")
    private DeliveryPlan deliveryPlan;

    @ManyToOne
    @JoinColumn(name = "assign_to_shipper_id")
    private Shipper assignToShipper;

    @ManyToOne
    @JoinColumn(name = "assign_to_vehicle_id")
    private Vehicle assignToVehicle;

    private String statusId;
}
