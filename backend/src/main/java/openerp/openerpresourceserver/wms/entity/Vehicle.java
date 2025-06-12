package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String id;

    private String vehicleName;

    private String vehicleTypeId;

    private BigDecimal capacity;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    private String statusId;

    private String deliveryStatusId;

    private String description;
}
