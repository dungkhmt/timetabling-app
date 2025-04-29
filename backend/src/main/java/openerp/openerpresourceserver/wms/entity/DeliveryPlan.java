package openerp.openerpresourceserver.wms.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wms2_delivery_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPlan extends BaseEntity {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @Column(name = "total_weight")
    private BigDecimal totalWeight;

    @Column(name = "delivery_plan_name")
    private String delveryPlanName;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;
}
