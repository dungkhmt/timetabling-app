package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private String address;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;
}
