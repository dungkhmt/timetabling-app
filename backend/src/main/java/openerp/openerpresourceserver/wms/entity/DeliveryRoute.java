package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery_route")
public class DeliveryRoute {
    @Id
    @Column(name = "id", length = 40)
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

    @Column(name = "status_id", length = 100)
    private String statusId;
}
