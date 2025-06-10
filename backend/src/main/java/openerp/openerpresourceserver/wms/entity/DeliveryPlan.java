package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "wms2_delivery_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPlan extends BaseEntity {
    @Id
    private String id;

    private String description;

    private String statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    private BigDecimal totalWeight;

    private String deliveryPlanName;

    private LocalDate deliveryDate;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;
}
