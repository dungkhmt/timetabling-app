package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "wms2_vehicle")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

    @Id
    @Column(name = "id", nullable = false, length = 40)
    private String id;

    @Column(name = "vehicle_type_id", length = 40)
    private String vehicleTypeId;

    @Column(name = "capacity")
    private BigDecimal capacity;

    @Column(name = "long")
    private Integer length;  // `long` is a reserved word in Java, so I renamed it to `length`

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "status_id", length = 40)
    private String statusId;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
