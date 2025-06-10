package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms2_facility")
public class Facility extends BaseEntity {
    @Id
    private String id;

    private String name;

    @Column(name = "is_default")
    private Boolean isDefault;

    private String phone;

    private String postalCode;

    private String statusId;

    private String currentAddressId;

    @Transient
    private String fullAddress;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;
}
